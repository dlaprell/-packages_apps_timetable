package eu.laprell.timetable.fragments.preferences;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import eu.laprell.timetable.R;

/**
 * Created by david on 22.01.15
 */
public class NotificationPreferenceFragment extends PreferenceFragment {

    private View mRootView;
    private CheckBox mHideOnEndChkBox;

    @Override
    public int getTitleResId() {
        return R.string.pref_enable_notifications_title;
    }

    @Override
    public View inflatePreferenceView(LayoutInflater inf, @Nullable ViewGroup container,
                                      @Nullable Bundle state) {
        mRootView = inf.inflate(R.layout.fragment_notification_pref, container, false);

        mHideOnEndChkBox = (CheckBox) mRootView.findViewById(R.id.pref_hide_notif_at_end);
        mHideOnEndChkBox.setChecked(getPreferences().getBoolean("pref_dismiss_notif_when_past",
                true));

        mHideOnEndChkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView == mHideOnEndChkBox) {
                    getPreferences().edit().putBoolean(
                            "pref_dismiss_notif_when_past", isChecked).apply();
                }
            }
        });

        return mRootView;
    }

    @Override
    public boolean useMainSwitch() {
        return true;
    }

    @Override
    public boolean getDefaultMainSwitchValue() {
        return true;
    }

    @Override
    public void onMainSwitchChanged(boolean newState) {
        getPreferences().edit().putBoolean("pref_enable_notifications", newState).apply();

        mRootView.setEnabled(newState);
    }
}
