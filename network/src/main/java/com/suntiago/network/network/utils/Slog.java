package com.suntiago.network.network.utils;

import android.util.Log;

/**
 * Created by shengpeng on 2015-09-17.
 */
@SuppressWarnings("ALL")
public final class Slog {
    public static boolean DEBUG = true;
    public static boolean SHOW_ACTIVITY_STATE = true;

    public static void setDebug(boolean debug, boolean showActivityStatus) {
        DEBUG = debug;
        SHOW_ACTIVITY_STATE = showActivityStatus;
    }

    static ILog mILog;

    public static void setLogCallback(ILog logCallback) {
        mILog = logCallback;
    }

    private Slog() {
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            if (mILog != null) {
                mILog.i(tag, msg);
            } else {
                Log.i(tag, msg);
            }
        }
    }

    private static void v(String tag, String msg) {
        if (DEBUG) {
            if (mILog != null) {
                mILog.v(tag, msg);
            } else {
                Log.v(tag, msg);
            }
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            if (mILog != null) {
                mILog.d(tag, msg);
            } else {
                Log.d(tag, msg);
            }
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            if (mILog != null) {
                mILog.e(tag, msg);
            } else {
                Log.e(tag, msg);
            }
        }
    }

    public static final void state(String packName, String state) {
        if (SHOW_ACTIVITY_STATE) {
            if (mILog != null) {
                mILog.d(packName, state);
            } else {
                Log.d(packName, state);
            }
        }
    }

    public interface ILog {
        void i(String tag, String msg);

        void v(String tag, String msg);

        void d(String tag, String msg);

        void e(String tag, String msg);

        void state(String packName, String state);
    }
}
