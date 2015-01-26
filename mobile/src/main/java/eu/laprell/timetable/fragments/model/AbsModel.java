package eu.laprell.timetable.fragments.model;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;

import java.io.File;

import eu.laprell.timetable.background.notifications.LessonNotifier2;
import eu.laprell.timetable.fragments.BaseFragment;

/**
 * Created by david on 22.01.15.
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class AbsModel {

    private BaseFragment mBase;
    private Context mContext;

    public AbsModel(BaseFragment f) {
        mBase = f;
        mContext = f.getActivity();
    }

    public Context getContext() {
        return mContext;
    }

    public String getString(int resId) {
        return mContext.getString(resId);
    }

    public String getString(int resId, Object... formatArgs) {
        return mContext.getString(resId, formatArgs);
    }

    public Resources getResources() {
        return mContext.getResources();
    }

    public File getFilesDir() {
        return mContext.getFilesDir();
    }

    public void sendBroadcast(Intent intent) {
        mContext.sendBroadcast(intent);
    }

    public void sendBroadcast(Intent intent, String receiverPermission) {
        mContext.sendBroadcast(intent, receiverPermission);
    }

    public String getPackageName() {
        return mContext.getPackageName();
    }

    public ApplicationInfo getApplicationInfo() {
        return mContext.getApplicationInfo();
    }

    public LessonNotifier2 getLessonNotifier() {
        return mBase.getLessonNotifier();
    }
}
