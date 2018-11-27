package com.suntiago.network.network;

import android.content.Context;

import com.suntiago.network.network.utils.SPUtils;
import com.suntiago.network.network.utils.Slog;

import java.util.HashMap;

import retrofit2.Retrofit;

public class Api {
    private static final String TAG = Api.class.getSimpleName();

    private static String DEFAULT_HOST = "192.168.1.208";

    private static String BASE_URL = "http://" + DEFAULT_HOST + ":8000/";
    private HashMap<Class, Object> mApiObjects;
    private static Api sApi;
    private static String UPDATE_HOST = DEFAULT_HOST + ":6020";

    //通过netty连接后台服务器 网络地址配置
    public static String NETTY_HOST = "";
    public static int NETTY_HOST_PORT = 0;

    private Api() {
        mApiObjects = new HashMap<>();
    }

    static Context sContext;

    public static void init(Context context) {
        sContext = context;
        Api.get().BASE_URL = SPUtils.getInstance(context).get("ip", Api.get().BASE_URL);
        Api.get().UPDATE_HOST = SPUtils.getInstance(context).get("UPDATE_HOST", UPDATE_HOST);
        Api.get().NETTY_HOST_PORT = SPUtils.getInstance(sContext).get("UPDATE_HOST_PORT", NETTY_HOST_PORT);

        Api.get().mApiObjects.clear();
    }

    public static Api get() {
        if (sApi == null) {
            sApi = new Api();
        }
        return sApi;
    }

    public synchronized <T> T getApi(Class<T> tClass) {
        Slog.d(TAG, "getApi  [tClass]:");
        if (mApiObjects != null) {
            if (mApiObjects.get(tClass) != null) {
                return (T) mApiObjects.get(tClass);
            }
        } else {
            mApiObjects = new HashMap<>();
        }
        Retrofit retrofit = new Retrofit.Builder()
                .client(HttpManager.getHttpClient(HttpLogInterceptor.Level.BODY))
                .baseUrl(BASE_URL)
                .addConverterFactory(HttpManager.sGsonConverterFactory)
                .addCallAdapterFactory(HttpManager.sRxJavaCallAdapterFactory)
                .build();
        mApiObjects.put(tClass, retrofit.create(tClass));
        return (T) mApiObjects.get(tClass);
    }

    public synchronized <T> T getApi(Class<T> tClass, String baseUrl) {
        Slog.d(TAG, "getApi  [tClass]:");
        if (mApiObjects != null) {
            if (mApiObjects.get(tClass) != null) {
                return (T) mApiObjects.get(tClass);
            }
        } else {
            mApiObjects = new HashMap<>();
        }
        Retrofit retrofit = new Retrofit.Builder()
                .client(HttpManager.getHttpClient(HttpLogInterceptor.Level.BODY))
                .baseUrl(baseUrl)
                .addConverterFactory(HttpManager.sGsonConverterFactory)
                .addCallAdapterFactory(HttpManager.sRxJavaCallAdapterFactory)
                .build();
        mApiObjects.put(tClass, retrofit.create(tClass));
        return (T) mApiObjects.get(tClass);
    }


    public void setApiConfig(String api, String netty_host, int netty_port) {
        String api_host = api.subSequence(7, api.length() - 6).toString();

        SPUtils.getInstance(sContext).put("ip", api);
        SPUtils.getInstance(sContext).put("UPDATE_HOST", netty_host);
        SPUtils.getInstance(sContext).put("UPDATE_HOST_PORT", netty_port);

        DEFAULT_HOST = api_host;
        BASE_URL = api;
        NETTY_HOST = netty_host;
        NETTY_HOST_PORT = netty_port;

        mApiObjects.clear();
    }

    public String getApiUPDATE() {
        return UPDATE_HOST;
    }

    public String getApi() {
        return BASE_URL;
    }

}
