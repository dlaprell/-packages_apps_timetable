package eu.laprell.timetable.background;

import android.content.Context;
import android.content.res.Resources;

import java.util.ArrayList;

import eu.laprell.timetable.BuildConfig;
import eu.laprell.timetable.R;
import eu.laprell.timetable.addon.Addons;

/**
 * Created by david on 20.11.14.
 */
public class MenuNavigation {

    public final class Menu {
        public static final int MENU_WEEK_OVERVIEW = 1;
        public static final int MENU_DAY_OVERVIEW = 2;
        public static final int MENU_TIME_GRID = 3;
        public static final int MENU_SETTINGS = 4;
        public static final int MENU_INFO = 5;
        public static final int MENU_TASKS = 6;

        public static final int MENU_SPACE = -1;
        public static final int MENU_LINE = -2;
        public static final int MENU_TOP = -3;

        public static final int MENU_DEBUG = 99;
    }

    public final class AddonsMenu {
        private static final int BASE = 100;
        public static final int MENU_KGH_REPRESENTATION_PLAN = BASE + 1;
    }

    private Context mContext;
    private ArrayList<String> mTitleStrings;
    private ArrayList<Integer> mImages;
    private ArrayList<Integer> mMenus;
    private ArrayList<Integer> mMenuExtended;

    public MenuNavigation(Context c) {
        mContext = c;

        mTitleStrings = new ArrayList<>();
        mImages = new ArrayList<>();
        mMenus = new ArrayList<>();
        mMenuExtended = new ArrayList<>();

        reloadFromConfiguration();
    }

    private void reloadFromConfiguration() {
        int curPos = -1;

        final GlobalConfigs c = new GlobalConfigs(mContext);

        mMenuExtended.add(Menu.MENU_TOP);

        // Day Overview
        mTitleStrings.add(getString(R.string.day_overview));
        mMenus.add(Menu.MENU_DAY_OVERVIEW);
        mImages.add(R.drawable.ic_view_day_grey600_24dp);
        mMenuExtended.add(++curPos);

        // Week Overview
        mTitleStrings.add(getString(R.string.week_overview));
        mMenus.add(Menu.MENU_WEEK_OVERVIEW);
        mImages.add(R.drawable.ic_view_week_grey600_24dp);
        mMenuExtended.add(++curPos);

        // For now only allow Tasks in Debug builds
        if(BuildConfig.DEBUG) {
            mMenuExtended.add(Menu.MENU_SPACE);

            mTitleStrings.add(getString(R.string.tasks));
            mMenus.add(Menu.MENU_TASKS);
            mImages.add(-1);
            mMenuExtended.add(++curPos);
        }

        if (c.getSchoolId() == Addons.Ids.ID_KREISGYMNASIUM_HEINSBERG) {
            mMenuExtended.add(Menu.MENU_SPACE);

            mTitleStrings.add(getString(R.string.representation_plan));
            mMenus.add(AddonsMenu.MENU_KGH_REPRESENTATION_PLAN);
            mImages.add(R.drawable.ic_assignment_ind_grey600_24dp);
            mMenuExtended.add(++curPos);
        }

        mMenuExtended.add(Menu.MENU_LINE);

        // Time Grid
        mTitleStrings.add(getString(R.string.time_grid));
        mMenus.add(Menu.MENU_TIME_GRID);
        mImages.add(R.drawable.ic_view_list_grey600_24dp);
        mMenuExtended.add(++curPos);

        // Settings
        mTitleStrings.add(getString(R.string.settings));
        mMenus.add(Menu.MENU_SETTINGS);
        mImages.add(R.drawable.ic_settings_grey600_24dp);
        mMenuExtended.add(++curPos);

        // Info
        mTitleStrings.add(getString(R.string.info));
        mMenus.add(Menu.MENU_INFO);
        mImages.add(R.drawable.ic_info_grey600_24dp);
        mMenuExtended.add(++curPos);

        // Debug
        if(c.isDebugMenuEnabled()) {
            mTitleStrings.add("Debug");
            mMenus.add(Menu.MENU_DEBUG);
            mImages.add(-1);
            mMenuExtended.add(++curPos);
        }
    }

    public void forceReloading() {
        mMenuExtended.clear();
        mMenus.clear();
        mImages.clear();
        mTitleStrings.clear();

        reloadFromConfiguration();
    }

    public int getImageAtPos(int pos) {
        return mImages.get(pos);
    }

    public String getTitleAtPos(int pos) {
        return mTitleStrings.get(pos);
    }

    public int getMenuAtPos(int pos) {
        return mMenus.get(pos);
    }

    public int getMenuCountFull() {
        return mMenuExtended.size();
    }

    public int getMenuRefAt(int pos) {
        return mMenuExtended.get(pos);
    }

    public int getAbsPosOfMenu(int m) {
        for(int i = 0;i < mMenuExtended.size();i++) {
            if(mMenuExtended.get(i) >= 0 && mMenus.get(mMenuExtended.get(i)) == m)
                return i;
        }
        return -1;
    }

    public int getMenuPosition(int menu) {
        for (int i = 0;i < mMenus.size();i++) {
            if(mMenus.get(i) == menu)
                return i;
        }
        return -1;
    }

    protected String getString(int r) {
        return getResources().getString(r);
    }

    protected Resources getResources() {
        return mContext.getResources();
    }
}
