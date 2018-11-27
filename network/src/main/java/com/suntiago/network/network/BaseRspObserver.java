package com.suntiago.network.network;


import com.suntiago.network.network.rsp.BaseResponse;
import com.suntiago.network.network.utils.Slog;

import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLProtocolException;

import retrofit2.adapter.rxjava.HttpException;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * 一个Observer的封装类
 * <p>
 * 1 我们的网络接口返回结果结构都是类似的，从BaseResponse派生；
 * 2 把error转换成具体的error code，通过onNext返回出去，这样上层就只要提供一个
 * 回调方法就行了。
 * <p>
 * Created by LiGang on 2016/4/28.
 */
public class BaseRspObserver<T> extends Subscriber<T> {
    private static final String TAG = BaseRspObserver.class.getSimpleName();

    private Action1<T> action;

    protected Class<T> entityClass;

    public BaseRspObserver(Class<T> clazz, Action1<T> action) {
        this.entityClass = clazz;
        this.action = action;
    }

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
        Slog.e(TAG, "onError:" + e);
        e.printStackTrace();
        int code;
        String msg = "";
        if (e instanceof ConnectException) {
            code = ErrorCode.CONNECTION_EXCEPTION;
        } else if (e instanceof HttpException) {
            HttpException ex = (HttpException) e;
            code = ex.code();
            msg = ex.message();
        } else if (e instanceof SocketTimeoutException) {
            code = ErrorCode.SOCKET_TIMEOUT_Exception;
        } else if (e instanceof SSLHandshakeException || e instanceof SSLProtocolException) {
            code = ErrorCode.SSLHandshakeException;
        } else if (e instanceof JSONException) {
            code = ErrorCode.JSON_Exception;
        } else {
            code = ErrorCode.OTHER_EXCEPTION;
        }
        try {
            T t = entityClass.newInstance();
            if (t instanceof BaseResponse) {
                BaseResponse rsp = (BaseResponse) t;
                rsp.error_code = code;
                rsp.error_msg = msg;
                onNext(t);
            }
        } catch (Exception e1) {
            Slog.e(TAG, "onError:" + e1);
            e1.printStackTrace();
        }
    }

    @Override
    public void onNext(T t) {
        Slog.d(TAG, "onNext");
        if (action != null) {
            action.call(t);
        }
    }
}