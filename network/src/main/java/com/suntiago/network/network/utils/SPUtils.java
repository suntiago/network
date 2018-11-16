/*
    ShengDao Android Client, PreferencesManager
    Copyright (c) 2014 ShengDao Tech Company Limited
 */

package com.suntiago.network.network.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Set;


/**
 * [PreferencesManager管理类，提供get和put方法来重写SharedPreferences所提供的方法，更为实用和便捷]
 *
 * @author huxinwu
 * @version 1.0
 * @date 2014-3-5
 **/
@SuppressWarnings("ALL")
@SuppressLint({"SdCardPath", "DefaultLocale"})
public class SPUtils {

    private final String tag = SPUtils.class.getSimpleName();

    private Context mContext;
    private SharedPreferences preferences;
    private String DATA_URL = "/data/data/";
    private String SHARED_PREFS = "/shared_prefs";

    private static String shareName = "SHARE_DATA";
    public static final String THEME = "Theme";
    public static final String LANG = "Lang";

    private static SPUtils instance;

    /**
     * 构造方法
     *
     * @param context
     */
    private SPUtils(Context context) {
        this(context, shareName);
    }

    /**
     * 构造方法
     *
     * @param context
     * @param shareName
     */
    private SPUtils(Context context, String shareName) {
        mContext = context;
        preferences = context.getSharedPreferences(shareName,
                Context.MODE_PRIVATE);
    }

    public static SharedPreferences savePreToSDcard(Context context, String shareName) {
        try {
            Field field;
            // 获取ContextWrapper对象中的mBase变量。该变量保存了ContextImpl对象
            field = ContextWrapper.class.getDeclaredField("mBase");
            field.setAccessible(true);
            // 获取mBase变量
            Object obj = field.get(context);
            // 获取ContextImpl。mPreferencesDir变量，该变量保存了数据文件的保存路径
            field = obj.getClass().getDeclaredField("mPreferencesDir");
            field.setAccessible(true);
            // 创建自定义路径
            File file = null;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                file = new File(Environment.getExternalStorageDirectory(), "app/sp/");
            }
            if (!file.exists()) {
                file.mkdirs();
            }
            // 修改mPreferencesDir变量的值
            field.set(obj, file);
            return context.getSharedPreferences(shareName, Activity.MODE_PRIVATE);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return context.getSharedPreferences(shareName, context.MODE_PRIVATE);
    }

    /**
     * 得到单例模式的PreferencesManager对象
     *
     * @param context 上下文
     * @return
     */
    public static SPUtils getInstance(Context context) {
        return getInstance(context, shareName);
    }

    /**
     * 得到单例模式的PreferencesManager对象
     *
     * @param context   上下文
     * @param shareName 文件名称
     * @return
     */
    public static SPUtils getInstance(Context context,
                                      String shareName) {
        if (instance == null) {
            synchronized (SPUtils.class) {
                if (instance == null) {
                    instance = new SPUtils(context, shareName);
                }
            }
        }
        return instance;
    }

    @SuppressLint("CommitPrefEdits")
    public void put(String key, boolean value) {
        Editor edit = preferences.edit();
        if (edit != null) {
            edit.putBoolean(key, value);
            edit.commit();
        }
    }

    @SuppressLint("CommitPrefEdits")
    public void put(String key, String value) {
        Editor edit = preferences.edit();
        if (edit != null) {
            edit.putString(key, value);
            edit.commit();
        }
    }

    @SuppressLint("CommitPrefEdits")
    public void put(String key, int value) {
        Editor edit = preferences.edit();
        if (edit != null) {
            edit.putInt(key, value);
            edit.commit();
        }
    }

    @SuppressLint("CommitPrefEdits")
    public void put(String key, float value) {
        Editor edit = preferences.edit();
        if (edit != null) {
            edit.putFloat(key, value);
            edit.commit();
        }
    }

    @SuppressLint("CommitPrefEdits")
    public void put(String key, long value) {
        Editor edit = preferences.edit();
        if (edit != null) {
            edit.putLong(key, value);
            edit.commit();
        }
    }

    @SuppressLint("CommitPrefEdits")
    public void put(String key, Set<String> value) {
        Editor edit = preferences.edit();
        if (edit != null) {
            edit.putStringSet(key, value);
            edit.commit();
        }
    }

    @SuppressLint("CommitPrefEdits")
    public void put(String key, Integer value) {
        Editor edit = preferences.edit();
        if (edit != null) {
            edit.putInt(key, value);
            edit.commit();
        }
    }


    public String get(String key) {
        return preferences.getString(key, "");
    }

    public String get(String key, String defValue) {
        return preferences.getString(key, defValue);
    }

    public boolean get(String key, boolean defValue) {
        return preferences.getBoolean(key, defValue);
    }

    public int get(String key, int defValue) {
        return preferences.getInt(key, defValue);
    }

    public float get(String key, float defValue) {
        return preferences.getFloat(key, defValue);
    }

    public long get(String key, long defValue) {
        return preferences.getLong(key, defValue);
    }

    public Set<String> get(String key, Set<String> defValue) {
        return preferences.getStringSet(key, defValue);
    }

    public int getTheme(int defThemeId) {
        return instance.get(THEME, defThemeId);
    }

    public void setTheme(int themeId) {
        instance.put(THEME, themeId);
    }

    public String getLanguage(String defLang) {
        return instance.get(LANG, defLang);
    }

    public void setLang(String Language) {
        instance.put(LANG, Language);
    }

    public void clearAll() {
        try {
            String fileName = shareName + ".xml";
            StringBuilder path = new StringBuilder(DATA_URL).append(mContext.getPackageName()).append(SHARED_PREFS);
            File file = new File(path.toString(), fileName);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
