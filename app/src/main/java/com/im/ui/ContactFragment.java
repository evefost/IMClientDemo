package com.im.ui;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.common.ui.base.BaseFragment;
import com.examp.bean.User;
import com.example.xie.imclient.R;
import com.im.sdk.core.ClientHandler;
import com.im.sdk.core.IMClient;
import com.im.sdk.protocol.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by xie on 2016/2/25.
 */
public class ContactFragment extends BaseFragment implements ClientHandler.IMEventListener {
    @InjectView(R.id.rcView)
    RecyclerView mRcView;
    private RcAdater mAdapter;
    private List<User> userList = new ArrayList<User>();

    @Override
    public int getLayoutId() {
        return R.layout.contact_layout;
    }

    @Override
    public void init(Bundle savedInstanceState) {

        setTitle("联系人");
        enableBack(false);
        IMClient.registIMEventListener(this);
        mAdapter = new RcAdater();
        mRcView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRcView.setAdapter(mAdapter);

        createUserList();
        mAdapter.notifyDataSetChanged();

        //获取我的好友
        Message.Data.Builder data = Message.Data.newBuilder();
        data.setCmd(Message.Data.Cmd.MINE_FRIENDS_VALUE);
        IMClient.instance().sendMessage(data);

    }

    @Override
    public void setListeners() {
        super.setListeners();
    }

    private void createUserList() {
        for (int i = 0; i < 50; i++) {
            User user = new User();
            String uuid = UUID.randomUUID().toString();
            uuid = uuid.replace("-", "");
            user.setUid(uuid);
            userList.add(user);
        }

    }

    @Override
    public void onSendFailure(Message.Data.Builder msg) {

    }

    @Override
    public void onSendSucceed(Message.Data.Builder msg) {

    }

    @Override
    public void onConnectFailure(String msg) {

    }

    @Override
    public void onConnecting() {

    }

    @Override
    public void onReceiveMessage(Message.Data msg) {
        if (msg.getCmd() == Message.Data.Cmd.MINE_FRIENDS_VALUE) {
            //获取到好友列表

        }
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected(boolean isException) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private class RcAdater extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(View.inflate(mActivity, R.layout.user_item, null));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final User user = userList.get(position);
            ViewHolder vholder = (ViewHolder) holder;
            vholder.tv_user_id.setText("uuid: " + user.getUid());
            vholder.tv_user_id.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChatActivity.lauchActivity(mActivity, user.getUid());
                }
            });
        }


        @Override
        public int getItemCount() {
            return userList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv_user_id;

            public ViewHolder(View itemView) {
                super(itemView);
                tv_user_id = (TextView) itemView.findViewById(R.id.tv_user_id);
            }
        }
    }
}
