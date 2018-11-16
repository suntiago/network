package com.suntiago.network.network;

/**
 * Created by yu.zai on 2015/10/7.
 */
public class ErrorCode {
    public final static int SUCCESS  = 1000;
    public final static int OTHER_EXCEPTION = -99;
    public final static int CONNECTION_EXCEPTION = -100;
    public final static int SOCKET_TIMEOUT_Exception = -101;
    public final static int SSLHandshakeException = -102;
    public final static int JSON_Exception = -103;

    public final static int SERVER_BadGateway = 502;
    public final static int SERVER_Exception = 500;

    public final static int SMSCODE_ERROR = 1004;
    public final static int PHONE_ERROR = 1009;
    public final static int LOGINOUT = 2001;
    public final static int PHONE_USED = 2003;
    public final static int PHONE_NO_REGISTER = 2004;
    public final static int PASSWORD_ERROR = 2006;
    public final static int OLDPASSWORD_ERROR = 2009;
    public final static int THIRD_USED = 2011;

    public static boolean isSuccess(int code) {
        return code == SUCCESS;
    }

    public static boolean isBindSuccesss(int code) {
        return code == 3019;
    }
}

