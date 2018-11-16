package com.suntiago.network.network.rsp;

import java.io.Serializable;

/**
 * Created by viroyal-android01 on 2016/7/21.
 */
public class BaseResponse implements Serializable{
    public int error_code;
    public String error_msg;

    public BaseResponse() {
    }

    public BaseResponse(int error_code, String error_msg) {
        this.error_code = error_code;
        this.error_msg = error_msg;
    }

    public String toString() {
        return "error_code:"+error_code+",error_msg:"+error_msg;
    }
}
