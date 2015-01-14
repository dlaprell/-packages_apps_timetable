package eu.laprell.timetable.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import eu.laprell.timetable.MainApplication;
import eu.laprell.timetable.utils.Const;

public class TimeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String act = intent.getAction();

        if (Const.ACTION_NOTIFY_NEXT_LESSON.equals(intent.getAction())) {
            final long lid = intent.getLongExtra(Const.EXTRA_LESSON_ID, -1);
            final long tid = intent.getLongExtra(Const.EXTRA_TIMEUNIT_ID, -1);
            final long pid = intent.getLongExtra(Const.EXTRA_PLACE_ID, -1);

            //BackgroundService.get().updateNextLesson(lid, tid, pid);
        } else if(Const.ACTION_CANCEL_NEXT_LESSON_NOTIFICATION.equals(intent.getAction())) {
            getLessonNotifier(context).cancelCurrentNotification();
        } else if(Const.ACTION_NEXT_TIMEUNIT_PENDING.equals(act)) {
            getLessonNotifier(context).pendingTimeUnit(intent);
        }
    }

    public LessonNotifier getLessonNotifier(Context c) {
        if(c.getApplicationContext() instanceof MainApplication) {
            return ((MainApplication) c.getApplicationContext()).getLessonNotifier();
        } else {
            return new LessonNotifier(c);
        }
    }
}
