package com.suntiago.network.network.download;

import android.os.Environment;

import java.io.File;

/**
 * 配置 下载参数
 */
public class DownloadConfig {
    private static DownloadConfig mInstance;

    //最大下载任务数量
    private int max_download_tasks = 1;

    //最大下载线程
    private int max_download_threads = 1;
    //超时时间
    private int min_operate_interval = 1000 * 5;
    //开启应用时，自动开始下载
    private boolean recoverDownloadWhenStart = false;

    //下载文件保存路径
    public static String DOWNLOAD_PATH = Environment.getExternalStorageDirectory()
            + File.separator
            + "download" + File.separator;

    public static long getSubThreadRefrashInterval(int fileSize) {
        if (fileSize <= 1024 * 1024 * 20) {
            //<=20M
            return 2 * 1000;
        } else if (fileSize > 1024 * 1024 * 20 && fileSize <= 1024 * 1024 * 100) {
            //20M~100M
            return 10 * 1000;
        } else {
            //>100M
            return 20 * 1000;
        }
    }

    public static DownloadConfig getInstance() {
        if (mInstance == null) {
            mInstance = new DownloadConfig();
        }
        return mInstance;
    }

    public int getMax_download_tasks() {
        return max_download_tasks;
    }

    public int getMax_download_threads() {
        return max_download_threads;
    }

    public int getMin_operate_interval() {
        return min_operate_interval;
    }

    public boolean isRecoverDownloadWhenStart() {
        return recoverDownloadWhenStart;
    }

    public void setMax_download_tasks(int max_download_tasks) {
        this.max_download_tasks = max_download_tasks;
    }

    public void setMax_download_threads(int max_download_threads) {
        this.max_download_threads = max_download_threads;
    }

    public void setMin_operate_interval(int min_operate_interval) {
        this.min_operate_interval = min_operate_interval;
    }

    public void setRecoverDownloadWhenStart(boolean recoverDownloadWhenStart) {
        this.recoverDownloadWhenStart = recoverDownloadWhenStart;
    }
}
