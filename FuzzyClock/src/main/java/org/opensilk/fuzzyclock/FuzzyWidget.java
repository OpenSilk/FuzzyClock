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

        Intent nextUpdate = new Intent(ACTION_UPDATE_WIDGET);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, nextUpdate, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        am.set(AlarmManager.RTC, cal.getTimeInMillis() + nextMilli, pi);
        Log.i(TAG, "Scheduled next clock update for " + (nextMilli/1000) + "s from now");
    }

    void cancelNextAlarm(Context context) {
        Intent nextUpdate = new Intent(ACTION_UPDATE_WIDGET);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, nextUpdate, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    private void updateTime(Context context) {
        Calendar calendar = Calendar.getInstance();
        boolean is24HourFormat = android.text.format.DateFormat.is24HourFormat(context);

//        calendar.setTimeInMillis(System.currentTimeMillis());
//        if (mTimeZoneId != null) {
//            calendar.setTimeZone(TimeZone.getTimeZone(mTimeZoneId));
//        }

        int minutes, hours;
        CharSequence timeH, timeM, separator;

        minutes = calendar.get(Calendar.MINUTE);

        if        (minutes >= 56) { timeM = ""; // O'CLOCK
        } else if (minutes >= 51) { timeM = context.getString(R.string.fuzzy_five);
        } else if (minutes >= 46) { timeM = context.getString(R.string.fuzzy_ten);
        } else if (minutes >= 41) { timeM = context.getString(R.string.fuzzy_quarter);
        } else if (minutes >= 36) { timeM = context.getString(R.string.fuzzy_twenty);
        } else if (minutes >= 25) { timeM = context.getString(R.string.fuzzy_half);
        } else if (minutes >= 20) { timeM = context.getString(R.string.fuzzy_twenty);
        } else if (minutes >= 15) { timeM = context.getString(R.string.fuzzy_quarter);
        } else if (minutes >= 10) { timeM = context.getString(R.string.fuzzy_ten);
        } else if (minutes >= 5)  { timeM = context.getString(R.string.fuzzy_five);
        } else                    { timeM = ""; // O'CLOCK
        }

        hours = calendar.get(is24HourFormat ? Calendar.HOUR_OF_DAY : Calendar.HOUR);

        // Adjust for next hour
        if (minutes >= 36) {
            if (is24HourFormat) {
                hours = (hours + 1) % 24;
            } else {
                hours = (hours + 1) % 12;
            }
        }

        switch (hours) {
            case 0:  timeH = context.getString(R.string.fuzzy_twelve); break;
            case 1:  timeH = context.getString(R.string.fuzzy_one); break;
            case 2:  timeH = context.getString(R.string.fuzzy_two); break;
            case 3:  timeH = context.getString(R.string.fuzzy_three); break;
            case 4:  timeH = context.getString(R.string.fuzzy_four); break;
            case 5:  timeH = context.getString(R.string.fuzzy_five); break;
            case 6:  timeH = context.getString(R.string.fuzzy_six); break;
            case 7:  timeH = context.getString(R.string.fuzzy_seven); break;
            case 8:  timeH = context.getString(R.string.fuzzy_eight); break;
            case 9:  timeH = context.getString(R.string.fuzzy_nine); break;
            case 10: timeH = context.getString(R.string.fuzzy_ten); break;
            case 11: timeH = context.getString(R.string.fuzzy_eleven); break;
            case 12: timeH = context.getString(R.string.fuzzy_twelve); break;
            case 13: timeH = context.getString(R.string.fuzzy_thirteen); break;
            case 14: timeH = context.getString(R.string.fuzzy_fourteen); break;
            case 15: timeH = context.getString(R.string.fuzzy_fifteen); break;
            case 16: timeH = context.getString(R.string.fuzzy_sixteen); break;
            case 17: timeH = context.getString(R.string.fuzzy_seventeen); break;
            case 18: timeH = context.getString(R.string.fuzzy_eighteen); break;
            case 19: timeH = context.getString(R.string.fuzzy_nineteen); break;
            case 20: timeH = context.getString(R.string.fuzzy_twenty); break;
            case 21: timeH = context.getString(R.string.fuzzy_twenty) + context.getString(R.string.fuzzy_one); break;
            case 22: timeH = context.getString(R.string.fuzzy_twenty) + context.getString(R.string.fuzzy_two); break;
            case 23: timeH = context.getString(R.string.fuzzy_twenty) + context.getString(R.string.fuzzy_three); break;
            case 24: timeH = context.getString(R.string.fuzzy_twenty) + context.getString(R.string.fuzzy_four); break;
            default: timeH = ""; break;
        }

        // Handle Noon and Midnight
        if (is24HourFormat) {
            if (hours == 12) {
                timeH = context.getString(R.string.fuzzy_noon);
            } else if (hours == 0) {
                timeH = context.getString(R.string.fuzzy_midnight);
            }
        } else {
            if (hours == 0) {
                if (minutes >= 36) {
                    timeH = (calendar.get(Calendar.AM_PM) == 1) ?
                            context.getString(R.string.fuzzy_midnight) :
                            context.getString(R.string.fuzzy_noon);
                } else {
                    timeH = (calendar.get(Calendar.AM_PM) == 0) ?
                            context.getString(R.string.fuzzy_midnight) :
                            context.getString(R.string.fuzzy_noon);
                }
            }
        }

        // Final shuffle before writing to display
        if (minutes >= 56) {
            separator = (hours > 12) ?
                    context.getString(R.string.fuzzy_hundred) :
                    context.getString(R.string.fuzzy_oclock);
            if (hours == 0 || (is24HourFormat && (hours == 12))) {
//                separator = (mCalendar.get(Calendar.AM_PM) == 1) ?
//                        context.getString(R.string.fuzzy_midnight) :
//                        context.getString(R.string.fuzzy_noon);
                separator = timeH;
                timeM = "";
            } else {
                timeM = timeH;
            }
            timeH = "";
        } else if (minutes >= 36) {
            separator = context.getString(R.string.fuzzy_to);
        } else if (minutes >= 5) {
            separator = context.getString(R.string.fuzzy_past);
        } else {
            separator = (hours > 12) ?
                    context.getString(R.string.fuzzy_hundred) :
                    context.getString(R.string.fuzzy_oclock);
            if (hours == 0 || (is24HourFormat && (hours == 12))) {
//                separator = (mCalendar.get(Calendar.AM_PM) == 0) ?
//                        context.getString(R.string.fuzzy_midnight) :
//                        context.getString(R.string.fuzzy_noon);
                separator = timeH;
                timeM = "";
            } else {
                timeM = timeH;
            }
            timeH = "";
        }

        // Write time to the display
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.fuzzy_widget);

        views.setTextViewText(R.id.timeDisplayMinutes, timeM);
        views.setTextViewText(R.id.timeDisplaySeparator, separator);
        views.setTextViewText(R.id.timeDisplayHours, timeH);

        AppWidgetManager appManager = AppWidgetManager.getInstance(context);

        int[] widgetIds = appManager.getAppWidgetIds(new ComponentName(context, FuzzyWidget.class));
        if (widgetIds != null && widgetIds.length > 0) {
            if (LOGV) for (int id: widgetIds) Log.v(TAG, "Updating widget id=" + id);
            appManager.updateAppWidget(widgetIds, views);
            setNextAlarm(context);
        } else {
            Log.i(TAG, "No widgets left to update...");
            cancelNextAlarm(context);
        }
    }
}
