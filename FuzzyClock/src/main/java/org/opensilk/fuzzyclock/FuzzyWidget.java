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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;

public class FuzzyWidget extends AppWidgetProvider {

    private static final String TAG = FuzzyWidget.class.getSimpleName();
    private static final boolean LOGV = true;

    private static final String ACTION_UPDATE_WIDGET = "org.opensilk.action.UPDATE_FUZZY_WIDGET";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_DATE_CHANGED.equals(action) ||
                Intent.ACTION_TIMEZONE_CHANGED.equals(action) ||
                Intent.ACTION_SCREEN_ON.equals(action) ||
                Intent.ACTION_TIME_CHANGED.equals(action) ||
                Intent.ACTION_LOCALE_CHANGED.equals(action) ||
                ACTION_UPDATE_WIDGET.equals(action)) {
            updateTime(context);
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (LOGV) Log.v(TAG, "onUpdate");
        updateTime(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        if (LOGV) Log.v(TAG, "onDeleted");
        updateTime(context);
    }

    @Override
    public void onDisabled(Context context) {
        if (LOGV) Log.v(TAG, "onDisabled");
        cancelNextAlarm(context);
    }

    void setNextAlarm(Context context) {
        FuzzyLogic fuzzyLogic = new FuzzyLogic();
        fuzzyLogic.setDateFormat(true); // use 24h
        fuzzyLogic.updateTime();
        fuzzyLogic.updateTime(); // Double call to force prev==cur
        long nextMilli = fuzzyLogic.getNextIntervalMilli();
        Intent nextUpdate = new Intent(ACTION_UPDATE_WIDGET);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, nextUpdate, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        am.set(AlarmManager.RTC, fuzzyLogic.getCalendar().getTimeInMillis() + nextMilli, pi);
        Log.i(TAG, "Scheduled next clock update for " + (nextMilli/1000) + "s from now");
    }

    void cancelNextAlarm(Context context) {
        Intent nextUpdate = new Intent(ACTION_UPDATE_WIDGET);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, nextUpdate, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    private void updateTime(Context context) {
        FuzzyLogic fuzzyLogic = new FuzzyLogic();
        fuzzyLogic.setDateFormat(android.text.format.DateFormat.is24HourFormat(context));
        fuzzyLogic.updateTime();
        FuzzyLogic.FuzzyTime time = fuzzyLogic.getFuzzyTime();
        CharSequence timeM = (time.minute != -1) ? context.getString(time.minute) : "";
        CharSequence timeH = (time.hour != -1) ? context.getString(time.hour) : "";
        CharSequence separator = (time.separator != -1) ? context.getString(time.separator) : "";

        // Write time to the display
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.fuzzy_widget);

        views.setTextViewText(R.id.timeDisplayMinutes, timeM);
        views.setTextViewText(R.id.timeDisplaySeparator, separator);
        views.setTextViewText(R.id.timeDisplayHours, timeH);

        AppWidgetManager appManager = AppWidgetManager.getInstance(context);

        int[] widgetIds = appManager.getAppWidgetIds(new ComponentName(context, FuzzyWidget.class));
        if (widgetIds != null && widgetIds.length > 0) {
            if (LOGV) { for (int id: widgetIds) { Log.v(TAG, "Updating widget id=" + id); } }
            appManager.updateAppWidget(widgetIds, views);
            setNextAlarm(context);
        } else {
            Log.i(TAG, "No widgets left to update...");
            cancelNextAlarm(context);
        }
    }
}
