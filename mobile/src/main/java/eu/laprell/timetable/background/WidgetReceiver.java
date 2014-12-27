package eu.laprell.timetable.background;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.widget.RemoteViews;

import eu.laprell.timetable.R;
import eu.laprell.timetable.database.Day;
import eu.laprell.timetable.database.DbAccess;
import eu.laprell.timetable.database.Lesson;
import eu.laprell.timetable.utils.RemoteViewUtils;

/**
 * Created by david on 19.12.14.
 */
public class WidgetReceiver extends AppWidgetProvider {

    private static final String ACTION_CLICK = "ACTION_CLICK";
    private DbAccess mAccess;
    private Day mDay;
    private Lesson mLesson;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        mAccess = new DbAccess(context);
        mDay = mAccess.get().getDayForDayOfWeek(Day.OF_WEEK.MONDAY);
        mLesson = null;
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        if(mAccess != null)
            mAccess.close();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                WidgetReceiver.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int widgetId : allWidgetIds) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
            // Set the text
            remoteViews.setTextViewText(R.id.lesson_text, "Mathe");
            remoteViews.setInt(R.id.background_image_container, "setImageResource",
                    R.drawable.math_grey);

            RemoteViewUtils.setDrawableParameters(remoteViews, R.id.background_image_container, false, -1,
                    0xFF2196F3, PorterDuff.Mode.MULTIPLY, -1);

            // Register an onClickListener
            Intent intent = new Intent(context, WidgetReceiver.class);

            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.layout, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
}
