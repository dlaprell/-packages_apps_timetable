package eu.laprell.timetable.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.laprell.timetable.R;
import eu.laprell.timetable.fragments.preferences.NotificationPreferenceFragment;
import fr.castorflex.android.verticalviewpager.VerticalViewPager;

/**
 * Created by david on 07.11.14.
 */
public class SettingsFragment extends BaseFragment {

    private VerticalViewPager mPager;

    public SettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_settings, container, false);

        mPager = (VerticalViewPager)v.findViewById(R.id.vertical_view_pager);
        mPager.setAdapter(new SettingsPagerAdapter(getFragmentManager()));
        mPager.setCurrentItem(0);

        return v;
    }

    class SettingsPagerAdapter extends FragmentPagerAdapter {
        public SettingsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new NotificationPreferenceFragment();
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public float getPageWidth(int position) {
            return getCount() <= 1 ? 1f : 0.9f;
        }
    }
}
