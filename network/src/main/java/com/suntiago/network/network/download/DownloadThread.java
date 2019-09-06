package com.suntiago.network.network.download;


import android.text.TextUtils;

import com.suntiago.network.network.download.DownloadEntry.DownloadStatus;
import com.suntiago.network.network.utils.Slog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * @des Download thread.
 */
public class DownloadThread implements Runnable {
    private final String TAG = getClass().getSimpleName();
    private String url;
    private int index;
    private int startPos;
    private int endPos;
    private String apkName;

    private DownloadListener listener;
    private volatile boolean isPaused;
    private volatile boolean isCanceled;
    private volatile boolean isError;
    private boolean isSingleDownload;

    private DownloadEntry.DownloadStatus status;

    public DownloadThread(DownloadEntry entry, int index, int startPos, int endPos,
                          DownloadListener listener) {
        this.url = entry.url;
        this.index = index;
        this.startPos = startPos;
        this.endPos = endPos;
        this.apkName = TextUtils.isEmpty(entry.name) ? url.substring(url.lastIndexOf("/") + 1) : entry.name;
        this.listener = listener;
        if (startPos == 0 && endPos == 0) {
            isSingleDownload = true;
        } else {
            isSingleDownload = false;
        }
    }

    @Override
    public void run() {
        status = DownloadEntry.DownloadStatus.downloading;
        HttpURLConnection connection = null;
        try {
//            String utf_8url = "";
//            for (int i = 0; i < url.length(); i++) {
//                String a = url.charAt(i) + "";
//                if (!"!@#$&*()+:/;?+'".contains(url.charAt(i) + "")) {
//                    a = URLEncoder.encode(a, "utf-8");
//                }
//                utf_8url += a;
//            }
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            if (!isSingleDownload) {
                connection.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
            }
            connection.setConnectTimeout(Constants.CONNECT_TIME);
            connection.setReadTimeout(Constants.READ_TIME);

            int responseCode = connection.getResponseCode();
            int contentLength = connection.getContentLength();
            File savePath = new File(DownloadConfig.DOWNLOAD_PATH);
            if (!savePath.exists()) {
                savePath.mkdirs();
            }
            File file = new File(savePath, apkName);
            Slog.e(TAG, apkName);
            RandomAccessFile raf = null;
            FileOutputStream fos = null;
            InputStream is = null;

            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                //支持断点下载
                byte[] buffer = new byte[2048];
                int len = -1;
                //if (DownloadConfig.getInstance().getMax_download_threads() > 1) {
                //子线程数>1时，使用RandomAccessFile
                Slog.d(TAG, "DownloadThread==>" + "run()#####使用RandomAccessFile." +
                        " Support ranges. Index:" + index + "==" + url + "***" +
                        startPos + "-" + endPos + "**" + contentLength);
                raf = new RandomAccessFile(file, "rw");
                raf.seek(startPos);
                is = connection.getInputStream();

                while ((len = is.read(buffer)) != -1) {
                    if (isPaused || isCanceled || isError) {
                        break;
                    }
                    raf.write(buffer, 0, len);
                    listener.onProgressChanged(index, len);
                }

                raf.close();
                is.close();
            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                //不支持断点下载
                Slog.d(TAG, "DownloadThread==>" + "run()#####not support ranges. Index:"
                        + index + "==" + url + "***" + startPos + "-"
                        + endPos + "**" + contentLength);
                fos = new FileOutputStream(file);
                is = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    if (isPaused || isCanceled || isError) {
                        break;
                    }
                    fos.write(buffer, 0, len);
                    listener.onProgressChanged(index, len);
                }

                fos.close();
                is.close();
            } else {
                Slog.d(TAG, "DownloadThread==>index:" + index + " run()#####server error");
                status = DownloadEntry.DownloadStatus.error;
                listener.onDownloadError(index, "server error:" + responseCode);
                return;
            }

            if (isPaused) {
                Slog.d(TAG, "DownloadThread==>index:" + index + " run()#####pause");
                status = DownloadEntry.DownloadStatus.pause;
                listener.onDownloadPaused(index);
            } else if (isCanceled) {
                Slog.d(TAG, "DownloadThread==>index:" + index + " run()#####cancel");
                status = DownloadStatus.cancel;
                listener.onDownloadCanceled(index);
            } else if (isError) {
                Slog.d(TAG, "DownloadThread==>index:" + index + " run()#####error");
                status = DownloadStatus.error;
                listener.onDownloadError(index, "cancel manually by error");
            } else {
                Slog.d(TAG, "DownloadThread==>index:" + index + " run()#####done");
                status = DownloadStatus.done;
                listener.onDownloadCompleted(index);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Slog.e("DownloadThread==>", e.toString());
            if (isPaused) {
                Slog.d(TAG, "DownloadThread==>" + " run()#####exception and pause");
                status = DownloadStatus.pause;
                listener.onDownloadPaused(index);
            } else if (isCanceled) {
                Slog.d(TAG, "DownloadThread==>index:" + index +
                        " run()#####exception and cancel");
                status = DownloadStatus.cancel;
                listener.onDownloadCanceled(index);
            } else {
                Slog.d(TAG, "DownloadThread==>index:" + index + " run()#####error");
                status = DownloadStatus.error;
                listener.onDownloadError(index, e.getMessage());
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
                Slog.d(TAG, "DownloadThread==>run()#####index:" + index +
                        "***" + url + "*****close connection");
            }
        }
    }

    interface DownloadListener {
        void onProgressChanged(int index, int progress);

        void onDownloadPaused(int index);

        void onDownloadCanceled(int index);

        void onDownloadCompleted(int index);

        void onDownloadError(int index, String message);
    }

    public void pause() {
        Slog.d(TAG, "DownloadThread==>pause()#####index:" + index);
        isPaused = true;
        Thread.currentThread().interrupt();
    }

    public void cancel() {
        Slog.d(TAG, "DownloadThread==>index:" + index + " cancel()");
        isCanceled = true;
        Thread.currentThread().interrupt();
    }

    public boolean isRunning() {
        return status == DownloadStatus.downloading;
    }

    public void cancelByError() {
        Slog.d(TAG, "DownloadThread==>index:" + index + " cancelByError()");
        isError = true;
        Thread.currentThread().interrupt();
    }

}
