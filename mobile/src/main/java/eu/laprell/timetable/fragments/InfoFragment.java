package eu.laprell.timetable.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.CreativeCommonsAttributionNoDerivs30Unported;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;
import eu.laprell.timetable.R;

/**
 * Created by david on 07.11.14.
 */
public class InfoFragment extends Fragment {

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

        view.findViewById(R.id.open_source).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLicensesDialog();
            }
        });

        return view;
    }

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
