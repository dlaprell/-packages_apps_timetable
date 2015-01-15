package eu.laprell.timetable.background;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Class for providing a global configuration
 */
public class GlobalConfigs {
    private static final GlobalConfigs sConfig = new GlobalConfigs();

    private Context mContext;
    private final GlobalConfigs mMaster;
    private SharedPreferences mPref;

    private boolean mEnabledDebugMenu;

    private GlobalConfigs() {
        mMaster = null;
    }

    private void loadConfig() {
        mPref = mContext.getSharedPreferences("global", 0);
        mEnabledDebugMenu = mPref.getBoolean("debug_menu", false);
    }

    public GlobalConfigs(Context c) {
        mContext = c;
        mMaster = sConfig;

        if(mMaster.mContext == null) {
            mMaster.mContext = c.getApplicationContext();
            mMaster.loadConfig();
        }
    }

    public boolean isDebugMenuEnabled() {
        if(mMaster != null)
            return mMaster.isDebugMenuEnabled();
        else
            return mEnabledDebugMenu;
    }

    public void setDebugMenuEnabled(boolean mEnabledDebugMenu) {
        if(mMaster != null)
            mMaster.setDebugMenuEnabled(mEnabledDebugMenu);
        else {
            this.mEnabledDebugMenu = mEnabledDebugMenu;
            mPref.edit().putBoolean("debug_menu", mEnabledDebugMenu).apply();
        }
    }
}
