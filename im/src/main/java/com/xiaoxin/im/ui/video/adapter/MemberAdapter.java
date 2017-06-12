package com.xiaoxin.im.ui.video.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.xiaoxin.im.R;
import com.xiaoxin.im.model.VIdeoIngMember;
import com.xiaoxin.im.utils.CharUtils;

/**
 * Created by WZH on 2016/12/25.
 */

public class MemberAdapter extends BaseListAdapter<VIdeoIngMember> {
    public MemberAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_menber, null, false);
            viewHolder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        VIdeoIngMember member = datas.get(position);
//        if (!TextUtils.isEmpty(member.img)) {

        Glide.with(ctx).load( CharUtils.getRandomPersonIcon()).placeholder(R.mipmap.default_head).into(viewHolder.avatar);
//        }else {
//            viewHolder.avatar.setImageResource(R.mipmap.default_head);
//        }
        return convertView;
    }

    public class ViewHolder {
        ImageView avatar;
    }
}
