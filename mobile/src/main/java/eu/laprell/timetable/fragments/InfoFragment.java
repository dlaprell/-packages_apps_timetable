package eu.laprell.timetable.fragments;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.CreativeCommonsAttributionNoDerivs30Unported;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;
import eu.laprell.timetable.MainActivity;
import eu.laprell.timetable.R;
import eu.laprell.timetable.background.GlobalConfigs;

/**
 * Created by david on 07.11.14.
 */
public class InfoFragment extends BaseFragment {

    public InfoFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        Context c = inflater.getContext();
        TextView tv = (TextView)view.findViewById(R.id.text_version);
        tv.setOnClickListener(mListener);
        try {
            PackageInfo pInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            tv.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Timetable", "Failed to get the package version", e);
        }

        view.findViewById(R.id.open_source).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLicensesDialog();
            }
        });

        return view;
    }

    private long mLastClick = 0;
    private int mClickCount = 0;
    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            long now = SystemClock.uptimeMillis();

            if(mLastClick + 100 * 1000 > now)
                mClickCount++;
            else
                mClickCount = 0;

            mLastClick = now;

            if(mClickCount > 20) {
                GlobalConfigs c = new GlobalConfigs(v.getContext());

                if(!c.isDebugMenuEnabled()) {
                    c.setDebugMenuEnabled(true);

                    if(getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).reloadDrawer();
                    }

                    Toast.makeText(v.getContext(), "You unlocked the debug menu",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private void showLicensesDialog() {
        final Notices notices = new Notices();

        notices.addNotice(new Notice("nineoldandroids", "http://nineoldandroids.com/",
                "Copyright 2012 Jake Wharton", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("Material Dialogs", "https://github.com/afollestad/material-dialogs",
                "Copyright (c) 2014 Aidan Michael Follestad", new MITLicense()));
        notices.addNotice(new Notice("Timely TextView", "https://github.com/adnan-SM/TimelyTextView",
                "Copyright 2014 Adnan A M.", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("DateTimePicker", "https://github.com/flavienlaurent/datetimepicker",
                "Copyright 2013 Flavien Laurent (DatePicker) edisonw (TimePicker)", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("SmoothProgressBar", "https://github.com/castorflex/SmoothProgressBar",
                "Copyright 2014 Antoine Merle", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("Material Design Icons", "https://github.com/google/material-design-icons/",
                "Google", new CreativeCommonsAttributionNoDerivs30Unported()));

        new LicensesDialog.Builder(getActivity()).setNotices(notices).setIncludeOwnLicense(true).build().show();
    }
}
