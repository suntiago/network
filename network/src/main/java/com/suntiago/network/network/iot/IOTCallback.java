package com.suntiago.network.network.iot;

/**
 * Created by zy on 2018/3/14.
 */

public interface IOTCallback {
    //接收消息
    void receive(String msg);

    //发送消息
    void receive(String msg, long messageId);

    //发送消息
    void send(String a);

    //发送消息
    void sendrrpc(String a, long rrpcId);

}
