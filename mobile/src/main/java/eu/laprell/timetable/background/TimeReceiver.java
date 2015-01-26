package eu.laprell.timetable.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import eu.laprell.timetable.BackgroundService;
import eu.laprell.timetable.MainApplication;
import eu.laprell.timetable.background.notifications.LessonNotifier2;
import eu.laprell.timetable.utils.Const;

public class TimeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String act = intent.getAction();

        Logger.log("TimeReceiver", "Got intent action=" + act);

        if(Const.ACTION_CANCEL_NEXT_LESSON_NOTIFICATION.equals(intent.getAction())) {
            getLessonNotifier(context).cancelCurrentNotification();
        } else if(Const.ACTION_NEXT_TIMEUNIT_PENDING.equals(act)) {
            getLessonNotifier(context).pendingTimeUnit(intent);
        }
    }

    public static LessonNotifier2 getLessonNotifier(Context c) {
        LessonNotifier2 n = MainApplication.getLessonNotifier(c);

        if(n == null) {
            if(BackgroundService.get() == null)
                c.startService(new Intent(c, BackgroundService.class));

            if(BackgroundService.get() != null)
                n = BackgroundService.get().getLessonNotifier();
        }

        return n;
    }
}
