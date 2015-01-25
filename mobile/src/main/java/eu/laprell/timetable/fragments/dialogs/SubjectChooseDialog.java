package eu.laprell.timetable.fragments.dialogs;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import eu.laprell.timetable.R;
import eu.laprell.timetable.database.DbAccess;
import eu.laprell.timetable.database.Lesson;
import eu.laprell.timetable.database.TimetableDatabase;
import eu.laprell.timetable.utils.Dialogs;

/**
 * Created by david on 11.12.14.
 */
public class SubjectChooseDialog {

    private Context mContext;
    private Dialogs.SubjectsDialogCallback mCallback;
    private ArrayList<String> mAlreadyUsed;
    private ArrayList<String> mNotUsed;
    private ViewAdapter mAdapter;

    private MaterialDialog mDialog;
    private MakeListTask mTask;

    public SubjectChooseDialog(Context context, Dialogs.SubjectsDialogCallback cb) {
        mContext = context;
        mCallback = cb;
        mAlreadyUsed = new ArrayList<String>();
        mNotUsed = new ArrayList<String>();

        mTask = new MakeListTask();
    }

    public void show() {
        mTask.execute();
    }

    private void doShow() {
        mDialog = new MaterialDialog.Builder(mContext)
                .adapter(mAdapter)
                .items(new String[mAdapter.getCount()])
                .autoDismiss(false)
                .title(R.string.select_subject)
                .neutralText(R.string.new_subject)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNeutral(MaterialDialog materialDialog) {
                        Dialogs.showNewSubjectDialog(mContext, new Dialogs.CustomSubjectDialogCallback() {
                            @Override
                            public void newSubject(String text) {
                                if(text != null) {
                                    if(mCallback != null)
                                        mCallback.selectedSubject(text);

                                    mDialog.dismiss();
                                }
                            }
                        });
                    }

                    @Override
                    public void onNegative(MaterialDialog materialDialog) {
                    }

                    @Override
                    public void onPositive(MaterialDialog materialDialog) {
                    }
                })
                .build();

        mDialog.show();

        ListView list = mDialog.getListView();
        if(list != null) {
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Object obj = mAdapter.getItem(position);

                    if(obj == NEW) {

                    } else {
                        String result = ((String) obj);
                        if(result != null) {
                            mDialog.dismiss();

                            if(mCallback != null) {
                                mCallback.selectedSubject(result);
                            }
                        }
                    }
                }
            });
        }
    }

    private static final Object NEW = new Object();
    private class ViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mAlreadyUsed.size() + mNotUsed.size() + 1;
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public int getItemViewType(int position) {
            /*if(position == 0)
                return 2;*/
            return getItem(position) == null ? 0 : 1;
        }

        @Override
        public Object getItem(int position) {
            /*if(position == 0) {
                return NEW;
            }

            position--;*/

            if(position < mAlreadyUsed.size()) {
                return mAlreadyUsed.get(position);
            } else if(position == mAlreadyUsed.size()) {
                return null;
            } else {
                return mNotUsed.get(position - (mAlreadyUsed.size() + 1));
            }
        }

        public boolean isAlreadyUsed(int position) {
            /*if(position == 0) {
                return true;
            }

            position--;*/

            if(position < mAlreadyUsed.size()) {
                return true;
            } else if(position == mAlreadyUsed.size()) {
                return false;
            } else if (position - mAlreadyUsed.size() < mNotUsed.size()) {
                return false;
            }
            return false;
        }

        @Override
        public long getItemId(int position) {
            if(getItem(position) == null /*|| position == 0*/)
                return 0;
            else
                return getItem(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(getItemViewType(position) == 0) {
                return LayoutInflater.from(mContext).inflate(R.layout.list_item_line, parent, false);
            } else {
                if(convertView == null || convertView.findViewById(R.id.title) == null)
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.md_listitem,
                            parent, false);

                TextView tv = (TextView) convertView.findViewById(R.id.title);
                //if(position != 0)
                    tv.setText((String)getItem(position));
                /*else {
                    tv.setText(R.string.new_subject);
                    tv.setTextColor(tv.getResources().getColor(R.color.accent));
                }*/
                tv.setTypeface(null, isAlreadyUsed(position) ? Typeface.BOLD : Typeface.NORMAL);

                return convertView;
            }
        }
    }

    private class MakeListTask extends AsyncTask<Void, Void, Void> {
        public MakeListTask() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            DbAccess access = new DbAccess(mContext);
            TimetableDatabase db = access.get();

            Lesson[] lessons = db.getLessonsByIds(db.getDatabaseEntries(TimetableDatabase.TYPE_LESSON));
            String[] subjectsList = mContext.getResources().getStringArray(R.array.array_subjects);
            Arrays.sort(subjectsList);

            for (int i = 0;i < subjectsList.length;i++) {
                String s = subjectsList[i];
                for (int x = 0;x < lessons.length;x++) {
                    if(s.equals(lessons[x].getTitle())) {
                        subjectsList[i] = null;
                    }
                }
            }

            for (int i = 0;i < lessons.length;i++)
                mAlreadyUsed.add(lessons[i].getTitle());

            for (int i = 0;i < subjectsList.length;i++) {
                if(subjectsList[i] != null)
                    mNotUsed.add(subjectsList[i]);
            }

            Collections.sort(mAlreadyUsed);
            Collections.sort(mNotUsed);

            access.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mAdapter = new ViewAdapter();
            doShow();
        }
    }
}