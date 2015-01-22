package eu.laprell.timetable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import eu.laprell.timetable.background.MenuNavigation;
import eu.laprell.timetable.database.Day;
import eu.laprell.timetable.fragments.BaseFragment;
import eu.laprell.timetable.fragments.DayOverviewFragment;
import eu.laprell.timetable.fragments.DebugFragment;
import eu.laprell.timetable.fragments.DrawerFragment;
import eu.laprell.timetable.fragments.InfoFragment;
import eu.laprell.timetable.fragments.SettingsFragmentCompat;
import eu.laprell.timetable.fragments.TaskFragment;
import eu.laprell.timetable.fragments.TimeGridFragment;
import eu.laprell.timetable.fragments.WeekOverviewFragment;
import eu.laprell.timetable.utils.Const;
import eu.laprell.timetable.utils.MetricsUtils;
import eu.laprell.timetable.utils.ToastAdListener;
import eu.laprell.timetable.widgets.ColorFilterDrawerLayout;
import eu.laprell.timetable.widgets.PartialDrawRelativeLayout;

/**
 * Created by david on 06.11.14.
 */
public class MainActivity extends ActionBarActivity implements DrawerFragment.DrawerNavigationCallback {

    private DrawerFragment.DrawerNavigationBackend mBackend;

    private Fragment mFragment;

    private View mContent;
    private AdView mAdView;

    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private ColorFilterDrawerLayout mDrawerLayout;
    private DrawerLayout.DrawerListener mDrawerListener = new DrawerLayout.DrawerListener() {

        private RectF mRFrame = new RectF();

        @Override
        public void onDrawerSlide(View view, float v) {
            mFrame.setPartialDrawEnabled(true);
            mDrawerLayout.setColorDrawing(true);

            final float overDrawWidth = mBackend.getDrawerContentWidth() * v;
            mRFrame.set(overDrawWidth, 0, mDrawerLayout.getWidth(), mDrawerLayout.getHeight());

            mFrame.setDrawingFrame(mRFrame);
            mDrawerLayout.setColorDrawingFrame(mRFrame);

            int c = (int) (100 * v);

            mDrawerLayout.setColorFilter(Color.argb(c, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);

            mDrawerToggle.onDrawerSlide(view, v);
            mBackend.dispatchDrawerOpened(v, overDrawWidth);

            if(mFragment instanceof BaseFragment)
                ((BaseFragment) mFragment).onDrawerOverlaps(overDrawWidth);
        }

        @Override
        public void onDrawerOpened(View view) {
            mDrawerToggle.onDrawerOpened(view);
        }

        @Override
        public void onDrawerClosed(View view) {
            mFrame.setPartialDrawEnabled(false);
            mDrawerLayout.setColorDrawing(false);

            mDrawerToggle.onDrawerClosed(view);
        }

        @Override
        public void onDrawerStateChanged(int i) {
            mDrawerToggle.onDrawerStateChanged(i);
        }
    };
    private PartialDrawRelativeLayout mFrame;
    private int mCurrentMenu;
    private int mShouldDisplayDay;
    private MenuNavigation mNav;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean noNav = true;

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        if(!p.getBoolean("already_setup", false)) {
            startActivityForResult(new Intent(this, SetupActivity.class), 1);
        }

        mNav = new MenuNavigation(this);

        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = (ColorFilterDrawerLayout)findViewById(R.id.drawer_layout);
        mFrame = (PartialDrawRelativeLayout)findViewById(R.id.main_fragment_container);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.app_name,
                R.string.app_name
        );

        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        mDrawerLayout.setDrawerListener(mDrawerListener);

        mContent = findViewById(R.id.content);

        mAdView = (AdView) findViewById(R.id.adView);
        if(BuildConfig.DEBUG)
            mAdView.setAdListener(new ToastAdListener(this));
        mAdView.loadAd(new AdRequest.Builder()
                /*.addKeyword("school")
                .addKeyword("student")
                .addKeyword("time")
                .addKeyword("book")*/
                .build());

        startService(new Intent(this, BackgroundService.class));

        if(getIntent() != null && getIntent().getAction() != null) {
            String act = getIntent().getAction();
            if(Const.ACTION_VIEW_IN_TIMETABLE.equals(act)) {
                mShouldDisplayDay = getIntent().getIntExtra(Const.EXTRA_DAY_OF_WEEK_BY_NUM, 1);
                mBackend.navigateMenu(MenuNavigation.Menu.MENU_DAY_OVERVIEW);

                noNav = false;
            }
        }

        if(savedInstanceState != null && noNav) {
            mShouldDisplayDay = savedInstanceState.getInt("show_day", BackgroundService.getDayOfWeek());
            mBackend.navigateMenu(MenuNavigation.Menu.MENU_DAY_OVERVIEW);

            noNav = true;
        }

        if(noNav) {
            int day = BackgroundService.getDayOfWeek();

            if(day <= Day.OF_WEEK.FRIDAY) {
                mBackend.navigateMenu(MenuNavigation.Menu.MENU_DAY_OVERVIEW);
                mShouldDisplayDay = day;
            } else
                mBackend.navigateMenu(MenuNavigation.Menu.MENU_WEEK_OVERVIEW);
        }

        WidgetService.updateWidgets(this);

        getWindow().setBackgroundDrawable(null);
    }

    private void checkForOpenDay() {
        if(mShouldDisplayDay > 0 && mShouldDisplayDay < 8) {
            if (mFragment != null && mFragment instanceof DayOverviewFragment) {
                DayOverviewFragment f = (DayOverviewFragment) mFragment;

                Bundle args = new Bundle();
                args.putInt("show_day", mShouldDisplayDay);
                f.setArguments(args);

                mShouldDisplayDay = -1;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("show_day", mCurrentMenu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode != Activity.RESULT_OK)
            finish();
        else if(requestCode == 1) {
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);

            mDrawerLayout.openDrawer(Gravity.START);
            mBackend.navigateMenu(MenuNavigation.Menu.MENU_TIME_GRID);

            p.edit().putBoolean("already_setup", true).apply();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdView.destroy();

        stopService(new Intent(this, BackgroundService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdView.pause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(Gravity.START)){
            mDrawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean navigateTo(int menu, String t) {
        mDrawerLayout.closeDrawers();

        changeFragment(menu, 0, 0);

        return false;
    }

    @SuppressLint("NewApi")
    private void changeFragment(int menu, int aIn, int aOut) {
        mToolbar.setTitle(mNav.getTitleAtPos(mNav.getMenuPosition(menu)));

        mCurrentMenu = menu;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(menu == MenuNavigation.Menu.MENU_WEEK_OVERVIEW)
            mFragment = new WeekOverviewFragment();
        else if(menu == MenuNavigation.Menu.MENU_INFO)
            mFragment = new InfoFragment();
        else if(menu == MenuNavigation.Menu.MENU_SETTINGS) {
            mFragment = new SettingsFragmentCompat();
        } else if (menu == MenuNavigation.Menu.MENU_TIME_GRID)
            mFragment = new TimeGridFragment();
        else if (menu == MenuNavigation.Menu.MENU_DAY_OVERVIEW) {
            mFragment = new DayOverviewFragment();
            checkForOpenDay();
        } else if(menu == MenuNavigation.Menu.MENU_TASKS) {
            mFragment = new TaskFragment();
        } else if(menu == MenuNavigation.Menu.MENU_DEBUG) {
            mFragment = new DebugFragment();
        }


        if(aIn != 0 || aOut != 0) {
            fragmentTransaction.setCustomAnimations(aIn, aOut);
        }

        fragmentTransaction.replace(mContent.getId(), mFragment);
        fragmentTransaction.commit();

        if(mFragment instanceof BaseFragment) {
            BaseFragment base = ((BaseFragment) mFragment);

            if(Const.FW_SUPPORTS_DROP_SHADOWS) {
                float elZ = MetricsUtils.convertDpToPixel(
                        base.getToolbarElevationDp());
                mToolbar.setElevation(elZ);
            }

            mContent.setBackgroundColor(base.getBackgroundColor());
        }
    }

    @Override
    public void setDrawerNavigationBackend(DrawerFragment.DrawerNavigationBackend b) {
        mBackend = b;
    }

    public void reloadDrawer() {
        if(mBackend != null) {
            mNav.forceReloading();
            mBackend.reloadDrawer();
        }
    }
}
