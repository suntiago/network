package com.suntiago.network.network;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import okio.Buffer;

/**
 * 用于创建https访问的SSLSocketFactory
 * Created by LiGang on 2016/1/16.
 */
class CloudSSLSocketFactory {
    private static final String TAG = "CloudSSLSocketFactory";

    private static SSLContext mCtx;

    private CloudSSLSocketFactory() {
    }

    public static synchronized SSLSocketFactory getSSLSocketFactory(String cer) {
        if (mCtx != null) {
            return mCtx.getSocketFactory();
        }

        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            List<InputStream> certificates = new ArrayList<>();

            certificates.add(new Buffer().writeUtf8(cer).inputStream());

            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));

                try {
                    if (certificate != null)
                        certificate.close();
                } catch (IOException e) {
                }
            }

            mCtx = SSLContext.getInstance("TLS");

            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            trustManagerFactory.init(keyStore);
            mCtx.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            return mCtx.getSocketFactory();
        } catch (Exception e) {
            Log.e(TAG, "error:" + e);
        }
        return null;
    }
}
