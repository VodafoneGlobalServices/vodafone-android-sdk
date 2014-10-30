package com.vodafone.global.sdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class Worker extends Thread {
    private final Handler.Callback callback;
    private Handler handler;

    public Worker(Handler.Callback callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            Looper.prepare();
            handler = new Handler(callback);
            Looper.loop();
        } catch (Exception e) {
            Log.e("WORKER", e.getMessage(), e);
        }
    }

    public Message createMessage(MessageType messageType) {
        Message message = Message.obtain();
        message.what = messageType.ordinal();
        return message;
    }

    public Message createMessage(MessageType messageType, Object object) {
        Message message = createMessage(messageType);
        message.obj = object;
        return message;
    }

    public Message createMessage(Message msg) {
        return Message.obtain(msg);
    }

    public void sendMessage(Message msg) {
        handler.sendMessage(msg);
    }

    public void sendMessageDelayed(Message msg, long delayMili) {
        handler.sendMessageDelayed(msg, delayMili);
    }

    public void clearMessageQueue() {
        for (MessageType messageType : MessageType.values()) {
            handler.removeMessages(messageType.ordinal());
        }
    }
}
