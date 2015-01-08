package eu.laprell.timetable;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViewsService;

import eu.laprell.timetable.background.ListProviderWidget;
import eu.laprell.timetable.background.WidgetProvider;

public class WidgetService extends RemoteViewsService {
    public WidgetService() {
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        return new ListProviderWidget(getApplicationContext(), intent);
    }

    public static void updateWidgets(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] ids = widgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            widgetManager.notifyAppWidgetViewDataChanged(ids, R.id.list);

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}
