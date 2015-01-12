package eu.laprell.timetable.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nineoldandroids.view.ViewPropertyAnimator;

import eu.laprell.timetable.BackgroundService;
import eu.laprell.timetable.R;
import eu.laprell.timetable.database.DbAccess;
import eu.laprell.timetable.database.Lesson;
import eu.laprell.timetable.database.Place;
import eu.laprell.timetable.database.TimeUnit;
import eu.laprell.timetable.database.TimetableDatabase;
import eu.laprell.timetable.fragments.dialogs.SubjectChooseDialog;
import eu.laprell.timetable.widgets.ViewBackgroundProcessor;

/**
 * Created by david on 02.12.14.
 */
public class Dialogs {

    public static void showNewRoomDialog(Context c, final CustomRoomDialogCallback cb) {
        new MaterialDialog.Builder(c)
                .title(R.string.new_place)
                .customView(R.layout.dialog_new_place, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .autoDismiss(true)
                .callback(new MaterialDialog.Callback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        EditText e = (EditText) dialog.findViewById(R.id.place);
                        cb.newRoom(e.getEditableText().toString());
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                    }
                }).build().show();
    }

    public interface CustomRoomDialogCallback {
        public void newRoom(String text);
    }

    public static void showNewSubjectDialog(Context c, final CustomSubjectDialogCallback cb) {
        new MaterialDialog.Builder(c)
                .title(R.string.new_subject)
                .customView(R.layout.dialog_new_place, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .autoDismiss(true)
                .callback(new MaterialDialog.Callback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        EditText e = (EditText) dialog.findViewById(R.id.place);
                        cb.newSubject(e.getEditableText().toString());
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                    }
                }).build().show();
    }

    public interface CustomSubjectDialogCallback {
        public void newSubject(String text);
    }

    public static void showNewTeacherDialog(Context c, final CustomTeacherDialogCallback cb) {
        new MaterialDialog.Builder(c)
                .title(R.string.new_teacher)
                .customView(R.layout.dialog_new_place, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .autoDismiss(true)
                .callback(new MaterialDialog.Callback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        EditText e = (EditText) dialog.findViewById(R.id.place);
                        cb.newTeacher(e.getEditableText().toString());
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                    }
                }).build().show();
    }

    public interface CustomTeacherDialogCallback {
        public void newTeacher(String text);
    }

    public static void showDayOfWeekList(Context c, final DayOfWeekCallback cb)  {
        new MaterialDialog.Builder(c)
                .title(c.getString(R.string.select_day_of_week))
                .items(c.getResources().getStringArray(R.array.array_days))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        cb.selectedDayOfWeek(which + 1, text);
                    }
                })
                .build()
                .show();
    }

    public interface DayOfWeekCallback {
        public void selectedDayOfWeek(int dayOfWeek, CharSequence name);
    }

    public static void showColorList(Context c, final ColorSelectedCallback cb) {
        final int[] colors = c.getResources().getIntArray(R.array.array_colors);

        final MaterialDialog dialog = new MaterialDialog.Builder(c)
                .title(R.string.select_color)
                .adapter(new ViewBackgroundProcessor(c,
                        colors))
                .build();

        ListView listView = dialog.getListView();
        if (listView != null) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int color = colors[position];

                    if (cb != null) {
                        cb.selectedColor(color);
                    }

                    dialog.dismiss();
                }
            });
        }

        dialog.show();
    }

    /*public static void showSubjectsList(final Context c, final SubjectsDialogCallback cb) {
        String[] subjectsList = c.getResources().getStringArray(R.array.array_subjects);
        Arrays.sort(subjectsList);

        new MaterialDialog.Builder(c)
                .title(c.getString(R.string.select_subject))
                .items(subjectsList)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        cb.selectedSubject(text.toString());
                    }
                })
                .build()
                .show();
    }*/

    public static void showSubjectsList2(Context c, SubjectsDialogCallback cb) {
        new SubjectChooseDialog(c, cb).show();
    }

    /*
     TODO: make it school dependent
     */
    public static void showTeachersList(final Context c, final TeacherSelectedCallback cb) {
        String[] teachersList = c.getResources().getStringArray(R.array.addon_array_kgh_teachers);

        new MaterialDialog.Builder(c)
                .title(c.getString(R.string.select_teacher))
                .items(teachersList)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        cb.selectedTeacher(text.toString());
                    }
                })
                //.positiveColorRes(R.color.accent_)
                //.positiveText(R.string.choose)
                //.negativeText(R.string.cancel)
                .neutralText(R.string.new_teacher)
                .callback(new MaterialDialog.FullCallback() {
                    @Override
                    public void onNeutral(MaterialDialog materialDialog) {
                        Dialogs.showNewTeacherDialog(c, new CustomTeacherDialogCallback() {
                            @Override
                            public void newTeacher(String text) {
                                cb.selectedTeacher(text);
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
                .build()
                .show();
    }

    public static void showTimeList(final Context c, final TimeDialogCallback cb) {
        new AsyncTask<Void, Void, TimeUnit[]>() {
            @Override
            protected TimeUnit[] doInBackground(Void... params) {
                TimetableDatabase db = BackgroundService.get().getTimetableDatabase();
                long[] ids = db.getDatabaseEntries(TimetableDatabase.TYPE_TIMEUNIT);

                return db.getTimeUnitsByIds(ids);
            }

            @Override
            protected void onPostExecute(final TimeUnit[] timeUnits) {
                int l = 1;
                String[] times = new String[timeUnits.length];
                for(int i = 0;i < times.length;i++)
                    times[i] = timeUnits[i].getTimeAsString()
                            + (timeUnits[i].isBreak() ? " | " + c.getString(R.string.break_) : " | " + l++);

                new MaterialDialog.Builder(c)
                        .title(c.getString(R.string.select_subject))
                        .items(times)
                        .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                cb.selectedTime(timeUnits[which], text.toString());
                            }
                        })
                        .positiveColorRes(R.color.accent_)
                        .positiveText(R.string.choose)
                        .negativeText(R.string.cancel)
                        .build()
                        .show();
            }
        }.execute();
    }

    public interface TimeDialogCallback {
        public void selectedTime(TimeUnit t, String text);
    }

    public interface SubjectsDialogCallback {
        public void selectedSubject(String name);
    }

    public interface TeacherSelectedCallback {
        public void selectedTeacher(String name);
    }
    public interface ColorSelectedCallback {
        public void selectedColor(int color);
    }

    public static void showRoomList(final Context c, final RoomDialogCallback cb) {
        new AsyncTask<Void, Void, Place[]>() {
            @Override
            protected Place[] doInBackground(Void... params) {
                TimetableDatabase db = BackgroundService.get().getTimetableDatabase();
                long[] ids = db.getDatabaseEntries(TimetableDatabase.TYPE_PLACE);

                return db.getPlacesByIds(ids);
            }

            @Override
            protected void onPostExecute(final Place[] placeEntries) {
                String[] places = new String[placeEntries.length];
                for(int i = 0;i < placeEntries.length;i++)
                    places[i] = placeEntries[i].getTitle();

                if(places.length == 0) {
                    showNewRoomDialog(c, new CustomRoomDialogCallback() {
                        @Override
                        public void newRoom(String text) {
                            cb.selectedCustomRoom(text);
                        }
                    });
                    return;
                }

                new MaterialDialog.Builder(c)
                        .title(c.getString(R.string.select_room))
                        .items(places)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                cb.selectedRoom(placeEntries[which], text.toString());
                            }
                        })
                        .callback(new MaterialDialog.FullCallback() {
                            @Override public void onPositive(MaterialDialog dialog) {}
                            @Override public void onNegative(MaterialDialog dialog) {}
                            @Override
                            public void onNeutral(MaterialDialog dialog) {
                                showNewRoomDialog(c, new CustomRoomDialogCallback() {
                                    @Override
                                    public void newRoom(String text) {
                                        cb.selectedCustomRoom(text);
                                    }
                                });
                            }
                        })
                        //.positiveText(android.R.string.ok)
                        //.positiveColorRes(R.color.accent_)
                        //.negativeText(android.R.string.cancel)
                        //.negativeColorRes(R.color.accent)
                        .neutralText(R.string.new_place)
                        .build()
                        .show();
            }
        }.execute();
    }

    public static void makeSimpleLessonDialog(final Context c, final LessonCreatedCallback cb, String day, TimeUnit t) {
        new SimpleLessonDialog(c, cb, day, t).show();
    }

    private static class SimpleLessonDialog implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        private MaterialDialog mDialog;
        private LessonCreatedCallback mCallback;

        private TextView mLessonTitle;
        private View mColorChooser;
        private View mOptionsContainer;
        private CheckBox mFreetimeBox;

        private Context mContext;
        private ColorCheckTask mTask;

        private int mSelectedColor;
        private String mSelectedLesson;

        public SimpleLessonDialog(Context c, LessonCreatedCallback cb, String day, TimeUnit t) {
            mContext = c;
            mCallback = cb;

            mDialog = new MaterialDialog.Builder(c)
                    .title(R.string.new_lesson)
                    .customView(R.layout.dialog_new_lesson, true)
                    .positiveText(R.string.done)
                    .negativeText(android.R.string.cancel)
                    .autoDismiss(false)
                    .callback(new MaterialDialog.Callback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            if (mFreetimeBox.isChecked()) {
                                if (mCallback != null)
                                    mCallback.createFreetime();

                                mDialog.dismiss();
                                return;
                            }

                            if (mSelectedLesson == null || mSelectedLesson.length() == 0) {
                                Toast.makeText(mContext, "You must select a subject", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Lesson l = new Lesson(-1);

                            l.setTitle(mSelectedLesson);
                            l.setColor(mSelectedColor);

                            if (mCallback != null)
                                mCallback.createdLesson(l);

                            mDialog.dismiss();
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            mDialog.dismiss();
                        }
                    }).build();

            final View root = mDialog.getCustomView();

            mLessonTitle = (TextView)root.findViewById(R.id.subject);
            mFreetimeBox = (CheckBox)root.findViewById(R.id.mark_as_freetime);
            mColorChooser = root.findViewById(R.id.color);
            mOptionsContainer = root.findViewById(R.id.options_container);

            root.findViewById(R.id.color_container).setOnClickListener(this);
            root.findViewById(R.id.subject_container).setOnClickListener(this);

            mFreetimeBox.setOnCheckedChangeListener(this);

            TextView tv = (TextView)root.findViewById(R.id.time);

            if(t == null) tv.setVisibility(View.GONE);
            else {
                String str = day + "\n" + t.makeTimeString("s - e");
                if(t.isBreak())
                    str += " " + mContext.getString(R.string.break_);
                tv.setText(str);
            }
        }

        public void show() {
            mDialog.show();
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.subject_container) {
                showSubjectsList2(mContext, new SubjectsDialogCallback() {
                    @Override
                    public void selectedSubject(String name) {
                        mLessonTitle.setText(name);

                        mSelectedLesson = name;

                        if(mTask != null)
                            mTask.cancel(false);

                        mTask = new ColorCheckTask();
                        mTask.execute(name);
                    }
                });
            } else if(v.getId() == R.id.color_container) {
                showColorList(mContext, new ColorSelectedCallback() {
                    @Override
                    public void selectedColor(int c) {
                        displayColor(c);
                    }
                });
            }
        }

        private void displayColor(int c) {
            mColorChooser.setBackgroundColor(c);

            mSelectedColor = c;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mColorChooser.setEnabled(!isChecked);
            mLessonTitle.setEnabled(!isChecked);

            ViewPropertyAnimator.animate(mOptionsContainer)
                    .alpha(isChecked ? 0.4f : 1f).setDuration(300).start();
        }

        private class ColorCheckTask extends AsyncTask<String, Void, Integer> {
            @Override
            protected Integer doInBackground(String... params) {
                DbAccess dbA = new DbAccess(mContext);

                Lesson l = dbA.get().getLessonByTitle(params[0]);

                dbA.close();
                return l == null ? null : l.getColor();
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);

                mTask = null;

                if(integer != null)
                    displayColor(integer);
            }
        }
    }

    public interface LessonCreatedCallback {
        public void createdLesson(Lesson l);
        public void createFreetime();
    }

    public interface RoomDialogCallback {
        public void selectedRoom(Place p, String text);
        public void selectedCustomRoom(String text);
    }
}
