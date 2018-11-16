package com.suntiago.network.network.iot;

/**
 * Created by zy on 2018/3/14.
 */

public interface IOTClient {
    /**
     * 发送消息
     */
    void send(String msg);

    /**
     * 发送消息
     */
    void sendRrpc(String msg, long msgid);

    void setReceiveCallback(IOTCallback iotCallback);

//    void connect();
//    void disconnect();

    /*网络状态自检*/
    void checkConnect();
}
