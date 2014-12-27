package eu.laprell.timetable;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import eu.laprell.timetable.addon.KghAddon;
import eu.laprell.timetable.widgets.ShortLoadingDialog;


public class SetupActivity extends Activity {

    private FrameLayout mContainer;
    private RecyclerView mRecyclerView;
    private AddonAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup);

        mContainer = (FrameLayout)findViewById(R.id.container);
        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view_list);

        mAdapter = new AddonAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mRootView;

        public ViewHolder(View v) {
            super(v);

            mRootView = v;
            v.setOnClickListener(mListener);
        }
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = mRecyclerView.getChildPosition(v);

            if(pos == 0) {

            } else {
                final ShortLoadingDialog dialog = new ShortLoadingDialog(SetupActivity.this);
                dialog.show();
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        KghAddon.runAddon(SetupActivity.this);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        dialog.finish(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                                overridePendingTransition(R.anim.fade_out, 0);
                            }
                        });
                    }
                }.execute();

                setResult(RESULT_OK);
            }
        }
    };

    public class AddonAdapter extends RecyclerView.Adapter<ViewHolder> {
        @Override
        public int getItemViewType(int position) {
            return position + 1;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewHolder vh = null;

            if(viewType == 1) {
                View v = LayoutInflater.from(SetupActivity.this).inflate(
                        R.layout.activity_add_item_add, parent, false);

                vh = new ViewHolder(v);
            } else if(viewType == 2) {
                View v = LayoutInflater.from(SetupActivity.this).inflate(
                        R.layout.list_item_school_addon, parent, false);

                vh = new ViewHolder(v);
            }

            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
