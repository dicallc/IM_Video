package com.xiaoxin.im.ui.conversation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.socks.library.KLog;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMMessageStatus;
import com.tencent.imsdk.ext.message.TIMMessageDraft;
import com.tencent.qcloud.presentation.event.RefreshEvent;
import com.tencent.qcloud.presentation.presenter.ChatPresenter;
import com.tencent.qcloud.presentation.viewfeatures.ChatView;
import com.tencent.qcloud.ui.ChatInput;
import com.tencent.qcloud.ui.TemplateTitle;
import com.tencent.qcloud.ui.VoiceSendingView;
import com.xiaoxin.im.R;
import com.xiaoxin.im.adapter.ChatAdapter;
import com.xiaoxin.im.model.CustomMessage;
import com.xiaoxin.im.model.FileMessage;
import com.xiaoxin.im.model.FriendProfile;
import com.xiaoxin.im.model.FriendshipInfo;
import com.xiaoxin.im.model.GroupInfo;
import com.xiaoxin.im.model.ImageMessage;
import com.xiaoxin.im.model.Message;
import com.xiaoxin.im.model.MessageFactory;
import com.xiaoxin.im.model.TextMessage;
import com.xiaoxin.im.model.VideoMessage;
import com.xiaoxin.im.model.VoiceMessage;
import com.xiaoxin.im.ui.contact.ProfileActivity;
import com.xiaoxin.im.ui.contact.add.AddFriendActivity;
import com.xiaoxin.im.ui.contact.send.ImagePreviewActivity;
import com.xiaoxin.im.ui.contact.send.TCVideoRecordActivity;
import com.xiaoxin.im.utils.FileUtil;
import com.xiaoxin.im.utils.MediaUtil;
import com.xiaoxin.im.utils.RecorderUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
@SuppressLint("WrongConstant")
public class ChatActivity extends FragmentActivity implements ChatView {

    private static final String TAG = "ChatActivity";

    private List<Message> messageList = new ArrayList<>();
    private ChatAdapter adapter;
    private ListView listView;
    private ChatPresenter presenter;
    private ChatInput input;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int IMAGE_STORE = 200;
    private static final int FILE_CODE = 300;
    private static final int IMAGE_PREVIEW = 400;
    private static final int VIDEO_RECORD = 500;
    private Uri fileUri;
    private VoiceSendingView voiceSendingView;
    private String identify;
    private RecorderUtil recorder = new RecorderUtil();
    private TIMConversationType type;
    private String titleStr;
    private Handler handler = new Handler();


    public static void navToChat(Context context, String identify, TIMConversationType type){
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("identify", identify);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        identify = getIntent().getStringExtra("identify");
        type = (TIMConversationType) getIntent().getSerializableExtra("type");
        presenter = new ChatPresenter(this, identify, type);
        input = (ChatInput) findViewById(R.id.input_panel);
        input.setChatView(this);
        adapter = new ChatAdapter(this, R.layout.item_message, messageList);
        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        input.setInputMode(ChatInput.InputMode.NONE);
                        break;
                }
                return false;
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            private int firstItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && firstItem == 0) {
                    //如果拉到顶端读取更多消息
                    presenter.getMessage(messageList.size() > 0 ? messageList.get(0).getMessage() : null);

                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                firstItem = firstVisibleItem;
            }
        });
        registerForContextMenu(listView);
        TemplateTitle title = (TemplateTitle) findViewById(R.id.chat_title);
        switch (type) {
            case C2C:
                title.setMoreImg(R.drawable.btn_person);
                if (FriendshipInfo.getInstance().isFriend(identify)){
                    title.setMoreImgAction(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                            intent.putExtra("identify", identify);
                            startActivity(intent);
                        }
                    });
                    FriendProfile profile = FriendshipInfo.getInstance().getProfile(identify);
                    title.setTitleText(titleStr = profile == null ? identify : profile.getName());
                }else{
                    title.setMoreImgAction(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent person = new Intent(ChatActivity.this,AddFriendActivity.class);
                            person.putExtra("id",identify);
                            person.putExtra("name",identify);
                            startActivity(person);
                        }
                    });
                    title.setTitleText(titleStr = identify);
                }
                break;
            case Group:
                title.setMoreImg(R.drawable.btn_group);
                title.setMoreImgAction(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ChatActivity.this, GroupProfileActivity.class);
                        intent.putExtra("identify", identify);
                        startActivity(intent);
                    }
                });
                title.setTitleText(GroupInfo.getInstance().getGroupName(identify));
                break;

        }
        voiceSendingView = (VoiceSendingView) findViewById(R.id.voice_sending);
        presenter.start();
    }

    @Override
    protected void onPause(){
        super.onPause();
        //退出聊天界面时输入框有内容，保存草稿
        if (input.getText().length() > 0){
            TextMessage message = new TextMessage(input.getText());
            presenter.saveDraft(message.getMessage());
        }else{
            presenter.saveDraft(null);
        }
        RefreshEvent.getInstance().onRefresh();
        presenter.readMessages();
        MediaUtil.getInstance().stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.stop();
    }


    /**
         * 显示消息
     *
     * @param message
     */
    @Override
    public void showMessage(TIMMessage message) {
        if (message == null) {
            adapter.notifyDataSetChanged();
        } else {
            Message mMessage = MessageFactory.getMessage(message);
            if (mMessage != null) {
                if (mMessage instanceof CustomMessage){
                    CustomMessage.Type messageType = ((CustomMessage) mMessage).getType();
                    switch (messageType){
                        case TYPING:
                            TemplateTitle title = (TemplateTitle) findViewById(R.id.chat_title);
                            title.setTitleText(getString(R.string.chat_typing));
                            handler.removeCallbacks(resetTitle);
                            handler.postDelayed(resetTitle,3000);
                            break;
                        default:
                            break;
                    }
                }else{
                    if (messageList.size()==0){
                        mMessage.setHasTime(null);
                    }else{
                        mMessage.setHasTime(messageList.get(messageList.size()-1).getMessage());
                    }
                    messageList.add(mMessage);
                    adapter.notifyDataSetChanged();
                    listView.setSelection(adapter.getCount()-1);
                }

            }
        }

    }

    /**
     * 显示消息
     *
     * @param messages
     */
    @Override
    public void showMessage(List<TIMMessage> messages) {
        int newMsgNum = 0;
        for (int i = 0; i < messages.size(); ++i){
            Message mMessage = MessageFactory.getMessage(messages.get(i));
            if (mMessage == null || messages.get(i).status() == TIMMessageStatus.HasDeleted) continue;
            if (mMessage instanceof CustomMessage && (((CustomMessage) mMessage).getType() == CustomMessage.Type.TYPING ||
                    ((CustomMessage) mMessage).getType() == CustomMessage.Type.INVALID)) continue;
            ++newMsgNum;
            if (i != messages.size() - 1){
                mMessage.setHasTime(messages.get(i+1));
                messageList.add(0, mMessage);
            }else{
                mMessage.setHasTime(null);
                messageList.add(0, mMessage);
            }
        }
        adapter.notifyDataSetChanged();
        listView.setSelection(newMsgNum);
    }

    /**
     * 清除所有消息，等待刷新
     */
    @Override
    public void clearAllMessage() {
        messageList.clear();
    }

    /**
     * 发送消息成功
     *
     * @param message 返回的消息
     */
    @Override
    public void onSendMessageSuccess(TIMMessage message) {
        showMessage(message);
    }

    /**
     * 发送消息失败
     *
     * @param code 返回码
     * @param desc 返回描述
     */
    @Override
    public void onSendMessageFail(int code, String desc, TIMMessage message) {
        long id = message.getMsgUniqueId();
        for (Message msg : messageList){
            if (msg.getMessage().getMsgUniqueId() == id){
                switch (code){
                    case 80001:
                        //发送内容包含敏感词
                        msg.setDesc(getString(R.string.chat_content_bad));
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        }

    }

    /**
     * 发送图片消息
     */
    @Override
    public void sendImage() {
        //Intent intent_album = new Intent("android.intent.action.GET_CONTENT");
        //intent_album.setType("image/*");
        //startActivityForResult(intent_album, IMAGE_STORE);
        Intent intent = new Intent(ChatActivity.this, ImageGridActivity.class);
        startActivityForResult(intent, IMAGE_STORE);
    }

    /**
     * 发送照片消息
     */
    @Override
    public void sendPhoto() {
        Intent intent = new Intent(this, ImageGridActivity.class);
        intent.putExtra(ImageGridActivity.EXTRAS_TAKE_PICKERS,true); // 是否是直接打开相机
        startActivityForResult(intent, IMAGE_STORE);
        //Intent intent_photo = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //if (intent_photo.resolveActivity(getPackageManager()) != null) {
        //    File tempFile = FileUtil.getTempFile(FileUtil.FileType.IMG);
        //    if (Build.VERSION.SDK_INT >= 24) {  //针对Android7.0，需要通过FileProvider封装过的路径，提供给外部调用
        //        fileUri = FileProvider.getUriForFile(ChatActivity.this, "com.xiaoxin.im", tempFile);//通过FileProvider创建一个content类型的Uri，进行封装
        //    } else { //7.0以下，如果直接拿到相机返回的intent值，拿到的则是拍照的原图大小，很容易发生OOM，所以我们同样将返回的地址，保存到指定路径，返回到Activity时，去指定路径获取，压缩图片
        //        try {
        //            fileUri = Uri.fromFile(tempFile);
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //        }
        //    }
        //    intent_photo.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        //
        //    startActivityForResult(intent_photo, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        //}
    }

    /**
     * 发送文本消息
     */
    @Override
    public void sendText() {
        Message message = new TextMessage(input.getText());
        presenter.sendMessage(message.getMessage());
        input.setText("");
    }

    /**
     * 发送文件
     */
    @Override
    public void sendFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_CODE);
    }


    /**
     * 开始发送语音消息
     */
    @Override
    public void startSendVoice() {
        voiceSendingView.setVisibility(View.VISIBLE);
        voiceSendingView.showRecording();
        recorder.startRecording();

    }

    /**
     * 结束发送语音消息
     */
    @Override
    public void endSendVoice() {
        voiceSendingView.release();
        voiceSendingView.setVisibility(View.GONE);
        recorder.stopRecording();
        if (recorder.getTimeInterval() < 1) {
            Toast.makeText(this, getResources().getString(R.string.chat_audio_too_short), Toast.LENGTH_SHORT).show();
        } else if (recorder.getTimeInterval() > 60) {
            Toast.makeText(this, getResources().getString(R.string.chat_audio_too_long), Toast.LENGTH_SHORT).show();
        } else {
            Message message = new VoiceMessage(recorder.getTimeInterval(), recorder.getFilePath());
            presenter.sendMessage(message.getMessage());
        }
    }

    /**
     * 发送小视频消息
     *
     * @param fileName 文件名
     */
    @Override
    public void sendVideo(String fileName) {
        Message message = new VideoMessage(fileName);
        presenter.sendMessage(message.getMessage());
    }


    /**
     * 结束发送语音消息
     */
    @Override
    public void cancelSendVoice() {

    }

    /**
     * 正在发送
     */
    @Override
    public void sending() {
        if (type == TIMConversationType.C2C){
            Message message = new CustomMessage(CustomMessage.Type.TYPING);
            presenter.sendOnlineMessage(message.getMessage());
        }
    }

    /**
     * 显示草稿
     */
    @Override
    public void showDraft(TIMMessageDraft draft) {
        input.getText().append(TextMessage.getString(draft.getElems(), this));
    }

    @Override
    public void videoAction() {
        Intent intent = new Intent(this, TCVideoRecordActivity.class);
        startActivityForResult(intent, VIDEO_RECORD);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                   ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Message message = messageList.get(info.position);
        menu.add(0, 1, Menu.NONE, getString(R.string.chat_del));
        if (message.isSendFail()){
            menu.add(0, 2, Menu.NONE, getString(R.string.chat_resend));
        }
        if (message instanceof ImageMessage || message instanceof FileMessage){
            menu.add(0, 3, Menu.NONE, getString(R.string.chat_save));
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Message message = messageList.get(info.position);
        switch (item.getItemId()) {
            case 1:
                message.remove();
                messageList.remove(info.position);
                adapter.notifyDataSetChanged();
                break;
            case 2:
                messageList.remove(message);
                presenter.sendMessage(message.getMessage());
                break;
            case 3:
                message.save();
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == IMAGE_STORE) {
                ArrayList<ImageItem> images =
                    (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                String path = images.get(0).path;
                KLog.e(images.get(0).path);
                File file = new File(path);
                if (file.exists() && file.length() > 0){
                    if (file.length() > 1024 * 1024 * 10){
                        Toast.makeText(this, getString(R.string.chat_file_too_large),Toast.LENGTH_SHORT).show();
                    }else{
                        Message message = new ImageMessage(path,false);
                        presenter.sendMessage(message.getMessage());
                    }
                }else{
                    Toast.makeText(this, getString(R.string.chat_file_not_exist),Toast.LENGTH_SHORT).show();
                }
            }
        }
        //if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
        //    if (resultCode == RESULT_OK && fileUri != null) {
        //        showImagePreview(fileUri.toString());
        //    }
        //} else if (requestCode == IMAGE_STORE) {
        //    if (resultCode == RESULT_OK && data != null) {
        //        showImagePreview(FileUtil.getFilePath(this, data.getData()));
        //    }
        //
        //} else
            if (requestCode == FILE_CODE) {
            if (resultCode == RESULT_OK) {
                sendFile(FileUtil.getFilePath(this, data.getData()));
            }
        }
        //else if (requestCode == IMAGE_PREVIEW){
        //    if (resultCode == RESULT_OK) {
        //        boolean isOri = data.getBooleanExtra("isOri",false);
        //        String path = data.getStringExtra("path");
        //        File file = new File(path);
        //        if (file.exists() && file.length() > 0){
        //            if (file.length() > 1024 * 1024 * 10){
        //                Toast.makeText(this, getString(R.string.chat_file_too_large),Toast.LENGTH_SHORT).show();
        //            }else{
        //                Message message = new ImageMessage(path,isOri);
        //                presenter.sendMessage(message.getMessage());
        //            }
        //        }else{
        //            Toast.makeText(this, getString(R.string.chat_file_not_exist),Toast.LENGTH_SHORT).show();
        //        }
        //    }
        //}
        else if (requestCode == VIDEO_RECORD) {
            if (resultCode == RESULT_OK) {
                String videoPath = data.getStringExtra("videoPath");
                String coverPath = data.getStringExtra("coverPath");
                long duration = data.getLongExtra("duration", 0);
                Message message = new VideoMessage(videoPath, coverPath, duration);
                presenter.sendMessage(message.getMessage());
            }
        }

    }


    private void showImagePreview(String path){
        if (path == null) return;
        Intent intent = new Intent(this, ImagePreviewActivity.class);
        intent.putExtra("path", path);
        startActivityForResult(intent, IMAGE_PREVIEW);
    }

    private void sendFile(String path){
        if (path == null) return;
        File file = new File(path);
        if (file.exists()){
            if (file.length() > 1024 * 1024 * 10){
                Toast.makeText(this, getString(R.string.chat_file_too_large),Toast.LENGTH_SHORT).show();
            }else{
                Message message = new FileMessage(path);
                presenter.sendMessage(message.getMessage());
            }
        }else{
            Toast.makeText(this, getString(R.string.chat_file_not_exist),Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 将标题设置为对象名称
     */
    private Runnable resetTitle = new Runnable() {
        @Override
        public void run() {
            TemplateTitle title = (TemplateTitle) findViewById(R.id.chat_title);
            title.setTitleText(titleStr);
        }
    };


}
