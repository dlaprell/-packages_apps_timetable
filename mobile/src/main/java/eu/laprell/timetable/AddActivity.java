package eu.laprell.timetable;

import android.app.SearchManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import eu.laprell.timetable.animation.ActivityTransitions;
import eu.laprell.timetable.animation.CircularRevealRelativeLayout;
import eu.laprell.timetable.database.Creator;
import eu.laprell.timetable.database.Place;
import eu.laprell.timetable.database.Suggestor;
import eu.laprell.timetable.database.TimeUnit;
import eu.laprell.timetable.database.TimetableDatabase;
import eu.laprell.timetable.utils.AnimUtils;
import eu.laprell.timetable.utils.Dialogs;
import eu.laprell.timetable.utils.MetricsUtils;
import eu.laprell.timetable.widgets.ShortLoadingDialog;

/**
 * Created by david on 09.11.14.
 */
public class AddActivity extends ActionBarActivity {

    private EditText mEditText;
    private LinearLayout mContent;
    private View mAddButton;

    private AddAdapter mAdapter;
    private ListPopupWindow mAddList;

    private Creator.Data mData;
    private TimeUnit[] mTimes;

    private Suggestor mSuggestor;

    private int mNextSuggestionsPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mData = new Creator.Data();

        setContentView(R.layout.activity_add);

        mEditText = (EditText)findViewById(R.id.edit_text);

        if(getIntent() != null) {
            String q = getIntent().getStringExtra(SearchManager.QUERY);
            if(q != null) {
                Log.d("TimeTable", "Query=" + q);
            }

            mEditText.setText(q);
        }

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                updatedInput(mEditText.getEditableText().toString());
                return false;
            }
        });

        mContent = (LinearLayout)findViewById(R.id.activity_add_content);

        mAddButton = getLayoutInflater().inflate(R.layout.activity_add_item_add,
                mContent, false);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddMenu();
            }
        });

        mContent.addView(mAddButton);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_48dp);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mAdapter = new AddAdapter();
        mAddList = new ListPopupWindow(this);
        mAddList.setAnimationStyle(R.style.NoEnterAnimationWindow);
        mAddList.setAnchorView(mAddButton);

        mAddList.setAdapter(mAdapter);

        mAddList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (mAdapter.getStringIdFrom(position)) {
                    case R.string.subject:
                        showSubjectList();
                        break;
                    case R.string.place:
                        showRoomList();
                        break;
                    case R.string.time:
                        showTimeList();
                        break;
                    case R.string.day_of_week:
                        showDayOfWeek();
                        break;
                    case R.string.color:
                        showColorList();
                        break;
                }
                mAddList.dismiss();
            }
        });

        mSuggestor = new Suggestor(new Suggestor.OnSuggestionCallback() {
            @Override
            public void onSuggestion(int type, boolean added, Object o) {
                View v;
                switch (type) {
                    case Suggestor.SUG_NEW_LESSON:
                        v = mContent.findViewById(CREATE_AS_NEW_LESSON);
                        if(added && v == null) {
                            makeNewSuggestions(R.string.create_as_new_lesson,
                                    CREATE_AS_NEW_LESSON);
                        } else if(!added && v != null){
                            AnimUtils.animateViewDeletingInLayoutAlpha(v, mContent);
                            mNextSuggestionsPosition--;
                        }
                        break;

                    case Suggestor.SUG_NEW_BREAK:
                        v = mContent.findViewById(CREATE_AS_NEW_BREAK);
                        if(added && v == null) {
                            makeNewSuggestions(R.string.create_as_new_break,
                                    CREATE_AS_NEW_BREAK);
                        } else if(!added && v != null){
                            AnimUtils.animateViewDeletingInLayoutAlpha(v, mContent);
                            mNextSuggestionsPosition--;
                        }
                        break;

                    case Suggestor.SUG_NEW_FREE_TIME:
                        v = mContent.findViewById(CREATE_AS_NEW_FREE_TIME);
                        if(added && v == null) {
                            makeNewSuggestions(R.string.create_as_new_free_time,
                                    CREATE_AS_NEW_FREE_TIME);
                        } else if(!added && v != null){
                            AnimUtils.animateViewDeletingInLayoutAlpha(v, mContent);
                            mNextSuggestionsPosition--;
                        }
                        break;
                }
            }
        });

        final CircularRevealRelativeLayout con =
                (CircularRevealRelativeLayout)findViewById(R.id.circular_reveal_container);

        con.setCaptureContent(true);
        con.setRenderOnlyHalf(true);

        con.getViewTreeObserver()
                .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                con.getViewTreeObserver().removeOnPreDrawListener(this);

                ActivityTransitions.animateFromBundle(getIntent().getExtras(),
                        con, null, 400);

                return true;
            }
        });
    }

    private void showAddMenu() {
        mAddList.show();
    }

    public void updatedInput(String nIn) {

    }

    public void addNewItem(int imageId, View v, Object tag) {
        final View c = getLayoutInflater().inflate(R.layout.list_item_image_with_text_and_remove, mContent, false);
        ImageView im = (ImageView)c.findViewById(R.id.image);
        if(imageId != -1)
            im.setImageResource(imageId);

        c.findViewById(R.id.remove).setOnClickListener(mRemoveListener);

        LinearLayout l = (LinearLayout)c.findViewById(R.id.container);
        l.addView(v);

        c.setTag(tag);

        AnimUtils.animateViewAddingInLayout(c, mContent, mNextSuggestionsPosition);
    }

    public void removeItem(View v) {
        v = (ViewGroup)v.getParent();

        if(v.getTag() != null && v.getTag() instanceof Integer) {
            int sid = (Integer)v.getTag();
            mAdapter.setActive(sid, false);

            int dep = -1;
            switch (sid) {
                case R.string.subject: dep = Suggestor.DEP_SUBJECT; break;
                case R.string.day_of_week: dep = Suggestor.DEP_DAY_OF_WEEK; break;
                case R.string.time: dep = Suggestor.DEP_TIME; break;
                case R.string.place: dep = Suggestor.DEP_PLACE; break;
                case R.string.color: dep = Suggestor.DEP_COLOR; break;
            }
            if(dep != -1) {
                mSuggestor.removeDependency(dep, null);
                if(dep == Suggestor.DEP_TIME && mData.is_break) {
                    mSuggestor.removeDependency(Suggestor.DEP_BREAK, null);
                    mData.is_break = false;
                }
            }
        }

        AnimUtils.animateViewDeletingInLayout(v, mContent);
    }

    private View.OnClickListener mRemoveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            removeItem(v);
        }
    };

    private View inflateItem(int lid) {
        return getLayoutInflater().inflate(lid, mContent, false);
    }

    private void showTimeList() {
        Dialogs.showTimeList(this, new Dialogs.TimeDialogCallback() {
            @Override
            public void selectedTime(TimeUnit t, String text) {
                TextView tv = (TextView) inflateItem(R.layout.single_text_view_add);
                tv.setText(text);
                tv.setTag(R.string.time);
                addNewItem(R.drawable.ic_schedule_grey600_36dp, tv, R.string.time);

                mData.time_id = t.getId();

                mSuggestor.addDependency(Suggestor.DEP_TIME, mData.time_id);
                if (t.isBreak()) {
                    mData.is_break = true;
                    mSuggestor.addDependency(Suggestor.DEP_BREAK, mData.time_id);
                }

                mAdapter.setActive(R.string.time, true);
            }
        });
    }

    private void showSubjectList() {
        Dialogs.showSubjectsList2(this, new Dialogs.SubjectsDialogCallback() {
            @Override
            public void selectedSubject(String name) {
                TextView tv = (TextView)inflateItem(R.layout.single_text_view_add);
                tv.setText(name);
                tv.setTag(R.string.subject);
                addNewItem(R.drawable.ic_label_grey600_36dp, tv, R.string.subject);

                mData.lesson = name;
                mData.lesson_id = 0;

                mSuggestor.addDependency(Suggestor.DEP_SUBJECT, mData.lesson);

                mAdapter.setActive(R.string.subject, true);
            }
        });
    }

    private void showRoomList() {
        Dialogs.showRoomList(this, new Dialogs.RoomDialogCallback() {
            @Override
            public void selectedRoom(Place p, String text) {
                addRoomToList(text, p);
            }

            @Override
            public void selectedCustomRoom(String text) {
                addRoomToList(text);
            }
        });
    }

    private void addRoomToList(String r) {
        addRoomToList(r, null);
    }

    private void addRoomToList(String r, Place p) {
        TextView tv = (TextView)inflateItem(R.layout.single_text_view_add);
        tv.setText(r);
        tv.setTag(R.string.subject);
        addNewItem(R.drawable.ic_room_grey600_36dp, tv, R.string.place);

        if(p == null)
            mData.place_id = 0;
        else
            mData.place_id = p.getId();
        mData.place = r;

        mSuggestor.addDependency(Suggestor.DEP_PLACE, mData.place);

        mAdapter.setActive(R.string.place, true);
    }

    private void showDayOfWeek() {
        Dialogs.showDayOfWeekList(this, new Dialogs.DayOfWeekCallback() {
            @Override
            public void selectedDayOfWeek(int dayOfWeek, CharSequence name) {
                TextView tv = (TextView) inflateItem(R.layout.single_text_view_add);
                tv.setText(name);
                tv.setTag(R.string.day_of_week);
                addNewItem(R.drawable.ic_event_grey600_36dp, tv, R.string.day_of_week);

                mData.day_of_week = dayOfWeek;

                mSuggestor.addDependency(Suggestor.DEP_DAY_OF_WEEK, mData.day_of_week);

                mAdapter.setActive(R.string.day_of_week, true);
            }
        });
    }

    private void showColorList() {
        Dialogs.showColorList(this, new Dialogs.ColorSelectedCallback() {
            @Override
            public void selectedColor(int color) {
                mData.color = color;

                FrameLayout tv = (FrameLayout) inflateItem(R.layout.single_view_list_item);
                tv.setBackgroundColor(mData.color);
                tv.setTag(R.string.color);
                addNewItem(R.drawable.ic_format_paint_grey600_36dp, tv, R.string.color);

                mSuggestor.addDependency(Suggestor.DEP_COLOR, mData.color);

                mAdapter.setActive(R.string.color, true);
            }
        });
    }

    private static final int CREATE_AS_NEW_LESSON = MetricsUtils.generateViewId();
    private static final int CREATE_AS_NEW_BREAK = MetricsUtils.generateViewId();
    private static final int CREATE_AS_NEW_FREE_TIME = MetricsUtils.generateViewId();
    private void makeNewSuggestions(int s, int id) {
        View v = getLayoutInflater().inflate(R.layout.activity_add_list_suggestion,
                mContent, false);

        ImageView image = (ImageView)v.findViewById(R.id.image);
        image.setImageResource(R.drawable.ic_forward_white_36dp);

        TextView tv = (TextView)v.findViewById(R.id.text);
        tv.setText(s);

        v.setId(id);
        v.setOnClickListener(mSuggestionsOnClickListener);

        AnimUtils.animateViewAddingInLayout(v, mContent, mNextSuggestionsPosition++);
    }

    private View.OnClickListener mSuggestionsOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int id = v.getId();
            if(id == CREATE_AS_NEW_LESSON
                    || id == CREATE_AS_NEW_BREAK
                    || id == CREATE_AS_NEW_FREE_TIME) {

                final ShortLoadingDialog d = new ShortLoadingDialog(AddActivity.this);
                d.show();

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        TimetableDatabase db = BackgroundService.get().getTimetableDatabase();
                        if(id == CREATE_AS_NEW_LESSON)
                            Creator.createLessonFromData(db, mData);
                        else if(id == CREATE_AS_NEW_BREAK)
                            Creator.createBreakFromData(db, mData);
                        else if(id == CREATE_AS_NEW_FREE_TIME)
                            Creator.createFreeTimeFromData(db, mData);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        d.finish(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        });
                    }
                }.execute();
            }
        }
    };

    private class AddAdapter extends BaseAdapter {
        private final int[] mIds = new int[] {
            R.string.subject,
            R.string.place,
            R.string.time,
            R.string.day_of_week,
            R.string.color
        };
        private boolean[] mSet = new boolean[mIds.length];

        @Override
        public int getCount() {
            int l = 0;
            for(int i = 0;i < mIds.length;i++)
                if(!mSet[i])l++;

            return l;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        public int getStringIdFrom(int position) {
            int x = 0;

            for(int i = 0;i < mIds.length;i++) {
                if(!mSet[i] && x == position) {
                    return mIds[i];
                } else if(!mSet[i]) x++;
            }
            return -1;
        }

        public boolean isActive(int resId) {
            for(int i = 0;i < mIds.length;i++)
                if(mIds[i] == resId)
                    return mSet[i];
            return false;
        }

        public void setActive(int resId, boolean e) {
            for(int i = 0;i < mIds.length;i++)
                if(mIds[i] == resId)
                    mSet[i] = e;

            if(getCount() == 0 && e)
                mAddButton.animate().alpha(0).setDuration(200).start();
            else if(getCount() == 1 && !e)
                mAddButton.animate().alpha(1).setDuration(200).start();

            notifyDataSetChanged();
        }

        @Override
        public long getItemId(int position) {
            return getStringIdFrom(position);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv;
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.adapter_list_popup_item,
                        mAddList.getListView(), false);
            }

            tv = (TextView)convertView;
            tv.setTag(mIds[position]);
            String s = getString(getStringIdFrom(position));
            tv.setText(s);

            return tv;
        }
    }
}
