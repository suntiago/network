package com.suntiago.network.network.download;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.suntiago.network.network.utils.Slog;


/**
 * @des Service to manager download tasks.
 */
public class DownloadService extends Service {
    private static final String TAG = "DownloadService";
    public static final int NOTIFY_DOWNLOADING = 1;
    public static final int NOTIFY_UPDATING = 2;
    public static final int NOTIFY_PAUSED_OR_CANCELLED = 3;
    public static final int NOTIFY_COMPLETED = 4;
    public static final int NOTIFY_ERROR = 5;
    public static final int NOTIFY_CONNECTING = 6;
    public static final int NOTIFY_NOT_ENOUGH_SIZE = 7;

    private HashMap<String, DownloadTask> mDownloadingTasks;
    private LinkedBlockingQueue<DownloadEntry> mWaitingQueue;

    private ExecutorService mExecutor;

    private Handler mHandler;

    private DataChanger dataChanger;
    private DBController dbController;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(getMainLooper()) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                final DownloadEntry entry = (DownloadEntry) msg.obj;
                Slog.d(TAG, "handleMessage  [msg]:" + msg.what);
                switch (msg.what) {
                    //下载完成
                    case NOTIFY_COMPLETED:
                        if (mDownloadingTasks.containsKey(entry.url)) {
                            mDownloadingTasks.remove(entry.url);
                        }

                        File savePath = new File(DownloadConfig.DOWNLOAD_PATH);
                        if (!savePath.exists()) {
                            return;
                        }
                        File file = new File(savePath, entry.name);
                        entry.loc_path = file.getPath();

                        //继续下载下一个
                        checkNext(entry);
                        break;
                    case NOTIFY_UPDATING:
                        break;
                    case NOTIFY_PAUSED_OR_CANCELLED:
                        checkNext(entry);
                        break;
                    case NOTIFY_ERROR:
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mDownloadingTasks.containsKey(entry.url)) {
                                    mDownloadingTasks.remove(entry.url);
                                }
                                resumeDownload(entry);
                            }
                        }, 5000);
                        break;

                    case NOTIFY_NOT_ENOUGH_SIZE:
                        Toast.makeText(getApplicationContext(),
                                "存储卡空间不足，请清理！", Toast.LENGTH_SHORT).show();
                        checkNext(entry);
                        break;
                }
                //更新下载状态
                DataChanger.getInstance(getApplication()).updateStatus(entry);
            }

        };

        mDownloadingTasks = new HashMap<String, DownloadTask>();
        mWaitingQueue = new LinkedBlockingQueue<DownloadEntry>();

        mExecutor = Executors.newCachedThreadPool();
        dataChanger = DataChanger.getInstance(getApplicationContext());
        dbController = DBController.getInstance(getApplicationContext());
        intializeDownload();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //防止App进程被强杀时数据丢失
    private void intializeDownload() {
        Slog.d(TAG, "intializeDownload  []:");
        ArrayList<DownloadEntry> mDownloadEtries = dbController.queryAll();
        if (mDownloadEtries != null) {
            for (DownloadEntry entry : mDownloadEtries) {
                if (entry.status == DownloadEntry.DownloadStatus.downloading
                        || entry.status == DownloadEntry.DownloadStatus.waiting) {
                    entry.status = DownloadEntry.DownloadStatus.pause;
                    if (DownloadConfig.getInstance().isRecoverDownloadWhenStart()) {
                        if (entry.isSupportRange) {
                            entry.status = DownloadEntry.DownloadStatus.pause;
                        } else {
                            entry.status = DownloadEntry.DownloadStatus.idle;
                            entry.reset();
                        }
                        addDownload(entry);
                    } else {
                        if (entry.isSupportRange) {
                            entry.status = DownloadEntry.DownloadStatus.pause;
                        } else {
                            entry.status = DownloadEntry.DownloadStatus.idle;
                            entry.reset();
                        }
                        dbController.newOrUpdate(entry);
                    }
                }
                dataChanger.addToOperatedEntryMap(entry.url, entry);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int action = intent.getIntExtra(Constants.KEY_DOWNLOAD_ACTION, -1);
            DownloadEntry entry =
                    (DownloadEntry) intent.getSerializableExtra(Constants.KEY_DOWNLOAD_ENTRY);
            /*****防止App进程被强杀时数据丢失*****/
            if (entry != null) {
                if (dataChanger.containsDownloadEntry(entry.url))
                    entry = dataChanger.queryDownloadEntryByUrl(entry.url);
                switch (action) {
                    case Constants.KEY_DOWNLOAD_ACTION_ADD:
                        addDownload(entry);
                        break;

                    case Constants.KEY_DOWNLOAD_ACTION_PAUSE:
                        pauseDownload(entry);
                        break;

                    case Constants.KEY_DOWNLOAD_ACTION_RESUME:
                        resumeDownload(entry);
                        break;

                    case Constants.KEY_DOWNLOAD_ACTION_CANCEL:
                        cancelDownload(entry);
                        break;

                    case Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL:
                        pauseAllDownload();
                        break;

                    case Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL:
                        recoverAllDownload();
                        break;

                    default:
                        break;

                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void recoverAllDownload() {
        Slog.d(TAG, "recoverAllDownload  []:");
        ArrayList<DownloadEntry> mRecoverableEntries =
                DataChanger.getInstance(getApplication()).queryAllRecoverableEntries();
        if (mRecoverableEntries == null) return;

        for (DownloadEntry entry : mRecoverableEntries) {
            addDownload(entry);
        }
        Slog.d(TAG, "DownloadService==>recoverAllDownload" + "***Task Size:" +
                mDownloadingTasks.size() + "***Waiting Queue:" + mWaitingQueue.size());
    }

    private void pauseAllDownload() {
        Slog.d(TAG, "pauseAllDownload  []:");
        while (mWaitingQueue.iterator().hasNext()) {
            DownloadEntry entry = mWaitingQueue.poll();
            entry.status = DownloadEntry.DownloadStatus.pause;
            DataChanger.getInstance(getApplication()).updateStatus(entry);
        }

        for (Map.Entry<String, DownloadTask> entry : mDownloadingTasks.entrySet()) {
            entry.getValue().pause();
        }
        mDownloadingTasks.clear();
        Slog.d(TAG, "DownloadService==>pauseAllDownload");
    }

    private void checkNext(DownloadEntry entry) {
        Slog.d(TAG, "checkNext  [entry]:");
        mDownloadingTasks.remove(entry.url);
        DownloadEntry newEntry = mWaitingQueue.poll();
        if (newEntry != null) {
            startDownload(newEntry);
        }
    }

    private void cancelDownload(DownloadEntry entry) {
        Slog.d(TAG, "cancelDownload  [entry]:");
        DownloadTask task = mDownloadingTasks.remove(entry.url);
        if (task != null) {
            task.cancel();
            Slog.d(TAG, "DownloadService==>pauseDownload#####cancel downloading task"
                    + "***Task Size:" + mDownloadingTasks.size()
                    + "***Waiting Queue:" + mWaitingQueue.size());
        } else {
            mWaitingQueue.remove(entry);
            entry.status = DownloadEntry.DownloadStatus.cancel;
            DataChanger.getInstance(getApplication()).updateStatus(entry);
            Slog.d(TAG, "DownloadService==>pauseDownload#####cancel waiting queue!"
                    + "***Task Size:" + mDownloadingTasks.size()
                    + "***Waiting Queue:" + mWaitingQueue.size());
        }
    }

    private void resumeDownload(DownloadEntry entry) {
        Slog.d(TAG, "resumeDownload  [entry]:");
        addDownload(entry);
        Slog.d(TAG, "DownloadService==>resumeDownload"
                + "***Task Size:" + mDownloadingTasks.size()
                + "***Waiting Queue:" + mWaitingQueue.size());
    }

    private void pauseDownload(DownloadEntry entry) {
        Slog.d(TAG, "pauseDownload  [entry]:");
        DownloadTask task = mDownloadingTasks.remove(entry.url);
        if (task != null) {
            Slog.d(TAG, "DownloadService==>pauseDownload#####pause downloading task"
                    + "***Task Size:" + mDownloadingTasks.size()
                    + "***Waiting Queue:" + mWaitingQueue.size());
            task.pause();
        } else {
            mWaitingQueue.remove(entry);
            entry.status = DownloadEntry.DownloadStatus.pause;
            DataChanger.getInstance(getApplication()).updateStatus(entry);
            Slog.d(TAG, "DownloadService==>pauseDownload#####pause waiting queue!"
                    + "***Task Size:" + mDownloadingTasks.size()
                    + "***Waiting Queue:" + mWaitingQueue.size());
        }

    }

    private void addDownload(DownloadEntry entry) {
        Slog.d(TAG, "addDownload  [entry]:");
        if (entry == null) {
            return;
        }
        checkDownloadPath(entry);
        if (isDownloadEntryRepeted(entry)) {
            return;
        }

        if (mDownloadingTasks.size() >= DownloadConfig.getInstance().getMax_download_tasks()) {
            mWaitingQueue.offer(entry);
            entry.status = DownloadEntry.DownloadStatus.waiting;
            DataChanger.getInstance(getApplication()).updateStatus(entry);
            Slog.d(TAG, "DownloadService==>addDownload#####bigger than max_tasks"
                    + "***Task Size:" + mDownloadingTasks.size()
                    + "***Waiting Queue:" + mWaitingQueue.size());
        } else {
            Slog.d(TAG, "DownloadService==>addDownload#####start tasks"
                    + "***Task Size:" + mDownloadingTasks.size()
                    + "***Waiting Queue:" + mWaitingQueue.size());
            startDownload(entry);
        }
    }

    private void startDownload(DownloadEntry entry) {
        Slog.d(TAG, "startDownload  [entry]:");
        DownloadTask task = new DownloadTask(entry, mHandler, mExecutor);
        mDownloadingTasks.put(entry.url, task);
        Slog.d(TAG, "DownloadService==>startDownload"
                + "***Task Size:" + mDownloadingTasks.size()
                + "***Waiting Queue:" + mWaitingQueue.size());
        task.start();
    }

    private void checkDownloadPath(DownloadEntry entry) {
        Slog.d(TAG, "checkDownloadPath  [entry]:");
        Slog.d(TAG, "DownloadService==>checkDownloadPath()");
        if (entry == null) {
            return;
        }
        File file = new File(DownloadConfig.DOWNLOAD_PATH
                + entry.url.substring(entry.url.lastIndexOf("/") + 1));
        if (file == null) {
            Slog.d(TAG, "checkDownloadPath  [entry file == null]:");
            entry.reset();
        }
        if (file != null && !file.exists()) {
            Slog.d(TAG, "checkDownloadPath  [entry !file.exists()]:");
            entry.reset();
        }
    }

    private boolean isDownloadEntryRepeted(DownloadEntry entry) {
        Slog.d(TAG, "isDownloadEntryRepeted  [entry]:");
        if (mDownloadingTasks.get(entry.url) != null) {
            Slog.d(TAG, "DownlaodService==>isDownloadEntryRepeted()" +
                    "##### The downloadEntry is in downloading tasks!!");
            return true;
        }

        if (mWaitingQueue.contains(entry)) {
            Slog.d(TAG, "DownlaodService==>isDownloadEntryRepeted()" +
                    "##### The downloadEntry is in waiting queue!!");
            return true;
        }
        return false;
    }
}
