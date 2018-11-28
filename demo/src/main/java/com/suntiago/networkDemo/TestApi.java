package com.suntiago.networkDemo;

import com.suntiago.network.network.rsp.BaseResponse;

import retrofit2.http.GET;
import retrofit2.http.Header;
import rx.Observable;

/**
 * Created by zy on 2018/11/27.
 */

public interface TestApi {
    /**
     * 获取阿里云oss相关
     */
    @GET("device/guest/teachers")
    Observable<BaseResponse> geturl(@Header("dev_mac") String dev);
}
