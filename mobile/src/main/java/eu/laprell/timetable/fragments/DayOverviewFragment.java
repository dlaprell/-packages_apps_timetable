package eu.laprell.timetable.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import eu.laprell.timetable.R;
import eu.laprell.timetable.database.Day;

/**
 * Created by david on 12.12.14.
 */
public class DayOverviewFragment extends BaseFragment {

    private ViewPager mPager;
    private PagerTabStrip mStrip;
    private TimetableCollectionPagerAdapter mAdapter;

    private String[] mWeekdays;

    private int mOpenDay;
    private int mColumnsCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWeekdays = getResources().getStringArray(R.array.array_days);

        if(savedInstanceState != null) {
            mOpenDay = savedInstanceState.getInt("opened_day", Day.OF_WEEK.MONDAY);
        } else if(getArguments() != null) {
            mOpenDay = getArguments().getInt("show_day", Day.OF_WEEK.MONDAY);
        }

        mColumnsCount = getResources().getInteger(R.integer.day_overview_columns);
    }

    @Override
    public float getToolbarElevationDp() {
        return 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_day_overview, container, false);

        mPager = ((ViewPager) root.findViewById(R.id.pager));
        mStrip = ((PagerTabStrip) root.findViewById(R.id.pager_tab_strip));

        mAdapter = new TimetableCollectionPagerAdapter(getFragmentManager());

        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(mOpenDay - 1, false);

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mOpenDay = position + 1;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("opened_day", mOpenDay);
    }

    public class TimetableCollectionPagerAdapter extends FragmentStatePagerAdapter {
        public TimetableCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new TimeTableDayFragment2();

            Bundle args = new Bundle();
            args.putInt("day", i + 1);
            args.putBoolean("enable_swipe", false);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public float getPageWidth(int position) {
            return (1f / mColumnsCount);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mWeekdays[position];
        }
    }

    private PagerSlidingTabStrip.CustomTabProvider mProvider = new PagerSlidingTabStrip.CustomTabProvider() {
        @Override
        public View getCustomTabView(ViewGroup viewGroup, int i) {
            return null;
        }
    };
}
