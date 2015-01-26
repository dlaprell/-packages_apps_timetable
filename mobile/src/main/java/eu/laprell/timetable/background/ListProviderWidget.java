package eu.laprell.timetable.background;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

import eu.laprell.timetable.R;
import eu.laprell.timetable.background.notifications.NotifUtils;
import eu.laprell.timetable.database.Day;
import eu.laprell.timetable.database.DbAccess;
import eu.laprell.timetable.database.Lesson;
import eu.laprell.timetable.database.Place;
import eu.laprell.timetable.database.TimeUnit;
import eu.laprell.timetable.database.TimetableDatabase;
import eu.laprell.timetable.utils.RemoteViewUtils;

/**
 * Created by david on 20.12.14.
 */
public class ListProviderWidget implements RemoteViewsService.RemoteViewsFactory {

    private class Data {
        private Lesson lesson;
        private TimeUnit time;
        private Place place;
        private int num;
    }

    private ArrayList<Data> mList = new ArrayList<>();
    private Context mContext;
    private int appWidgetId;
    private DbAccess mAccess;
    private Day mDay;
    private String mBreakFirstLetter;
    private String mBreak;
    private String mFree;

    public ListProviderWidget(Context context, Intent intent) {
        mContext = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        mAccess = new DbAccess(context);

        populateListItem();

        mBreak = mContext.getString(R.string.break_);
        mBreakFirstLetter = mContext.getString(R.string.break_first_letter);
        mFree = mContext.getString(R.string.free);
    }

    private void populateListItem() {
        mDay = mAccess.get().getDayForDayOfWeek(NotifUtils.getDayOfWeek());

        final TimetableDatabase db = mAccess.get();

        long[] tids = mDay.getTimeUnits();
        long[] pids = mDay.getPlaces();
        long[] lids = mDay.getLessons();

        int num = 0;

        for (int i = 0;i < pids.length;i++) {
            Data d = new Data();
            d.time = (TimeUnit) db.getDatabaseEntryById(TimetableDatabase.TYPE_TIMEUNIT, tids[i]);
            d.lesson = (Lesson) db.getDatabaseEntryById(TimetableDatabase.TYPE_LESSON, lids[i]);
            d.place = (Place) db.getDatabaseEntryById(TimetableDatabase.TYPE_PLACE, pids[i]);

            if(d.time.isBreak())
                d.num = -1;
            else
                d.num = ++num;

            mList.add(d);
        }
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        return mList.get(position).time.getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public int getViewType(int position) {
        return mList.get(position).lesson == null ? 1 : 0;
    }

    /*
        *Similar to getView of Adapter where instead of View
        *we return RemoteViews
        *
        */
    @Override
    public RemoteViews getViewAt(int position) {
        int layout;
        if(getViewType(position) == 0)
            layout = R.layout.widget_list_item_lesson;
        else
            layout = R.layout.widget_list_item_simple;

        final RemoteViews remoteViews = new RemoteViews(
                mContext.getPackageName(), layout);
        Data data = mList.get(position);

        if(getViewType(position) == 0) {
            String addInfo = data.time.makeTimeString("s - e");

            if (data.place != null && data.place.getTitle() != null
                    && data.place.getTitle().length() != 0)
                addInfo += " | " + data.place.getTitle();

            remoteViews.setTextViewText(R.id.add_info, addInfo);

            int imageId = mAccess.get().getImageIdForLesson(data.lesson);

            if(TimetableDatabase.isNoId(imageId)) {
                remoteViews.setInt(R.id.background_image_container, "setBackgroundColor", data.lesson.getColor());
            } else {
                remoteViews.setInt(R.id.background_image_container, "setImageResource", imageId);

                RemoteViewUtils.setDrawableParameters(remoteViews, R.id.background_image_container, false, -1,
                        data.lesson.getColor(), PorterDuff.Mode.MULTIPLY, -1);
            }
        }

        remoteViews.setTextViewText(R.id.num, data.num == -1
                ? mBreakFirstLetter : String.valueOf(data.num));

        if(data.lesson != null)
            remoteViews.setTextViewText(R.id.lesson_text, data.lesson.getTitle());
        else
            remoteViews.setTextViewText(R.id.lesson_text, data.time.isBreak() ? mBreak : mFree);

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }
}
