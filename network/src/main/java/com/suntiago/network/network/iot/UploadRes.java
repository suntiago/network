package com.suntiago.network.network.iot;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.google.gson.Gson;

import com.suntiago.network.network.utils.AppUtils;
import com.suntiago.network.network.utils.MacUtil;

/**
 * Created by zy on 2017/11/30.
 */

public class UploadRes {
    public String id;
    public String action;
    public String dev_sn;
    public String file_url;
    public String pkg_name;
    public int version_code;
    public String minutes_count;

    public UploadRes(Context context,
                     String id, String action, String file_url, String pkg_name) {
        dev_sn = MacUtil.getLocalMacAddressFromIp();
        this.id = id;
        this.action = action;
        this.file_url = file_url;
        this.pkg_name = pkg_name;
        if ("log-upload".equals(action)) {
            PackageInfo p = AppUtils.getPackageInfo(context, pkg_name);
            if (p != null) {
                version_code = p.versionCode;
            }
        }else if ("top-error".equals(action)) {
            dev_sn = MacUtil.getLocalMacAddressFromIp();
            this.action = action;
        }
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
