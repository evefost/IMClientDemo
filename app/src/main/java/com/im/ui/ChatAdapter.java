package com.im.ui;

import android.content.Context;
import android.util.Base64;

import com.examp.bean.LocalMessage;
import com.example.xie.imclient.R;
import com.mdroid.xxtea.Tea;
import com.zhy.base.adapter.ViewHolder;
import com.zhy.base.adapter.recyclerview.MultiItemCommonAdapter;
import com.zhy.base.adapter.recyclerview.MultiItemTypeSupport;

import java.util.List;

public class ChatAdapter extends MultiItemCommonAdapter<LocalMessage> {

    public ChatAdapter(Context context, List<LocalMessage> datas) {
        super(context, datas, new MultiItemTypeSupport<LocalMessage>() {
            @Override
            public int getLayoutId(int itemType) {
                if (itemType == LocalMessage.RECIEVE_MSG) {
                    return R.layout.chat_item_other_text;
                } else {
                    return R.layout.chat_item_me_text;
                }
            }

            @Override
            public int getItemViewType(int postion, LocalMessage msg) {
                if (msg.isComMsg()) {
                    return LocalMessage.RECIEVE_MSG;
                }
                return LocalMessage.SEND_MSG;
            }
        });
    }

    @Override
    public void convert(ViewHolder holder, LocalMessage chatMessage) {
        String msg = chatMessage.getData().getContent();
        if (chatMessage.getData().getIsEncript()) {
            byte[] debyte = Tea.decrypt(Base64.decode(msg, Base64.DEFAULT), chatMessage.getData().getEncriptKey().getBytes());
            msg = new String(debyte);
        }
        switch (holder.getLayoutId()) {
            case R.layout.chat_item_me_text:

                holder.setText(R.id.tv_message, msg);
                holder.setVisible(R.id.progressbar, chatMessage.getStatus() == LocalMessage.STATUS_SEND_ING);
                break;
            case R.layout.chat_item_other_text:
                holder.setText(R.id.tv_message, msg);
                break;
        }
    }

    public void updateMsgStatus(String msgId, int status) {
        if (mDatas == null) {
            return;
        }
        for (LocalMessage lmsg : mDatas) {
            if (msgId.equals(lmsg.getData().getId())) {
                lmsg.setStatus(status);
                notifyDataSetChanged();
                break;
            }
        }
    }
}
