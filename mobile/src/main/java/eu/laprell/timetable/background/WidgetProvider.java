package eu.laprell.timetable.background;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.RemoteViews;

import eu.laprell.timetable.R;
import eu.laprell.timetable.WidgetService;

public class WidgetProvider extends AppWidgetProvider {

    private String[] mDays;
    private String mDayString;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        getDaysIfNecessary(context);
    }

    private void getDaysIfNecessary(Context context) {
        if(mDays == null)
            mDays = context.getResources().getStringArray(R.array.array_days);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        getDaysIfNecessary(context);
        mDayString = mDays[LessonNotifier.getDayOfWeek() - 1];

        final int N = appWidgetIds.length;
        for (int i = 0; i < N; ++i) {
            RemoteViews remoteViews = updateWidgetListView(context,
                    appWidgetIds[i]);
            appWidgetManager.updateAppWidget(appWidgetIds[i],
                    remoteViews);
        }

        SharedPreferences pref = context.getSharedPreferences("widgets", 0);
        int day = LessonNotifier.getDayOfYear();
        if(pref.getInt("last_day_shown", -1) != day) {
            WidgetService.updateWidgets(context);

            pref.edit().putInt("last_day_shown", day).apply();
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private RemoteViews updateWidgetListView(Context context,
                                             int appWidgetId) {

        //which layout to show on widget
        RemoteViews remoteViews = new RemoteViews(
                context.getPackageName(), R.layout.widget_layout);

        //RemoteViews Service needed to provide adapter for ListView
        Intent svcIntent = new Intent(context, WidgetService.class);
        //passing app widget id to that RemoteViews Service
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //setting a unique Uri to the intent
        //don't know its purpose to me right now
        svcIntent.setData(Uri.parse(
                svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        //setting adapter to listview of the widget
        remoteViews.setRemoteAdapter(R.id.list,
                svcIntent);

        remoteViews.setTextViewText(R.id.day_text, mDayString);
        //setting an empty view in case of no data
        remoteViews.setEmptyView(R.id.list, R.id.empty_day);
        return remoteViews;
    }
}