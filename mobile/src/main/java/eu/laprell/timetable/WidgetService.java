package eu.laprell.timetable;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.widget.RemoteViewsService;

import eu.laprell.timetable.background.ListProviderWidget;

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
}
