package com.suntiago.network.network.upload;

import com.suntiago.network.network.rsp.BaseResponse;

/**
 * 阿里云OSS服务数据实体.
 * <p>
 * Created by yj on 2017/10/26.
 */

public class OssAttributesResponse extends BaseResponse {

    public OssAttributes extra;

    public static class OssAttributes {
        public String access_key;
        public String bucket_name;
        public String endpoint;
        public String secret_key;
        public String security_token;
    }

}
