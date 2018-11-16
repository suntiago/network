package com.suntiago.network.network.iot;

import android.content.Context;

import com.google.gson.Gson;

import com.suntiago.network.network.utils.Slog;

/**
 * Created by zy on 2018/3/13.
 */

public class CommandControl implements UpdateSHelper.Callback, IOTCallback {
    private static final String TAG = "CommandControl";

    UpdateSHelper mUpdateSHelper;

    Context mContext;

    IOTClient mIOTClient;

    private int mCountTopActivityError = 0;

    public CommandControl(Context context) {
        mContext = context;
        mUpdateSHelper = new UpdateSHelper(this, mContext);
        mIOTClient = new IOTClientI(mContext);
        mIOTClient.setReceiveCallback(this);
    }


    //发送消息
    public void send(String a) {
        Slog.d(TAG, "send   [a]:" + a);
        mIOTClient.send(a);
    }

    //发送消息
    public void sendrrpc(String a, long rrpcId) {
        Slog.d(TAG, "sendrrpc   [a]:" + a + " rrpcId:" + rrpcId + "");
        mIOTClient.sendRrpc(a, rrpcId);
    }

    //接收消息
    @Override
    public void receive(String msg) {
        //收到更新的消息
        Slog.d(TAG, "receive  [msg]:" + msg);
        try {
            mUpdateSHelper.addS(new Gson().fromJson(msg, UpdateS.class));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void receive(String msg, long messageId) {
        Slog.d(TAG, "receive  [msg, messageId]:" + msg + " messageId:" + messageId + "");
        try {
            UpdateS updateS = new Gson().fromJson(msg, UpdateS.class);
            updateS.mrrpcId = messageId;
            mUpdateSHelper.addS(updateS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleNew(UpdateS updateS) {
        Slog.d(TAG, "handleNew  [updateS]:" + updateS.toString());

    }
}