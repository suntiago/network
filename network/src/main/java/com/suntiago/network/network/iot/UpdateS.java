package com.suntiago.network.network.iot;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Created by zy on 2017/11/17.
 */
public class UpdateS {
    public String action;
    public long id;
    public int uninstall;
    public String url;
    @SerializedName("version_code")
    public int version_code;
    @SerializedName("pkg_name")
    public String pkg_name;
    //用以保存rrpc的messageId;
    public long mrrpcId =0;

    public UpdateS simlulate() {
        action = "install";
        id = 12345;
        uninstall = 0;
        url = "http://192.168.1.208:8000/upload_custom/intelligenceclass_11.apk";
        version_code = 1;
        pkg_name = "com.viroyal.intelligenceclass";
        return this;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
