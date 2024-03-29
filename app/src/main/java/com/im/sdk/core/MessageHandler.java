package com.im.sdk.core;

import android.os.Handler;
import android.text.TextUtils;

import com.example.xie.ClientApplication;
import com.im.sdk.protocol.Message;
import com.im.sdk.protocol.Message.Data.Cmd;
import com.xy.util.Log;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Created by xie on 2016/1/31.
 * 处理所有消息的发送接收
 */
public class MessageHandler {

    public static long timeOut = 12 * 1000;
    private static MessageHandler instance = new MessageHandler();
    ConcurrentHashMap<String, Message.Data.Builder> mQueue = new ConcurrentHashMap<>();
    private Handler timerHandler = new Handler();
    private ExecutorService mExecutor;
    private boolean looping = false;
    private boolean isStopLoop = false;

    private MessageHandler() {
    }

    public static MessageHandler instance() {
        return instance;
    }

    public void setExcutor(ExecutorService executor) {
        this.mExecutor = executor;
    }

    private void loopMessage() {
        Log.i("loopMessage mQueue size[" + mQueue.size());
        if (isStopLoop || mQueue.size() == 0) {
            Log.i("stopLoop.......");
            looping = false;
            return;
        }
        looping = true;
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkTimeOutMessage();
                loopMessage();
            }
        }, 5 * 1000);
    }

    private void checkTimeOutMessage() {
        Log.i("checkTimeOutMessage size[ " + mQueue.size() + " ]");
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, Message.Data.Builder> entry : mQueue.entrySet()) {
            long timeStrart = entry.getValue().getCreateTime();
            long time = currentTime - timeStrart;
            if (time >= timeOut) {
                IMClient.instance().onSendFailure(entry.getValue());
                Log.e("messaage send timeout [" + entry.getKey());
                pop(entry.getKey());
            }
        }
    }


    public void push(Message.Data.Builder msg) {
        if (msg != null) {
            mQueue.put(msg.getId(), msg);
        }
    }

    public Message.Data.Builder pop(String msgId) {
        if (msgId != null) {
            return mQueue.remove(msgId);
        }
        return null;
    }

    public void handSendMsg(final Message.Data.Builder msg) {
        //必须设置发送时间
        if (0 == msg.getCreateTime()) {
            msg.setCreateTime(System.currentTimeMillis());
        }
        proccessSendMessage(msg);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (IMClient.instance().isConnected()) {
                    try {
                        Log.e("message sending ...:" + msg);
                        IMClient.instance().writeMsg(msg);
                        if (!isStopLoop && !looping) {
                            Log.i("start to checkTimeOut Messages");
                            loopMessage();
                        }
                    } catch (Exception e) {
                        Log.e("message send failue :" + e.toString());
                        pop(msg.getId());
                        if (msg.getCmd() != Cmd.HEARTBEAT_VALUE) {
                            //心跳消息不用通知
                            IMClient.instance().onSendFailure(msg);
                        }
                    }
                } else {
                    Log.i("message sever is disconnect ,reconnect");
                    boolean stopReconnect = IMClient.instance().reconnect();
                    if (stopReconnect) {
                        if (msg.getCmd() != Cmd.HEARTBEAT_VALUE) {
                            //心跳消息不用通知
                            IMClient.instance().onSendFailure(msg);
                        }
                    }
                }
            }
        });
    }

    /**
     * 连接成功
     */
    public void bindDevice() {
        //绑定设备
        String clientId = ((ClientApplication) ClientApplication.instance()).getClientId();
        Message.Data.Builder data = Message.Data.newBuilder();
        data.setId(UUID.randomUUID().toString());
        data.setCmd(Cmd.BIND_CLIENT_VALUE);
        data.setClientId(clientId);
        data.setCreateTime(System.currentTimeMillis());
        handSendMsg(data);
    }

    private void proccessSendMessage(Message.Data.Builder data) {
        Log.i("proccess Send Message=====>>=====>>cmd[" + data.getCmd());
        push(data);
        switch (data.getCmd()) {
            case Cmd.BIND_CLIENT_VALUE:
                Log.i("is bind device message ");
                break;
            case Cmd.LOGIN_VALUE:
                Log.i("is login message");
                break;
            case Cmd.HEARTBEAT_VALUE:
                Log.i("is hearbreak mesage");
                break;
            case Cmd.CHAT_TXT_VALUE:
                Log.i("is nomal chat message");
                break;
        }
    }

    /**
     * 所有消息的接收在这处理
     */
    public void proccessReceiveMsg(Message.Data data) {
        Log.i("处理收到消息<<======<<======cmd[" + data.getCmd());
        switch (data.getCmd()) {
            case Cmd.BIND_CLIENT_VALUE:
                Log.i("绑定client ok");
                break;
            case Cmd.LOGIN_VALUE:
                if (TextUtils.isEmpty(data.getSenderId())) {
                    Log.i("服务端登录请求    msg[" + data.getContent());
                } else {
                    Log.i("登录结果 LoginSuccess[" + data.getContent());
                    //移除发送消息
                    Message.Data.Builder pop = pop(data.getId());
                    if (pop != null) {
                        pop.setContent(data.getContent());
                        IMClient.instance().onSendSucceed(pop);
                    }
                }
                break;
            case Cmd.OTHER_LOGGIN_VALUE:
                Log.i("帐号别处登录     account[" + data.getSenderId());
                break;
            case Cmd.HEARTBEAT_VALUE:
                Log.i("心跳回应                [" + data.getCreateTime());
                //移除心跳消息
                pop(data.getId());
                break;
            case Cmd.CHAT_TXT_VALUE:
                Log.i("收到聊天消息");
                break;
            case Message.Data.Cmd.CHAT_TXT_ECHO_VALUE:
                Log.i("消息回应,发送成功   id[" + data.getId());
                Message.Data.Builder pop = pop(data.getId());
                if (pop != null) {
                    Log.i("createTime:" + data.getCreateTime() + "==pop:" + pop.getContent());
                    pop.setCmd(Cmd.CHAT_TXT_ECHO_VALUE);
                    //移除发送消息
                    IMClient.instance().onSendSucceed(pop);
                }
                break;
            case Cmd.MINE_FRIENDS_VALUE:
                Log.i("消息回应,好友列表" + data.getContent());
                Message.Data.Builder pop2 = pop(data.getId());
                break;
        }
    }

}
