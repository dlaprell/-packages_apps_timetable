package eu.laprell.timetable.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.laprell.timetable.R;
import eu.laprell.timetable.database.Task;

/**
 * Created by david on 23.12.14.
 */
public class TaskViewFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_view, container, false);
    }

    public void updateContent(Task newTask) {

    }
}
