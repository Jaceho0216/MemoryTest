package com.askey.memorytest;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class LogService extends Service {
    private static final String TAG = "LogService";

    private static final int DEF_LOG_FILE_SIZE = 20000;  //max file size: KB;
    private static final int DEF_LOG_FILE_COUNT = 50;  //max file count:


    private static Process mProcess = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate(Log) E " + Thread.currentThread().getId());

        stopLogging();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand(Log) E, startId: " + startId + ", Thread ID: " + Thread.currentThread().getId());

        clearLogging();
        startLogging();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind(Log) E " + Thread.currentThread().getId());

        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy(Log) E " + Thread.currentThread().getId());
        stopLogging();

        super.onDestroy();
    }

    private Runnable r = new Runnable () {
        public void run() {
            startLogging();
        }
    };

    private void startLogging() {
        Log.d(TAG, "startLogging E");
        String filepath = getLogFilePath();
        if (!filepath.equals("")) {
            int filesize = DEF_LOG_FILE_SIZE;
            int filecount = DEF_LOG_FILE_COUNT;

            String[] cmds = {"logcat", "-f", filepath, "-r", String.valueOf(filesize), "-n", String.valueOf(filecount)};
            accessShell(cmds);
        }
    }

    private String getLogFilePath() {
        String path = getInternalStoragePath();

        File folder = new File(path + "/MemoryTestLog");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }

        if (success) {
            return folder.getAbsolutePath() + "/Memory" + formatFileName() + ".log";
        }
        return "";
    }

    private String formatFileName() {
        SimpleDateFormat formatter= new SimpleDateFormat("MMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    private String getInternalStoragePath() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return this.getFilesDir().getAbsolutePath();
        }
        return "";
    }

    private void stopLogging() {
        Log.d(TAG, "stopLogging E");
        if (mProcess != null) {
            mProcess.destroy();
            mProcess = null;
        }
    }

    private void clearLogging() {
        Log.d(TAG, "clearLogging E");

        String[] cmds = {"logcat", "-c"};
        accessShell(cmds);
    }

    private void accessShell(String[] cmds) {
        Log.d(TAG, "accessShell E");
        try {
            mProcess = Runtime.getRuntime().exec(cmds);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
