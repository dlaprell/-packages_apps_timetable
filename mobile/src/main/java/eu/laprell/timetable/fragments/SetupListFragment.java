package eu.laprell.timetable.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import eu.laprell.timetable.R;
import eu.laprell.timetable.addon.CorneliusBurghAddon;
import eu.laprell.timetable.addon.KghAddon;
import eu.laprell.timetable.fragments.interfaces.SetupResultInterface;
import eu.laprell.timetable.widgets.ShortLoadingDialog;

/**
 * Created by david on 14.01.15.
 */
public class SetupListFragment extends BaseFragment {
    private FrameLayout mContainer;
    private RecyclerView mRecyclerView;
    private AddonAdapter mAdapter;

    private SetupResultInterface mCallback;

    @Override
    public void onAttach(Activity activity) {
        try {
            mCallback = (SetupResultInterface)activity;
        } catch (ClassCastException ex) {
            try {
                mCallback = (SetupResultInterface)getParentFragment();
            } catch (ClassCastException e) {
                throw new ClassCastException("Either activity or Parent Fragment must" +
                        "implement SetupResultInterface");
            }
        }

        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_setup_list, container, false);

        mContainer = (FrameLayout)v.findViewById(R.id.container);
        mRecyclerView = (RecyclerView)v.findViewById(R.id.recycler_view_list);

        mAdapter = new AddonAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));

        return v;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @SuppressWarnings("unused")
        private View mRootView;
        private ImageView mBackImage;
        private TextView mText;

        public ViewHolder(View v) {
            super(v);

            mRootView = v;
            v.setOnClickListener(mListener);

            mBackImage = (ImageView)v.findViewById(R.id.image);
            mText = (TextView)v.findViewById(R.id.school_name);
        }
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            final int pos = mRecyclerView.getChildPosition(v);

            if(pos == 0) {
                mCallback.makeCustomSchool();
            } else {
                final ShortLoadingDialog dialog = new ShortLoadingDialog(v.getContext());
                dialog.show();
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        if(pos == 1) {
                            KghAddon.runAddon(v.getContext());
                        } else if(pos == 2) {
                            CorneliusBurghAddon.runAddon(v.getContext());
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        dialog.finish(new Runnable() {
                            @Override
                            public void run() {
                                mCallback.finishedWithSetup();
                            }
                        });
                    }
                }.execute();
            }
        }
    };

    public class AddonAdapter extends RecyclerView.Adapter<ViewHolder> {
        @Override
        public int getItemViewType(int position) {
            return (position == 0) ? 1 : 2;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewHolder vh = null;

            if(viewType == 1) {
                View v = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.list_item_add_school, parent, false);

                vh = new ViewHolder(v);
            } else if(viewType == 2) {
                View v = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.list_item_school_addon, parent, false);

                vh = new ViewHolder(v);
            }

            return vh;
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if(position > 0) {
                String schoolName = null;
                int resId = -1;

                switch (position) {
                    case 1:
                        schoolName = "Kreisgymnasium Heinsberg";
                        resId = R.drawable.kgh;
                        break;
                    case 2:
                        schoolName = "Cornelius-Burgh-Gymnasium Erkelenz";
                        resId = R.drawable.conny;
                        break;
                }

                if (schoolName != null && resId != -1) {
                    holder.mBackImage.setImageResource(resId);
                    holder.mText.setText(schoolName);
                }
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

}
