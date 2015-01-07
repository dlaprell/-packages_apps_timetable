package eu.laprell.timetable.background;

import android.content.Context;
import android.content.res.Resources;

import eu.laprell.timetable.BuildConfig;
import eu.laprell.timetable.R;

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
    }

    private Context mContext;
    private String[] mTitleStrings;
    private int[] mImages;
    private int[] mMenus;
    private int[] mMenuExtended;

    public MenuNavigation(Context c) {
        mContext = c;

        if(BuildConfig.DEBUG) {
            mTitleStrings = new String[]{
                    getString(R.string.day_overview),
                    getString(R.string.week_overview),
                    getString(R.string.tasks),
                    getString(R.string.time_grid),
                    getString(R.string.settings),
                    getString(R.string.info)
            };

            mMenus = new int[]{
                    MenuNavigation.Menu.MENU_DAY_OVERVIEW,
                    MenuNavigation.Menu.MENU_WEEK_OVERVIEW,
                    MenuNavigation.Menu.MENU_TASKS,
                    MenuNavigation.Menu.MENU_TIME_GRID,
                    MenuNavigation.Menu.MENU_SETTINGS,
                    MenuNavigation.Menu.MENU_INFO,
            };

            mMenuExtended = new int[]{
                    Menu.MENU_TOP,
                    Menu.MENU_SPACE,
                    0, // MENU_DAY_OVERVIEW
                    1, // MENU_WEEK_OVERVIEW
                    Menu.MENU_SPACE,
                    2, // MENU_TASKS
                    Menu.MENU_SPACE,
                    Menu.MENU_LINE,
                    Menu.MENU_SPACE,
                    3, // MENU_TIME_GRID
                    4, // MENU_SETTINGS
                    5, // MENU_INFO
            };

            mImages = new int[]{
                    R.drawable.ic_view_day_grey600_24dp,
                    R.drawable.ic_view_week_grey600_24dp,
                    -1,
                    R.drawable.ic_view_list_grey600_24dp,
                    R.drawable.ic_settings_grey600_24dp,
                    R.drawable.ic_info_grey600_24dp
            };
        } else {
            mTitleStrings = new String[]{
                    getString(R.string.day_overview),
                    getString(R.string.week_overview),
                    getString(R.string.time_grid),
                    getString(R.string.settings),
                    getString(R.string.info)
            };

            mMenus = new int[]{
                    MenuNavigation.Menu.MENU_DAY_OVERVIEW,
                    MenuNavigation.Menu.MENU_WEEK_OVERVIEW,
                    MenuNavigation.Menu.MENU_TIME_GRID,
                    MenuNavigation.Menu.MENU_SETTINGS,
                    MenuNavigation.Menu.MENU_INFO,
            };

            mMenuExtended = new int[]{
                    Menu.MENU_TOP,
                    Menu.MENU_SPACE,
                    0, // MENU_DAY_OVERVIEW
                    1, // MENU_WEEK_OVERVIEW
                    Menu.MENU_SPACE,
                    Menu.MENU_LINE,
                    Menu.MENU_SPACE,
                    2, // MENU_TIME_GRID
                    3, // MENU_SETTINGS
                    4, // MENU_INFO
            };

            mImages = new int[]{
                    R.drawable.ic_view_day_grey600_24dp,
                    R.drawable.ic_view_week_grey600_24dp,
                    R.drawable.ic_view_list_grey600_24dp,
                    R.drawable.ic_settings_grey600_24dp,
                    R.drawable.ic_info_grey600_24dp
            };
        }
    }

    public int getImageAtPos(int pos) {
        return mImages[pos];
    }

    public String getTitleAtPos(int pos) {
        return mTitleStrings[pos];
    }

    public int getMenuAtPos(int pos) {
        return mMenus[pos];
    }

    public int getMenuCountFull() {
        return mMenuExtended.length;
    }

    public int getMenuCountSimple() {
        return mMenus.length;
    }

    public int getMenuRefAt(int pos) {
        return mMenuExtended[pos];
    }

    public int getAbsPosOfMenu(int m) {
        for(int i = 0;i < mMenuExtended.length;i++) {
            if(mMenuExtended[i] >= 0 && mMenus[mMenuExtended[i]] == m)
                return i;
        }
        return -1;
    }

    public int getMenuPosition(int menu) {
        for (int i = 0;i < mMenus.length;i++) {
            if(mMenus[i] == menu)
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
