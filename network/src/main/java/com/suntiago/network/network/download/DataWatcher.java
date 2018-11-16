package com.suntiago.network.network.download;


import java.util.Observable;
import java.util.Observer;


/**
 * @des Observer
 */
public abstract class DataWatcher implements Observer {

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof DownloadEntry) {
            onDataChanged((DownloadEntry) data);
        }
    }

    public abstract void onDataChanged(DownloadEntry downloadEntry);

}
