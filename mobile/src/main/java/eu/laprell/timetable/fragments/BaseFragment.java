package eu.laprell.timetable.fragments;

import android.support.v4.app.Fragment;

import eu.laprell.timetable.MainApplication;
import eu.laprell.timetable.R;
import eu.laprell.timetable.background.notifications.LessonNotifier2;

/**
 * Created by david on 14.01.15.
 */
public class BaseFragment extends Fragment {

    public LessonNotifier2 getLessonNotifier() {
        if(getActivity() != null && getActivity().getApplication() instanceof MainApplication) {
            return ((MainApplication) getActivity().getApplication()).getLessonNotifier();
        } else if(getActivity() != null) {
            return MainApplication.getLessonNotifier(getActivity());
        }
        return null;
    }

    public float getToolbarElevationDp() {
        return 4;
    }

    public void onDrawerOverlaps(float overDrawWidth) {

    }

    public int getBackgroundColor() {
        return 0xFFE3E3E3;
    }

    public boolean isTabletLayout() {
        return getResources().getBoolean(R.bool.is_tablet_720dp);
    }
}
