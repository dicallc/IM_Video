package com.xiaoxin.im.ui.conversation;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.tencent.imsdk.TIMConversation;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.ext.group.TIMGroupCacheInfo;
import com.tencent.imsdk.ext.group.TIMGroupPendencyItem;
import com.tencent.imsdk.ext.sns.TIMFriendFutureItem;
import com.tencent.qcloud.presentation.presenter.ConversationPresenter;
import com.tencent.qcloud.presentation.presenter.FriendshipManagerPresenter;
import com.tencent.qcloud.presentation.presenter.GroupManagerPresenter;
import com.tencent.qcloud.presentation.viewfeatures.ConversationView;
import com.tencent.qcloud.presentation.viewfeatures.FriendshipMessageView;
import com.tencent.qcloud.presentation.viewfeatures.GroupManageMessageView;
import com.tencent.qcloud.ui.TemplateTitle;
import com.xiaoxin.im.R;
import com.xiaoxin.im.adapter.ConversationAdapter;
import com.xiaoxin.im.model.Conversation;
import com.xiaoxin.im.model.CustomMessage;
import com.xiaoxin.im.model.FriendshipConversation;
import com.xiaoxin.im.model.GroupManageConversation;
import com.xiaoxin.im.model.MessageFactory;
import com.xiaoxin.im.model.NomalConversation;
import com.xiaoxin.im.ui.HomeActivity;
import com.xiaoxin.im.ui.customview.GuideView;
import com.xiaoxin.im.ui.gank.GankActivity;
import com.xiaoxin.im.ui.video.VideoHomeActivity;
import com.xiaoxin.im.utils.PushUtil;
import com.xiaoxin.im.utils.SpUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 会话列表界面
 */
public class ConversationFragment extends Fragment
    implements ConversationView, FriendshipMessageView, GroupManageMessageView {
  private final String TAG = "ConversationFragment";

  private View view;
  private List<Conversation> conversationList = new LinkedList<>();
  private ConversationAdapter adapter;
  private ListView listView;
  private ConversationPresenter presenter;
  private FriendshipManagerPresenter friendshipManagerPresenter;
  private GroupManagerPresenter groupManagerPresenter;
  private List<String> groupList;
  private FriendshipConversation friendshipConversation;
  private GroupManageConversation groupManageConversation;
  private TemplateTitle mTitle;
  private GuideView guideView;
  private GuideView mGuideView1;
  private String mIsFirst;

  public ConversationFragment() {
  }

  public class MyThread implements Runnable {
    @Override public void run() {
      try {
        Thread.sleep(2000);// 线程暂停10秒，单位毫秒
        getActivity().runOnUiThread(new Runnable() {
          @Override public void run() {

            if (TextUtils.isEmpty(mIsFirst) || "true".equals(mIsFirst)) FirstGuide();
          }
        });
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mIsFirst = (String) SpUtils.getParam("isFirst", "");
    new Thread(new MyThread()).start();
  }

  private void FirstGuide() {
    final ImageView iv = new ImageView(getActivity());
    iv.setImageResource(R.drawable.img_new_task_guide);
    RelativeLayout.LayoutParams params =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    iv.setLayoutParams(params);
    guideView =
        GuideView.Builder.newInstance(getActivity())
            .setTargetView(mTitle.getMoreImg())//设置目标
            .setCustomGuideView(iv)
            .setDirction(GuideView.Direction.LEFT_BOTTOM)
            .setShape(GuideView.MyShape.CIRCULAR)   // 设置圆形显示区域，
            .setBgColor(getResources().getColor(R.color.shadow))
            .setOnclickListener(new GuideView.OnClickCallback() {
              @Override public void onClickedGuideView() {
                guideView.hide();
                mGuideView1.show();
              }
            })
            .build();
    final ImageView iv2 = new ImageView(getActivity());
    iv2.setImageResource(R.drawable.img_new_task_guide_back);
    RelativeLayout.LayoutParams params1 =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    iv2.setLayoutParams(params1);
    mGuideView1 =
        GuideView.Builder.newInstance(getActivity())
            .setTargetView(mTitle.getBackImage())//设置目标
            .setCustomGuideView(iv2)
            .setDirction(GuideView.Direction.RIGHT_BOTTOM)
            .setShape(GuideView.MyShape.CIRCULAR)   // 设置圆形显示区域，
            .setBgColor(getResources().getColor(R.color.shadow))
            .setOnclickListener(new GuideView.OnClickCallback() {
              @Override public void onClickedGuideView() {
                mGuideView1.hide();
                //SpUtils.setParam(getActivity(),"isFirst","false");
              }
            })
            .build();
    guideView.show();
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    if (view == null) {
      view = inflater.inflate(R.layout.fragment_conversation, container, false);
      listView = (ListView) view.findViewById(R.id.list);
      adapter =
          new ConversationAdapter(getActivity(), R.layout.item_conversation, conversationList);
      listView.setAdapter(adapter);
      listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          conversationList.get(position).navToDetail(getActivity());
          if (conversationList.get(position) instanceof GroupManageConversation) {
            groupManagerPresenter.getGroupManageLastMessage();
          }
        }
      });
      mTitle = (TemplateTitle) view.findViewById(R.id.conversation_antionbar);
      mTitle.setMoreImg(R.mipmap.book);
      mTitle.setMoreImgAction(new View.OnClickListener() {
        @Override public void onClick(View v) {

          Intent mIntent = new Intent(getActivity(), GankActivity.class);
          startActivity(mIntent);
        }
      });
      mTitle.setBackImage(R.mipmap.ic_video);
      mTitle.setBackListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          Intent mIntent = new Intent(getActivity(), VideoHomeActivity.class);
          startActivity(mIntent);
        }
      });
      friendshipManagerPresenter = new FriendshipManagerPresenter(this);
      groupManagerPresenter = new GroupManagerPresenter(this);
      presenter = new ConversationPresenter(this);
      //获取绘画列表
      presenter.getConversation();
      registerForContextMenu(listView);
    }
    adapter.notifyDataSetChanged();
    return view;
  }

  @Override public void onResume() {
    super.onResume();
    refresh();
    PushUtil.getInstance().reset();
  }

  /**
   * 更新最新消息显示
   *
   * @param message 最后一条消息
   */
  @Override public void updateMessage(TIMMessage message) {
    if (message == null) {
      adapter.notifyDataSetChanged();
      return;
    }
    if (message.getConversation().getType() == TIMConversationType.System) {
      groupManagerPresenter.getGroupManageLastMessage();
      return;
    }
    if (MessageFactory.getMessage(message) instanceof CustomMessage) return;
    NomalConversation conversation = new NomalConversation(message.getConversation());
    Iterator<Conversation> iterator = conversationList.iterator();
    while (iterator.hasNext()) {
      Conversation c = iterator.next();
      if (conversation.equals(c)) {
        conversation = (NomalConversation) c;
        iterator.remove();
        break;
      }
    }
    conversation.setLastMessage(MessageFactory.getMessage(message));
    conversationList.add(conversation);
    Collections.sort(conversationList);
    refresh();
  }

  /**
   * 更新好友关系链消息
   */
  @Override public void updateFriendshipMessage() {
    friendshipManagerPresenter.getFriendshipLastMessage();
  }

  /**
   * 删除会话
   */
  @Override public void removeConversation(String identify) {
    Iterator<Conversation> iterator = conversationList.iterator();
    while (iterator.hasNext()) {
      Conversation conversation = iterator.next();
      if (conversation.getIdentify() != null && conversation.getIdentify().equals(identify)) {
        iterator.remove();
        adapter.notifyDataSetChanged();
        return;
      }
    }
  }

  /**
   * 更新群信息
   */
  @Override public void updateGroupInfo(TIMGroupCacheInfo info) {
    for (Conversation conversation : conversationList) {
      if (conversation.getIdentify() != null && conversation.getIdentify()
          .equals(info.getGroupInfo().getGroupId())) {
        adapter.notifyDataSetChanged();
        return;
      }
    }
  }

  /**
   * 刷新
   */
  @Override public void refresh() {
    Collections.sort(conversationList);
    adapter.notifyDataSetChanged();
    if (getActivity() instanceof HomeActivity) {
      ((HomeActivity) getActivity()).setMsgUnread(getTotalUnreadNum() == 0);
    }
  }

  /**
   * 获取好友关系链管理系统最后一条消息的回调
   *
   * @param message 最后一条消息
   * @param unreadCount 未读数
   */
  @Override public void onGetFriendshipLastMessage(TIMFriendFutureItem message, long unreadCount) {
    if (friendshipConversation == null) {
      friendshipConversation = new FriendshipConversation(message);
      conversationList.add(friendshipConversation);
    } else {
      friendshipConversation.setLastMessage(message);
    }
    friendshipConversation.setUnreadCount(unreadCount);
    Collections.sort(conversationList);
    refresh();
  }

  /**
   * 获取好友关系链管理最后一条系统消息的回调
   *
   * @param message 消息列表
   */
  @Override public void onGetFriendshipMessage(List<TIMFriendFutureItem> message) {
    friendshipManagerPresenter.getFriendshipLastMessage();
  }

  /**
   * 初始化界面或刷新界面
   */
  @Override public void initView(List<TIMConversation> conversationList) {
    this.conversationList.clear();
    groupList = new ArrayList<>();
    for (TIMConversation item : conversationList) {
      switch (item.getType()) {
        case C2C:
        case Group:
          this.conversationList.add(new NomalConversation(item));
          groupList.add(item.getPeer());
          break;
      }
    }
    friendshipManagerPresenter.getFriendshipLastMessage();
    groupManagerPresenter.getGroupManageLastMessage();
  }

  /**
   * 获取群管理最后一条系统消息的回调
   *
   * @param message 最后一条消息
   * @param unreadCount 未读数
   */
  @Override public void onGetGroupManageLastMessage(TIMGroupPendencyItem message,
      long unreadCount) {
    if (groupManageConversation == null) {
      groupManageConversation = new GroupManageConversation(message);
      conversationList.add(groupManageConversation);
    } else {
      groupManageConversation.setLastMessage(message);
    }
    groupManageConversation.setUnreadCount(unreadCount);
    Collections.sort(conversationList);
    refresh();
  }

  /**
   * 获取群管理系统消息的回调
   *
   * @param message 分页的消息列表
   */
  @Override public void onGetGroupManageMessage(List<TIMGroupPendencyItem> message) {

  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    Conversation conversation = conversationList.get(info.position);
    if (conversation instanceof NomalConversation) {
      menu.add(0, 1, Menu.NONE, getString(R.string.conversation_del));
    }
  }

  @Override public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info =
        (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    NomalConversation conversation = (NomalConversation) conversationList.get(info.position);
    switch (item.getItemId()) {
      case 1:
        if (conversation != null) {
          if (presenter.delConversation(conversation.getType(), conversation.getIdentify())) {
            conversationList.remove(conversation);
            adapter.notifyDataSetChanged();
          }
        }
        break;
      default:
        break;
    }
    return super.onContextItemSelected(item);
  }

  private long getTotalUnreadNum() {
    long num = 0;
    for (Conversation conversation : conversationList) {
      num += conversation.getUnreadNum();
    }
    return num;
  }
}
