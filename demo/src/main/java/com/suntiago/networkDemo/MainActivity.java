package com.suntiago.networkDemo;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.suntiago.getpermission.rxpermissions.RxPermissions;
import com.suntiago.network.network.download.DataChanger;
import com.suntiago.network.network.download.DataWatcher;
import com.suntiago.network.network.download.DownloadConfig;
import com.suntiago.network.network.download.DownloadEntry;
import com.suntiago.network.network.download.DownloadManager;
import com.suntiago.network.network.utils.Slog;

import java.io.File;

import rx.functions.Action1;

/**
 * Created by zy on 2018/11/16.
 */

public class MainActivity extends Activity {
    private final String TAG = getClass().getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private DataChanger dataChanger;

    //测试下载模块的代码
    public void testDownload(View view) {
        //指定下载路径
        DownloadConfig.DOWNLOAD_PATH = Environment.getExternalStorageDirectory()
                + File.separator
                + "demo_download" + File.separator;

        dataChanger = DataChanger.getInstance(this);
        DownloadManager.getInstance(this).addObserver(new DataWatcher() {

            @Override
            public void onDataChanged(DownloadEntry data) {
                Slog.d(TAG, "onDataChanged  [data]:" + "正在下载" + data.percent + "%");
                Toast.makeText(MainActivity.this, "onDataChanged  [data]:" + "正在下载" + data.percent + "%",
                        Toast.LENGTH_SHORT).show();
            }
        });

        RxPermissions.getInstance(this)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean granted) {
                        if (granted) {
                            startDownload();
                        } else {
                            Slog.d(TAG, "uploadFile  [id, filepath, pkgName, action]:");
                        }
                    }
                });
    }

    //开始下载
    private void startDownload() {
        String url = "https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=401714075," +
                "168034038&fm=173&app=25&f=JPEG?w=640&h=360&s=05D1AB6C2D27261D8CB1C49E0300009B";
        DownloadEntry entry;
        if (dataChanger.containsDownloadEntry(url)) {
            entry = dataChanger.queryDownloadEntryByUrl(url);
        } else {
            entry = new DownloadEntry(url);
            entry.name = url.substring(url.lastIndexOf("/") + 1);//apk名字
        }
        DownloadManager.getInstance(this).add(entry);
    }

}
