package eu.laprell.timetable.database;

import android.os.Handler;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by david on 13.11.14.
 */
public class Suggestor {

    public static final int DEP_SUBJECT = 1;
    public static final int DEP_PLACE = 2;
    public static final int DEP_TIME = 3;
    public static final int DEP_DAY_OF_WEEK = 4;
    public static final int DEP_COLOR = 5;
    public static final int DEP_BREAK = 6;

    public static final int SUG_NEW_LESSON = -1;
    public static final int SUG_NEW_BREAK = -2;
    public static final int SUG_NEW_FREE_TIME = -3;

    private class Item {
        private int[] dependencies;
        private boolean[] set;
        private Object[] datas;
        private int s_counter;
        private int suggestion;

        public Item(){}
        public Item(int s, int[] dep){
            dependencies = dep;
            suggestion = s;
            set = new boolean[dep.length];
            datas = new Object[dep.length];
        }
    }

    private Item[] mAllSuggestions = new Item[] {
            new Item(SUG_NEW_LESSON, new int[] {
                    DEP_SUBJECT,
                    DEP_TIME,
                    DEP_DAY_OF_WEEK
            }),
            new Item(SUG_NEW_BREAK, new int[] {
                    DEP_DAY_OF_WEEK,
                    DEP_TIME,
                    DEP_BREAK
            }),
            new Item(SUG_NEW_FREE_TIME, new int[] {
                    DEP_DAY_OF_WEEK,
                    DEP_TIME
            })
    };

    private boolean[] mSet = new boolean[mAllSuggestions.length];

    private OnSuggestionCallback mListener;
    private ExecutorService mExecutor;
    private Handler mHandler;

    public Suggestor(OnSuggestionCallback mListener) {
        this.mListener = mListener;
        mExecutor = Executors.newCachedThreadPool();

        try {
            mHandler = new Handler();
        } catch (Exception ex) {
            Log.e("TimeTable", "Failed to create deliver Handler", ex);
        }
    }

    public boolean isValidDependency(int d) {
        return (d >= 1 && d <= 6);
    }

    public void clearAll() {
        mSet = new boolean[mAllSuggestions.length];
        for(int i = 0;i < mAllSuggestions.length;i++) {
            mAllSuggestions[i].set = new boolean[mAllSuggestions[i].set.length];
            mAllSuggestions[i].s_counter = 0;
        }
    }

    public void addDependency(final int d, final Object o) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                _addDependency(d, o);
            }
        });
    }

    public void removeDependency(final int d, final Object o) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                _removeDependency(d, o);
            }
        });
    }

    private void _addDependency(int d, Object o) {
        if(!isValidDependency(d))
            return;

        // Loop through all availible suggestions
        for(int i = 0;i < mAllSuggestions.length;i++) {

            // If the suggestion is already set we can skip it
            //  -> faster this way
            if(!mSet[i]) {

                // Get a local reference for faster access
                final Item item = mAllSuggestions[i];

                // Loop through the items dependencies
                for (int x = 0;x < item.set.length;x++) {

                    // check if it is the right dependency and not already set
                    if(item.dependencies[x] == d && !item.set[x]) {

                        // Mark item set flag for dependency as true
                        item.set[x] = true;

                        // Check if now all dependencies are set
                        if(++item.s_counter == item.set.length) {
                            collectSuggestionDataAndSend(item.suggestion, true);

                            // Mark the whole item as already checked
                            mSet[i] = true;
                        }
                    } // item.dependencies[x] == d && !item.set[x]
                } // for (int x = 0;x < item.set.length;x++)
            } // !mSet[i]
        } // for(int i = 0;i < mAllSuggestions.length;i++)
    }

    public boolean isSuggestionActive(int s) {
        return false; // TODO: Guess!

        //              -> Implement ^^
    }

    private void _removeDependency(int d, Object o) {
        if(!isValidDependency(d))
            return;

        // Loop through all availible suggestions
        for(int i = 0;i < mAllSuggestions.length;i++) {

            // Get a local reference for faster access
            final Item item = mAllSuggestions[i];

            // Loop through the items dependencies
            for (int x = 0;x < item.set.length;x++) {

                // check if it is the right dependency and set
                if(item.dependencies[x] == d && item.set[x]) {

                    // Mark item set flag for dependency as true
                    item.set[x] = false;

                    // Check now which dependencies are set
                    item.s_counter--;
                    collectSuggestionDataAndSend(item.suggestion, false);

                    // Mark the whole item as not checked
                    mSet[i] = false;

                } // item.dependencies[x] == d && !item.set[x]
            } // for (int x = 0;x < item.set.length;x++)
        } // for(int i = 0;i < mAllSuggestions.length;i++)
    }

    private void collectSuggestionDataAndSend(int sug, final boolean added) {
        Object data = null;
        switch (sug) {
            case SUG_NEW_LESSON:

                break;
        }

        final Object o = data;
        final int sug_ = sug;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                reportSuggestionToListener(sug_, added, o);
            }
        });
    }

    private void reportSuggestionToListener(int sug, boolean b, Object o) {
        if(mListener != null) {
            mListener.onSuggestion(sug, b, o);
        }
    }

    public interface OnSuggestionCallback {
        public void onSuggestion(int type, boolean added, Object data);
    }
}
