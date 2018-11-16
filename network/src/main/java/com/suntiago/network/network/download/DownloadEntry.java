package com.suntiago.network.network.download;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import com.suntiago.network.network.utils.Slog;

/**
 *
 * @des Download entity
 */
@DatabaseTable(tableName = "downloadentry")
public class DownloadEntry implements Serializable {

    private static final String TAG = "DownloadEntry";
    private static final long serialVersionUID = 1L;

    @DatabaseField(id = true)
    public String url;

    @DatabaseField
    public String name;

    @DatabaseField
    public int currentLength;

    @DatabaseField
    public int totalLength;

    @DatabaseField
    public DownloadStatus status = DownloadStatus.idle;

    @DatabaseField
    public boolean isSupportRange;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    public HashMap<Integer, Integer> ranges;

    @DatabaseField
    public int percent;

    @DatabaseField
    public String pkg_name;

    @DatabaseField
    public String loc_path;

    public enum DownloadStatus {
        idle, waiting, connecting, downloading, pause, resume, cancel, done, error
    }

    public DownloadEntry(String url) {
        this.url = url;
    }

    public DownloadEntry() {
    }

    @Override
    public String toString() {
        return name + " is " + status.name() + " with " + currentLength + "/" + totalLength + " " + percent + "%"
                + " pkg_name:"+pkg_name;
    }

    @Override
    public boolean equals(Object o) {
        return o.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    public void reset() {
        Slog.d(TAG, "reset  []:");
        currentLength = 0;
        percent = 0;
        ranges = null;
        status = DownloadStatus.idle;
        String path = DownloadConfig.DOWNLOAD_PATH + name;
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

}
