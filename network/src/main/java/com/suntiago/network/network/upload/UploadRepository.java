package com.suntiago.network.network.upload;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.suntiago.network.network.Api;
import com.suntiago.network.network.BaseRspObserver;
import com.suntiago.network.network.UpdateApi;
import com.suntiago.network.network.rsp.FileUploadResponse;
import com.suntiago.network.network.utils.Slog;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 上传文件数据仓库类.
 * <p>
 * Created by yj on 2017/9/26.
 */

public class UploadRepository {
    private static final String TAG = "UploadRepository";


    static String sAliHostName = "";
    static String sBucketOther = "";
    static String sSchoolName = "";
    static String sApiHost = "";
    static String sMasterKey = "";
    static String sSchoolId = "";

    /**
     * initAlioss
     *
     * @param aliHostName 阿里服务器地址
     * @param bucketOther 文件夹名
     * @param schoolName  学校名
     * @param apiHost     自己的服务器地址，需要拉取alioss相关信息
     * @param masterKey   与自己的服务器通信时需要传的秘钥
     * @param schoolId    学校的id
     * @return
     * @throws
     */
    public static void initAlioss(String aliHostName, String bucketOther,
                                  String schoolName,
                                  String apiHost, String masterKey, String schoolId) {
        sAliHostName = aliHostName;
        sBucketOther = bucketOther;
        sSchoolName = schoolName;
        sApiHost = apiHost;
        sMasterKey = masterKey;
        sSchoolId = schoolId;
    }

    private static void uploadToOss(Context context, String path, String folder, String name,
                                    OssAttributesResponse.OssAttributes oss,
                                    final ApiCallback callback) {
        Slog.d(TAG, "uploadToOss  [context, path, folder, name, oss, callback]:");
        File file = new File(path);
        if (!file.exists()) {
            FileUploadResponse r = new FileUploadResponse();
            r.error_code = -1;
            r.error_msg = "文件不存在";
            callback.onResult(r);
            return;
        }
        //根据文件类型判断并加上后缀
        //name += getFileExtension(file);
        if (!folder.endsWith("/")) folder += "/";
        name = folder + name;
        //转换百分号编码
        //    name = EncryptionUtil.encodeUTF8(name);
        //配置上传信息
        OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(
                oss.access_key,
                oss.secret_key,
                oss.security_token);
        ClientConfiguration conf = new ClientConfiguration();
        // 连接超时，默认15秒
        conf.setConnectionTimeout(15 * 1000);
        // socket超时，默认15秒
        conf.setSocketTimeout(15 * 1000);
        // 最大并发请求书，默认5个
        conf.setMaxConcurrentRequest(5);
        // 失败后最大重试次数，默认2次
        conf.setMaxErrorRetry(2);
        OSSClient ossClient = new OSSClient(context, oss.endpoint, credentialProvider, conf);
        //下面3个参数依次为bucket名，Object名，上传文件路径
        PutObjectRequest put = new PutObjectRequest(oss.bucket_name, name, path);
        // 异步上传，可以设置进度回调
        //put.setProgressCallback((request, currentSize, totalSize) ->
        String finalName = name;
        final String finalRecallUrl = sAliHostName + "/" + encodeUTF8(finalName);
        ossClient.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest,
                PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Slog.d(TAG, "onSuccess  [request, result]:");
                callback.onResult("成功");
                FileUploadResponse r = new FileUploadResponse();
                r.error_code = 1000;
                r.error_msg = "成功";
                r.extra = finalRecallUrl;
                callback.onResult(r);
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion,
                                  ServiceException serviceException) {
                String error = "上传失败";
                Slog.d(TAG, "onFailure  [request, clientExcepion, serviceException]:");
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常
                    Slog.e("ErrorCode", serviceException.getErrorCode());
                    Slog.e("RequestId", serviceException.getRequestId());
                    Slog.e("HostId", serviceException.getHostId());
                    Slog.e("RawMessage", serviceException.getRawMessage());
                    error = serviceException.getMessage();
                }
                FileUploadResponse r = new FileUploadResponse();
                r.error_code = -1;
                r.error_msg = "上传失败";
                callback.onResult(r);
            }
        });

    }

    /**
     * 通过ali oss上传文件
     *
     * @param context
     * @param filePath 需要上传的文件的本地路径
     * @param folder   服务器上对应的目录
     * @param name     服务器上保存的文件名
     * @param callback 上传回调
     * @return
     * @throws
     */
    public static void upload(final Context context,
                              final String filePath,  String folder,
                              final String name, final ApiCallback callback) {
        if (!new File(filePath).exists()) {
            FileUploadResponse r = new FileUploadResponse();
            r.error_code = -1;
            r.error_msg = "文件不存在";
            callback.onResult(r);
            Slog.d(TAG, "upload  [context, filePath, folder, name, callback]:文件不存在");

            return;
        }
        if (TextUtils.isEmpty(folder)) {
            folder = sBucketOther;
        }
        folder = sSchoolName + "/" + folder;
        //先调用Oss接口获取Oss信息
        final String finalFolder = folder;
        getOssInfo(new Action1<OssAttributesResponse>() {
            @Override
            public void call(OssAttributesResponse info) {
                if (info.error_code == 1000) {
                    uploadToOss(context, filePath, finalFolder, name, info.extra, callback);
                } else {
                    FileUploadResponse f = new FileUploadResponse();
                    f.error_code = info.error_code;
                    f.error_msg = info.error_msg;
                    callback.onResult(f);
                }
            }
        });
    }

    private static void getOssInfo(final Action1<OssAttributesResponse> callback) {
        Api.get().getApi(UpdateApi.class)
                .getOssInfo(sApiHost, sMasterKey, sSchoolId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<OssAttributesResponse>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        OssAttributesResponse f = new OssAttributesResponse();
                        f.error_code = -1;
                        f.error_msg = e.getMessage();
                        callback.call(f);
                    }

                    @Override
                    public void onNext(OssAttributesResponse ossAttributesResponse) {
                        Slog.d(TAG, "onNext  [ossAttributesResponse]:");
                        callback.call(ossAttributesResponse);
                    }
                });
    }

    /**
     * 获取文件扩展名
     *
     * @param file
     * @return
     */
    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return "." + fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    /**
     * 转换字符为UTF-8编码
     */
    private static String encodeUTF8(String string) {
        try {
            return URLEncoder.encode(string, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return string;
        }
    }

    public interface ApiCallback {
        void onResult(FileUploadResponse response);

        void onResult(String s);
    }

    /**
     * 本地的服务器上传文件
     *
     * @param localpath 待上传文件的本地路径
     * @param usage     用途
     * @param action1   上传回调
     * @return
     * @throws
     */
    public static void upload(String localpath, String usage, final Action1<FileUploadResponse> action1) {
        Slog.d(TAG, "upload  [id, filepath, pkgName, action]:");
        File file = new File(localpath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        Api.get().getApi(UpdateApi.class)
                .uploadFile(usage, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseRspObserver<FileUploadResponse>(FileUploadResponse.class, new Action1<FileUploadResponse>() {
                    @Override
                    public void call(FileUploadResponse fileUploadResponse) {
                        action1.call(fileUploadResponse);
                    }
                }));
    }
}
