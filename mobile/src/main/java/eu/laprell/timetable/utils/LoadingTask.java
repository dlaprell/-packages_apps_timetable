package eu.laprell.timetable.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by david on 10.11.14.
 */
public class LoadingTask extends AsyncTask<Void, Void, View> {
    private View[] mCache = new View[2];

    private int mResId;
    private LayoutInflater mInflater;

    public LoadingTask(int id, Context c) {
        mResId = id;
        mInflater = LayoutInflater.from(c);
    }

    @Override
    protected View doInBackground(Void... params) {
        int i = 0;

        for(;i < mCache.length;i++) {
            if(mCache[i] == null)
                break;
        }

        if(i != mCache.length)
            mCache[i] = mInflater.inflate(mResId, null, false);
        else return null;

        return mCache[i];
    }

    @Override
    protected void onPostExecute(View view) {
        if(view != null && isCancelled())
            execute();
    }

    public View getView() {
        return getView(null);
    }

    public View getView(ViewGroup con) {
        for(int i = 0;i < mCache.length;i++) {
            if(mCache[i] != null) {
                View v = mCache[i];
                v.invalidate();
                mCache[i] = null;
                return v;
            }
        }

        if(isCancelled())
            execute();

        return mInflater.inflate(mResId, con, false);
    }
}
