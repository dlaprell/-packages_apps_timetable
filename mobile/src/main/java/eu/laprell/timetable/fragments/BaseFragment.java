package eu.laprell.timetable.fragments;

import android.support.v4.app.Fragment;

import eu.laprell.timetable.MainApplication;
import eu.laprell.timetable.background.LessonNotifier;

/**
 * Created by david on 14.01.15.
 */
public class BaseFragment extends Fragment {

    public LessonNotifier getLessonNotifier() {
        if(getActivity() != null && getActivity().getApplication() instanceof MainApplication) {
            return ((MainApplication) getActivity().getApplication()).getLessonNotifier();
        }
        return null;
    }
}
