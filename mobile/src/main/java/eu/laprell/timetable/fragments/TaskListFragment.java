package eu.laprell.timetable.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import eu.laprell.timetable.R;
import eu.laprell.timetable.database.DbAccess;
import eu.laprell.timetable.database.Task;
import eu.laprell.timetable.widgets.FloatingActionButton;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

/**
 * Created by david on 23.12.14.
 */
public class TaskListFragment extends Fragment {

    private DbAccess mAccess;
    private TaskListCallback mCallback;
    private ArrayList<Task> mList;

    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;
    private CircularProgressBar mProgress;

    private TaskListAdapter mAdapter;

    public TaskListFragment() {
        mList = new ArrayList<>();
    }

    @Override
    public void onAttach(Activity activity) {
        try {
            mCallback = (TaskListCallback)activity;
        } catch (ClassCastException ex) {
            try {
                mCallback = (TaskListCallback)getParentFragment();
            } catch (ClassCastException e) {
                throw new ClassCastException("Either activity or Parent Fragment must implement TaskListCallback");
            }
        }

        mAccess = new DbAccess(activity);

        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_task_list, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_list);
        mFab = (FloatingActionButton) v.findViewById(R.id.fab_add);
        mProgress = (CircularProgressBar) v.findViewById(R.id.circular_loading);

        // use the true setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(false);

        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new TaskListAdapter();

        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    public interface TaskListCallback {
        public void displayTask(Task t);
        public void makeNewTask();
    }

    public class VHolder extends RecyclerView.ViewHolder {
        public VHolder(View itemView) {
            super(itemView);
        }
    }

    public class TaskListAdapter extends RecyclerView.Adapter<VHolder> {

        @Override
        public VHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(VHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }
}
