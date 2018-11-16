package com.suntiago.network.network.iot;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.suntiago.network.network.utils.SPUtils;
import com.suntiago.network.network.utils.Slog;

/**
 * Created by zy on 2017/11/17.
 */

public class UpdateSHelper {
    private static final String TAG = "UpdateSHelper";
    int maxUpdateSSize = 200;
    List<UpdateS> mUpdateS = new ArrayList<>();

    HashMap<String, UpdateS> mUpdateSHashMap = new HashMap();
    boolean working = false;
    Callback mCallback;
    Context mContext;

    public UpdateSHelper(Callback k, Context context) {
        mCallback = k;
        mContext = context;
        //readCache();
    }

    public void addS(UpdateS s) {
        if (!TextUtils.isEmpty(s.url)) {
            s.url = s.url.replace("\\", "/");
        }
        Slog.d(TAG, "addS  [s]:" + s.toString());
        if (mUpdateSHashMap.containsKey(s.id + "")) {
            Slog.d(TAG, "addS  [s]:" + "already added, ignore");
            UpdateS ss = mUpdateSHashMap.get(s.id + "");
            Slog.d(TAG, "addS  [s]:" + "update rrpc id" + ss.mrrpcId + " --> " + s.mrrpcId);
            ss.mrrpcId = s.mrrpcId;
            return;
        } else {
            mUpdateS.add(0, s);
            mUpdateSHashMap.put(s.id + "", s);
            //缓存限制
            while (mUpdateS.size() >= maxUpdateSSize) {
                int endIndex = mUpdateS.size() - 1;
                UpdateS us = mUpdateS.get(endIndex);
                mUpdateS.remove(endIndex);
                mUpdateSHashMap.remove(us.id + "");
            }
            handleS(s);
        }


        //saveCache();
    }

    //处理事件
    private void handleS(UpdateS s) {
        mCallback.handleNew(s);
    }

    //解决事件
    public void dealS() {
        working = false;
    }

    //处理失败，回滚
    public void backS() {

    }

    public UpdateS findByid(String id) {
        return mUpdateSHashMap.get(id);
    }

    public interface Callback {
        //通知有新的事件需要处理
        void handleNew(UpdateS updateS);
    }

    //通过包名获取升级的指令记录
    private UpdateS getUpdateS(String pkg_name) {
        for (UpdateS update : mUpdateS) {
            Slog.d(TAG, "getUpdateS  [pkg_name]:" + update.toString());
            if (update.pkg_name != null && update.pkg_name.equals(pkg_name)) {
                return update;
            }
        }
        return null;
    }
    private void saveCache() {
        Gson g = new Gson();
        SPUtils.getInstance(mContext).put("updateS—key",
                g.toJson(mUpdateS, new TypeToken<ArrayList<UpdateS>>() {
                }.getType()));
    }

    private void readCache() {
        Gson g = new Gson();
        String ss = SPUtils.getInstance(mContext).get("updateS—key");
        if (TextUtils.isEmpty(ss)) {
            return;
        }
        mUpdateS = g.fromJson(ss, new TypeToken<ArrayList<UpdateS>>() {
        }.getType());
    }
}
