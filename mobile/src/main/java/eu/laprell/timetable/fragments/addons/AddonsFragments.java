package eu.laprell.timetable.fragments.addons;

import android.support.v4.app.Fragment;

import eu.laprell.timetable.background.MenuNavigation;

/**
 * Created by david on 23.01.15.
 */
public class AddonsFragments {

    public static Fragment getAddonFragmentForMenu(int menu) {
        switch (menu) {
            case MenuNavigation.AddonsMenu.MENU_KGH_REPRESENTATION_PLAN:
                return new KghMasterRepPlanFragment();
        }
        return null;
    }
}
