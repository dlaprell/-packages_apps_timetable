package eu.laprell.timetable.background;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import eu.laprell.timetable.BuildConfig;
import eu.laprell.timetable.MainApplication;

/**
 * Created by david on 08.01.15.
 */
public class Logger {

    private static final Logger sLogger = new Logger();

    private Handler mHandler;
    private boolean mToLogCat = BuildConfig.DEBUG;
    private Context mContext;
    private File mLogFile;

    private Runnable mInit = new Runnable() {
        @Override
        public void run() {
            mLogFile = new File(mContext.getFilesDir(), "/log/cur.txt");
            initLogFile();
        }
    };

    private Logger() {
    }

    private void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                mHandler.post(mInit);
                Looper.loop();
            }
        }).start();
    }

    private void initLogFile() {
        if(!mLogFile.exists()) {
            mLogFile.getParentFile().mkdirs();
            try {
                mLogFile.createNewFile();
            } catch (IOException e) {
                mContext = null;
                e.printStackTrace();
            }
        }
    }

    private void _clearLog() {
        mLogFile.delete();
        initLogFile();
    }

    private void log(int level, String tag, String msg, Throwable t) {
        if(mHandler != null && mContext != null)
            mHandler.post(new LogData(level, tag, msg, t, System.currentTimeMillis()));
    }

    private class LogData implements Runnable {

        private int mLevel;
        private long mTime;
        private String mTab, mMsg;
        private Throwable mThro;

        private LogData(int mLevel, String mTab, String mMsg, Throwable mThro, long time) {
            this.mLevel = mLevel;
            this.mTab = mTab;
            this.mMsg = mMsg;
            this.mThro = mThro;
            mTime = time;
        }

        @Override
        public void run() {
            if(mToLogCat) {
                Log.w("Timetable_" + mTab, mMsg, mThro);
            }

            if(!MainApplication.sLoggingEnabled) return;

            try {
                OutputStream out = new FileOutputStream(mLogFile);

                PrintWriter writer = new PrintWriter(out);

                String time = new SimpleDateFormat("dd.MM.yyyy HH:mm:ssZ").format(new Date(mTime));

                writer.append(time);
                writer.append('\\');
                writer.append(mTab);
                writer.append('\\');
                writer.append(mMsg);
                writer.append('\n');

                if(mThro != null) {
                    writer.append(mThro.getMessage());
                    mThro.printStackTrace(writer);
                }

                writer.flush();
                writer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private String getLog() throws IOException {
        InputStream in = new FileInputStream(mLogFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        reader.close();

        return out.toString();
    }

    public static String getCurrentLog() throws IOException {
        return sLogger.getLog();
    }

    public static void log(String tag, String msg, Throwable t) {
        sLogger.log(0, tag, msg, t);
    }

    public static void log(String tag, String msg) {
        log(tag, msg, null);
    }

    public static void assignContext(Context ctx) {
        if(sLogger.mContext == null) {
            sLogger.mContext = ctx;
            sLogger.init();
        }
    }

    public static void clearLog() {
        sLogger.mHandler.post(new Runnable() {
            @Override
            public void run() {
                sLogger._clearLog();
            }
        });
    }
}
