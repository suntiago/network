package com.suntiago.network.network.download;


import com.suntiago.network.network.utils.Slog;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 */
public class ConnectThread implements Runnable {
    private final String TAG = getClass().getSimpleName();
    private final ConnectListener listener;
    private final String url;
    private volatile boolean isRunning;

    public ConnectThread(String url, ConnectListener listener) {
        this.url = url;
        this.listener = listener;
    }

    @Override
    public void run() {
        isRunning = true;
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
            //connection.setRequestProperty("Range","bytes=0-");
            connection.setConnectTimeout(Constants.CONNECT_TIME);
            connection.setReadTimeout(Constants.READ_TIME);
            int responseCode = connection.getResponseCode();
            int contentLength = connection.getContentLength();
            boolean isSupportRange = false;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String ranges = connection.getHeaderField("Accept-Ranges");
                if ("bytes".equals(ranges)) {
                    isSupportRange = true;
                }
                listener.onConnectSuccess(isSupportRange, contentLength);
                Slog.d(TAG, "ConnectThread==>run()#####" + url +
                        "*****connect success " + responseCode);
            } else {
                listener.onConnectFaile("server error:" + responseCode);
                Slog.d(TAG, "ConnectThread==>run()#####" + url +
                        "*****server error: " + responseCode);
            }
            isRunning = false;
        } catch (Exception e) {
            Slog.d(TAG, "ConnectThread==>run()#####" + url +
                    "*****connect exception***" + e.getMessage());
            isRunning = false;
            listener.onConnectFaile(e.getMessage());
        } finally {
            if (connection != null) {
                Slog.d(TAG, "ConnectThread==>run()#####" + url +
                        "*****close connection");
                connection.disconnect();
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void cancel() {
        Thread.currentThread().interrupt();
    }

    interface ConnectListener {
        void onConnectSuccess(boolean isSupportRange, int totalLength);

        void onConnectFaile(String message);
    }

}