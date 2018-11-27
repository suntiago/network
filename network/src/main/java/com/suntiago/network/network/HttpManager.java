package com.suntiago.network.network;

import com.suntiago.network.network.utils.Slog;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by shengpeng on 2016-04-14.
 */
public class HttpManager {

    private static boolean DEBUG = true;

    private static final int CONNECT_TIMEOUT = 12000;
    private static final int READ_TIMEOUT = 12000;

    private static OkHttpClient sOkHttpClient = new OkHttpClient();
    public static Converter.Factory sGsonConverterFactory = GsonConverterFactory.create();
    public static CallAdapter.Factory sRxJavaCallAdapterFactory = RxJavaCallAdapterFactory.create();

    private static SSLSocketFactory sSslSocketFactory;

    public static void init(boolean debug, String cer) {
        //sSslSocketFactory = CloudSSLSocketFactory.getSSLSocketFactory(cer);
        DEBUG = debug;
    }

    public static OkHttpClient getHttpClient(HashMap<String, String> host, HttpLogInterceptor.Level level) {
        OkHttpClient.Builder builder = sOkHttpClient.newBuilder();
        builder.connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);
        builder.readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
        try {
            if (sSslSocketFactory == null) {
                SSLSocketFactory sslSocketFactory = trustAllHosts();
                builder.sslSocketFactory(sslSocketFactory);
            } else {
                builder.sslSocketFactory(sSslSocketFactory);
            }
            builder.hostnameVerifier(DO_NOT_VERIFY);
            if (sSslSocketFactory != null) {

            }
        } catch (Exception e) {
            Slog.e("Api", "getHttpClient e:" + e);

        } finally {

        }
        //allowAllSSL(builder);
        // 设置request 拦截器
        RequestInterceptor requestInterceptor = new RequestInterceptor();
        if (host != null) {
            requestInterceptor.setHost(host);
        }
        builder.addInterceptor(requestInterceptor);

        // 设置log拦截器
        if (DEBUG) {
            HttpLogInterceptor logging = new HttpLogInterceptor();
            logging.setLevel(level);
            builder.interceptors().add(logging);
        }
        return builder.build();
    }


    public static OkHttpClient getHttpClient(HttpLogInterceptor.Level level) {
        return getHttpClient(null, level);
    }

    /**
     * 允许所有的SSL
     */
    private static void allowAllSSL(OkHttpClient.Builder client) {
        TrustManager[] trustManager = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws
                            CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws
                            CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManager, new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            client.sslSocketFactory(sslSocketFactory);
            client.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    /**
     * Trust every server - dont check for any certificate
     */
    private static SSLSocketFactory trustAllHosts() {
        SSLSocketFactory sslSocketFactory = null;
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }

                    public void checkClientTrusted(X509Certificate[] chain,
                                                   String authType) throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] chain,
                                                   String authType) throws CertificateException {
                    }
                }
        };
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            sslSocketFactory = sc.getSocketFactory();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return sslSocketFactory;
    }

    // always verify the host - dont check for certificate
    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * This interceptor compresses the HTTP request body. Many webservers can't handle this!
     */
    final static class RequestInterceptor implements Interceptor {

        HashMap<String, String> host;

        public void setHost(HashMap<String, String> host) {
            this.host = host;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request.Builder build = originalRequest.newBuilder();
            if (host != null) {
                for (String s : host.keySet()) {
                    build.header(s, host.get(s));
                }
            }
            Request compressedRequest = build.build();
            return chain.proceed(compressedRequest);
        }
    }

}
