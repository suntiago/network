package com.suntiago.networkDemo;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by zy on 2018/11/27.
 */

public interface TestApi {
    /**
     * 获取阿里云oss相关
     */
    @GET
    Observable<String> geturl();
}
