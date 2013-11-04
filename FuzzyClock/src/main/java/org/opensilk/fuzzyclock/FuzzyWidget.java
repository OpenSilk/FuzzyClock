package org.opensilk.fuzzyclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class FuzzyWidget extends AppWidgetProvider {

    private static final String TAG = FuzzyWidget.class.getSimpleName();
    private static final boolean LOGV = true;

    private static final String ACTION_POKE_SERVICE = "org.opensilk.action.POKE_SERVICE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_POKE_SERVICE.equals(action)) {
            pokeService(context);
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (LOGV) Log.v(TAG, "onUpdate");
        pokeService(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        if (LOGV) Log.v(TAG, "onDeleted");
        pokeService(context);
    }

    @Override
    public void onDisabled(Context context) {
        if (LOGV) Log.v(TAG, "onDisabled");
        context.stopService(new Intent(context, FuzzyWidgetService.class));
        cancelNextAlarm(context);
    }

    private void pokeService(Context context) {
        if (LOGV) Log.v(TAG, "pokeService");
        context.startService(new Intent(context, FuzzyWidgetService.class));
        setNextAlarm(context);
    }

    void setNextAlarm(Context context) {
        Calendar cal = Calendar.getInstance();
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        int nextHours = hours, nextMinutes;
        long nextMilli = 0;

        if        (minutes >= 56) { nextHours += 1; nextMinutes = 5;
        } else if (minutes >= 51) { nextMinutes = 56;
        } else if (minutes >= 46) { nextMinutes = 51;
        } else if (minutes >= 41) { nextMinutes = 46;
        } else if (minutes >= 36) { nextMinutes = 41;
        } else if (minutes >= 25) { nextMinutes = 36;
        } else if (minutes >= 20) { nextMinutes = 25;
        } else if (minutes >= 15) { nextMinutes = 20;
        } else if (minutes >= 10) { nextMinutes = 15;
        } else if (minutes >= 5)  { nextMinutes = 10;
        } else                    { nextMinutes = 5;
        }

        nextMilli += (nextHours - hours) * 60 * 60 * 1000;
        nextMilli += (nextMinutes - minutes) * 60 * 1000;

        Intent nextUpdate = new Intent(ACTION_POKE_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, nextUpdate, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        am.set(AlarmManager.RTC, cal.getTimeInMillis() + nextMilli, pi);
        Log.i(TAG, "Scheduled next clock update for " + (nextMilli/1000) + "s from now");
    }

    void cancelNextAlarm(Context context) {
        Intent nextUpdate = new Intent(ACTION_POKE_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, nextUpdate, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }
}
