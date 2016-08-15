package com.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.sip.SipManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.InjectView;
import butterknife.OnClick;
import com.common.ui.base.BaseActivity;
import com.examp.bean.LocalMessage;
import com.example.xie.ClientApplication;
import com.example.xie.imclient.R;
import com.im.sdk.core.ClientHandler;
import com.im.sdk.core.IMClient;
import com.im.sdk.protocol.Message;
import com.mdroid.xxtea.Tea;
import com.xy.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by xie on 2016/2/1.
 */
public class ChatActivity extends BaseActivity implements ClientHandler.IMEventListener {
    SipManager sm;

    @InjectView(R.id.rcView)
    RecyclerView mRcView;
    @InjectView(R.id.et_input)
    EditText mEtInput;
    @InjectView(R.id.tv_send)
    TextView mTvSend;
    @InjectView(R.id.rl_input)
    RelativeLayout mRlInput;
    @InjectView(R.id.tv_connect_state)
    TextView mTvConnectState;
    List<LocalMessage> messageList = new ArrayList<LocalMessage>();
    private ChatAdapter mAdapter;
    private String receiverId;

    public static void lauchActivity(Activity activity, String uid) {
        Intent intent = new Intent(activity.getApplicationContext(), ChatActivity.class);
        intent.putExtra("uid", uid);
        activity.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.chat_layout;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        IMClient.instance().registIMEventListener(this);
        receiverId = getIntent().getStringExtra("uid") == null ? "" : getIntent().getStringExtra("uid");
        setTitle("与" + receiverId);
        mTvConnectState.setText(IMClient.instance().isConnecting() ? "连接中..." : "服务器已断开..");
        mTvConnectState.setVisibility(IMClient.instance().isConnected() ? View.GONE : View.VISIBLE);
        mAdapter = new ChatAdapter(mContext, messageList);
        mRcView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRcView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        IMClient.instance().unRegistIMEventListener(this);
    }


    @OnClick({R.id.tv_send})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_send:
                Message.Data.Builder msg = Message.Data.newBuilder();
                msg.setId(UUID.randomUUID().toString());
                msg.setCmd(Message.Data.Cmd.CHAT_TXT_VALUE);
                msg.setCreateTime(System.currentTimeMillis());
                msg.setSenderId(mApp.getUid());
                msg.setReceiverId(receiverId);
                String ct = mEtInput.getText().toString().trim();
                String key = ClientApplication.mEncriptKey;
                if (!TextUtils.isEmpty(key)) {
                    msg.setContent(Base64.encodeToString(Tea.encrypt(ct.getBytes(), key.getBytes()), Base64.DEFAULT));
                    msg.setIsEncript(true);
                    msg.setEncriptKey(key);
                } else {
                    msg.setContent(ct);
                }
                mEtInput.setText("");
                LocalMessage localMessage = new LocalMessage(msg);
                IMClient.instance().sendMessage(msg);
                messageList.add(localMessage);
                mAdapter.notifyDataSetChanged();
                mRcView.scrollToPosition(messageList.size() - 1);
                break;
        }

    }


    @Override
    public void onReceiveMessage(Message.Data msg) {
        if (msg.getCmd() == Message.Data.Cmd.CHAT_TXT_VALUE) {
            LocalMessage localMessage = new LocalMessage(msg);
            messageList.add(localMessage);
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onSendFailure(Message.Data.Builder msg) {
        mAdapter.updateMsgStatus(msg.getId(), LocalMessage.STATUS_FAILURE);
    }

    @Override
    public void onSendSucceed(Message.Data.Builder msg) {
        mAdapter.updateMsgStatus(msg.getId(), LocalMessage.STATUS_SUCCESS);
    }


    @Override
    public void onConnectFailure(String msg) {
        mTvConnectState.setText("连接失败..");
        mTvConnectState.setVisibility(View.VISIBLE);
    }


    @Override
    public void onConnected() {
        mTvConnectState.setVisibility(View.GONE);
    }

    @Override
    public void onDisconnected(boolean isException) {
        Log.i("onDisconnected");
        mTvConnectState.setText("服务器已断开..");
        mTvConnectState.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnecting() {
        Log.i("onConnecting");
        mTvConnectState.setText("连接中..");
        mTvConnectState.setVisibility(View.VISIBLE);
    }


}
