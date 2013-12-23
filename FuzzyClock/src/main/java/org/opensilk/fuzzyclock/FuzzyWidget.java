/*
 *  Copyright (C) 2013 OpenSilk Productions LLC
 *
 *  This file is part of Fuzzy Clock
 *
 *  Fuzzy Clock is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  Fuzzy Clock is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Fuzzy Clock.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opensilk.fuzzyclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import hugo.weaving.DebugLog;

public class FuzzyWidget extends AppWidgetProvider {

    private static final String TAG = FuzzyWidget.class.getSimpleName();
    private static final boolean LOGV = true;

    public static final String ACTION_UPDATE_WIDGET_TIME = "org.opensilk.action.UPDATE_FUZZY_WIDGET_TIME";
    public static final String ACTION_UPDATE_WIDGET_SETTINGS = "org.opensilk.action.UPDATE_FUZZY_WIDGET_SETTINGS";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (LOGV) Log.v(TAG, "onReceive: " + action);
        if (Intent.ACTION_SCREEN_ON.equals(action) ||
                ACTION_UPDATE_WIDGET_TIME.equals(action)) {
            pokeService(context, ACTION_UPDATE_WIDGET_TIME);
        } else if (ACTION_UPDATE_WIDGET_SETTINGS.equals(action)) {
            pokeService(context, ACTION_UPDATE_WIDGET_SETTINGS);
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (LOGV) Log.v(TAG, "onUpdate");
        pokeService(context, ACTION_UPDATE_WIDGET_SETTINGS);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        if (LOGV) Log.v(TAG, "onDeleted");
        pokeService(context, ACTION_UPDATE_WIDGET_SETTINGS);
    }

    @Override
    public void onDisabled(Context context) {
        if (LOGV) Log.v(TAG, "onDisabled");
        context.stopService(new Intent(context, FuzzyWidgetService.class));
        cancelNextAlarm(context);
    }

    void setNextAlarm(Context context) {
        FuzzyLogic fuzzyLogic = new FuzzyLogic();
        fuzzyLogic.setDateFormat(true); // use 24h
        fuzzyLogic.updateTime();
        fuzzyLogic.updateTime(); // Double call to force prev==cur
        long nextMilli = fuzzyLogic.getNextIntervalMilli();
        Intent nextUpdate = new Intent(ACTION_UPDATE_WIDGET_TIME);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, nextUpdate, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC, fuzzyLogic.getCalendar().getTimeInMillis() + nextMilli, pi);
        } else {
            am.set(AlarmManager.RTC, fuzzyLogic.getCalendar().getTimeInMillis() + nextMilli, pi);
        }
        Log.i(TAG, "Scheduled next clock update for " + (nextMilli/1000) + "s from now");
    }

    void cancelNextAlarm(Context context) {
        Intent nextUpdate = new Intent(ACTION_UPDATE_WIDGET_TIME);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, nextUpdate, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    @DebugLog
    private void pokeService(Context context, String action) {
        context.startService(new Intent(action, null, context, FuzzyWidgetService.class));
        setNextAlarm(context);
    }
}
