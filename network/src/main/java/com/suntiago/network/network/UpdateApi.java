package com.suntiago.network.network;

import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;
import rx.Observable;
import com.suntiago.network.network.rsp.DeviceSecretResponse;
import com.suntiago.network.network.rsp.FileUploadResponse;
import com.suntiago.network.network.rsp.MacRequest;
import com.suntiago.network.network.upload.OssAttributesResponse;

/**
 * Created by cxy_nj on 2016/12/27.
 */

public interface UpdateApi {

    @Multipart
    @POST("/upload")
    Observable<FileUploadResponse> uploadFile(@Part("usage") String sign, @Part MultipartBody.Part file);

    /**
     * 获取app信息
     *
     * @return
     */
    @POST("/devmonitor/dev/reg")
    Observable<DeviceSecretResponse> getDeviceSecret(@Body MacRequest macRequest);

    /**
     * 获取阿里云oss相关
     */
    @GET
    Observable<OssAttributesResponse> getOssInfo(@Url String url, @Header("master_key") String master_key,
                                                 @Header("school_id") String school_id);
}
