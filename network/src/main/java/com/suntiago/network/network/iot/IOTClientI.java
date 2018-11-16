package com.suntiago.network.network.iot;

import android.content.Context;
import android.text.TextUtils;

import com.aliyun.alink.linksdk.channel.core.base.AError;
import com.aliyun.alink.linksdk.channel.core.base.ARequest;
import com.aliyun.alink.linksdk.channel.core.base.AResponse;
import com.aliyun.alink.linksdk.channel.core.base.IOnCallListener;
import com.aliyun.alink.linksdk.channel.core.persistent.IOnSubscribeListener;
import com.aliyun.alink.linksdk.channel.core.persistent.PersistentConnectState;
import com.aliyun.alink.linksdk.channel.core.persistent.PersistentNet;
import com.aliyun.alink.linksdk.channel.core.persistent.event.IConnectionStateListener;
import com.aliyun.alink.linksdk.channel.core.persistent.event.IOnPushListener;
import com.aliyun.alink.linksdk.channel.core.persistent.event.PersistentEventDispatcher;
import com.aliyun.alink.linksdk.channel.core.persistent.mqtt.MqttInitParams;
import com.aliyun.alink.linksdk.channel.core.persistent.mqtt.request.MqttPublishRequest;
import com.aliyun.alink.linksdk.tools.ALog;
import com.suntiago.network.network.Api;
import com.suntiago.network.network.UpdateApi;
import com.suntiago.network.network.rsp.DeviceSecretResponse;
import com.suntiago.network.network.rsp.MacRequest;
import com.suntiago.network.network.utils.MacUtil;
import com.suntiago.network.network.utils.SPUtils;
import com.suntiago.network.network.utils.Slog;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.aliyun.alink.linksdk.channel.core.persistent.PersistentConnectState.CONNECTFAIL;

/**
 * Created by zy on 2018/3/14.
 */

public class IOTClientI implements IOTClient {
    Context mContext;
    private static final String TAG = "CommandControl";

    private String productKey = "";
    private String deviceName = "";
    private String deviceSecret = "";

    private boolean isUiSafe = true;
    private IOTCallback mIOTCallback;

    String topicGet = "";
    String topicRrpc = "";

    public IOTClientI(Context context) {
        mContext = context;
        initAliIOT();
    }

    private void initAliIOT() {
        Slog.d(TAG, "initAliIOT  []:");
        //设置阿里IOT物联网套件log等级
        ALog.setLevel(ALog.LEVEL_DEBUG);

        deviceName = MacUtil.getLocalMacAddressFromIp();
        Slog.d(TAG, "initAliIOT  []deviceName:" + deviceName);

        //环境配置需要在SDK初始化之前。SDK 支持自定义切换Host。
        //默认连接的地址为上海节点：
        //MqttConfigure.mqttHost = "ssl://[productKey].iot-as-mqtt.cn-shanghai.aliyuncs.com:1883";

        //读取缓存的三元组信息
        deviceSecret = SPUtils.getInstance(mContext).get(deviceName + "Secret");
        productKey = SPUtils.getInstance(mContext).get(deviceName + "key");

        //如果获取的信息为空，去后台拉去信息
        if (TextUtils.isEmpty(deviceSecret) || TextUtils.isEmpty(productKey)) {
            getDeviceSecret(deviceName);
        } else {
            initmqtt();
        }
    }

    //初始化iot套件
    private void initmqtt() {
        Slog.d(TAG, "initmqtt  []:");

        topicGet = "/" + productKey + "/" + deviceName + "/get";
        topicRrpc = "/sys/" + productKey + "/" + deviceName + "/rrpc/request/";
        connect();
    }

    //从网路拉去三元组信息
    private void getDeviceSecret(final String deviceName) {
        Slog.d(TAG, "getDeviceSecret  [deviceName]:");
        if (TextUtils.isEmpty(deviceName)) {
            this.deviceName = MacUtil.getLocalMacAddressFromIp();
        }
        Api.get().getApi(UpdateApi.class)
                .getDeviceSecret(new MacRequest(deviceName))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DeviceSecretResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Slog.e(TAG, "getDeviceSecret onError  [e]:" + e.getMessage());
                        //如果加载失败，稍后重新加载
                    }

                    @Override
                    public void onNext(DeviceSecretResponse appsResponse) {
                        if (appsResponse.error_code == 1000) {
                            deviceSecret = appsResponse.extra.ali_iot_dev_secret;
                            productKey = appsResponse.extra.ali_iot_product_key;
                            //保存三元组信息
                            SPUtils.getInstance(mContext).put(deviceName + "Secret", deviceSecret);
                            SPUtils.getInstance(mContext).put(deviceName + "key", productKey);
                            initmqtt();
                        } else {
                            Slog.e(TAG, "getDeviceSecret onNext " +
                                    appsResponse.error_code + appsResponse.error_msg);
                        }
                    }
                });
    }

    @Override
    public void send(String msg) {
        Slog.d(TAG, "send  [msg]:" + msg);
        //Publish 请求
        MqttPublishRequest publishRequest = new MqttPublishRequest();
        publishRequest.isRPC = false;
        publishRequest.topic = "/" + productKey + "/" + deviceName + "/update";
        publishRequest.payloadObj = msg;
        PersistentNet.getInstance().asyncSend(publishRequest, new IOnCallListener() {
            @Override
            public void onSuccess(ARequest request, AResponse response) {
                ALog.d(TAG, "send , onSuccess");
            }

            @Override
            public void onFailed(ARequest request, AError error) {
                ALog.d(TAG, "send , onFailed");
            }

            @Override
            public boolean needUISafety() {
                return isUiSafe;
            }
        });
    }

    @Override
    public void sendRrpc(String msg, long msgid) {
        Slog.d(TAG, "sendRrpc  [msg]:" + msg);
        //Publish 请求
        MqttPublishRequest publishRequest = new MqttPublishRequest();
        publishRequest.isRPC = false;
        publishRequest.topic = "/sys/" + productKey + "/" + deviceName + "/rrpc/response/" + msgid + "";
        publishRequest.payloadObj = msg;
        PersistentNet.getInstance().asyncSend(publishRequest, new IOnCallListener() {
            @Override
            public void onSuccess(ARequest request, AResponse response) {
                ALog.d(TAG, "send , onSuccess");
            }

            @Override
            public void onFailed(ARequest request, AError error) {
                ALog.d(TAG, "send , onFailed");
            }

            @Override
            public boolean needUISafety() {
                return isUiSafe;
            }
        });
    }

    @Override
    public void setReceiveCallback(IOTCallback iotCallback) {
        mIOTCallback = iotCallback;
    }

    @Override
    public void checkConnect() {
        Slog.d(TAG, "checkConnect  []:");
        //如果没有三元组信息，则去拉取信息
        if (TextUtils.isEmpty(deviceSecret) || TextUtils.isEmpty(productKey)) {
            Slog.d(TAG, "checkConnect  []:" + "检查过程中没有发现三元组信息，重新拉取信息");
            getDeviceSecret(deviceName);
        } else {
            PersistentConnectState p = PersistentNet.getInstance().getConnectState();
            Slog.d(TAG, "checkConnect  [PersistentConnectState]:" + p.name());
            if (p == CONNECTFAIL) {
                //如果连接失败，则重连
                connect();
            }
        }
    }

    private void connect() {
        Slog.d(TAG, "connect  []:");
        MqttInitParams initParams = new MqttInitParams(productKey, deviceName, deviceSecret);
        PersistentNet.getInstance().init(mContext, initParams);
        new Thread(new Runnable() {
            @Override
            public void run() {
                PersistentEventDispatcher.getInstance().registerOnTunnelStateListener(mIConnectionStateListener, isUiSafe);// 注册监听
            }
        }).start();
    }


    IConnectionStateListener mIConnectionStateListener = new IConnectionStateListener() {
        @Override
        public void onConnectFail(String s) {
            Slog.d(TAG, "registerOnTunnelStateListener onConnectFail  [s]:" + s);
        }

        @Override
        public void onConnected() {
            Slog.d(TAG, "registerOnTunnelStateListener onConnected  []:");
            //订阅消息
            PersistentNet.getInstance().subscribe(topicGet, mIOnSubscribeListener);
            //订阅消息
            PersistentNet.getInstance().subscribe(topicRrpc + "+", mIOnSubscribeListener1);
            //接收消息
            PersistentEventDispatcher.getInstance()
                    .registerOnPushListener(mIOnPushListener, isUiSafe);//
        }

        @Override
        public void onDisconnect() {
            Slog.d(TAG, "onDisconnect  []:");
            //取消订阅消息
            PersistentNet.getInstance().unSubscribe(topicGet, mIOnSubscribeListener);
            //取消订阅消息
            PersistentNet.getInstance().unSubscribe(topicRrpc + "+", mIOnSubscribeListener1);
            PersistentEventDispatcher.getInstance()
                    .unregisterOnPushListener(mIOnPushListener);

        }
    };


    IOnPushListener mIOnPushListener = new IOnPushListener() {
        @Override
        public void onCommand(String s, String s1) {
            Slog.d(TAG, "registerOnPushListener onCommand  [s, s1]:" + s + " " + s1);
            if (s.startsWith(topicRrpc)) {
                String id = s.substring(topicRrpc.length(), s.length());
                try {
                    Long idlong = Long.parseLong(id);
                    if (idlong != null && mIOTCallback != null) {
                        mIOTCallback.receive(s1, idlong);
                    }
                } catch (NumberFormatException n) {
                }

            } else if (mIOTCallback != null) {
                mIOTCallback.receive(s1);
            }
        }

        @Override
        public boolean shouldHandle(String s) {
            return true;
        }
    };

    IOnSubscribeListener mIOnSubscribeListener1 = new IOnSubscribeListener() {
        @Override
        public void onSuccess(String s) {
            Slog.d(TAG, "mIOnSubscribeListener1 subscribe onSuccess  [s]:" + s);
        }

        @Override
        public void onFailed(String s, AError aError) {
            Slog.d(TAG, "mIOnSubscribeListener1 subscribe onFailed  [s, aError]:" + s);
        }

        @Override
        public boolean needUISafety() {
            return isUiSafe;
        }
    };

    IOnSubscribeListener mIOnSubscribeListener = new IOnSubscribeListener() {
        @Override
        public void onSuccess(String s) {
            Slog.d(TAG, "mIOnSubscribeListener subscribe onSuccess  [s]:" + s);
        }

        @Override
        public void onFailed(String s, AError aError) {
            Slog.d(TAG, "mIOnSubscribeListener subscribe onFailed  [s, aError]:" + s);
        }

        @Override
        public boolean needUISafety() {
            return isUiSafe;
        }
    };
}
