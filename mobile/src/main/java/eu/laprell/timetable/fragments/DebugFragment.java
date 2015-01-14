package eu.laprell.timetable.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;

import eu.laprell.timetable.MainApplication;
import eu.laprell.timetable.R;
import eu.laprell.timetable.background.Logger;
import eu.laprell.timetable.database.AbsTimetableDatabase;
import eu.laprell.timetable.database.Day;
import eu.laprell.timetable.database.DbAccess;
import eu.laprell.timetable.database.Lesson;
import eu.laprell.timetable.database.TimeUnit;
import eu.laprell.timetable.database.TimetableDatabase;
import eu.laprell.timetable.utils.IntentUtils;

/**
 * Created by david on 11.01.15
 */
public class DebugFragment extends BaseFragment implements View.OnClickListener {

    private Button mLoggingButton;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_debug, container, false);

        v.findViewById(R.id.dbg_show_test_notif).setOnClickListener(this);
        v.findViewById(R.id.dbg_share_last_log).setOnClickListener(this);
        v.findViewById(R.id.dbg_clear_log).setOnClickListener(this);

        mLoggingButton = (Button)v.findViewById(R.id.dbg_toggle_logging);
        mLoggingButton.setOnClickListener(this);
        updateLoggingText();

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dbg_show_test_notif:
                showTestNotif();
                break;
            case R.id.dbg_toggle_logging:
                MainApplication.sLoggingEnabled = !MainApplication.sLoggingEnabled;

                if(getActivity().getApplication() instanceof MainApplication)
                    ((MainApplication)getActivity().getApplication()).saveLogSettings();

                updateLoggingText();
                break;
            case R.id.dbg_share_last_log:
                shareLastLog();
                break;
            case R.id.dbg_clear_log:
                Logger.clearLog();
                break;
        }
    }

    private void shareLastLog() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    return Logger.getCurrentLog();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                if(s == null) {
                    Toast.makeText(getActivity(), "Failed to get logs",
                            Toast.LENGTH_SHORT).show();
                } else {
                    IntentUtils.shareText(getActivity(), "Log", s, "Share Log with");
                }
            }
        }.execute();
    }

    private void updateLoggingText() {
        if(MainApplication.sLoggingEnabled) {
            mLoggingButton.setText(getText(R.string.dbg_stop_logging));
        } else {
            mLoggingButton.setText(getText(R.string.dbg_start_logging));
        }
    }

    private void showTestNotif() {
        final Context context = getActivity();

        Toast.makeText(context, "Will be showing random lesson", Toast.LENGTH_SHORT).show();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DbAccess access = new DbAccess(context);
                TimetableDatabase db = access.get();

                Random r = new Random();

                long[] lids = db.getDatabaseEntries(AbsTimetableDatabase.TYPE_LESSON);
                if(lids.length == 0) return null;

                Lesson l = (Lesson) db.getDatabaseEntryById(AbsTimetableDatabase.TYPE_LESSON,
                        lids[r.nextInt(lids.length)]);

                long[] tids = db.getDatabaseEntries(AbsTimetableDatabase.TYPE_TIMEUNIT);
                if(tids.length == 0) return null;

                TimeUnit t = (TimeUnit) db.getDatabaseEntryById(AbsTimetableDatabase.TYPE_TIMEUNIT,
                        tids[r.nextInt(tids.length)]);

                Day d = db.getDayForDayOfWeek(r.nextInt(5) + 1);

                getLessonNotifier().makeTestNotification(l, t, d);

                access.close();
                return null;
            }
        }.execute();
    }
}
