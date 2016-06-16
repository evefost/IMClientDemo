package com.im.sdk.core;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.example.xie.ClientApplication;
import com.google.protobuf.ByteString;
import com.im.sdk.protocol.Message;
import com.mdroid.xxtea.Tea;
import com.xy.util.Log;


public class HeartBeatManager {
    private static HeartBeatManager inst;
    private final int HEARTBEAT_INTERVAL = 30 * 1000;
    private final String ACTION_SENDING_HEARTBEAT = "com.custom.protocal.im.heart.break";
    // 心跳检测4分钟检测一次，并且发送心跳包
    // 服务端自身存在通道检测，5分钟没有数据会主动断开通道
    private Context context = ClientApplication.instance();
    private PendingIntent pendingIntent;
    /**
     * --------------------boradcast-广播相关-----------------------------
     */
    private BroadcastReceiver imReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_SENDING_HEARTBEAT)) {
                Log.d( "发送心跳包");
                Message.Data.Builder data = Message.Data.newBuilder();
                String content = "abc";
                String key = "123";
                byte[] enBytes = Tea.encrypt(content.getBytes(), key.getBytes());
                //byte[] deBytes = Tea.decrypt(new String(enBytes).getBytes(),key.getBytes());
                //Log.i("src content:"+new String(deBytes));
                data.setCmd(Message.Data.Cmd.HEARTBEAT_VALUE);
                data.setContent(new String(enBytes));
                data.setBody(ByteString.copyFrom(enBytes));
                IMClient.instance().sendMessage(data);
            }
        }
    };

    private HeartBeatManager() {
    }

    public static HeartBeatManager instance() {
        if (inst == null) {
            inst = new HeartBeatManager();
        }
        return inst;
    }

    // 登陆成功之后
    public void startHeartBeat() {
        Log.w( "定时启动心跳 ,周期[" + HEARTBEAT_INTERVAL);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SENDING_HEARTBEAT);
        context.registerReceiver(imReceiver, intentFilter);
        //获取AlarmManager系统服务
        if (pendingIntent == null) {
            Log.w( "heartbeat#fill in pendingintent");
            Intent intent = new Intent(ACTION_SENDING_HEARTBEAT);
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            if (pendingIntent == null) {
                Log.w( "heartbeat#scheduleHeartbeat#pi is null");
                return;
            }
        }
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, pendingIntent);
        Log.i( "已启动心跳");
    }

    public void reset() {
        Log.d( "heartbeat#reset begin");
        try {
            context.unregisterReceiver(imReceiver);
            cancelHeartbeatTimer();
            Log.d( "heartbeat#reset stop");
        } catch (Exception e) {
            Log.e( "heartbeat#reset error:%s", e.getCause());
        }
    }

    public void cancelHeartbeatTimer() {
        Log.w( "cancelHeartbeatTimer");
        if (pendingIntent == null) {
            Log.w( "heartbeat#pi is null");
            return;
        }
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }

}