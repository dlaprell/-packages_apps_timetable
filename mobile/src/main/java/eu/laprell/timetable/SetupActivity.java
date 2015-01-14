package eu.laprell.timetable;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.FrameLayout;

import eu.laprell.timetable.database.DbAccess;
import eu.laprell.timetable.database.TimeUnit;
import eu.laprell.timetable.fragments.SetupListFragment;
import eu.laprell.timetable.fragments.interfaces.SetupResultInterface;


public class SetupActivity extends FragmentActivity implements SetupResultInterface {

    private FrameLayout mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup);

        mContainer = (FrameLayout)findViewById(R.id.container);

        getSupportFragmentManager().beginTransaction()
                .replace(mContainer.getId(), new SetupListFragment())
                .commit();
    }


    private void finishedSetup() {
        setResult(RESULT_OK);

        finish();
        overridePendingTransition(R.anim.fade_out, 0);
    }

    @Override
    public void finishedWithSetup() {
        finishedSetup();
    }

    @Override
    public void makeCustomSchool() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DbAccess access = new DbAccess(SetupActivity.this);

                TimeUnit t = new TimeUnit(-1);
                t.setStartTime(8, 0);
                t.setEndTime(9, 0);

                access.get().insertDatabaseEntryForId(t);

                access.close();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                finishedWithSetup();
            }
        }.execute();
    }
}
