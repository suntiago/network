package com.suntiago.network.network.rsp;

/**
 * Created by zy on 2018/3/14.
 */

public class DeviceSecretResponse extends BaseResponse {
    public DeviceSecret extra;

    public static class DeviceSecret {
        public String ali_iot_dev_secret;
        public String ali_iot_product_key;
    }
}
