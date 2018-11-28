package com.suntiago.network.network;

import android.content.Context;
import android.text.TextUtils;

import com.suntiago.network.network.utils.SPUtils;
import com.suntiago.network.network.utils.Slog;

import java.util.HashMap;

import retrofit2.Retrofit;

public class Api {
    private static final String TAG = Api.class.getSimpleName();


    private static String BASE_URL = "http://" + "192.168.1.208" + ":8000/";
    private HashMap<String, HashMap<Class, Object>> mApiObjects = new HashMap();
    private static Api sApi;
    private static String UPDATE_HOST = "192.168.1.208" + ":6020";

    //通过netty连接后台服务器 网络地址配置
    public static String NETTY_HOST = "";
    public static int NETTY_HOST_PORT = 0;

    private Api() {
        mApiObjects = new HashMap<>();
    }

    static Context sContext;

    private HashMap<String, HashMap<String, String>> headersKeyValue = new HashMap();

    public static void init(Context context) {
        sContext = context;
        SPUtils sp = SPUtils.getInstance(context);
        BASE_URL = sp.get("ip", Api.get().BASE_URL);
        UPDATE_HOST = sp.get("UPDATE_HOST", UPDATE_HOST);
        NETTY_HOST_PORT = sp.get("UPDATE_HOST_PORT", NETTY_HOST_PORT);
    }


    public static Api get() {
        if (sApi == null) {
            sApi = new Api();
        }
        return sApi;
    }

    public Api addHeader(String host, String key, String value) {
        if (TextUtils.isEmpty(host) || TextUtils.isEmpty(key)) {
            return this;
        }
        if (headersKeyValue.containsKey(host)) {
            HashMap h = headersKeyValue.get(host);
            if (h.containsKey(key)) {
                h.remove(key);
            }
            if (TextUtils.isEmpty(value)) {
                h.put(key, value);
            }
            return this;
        } else {
            headersKeyValue.put(host, new HashMap<String, String>());
            return addHeader(host, key, value);
        }
    }

    private HashMap getHeaders(String host) {
        if (TextUtils.isEmpty(host)) {
            return null;
        }
        if (headersKeyValue.containsKey(host)) {
            return headersKeyValue.get(host);
        } else {
            return null;
        }
    }

    private <T> T getApiObjects(String host, Class<T> tClass) {
        if (TextUtils.isEmpty(host)) {
            return null;
        }
        if (mApiObjects.containsKey(host)) {
            HashMap hashMap = mApiObjects.get(host);
            if (hashMap.containsKey(tClass)) {
                return (T) hashMap.get(tClass);
            }
        } else {
            return null;
        }
        return null;
    }

    private <T> void addApiObjects(String host, Class<T> tClass, Object o) {
        if (TextUtils.isEmpty(host)) {
            return;
        }
        if (mApiObjects.containsKey(host)) {
            HashMap hashMap = mApiObjects.get(host);
            if (hashMap.containsKey(tClass)) {
                hashMap.remove(tClass);
            }
            hashMap.put(tClass, o);
        } else {
            mApiObjects.put(host, new HashMap<Class, Object>());
            addApiObjects(host, tClass, o);
        }
    }

    public synchronized <T> T getApi(Class<T> tClass) {
        Slog.d(TAG, "getApi  [tClass]:");
        if (getApiObjects(BASE_URL, tClass) != null) {
            return getApiObjects(BASE_URL, tClass);
        }
        Retrofit retrofit = new Retrofit.Builder()
                .client(HttpManager.getHttpClient(getHeaders(BASE_URL), HttpLogInterceptor.Level.BODY))
                .baseUrl(BASE_URL)
                .addConverterFactory(HttpManager.sGsonConverterFactory)
                .addCallAdapterFactory(HttpManager.sRxJavaCallAdapterFactory)
                .build();
        addApiObjects(BASE_URL, tClass, retrofit.create(tClass));
        return (T) getApiObjects(BASE_URL, tClass);
    }

    public synchronized <T> T getApi(Class<T> tClass, String baseUrl) {
        Slog.d(TAG, "getApi  [tClass]:");
        if (getApiObjects(baseUrl, tClass) != null) {
            return getApiObjects(baseUrl, tClass);
        }
        Retrofit retrofit = new Retrofit.Builder()
                .client(HttpManager.getHttpClient(getHeaders(baseUrl), HttpLogInterceptor.Level.BODY))
                .baseUrl(baseUrl)
                .addConverterFactory(HttpManager.sGsonConverterFactory)
                .addCallAdapterFactory(HttpManager.sRxJavaCallAdapterFactory)
                .build();
        addApiObjects(baseUrl, tClass, retrofit.create(tClass));
        return (T) getApiObjects(baseUrl, tClass);
    }

    public void setApiConfig(String api, String netty_host, int netty_port) {
        SPUtils.getInstance(sContext).put("ip", api);
        SPUtils.getInstance(sContext).put("UPDATE_HOST", netty_host);
        SPUtils.getInstance(sContext).put("UPDATE_HOST_PORT", netty_port);
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
