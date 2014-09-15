package com.vodafone.global.sdk.http.worker;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

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
        } catch (Throwable t) {
            //TODO: Add some logging
        }
    }

    public Message createMessage(int what) {
        Message message = Message.obtain();
        message.what = what;
        return message;
    }

    public Message createMessage(int what, Object object) {
        Message message = Message.obtain();
        message.what = what;
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
}
