package eu.laprell.timetable.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.laprell.timetable.NewTaskActivity;
import eu.laprell.timetable.R;
import eu.laprell.timetable.database.Task;

/**
 * Created by david on 23.12.14.
 */
public class TaskFragment extends Fragment implements TaskListFragment.TaskListCallback {

    private TaskListFragment mListFragment;
    private TaskViewFragment mViewFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_task, container, false);

        mListFragment = (TaskListFragment) getFragmentManager().findFragmentById(R.id.tasks_list);
        mViewFragment = (TaskViewFragment) getFragmentManager().findFragmentById(R.id.tasks_view);

        return v;
    }

    @Override
    public void displayTask(Task t) {
        if(mViewFragment == null) {
            // TODO: implement as always
            //startActivityForResult(new Intent(getActivity(), NewTaskActivity.class), 1);
        } else {
            mViewFragment.updateContent(t);
        }
    }

    @Override
    public void makeNewTask() {
        if(mViewFragment == null) {
            startActivityForResult(new Intent(getActivity(), NewTaskActivity.class), 1);
        } else {
            Bundle args = new Bundle();
            args.putBoolean("single_activity", false);

            Fragment f = new NewTaskFragment();
            f.setArguments(args);

            getFragmentManager().beginTransaction()
                    .replace(R.id.tasks_view, f)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
