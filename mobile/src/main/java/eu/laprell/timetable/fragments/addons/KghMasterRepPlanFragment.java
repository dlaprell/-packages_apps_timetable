package eu.laprell.timetable.fragments.addons;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nineoldandroids.view.ViewPropertyAnimator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import eu.laprell.timetable.R;
import eu.laprell.timetable.background.Logger;
import eu.laprell.timetable.background.SecurePreferences;
import eu.laprell.timetable.fragments.BaseFragment;
import eu.laprell.timetable.utils.AnimUtils;
import eu.laprell.timetable.utils.FileUtils;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

/**
 * Created by david on 24.01.15.
 */
public class KghMasterRepPlanFragment extends BaseFragment {
    private static final String URL =
            "https://firtecy.pf-control.de/laprell/timetable/kgh_representation_plan.php";

    private static final String URL_TEST_LOGIN =
            "https://firtecy.pf-control.de/laprell/timetable/kgh_representation_plan.php";

    private static final int RES_SUCCESS = 1;
    private static final int RES_NO_INTERNET = 2;
    private static final int RES_FAILED = 3;

    private CircularProgressBar mProgress;
    private ViewPager mPager;

    private RepreDayAdapter mAdapter;

    private AsyncTask mDataLodingTask;

    private SecurePreferences mLoginStore;
    private MaterialDialog mLoginDialog;

    private final ArrayList<KghRepresentationPlanFragment> mChilds = new ArrayList<>(3);

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_representation_plan_master,
                container, false);

        mPager = ((ViewPager) root.findViewById(R.id.pager));
        mProgress = (CircularProgressBar) root.findViewById(R.id.circular_loading);
        mAdapter = new RepreDayAdapter(getFragmentManager());
        mPager.setAdapter(mAdapter);

        checkForData();

        return root;
    }

    private String getChecksum(long l) {
        return String.valueOf(String.valueOf(l).hashCode() * 11);
    }

    private void checkForData() {
        // First try to get the key to decrypt the preferences
        // the full key is not saved to disk. The info for the key
        // is even protected by a checksum

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mPager.getContext());
        long firstTime = pref.getLong("first_time_kgh", -1);

        if (firstTime != -1) {
            if (!getChecksum(firstTime).equals(pref.getString("cache_state", ""))) {
                firstTime = -1;
            }
        }

        if(firstTime == -1) {
            firstTime = System.currentTimeMillis() * 31;
            pref.edit().putLong("first_time_kgh", firstTime)
                    .putString("cache_state", getChecksum(firstTime)).apply();
        }

        // Now generate the real key

        firstTime = firstTime % 99981599;
        long a = Math.max(2, firstTime % 7);
        String key = String.valueOf(Math.pow(firstTime, a));

        mLoginStore = new SecurePreferences(mPager.getContext(), "kgh_login", key, true);

        // Test if there are login credentials
        if(mLoginStore.getBoolean("has_kgh_login", false)) {
            String user = mLoginStore.getString("kgh_username", "");
            String password = mLoginStore.getString("kgh_password", "");

            // Go ahead and load the informations
            loadData(user, password);
        } else {
            // No login credentials? Ask the user to enter one
            showLoginScreen();
        }
    }

    private void showLoginScreen() {
        mLoginDialog = new MaterialDialog.Builder(mPager.getContext())
                .title(R.string.kgh_login)
                .customView(R.layout.dialog_kgh_login, true)
                .positiveText(R.string.login)
                .negativeText(android.R.string.cancel)
                .autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String u = ((EditText) dialog.findViewById(R.id.user_name))
                                .getEditableText().toString();
                        String p = ((EditText) dialog.findViewById(R.id.password))
                                .getEditableText().toString();

                        // Before we dismiss the dialog, we check if the credentials are valid
                        checkLoginInfo(u, p);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        // The user should always can exit the dialog
                        dialog.dismiss();
                    }
                }).build();

        final View positiveAction = mLoginDialog.getActionButton(DialogAction.POSITIVE);
        final EditText passwordInput = (EditText) mLoginDialog.getCustomView().findViewById(R.id.password);

        // No password is not valid so only enable the "login" button when the user has entered a
        // password
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                positiveAction.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mLoginDialog.show();
        positiveAction.setEnabled(false); // disabled by default
    }

    public void checkLoginInfo(final String u, final String p) {
        final CircularProgressBar progress = (CircularProgressBar) mLoginDialog.findViewById(
                R.id.circular_loading);

        final View wrapper = mLoginDialog.findViewById(R.id.wrapper);

        mLoginDialog.getActionButton(DialogAction.NEGATIVE).setEnabled(false);
        mLoginDialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);

        // Crossfading
        progress.setAlpha(0f);
        progress.setVisibility(View.VISIBLE);
        ViewPropertyAnimator.animate(progress).alpha(1f).setDuration(200).start();

        wrapper.setAlpha(1f);
        ViewPropertyAnimator.animate(wrapper).alpha(0f).setDuration(200).start();

        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                if(!hasConnection())
                    return RES_NO_INTERNET;

                return testLogin(u, p) ? RES_SUCCESS : -1;
            }

            @Override
            protected void onPostExecute(Integer i) {
                if (i == RES_SUCCESS) {
                    // save the login data
                    mLoginStore.put("kgh_username", u);
                    mLoginStore.put("kgh_password", p);
                    mLoginStore.put("has_kgh_login", true);

                    // Now go ahead and load the data
                    loadData(u, p);

                    mLoginDialog.dismiss();
                    mLoginDialog = null;
                } else if (i == RES_NO_INTERNET) {
                    Toast.makeText(mPager.getContext(),
                            R.string.toast_repre_plan_no_internet, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mPager.getContext(),
                            R.string.toast_failed_to_login, Toast.LENGTH_LONG).show();
                }

                if(i != RES_SUCCESS) {
                    // Crossfade back...
                    ViewPropertyAnimator.animate(progress).alpha(0f).setDuration(200).start();
                    ViewPropertyAnimator.animate(wrapper).alpha(1f).setDuration(200).start();

                    mLoginDialog.getActionButton(DialogAction.NEGATIVE).setEnabled(true);
                    mLoginDialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                }
            }
        }.execute();
    }

    private boolean testLogin(String username, String password) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(URL_TEST_LOGIN);

        InputStream inputStream;
        try {
            // Add your data
            List<NameValuePair> values = new ArrayList<>(2);
            values.add(new BasicNameValuePair("user", username));
            values.add(new BasicNameValuePair("password", password));
            httppost.setEntity(new UrlEncodedFormEntity(values));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            inputStream = entity.getContent();
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, "UTF-8"), 128);
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            String result = sb.toString();

            JSONObject json = new JSONObject(result);
            return json.optInt("error", -1) == 0 && json.optBoolean("exists", false);
        } catch (IOException | JSONException e) {
            Logger.log("KghRepresentationPlan", "Failed to fetch from net", e);
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public float getToolbarElevationDp() {
        return 0;
    }

    private boolean hasConnection() {
        ConnectivityManager check = (ConnectivityManager)
                mPager.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = check.getAllNetworkInfo();

        boolean hasConnection = false;
        for (int i = 0; i < info.length; i++){
            if (info[i].getState() == NetworkInfo.State.CONNECTED){
                hasConnection = true;
            }
        }

        return hasConnection;
    }

    private void loadData(final String user, final String pwd) {
        mDataLodingTask = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                int i = 1;
                boolean success;

                final File cache = mPager.getContext().getCacheDir();

                // We have 3 different versions of getting the data
                // When we have network connection:
                //    - just fetch them from the server
                // When we have no network connection:
                //    - try to load last cache, else
                //    - show a warning, that no data was loaded

                if(!hasConnection()) {
                    do {
                        KghRepresentationPlanFragment f = new KghRepresentationPlanFragment();

                        // try to get the cache files
                        try {
                            File file = new File(cache, "kgh_rep_f" + i);

                            // test if the cache 'i' is existing
                            if(file.exists()) {
                                String data = FileUtils.readFromfile(file);
                                success = parseJson(f, data);
                            } else {
                                success = false;
                            }
                        } catch (IOException | JSONException ex) {
                            // Pretty simple: if we fail, we shouldn't display it
                            success = false;
                        }

                        // Only if we succeeded we should add it
                        if (success) {
                            mChilds.add(f);
                        }

                        i++;
                    } while (success);

                    // If we couldn't get one child we should give a "failed" warning to the
                    // user otherwise, we should just tell the user that the information is
                    // outdated
                    return mChilds.size() == 0 ? RES_FAILED : RES_NO_INTERNET;
                } else {
                    do {
                        // try to fetch the data pages for as long as there are some

                        KghRepresentationPlanFragment f = new KghRepresentationPlanFragment();
                        success = fetchData(f, user, pwd, i);

                        if (success) {
                            mChilds.add(f);
                        }

                        i++;
                    } while (success);


                    File f = new File(cache, "kgh_rep_f" + i);

                    // Now delete every cache file that was not updated
                    while (f.exists()) {
                        f.delete();
                        i++;
                        f = new File(cache, "kgh_rep_f" + i);
                    }

                    return RES_SUCCESS;
                }
            }

            @Override
            protected void onPostExecute(Integer i) {
                super.onPostExecute(i);

                mDataLodingTask = null;

                if (i != RES_FAILED) {
                    if(i == RES_NO_INTERNET) {
                        Toast.makeText(mPager.getContext(),
                                R.string.toast_repre_plan_no_internet_showing_cache,
                                Toast.LENGTH_LONG).show();
                    }

                    for (KghRepresentationPlanFragment f : mChilds) {
                        f.setFakeZero(false);
                    }

                    mAdapter.mFakeZero = false;
                    mAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(mPager.getContext(),
                            R.string.toast_repre_plan_no_internet, Toast.LENGTH_LONG).show();
                }

                AnimUtils.animateProgressExit(mProgress);
            }
        }.execute();
    }

    private boolean fetchData(KghRepresentationPlanFragment f, String username, String password,
                              int num) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(URL);

        InputStream inputStream;
        try {
            // Add your data
            List<NameValuePair> values = new ArrayList<>(3);
            values.add(new BasicNameValuePair("user", username));
            values.add(new BasicNameValuePair("password", password));
            values.add(new BasicNameValuePair("num", String.valueOf(num)));
            httppost.setEntity(new UrlEncodedFormEntity(values));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            inputStream = entity.getContent();
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, "UTF-8"), 128);
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            String result = sb.toString();

            // Write a new cache file, so that we can get it if the user has no connection later
            writeNewCache(result, num);

            return parseJson(f, result);
        } catch (IOException | JSONException e) {
            Logger.log("KghRepresentationPlan", "Failed to fetch from net", e);
            e.printStackTrace();
        }

        return false;
    }

    private void writeNewCache(String result, int num) {
        File cacheFile = new File(mPager.getContext().getCacheDir(), "kgh_rep_f" + num);

        if(cacheFile.exists() && !cacheFile.delete())
            return;

        try {
            if(!cacheFile.createNewFile())
                return;

            writeToFile(cacheFile, result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFile(File file, String data) {
        FileOutputStream stream = null;

        try {
            stream = new FileOutputStream(file);
            stream.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private boolean parseJson(KghRepresentationPlanFragment f, String result) throws JSONException {
        final JSONObject jObject = new JSONObject(result);

        // error codes:
        // 0 => no error
        // 1 => user or password missing
        // 2 => using no https (important: should always be active)
        // 3 => not existing ('exists'=false)
        if (jObject.optInt("error", -1) == 0) {
            JSONObject data = jObject.getJSONObject("data");

            f.setForDay(jObject.getString("date"));

            String curLevel = "";

            ArrayList<String> sortedList = new ArrayList<>();
            Iterator<String> iter = data.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                sortedList.add(key);
            }

            Collections.sort(sortedList);

            // First level are the arrays of the school levels:
            //
            // EF -> data, ...
            //    -> more data, ..
            // Q1 -> data, ....

            for(String key : sortedList) {
                if(!curLevel.equals(key)) {
                    curLevel = key;

                    Data d = new Data();
                    d.newLevel = true;
                    d.level_name = key;
                    f.getList().add(d);
                }

                JSONArray item = data.getJSONArray(key);

                // Noe the second level are the changes in the school level
                for (int i = 0;i < item.length();i++) {
                    JSONObject obj = item.getJSONObject(i);

                    f.getList().add(parseJsonObjectToData(obj));
                }
            }

            return true;
        } // else there was some kind of error ... but the error doesn't matter

        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(mDataLodingTask != null) {
            mDataLodingTask.cancel(false);
            mDataLodingTask = null;
        }
    }

    private Data parseJsonObjectToData(JSONObject obj) throws JSONException {
        final Data d =  new Data();

        d.type = obj.getString("type");
        d.course_number = obj.getString("subject");
        d.place = obj.getString("place");
        d.orig_teacher = obj.getString("orig_teacher");
        d.new_teacher = obj.getString("new_teacher");
        d.lesson = obj.getString("lesson");

        return d;
    }

    public class Data {
        public boolean newLevel = false;
        public String level_name;
        public String type;
        public String orig_teacher;
        public String new_teacher;
        public String place;
        public String course_number;
        public String lesson;
    }

    public class RepreDayAdapter extends FragmentStatePagerAdapter {
        private boolean mFakeZero = true;

        public RepreDayAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return mChilds.get(i);
        }

        @Override
        public int getCount() {
            return mFakeZero ? 0 : mChilds.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mChilds.get(position).getForDay();
        }
    }
}
