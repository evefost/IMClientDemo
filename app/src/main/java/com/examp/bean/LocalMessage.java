package com.examp.bean;

import com.example.xie.ClientApplication;
import com.im.sdk.protocol.Message;

/**
 * Created by mis on 2016/2/24.
 */
public class LocalMessage {

    public static final  int RECIEVE_MSG = 0;
    public static final  int SEND_MSG = 1;
    public static int STATUS_SEND_ING = 0;
    public static int STATUS_SUCCESS = 1;
    public static int STATUS_FAILURE = -1;

    private int status;
    private Message.Data data;

    private LocalMessage() {

    }

    public LocalMessage(Message.Data data) {
        this.data = data;
    }

    public LocalMessage(Message.Data.Builder data) {
        this.data = data.build();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Message.Data getData() {
        return data;
    }

    public void setData(Message.Data data) {
        this.data = data;
    }

    public boolean isComMsg() {
        if (data.getSenderId().equals(((ClientApplication) ClientApplication.instance()).getUid())) {
            return false;
        } else {
            return true;
        }
    }

}
