package com.suntiago.network.network.download;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;

import com.suntiago.network.network.utils.Slog;


/**
 *
 * @des DBController
 */
public class DBController {
    private final String TAG = getClass().getSimpleName();
    private static DBController mInstance;
    private OrmDBHelper mDBhelper;

    private DBController(Context context) {
        mDBhelper = new OrmDBHelper(context);
        mDBhelper.getWritableDatabase();
    }

    public static DBController getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DBController(context);
        }
        return mInstance;
    }

    public synchronized void newOrUpdate(DownloadEntry entry) {
        try {
            Dao<DownloadEntry, String> dao = mDBhelper.getDao(DownloadEntry.class);
            dao.createOrUpdate(entry);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized ArrayList<DownloadEntry> queryAll() {
        Dao<DownloadEntry, String> dao;
        try {
            dao = mDBhelper.getDao(DownloadEntry.class);
            return (ArrayList<DownloadEntry>) dao.query(dao.queryBuilder().prepare());
        } catch (SQLException e) {
            Slog.e(TAG,e.getMessage());
            return null;
        }
    }

    public synchronized DownloadEntry queryByUrl(String url) {
        try {
            Dao<DownloadEntry, String> dao = mDBhelper.getDao(DownloadEntry.class);
            return dao.queryForId(url);
        } catch (SQLException e) {
            Slog.e(TAG, e.getMessage());
            return null;
        }
    }

    public synchronized void deleteByUrl(String url) {
        try {
            Dao<DownloadEntry, String> dao = mDBhelper.getDao(DownloadEntry.class);
            dao.deleteById(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
