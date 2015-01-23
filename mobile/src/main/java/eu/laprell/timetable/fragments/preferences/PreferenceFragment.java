package eu.laprell.timetable.fragments.preferences;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import eu.laprell.timetable.R;

/**
 * Created by david on 22.01.15.
 */
public abstract class PreferenceFragment extends Fragment {

    private View mRootView;
    private TextView mTitleText;
    private Switch mSwitch;
    private FrameLayout mPrefContainer;

    private SharedPreferences mPref;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mPref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle state) {
        mRootView = inflater.inflate(R.layout.fragment_preference, container, false);

        mTitleText = (TextView) mRootView.findViewById(R.id.title);
        mPrefContainer = (FrameLayout) mRootView.findViewById(R.id.container);
        mSwitch = (Switch) mRootView.findViewById(R.id.pref_switch);

        mSwitch.setVisibility(useMainSwitch() ? View.VISIBLE : View.GONE);

        final View content = inflatePreferenceView(inflater, mPrefContainer, state);
        if(content != null) {
            mPrefContainer.addView(content);
        }

        mTitleText.setText(getTitleResId());

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onMainSwitchChanged(isChecked);
            }
        });

        mSwitch.setChecked(getDefaultMainSwitchValue());

        return mRootView;
    }

    public boolean useMainSwitch() {
        return false;
    }

    public boolean getDefaultMainSwitchValue() {
        return true;
    }

    public void onMainSwitchChanged(boolean newState) {
    }

    public final SharedPreferences getPreferences() {
        return mPref;
    }

    public abstract int getTitleResId();

    public abstract View inflatePreferenceView(LayoutInflater inf, @Nullable ViewGroup container,
                                               @Nullable Bundle state);
}
