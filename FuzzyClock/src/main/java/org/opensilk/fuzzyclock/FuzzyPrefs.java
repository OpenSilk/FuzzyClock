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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class FuzzyPrefs {

    public static final String DREAM_COLOR_MINUTE = "color_minute_dream";
    public static final String DREAM_COLOR_HOUR = "color_hour_dream";
    public static final String DREAM_COLOR_SEPARATOR = "color_separator_dream";
    public static final String DREAM_FONT_SIZE_PORT = "font_size_port_dream";
    public static final String DREAM_FONT_SIZE_LAND = "font_size_land_dream";

    public static final String WIDGET_COLOR_MINUTE = "color_minute_widget_";
    public static final String WIDGET_COLOR_HOUR = "color_hour_widget_";
    public static final String WIDGET_COLOR_SEPARATOR = "color_separator_widget_";
    public static final String WIDGET_FONT_SIZE_PORT = "font_size_port_widget_";
    public static final String WIDGET_FONT_SIZE_LAND = "font_size_land_widget_";

    private final Context mContext;
    private final boolean isPortrait;
    private final int mWidgetId;

    public final FuzzyColor color = new FuzzyColor();
    public float size;

    public class FuzzyColor {
        public int minute;
        public int hour;
        public int separator;
    }

    public FuzzyPrefs(Context context) {
        this(context, -1);
    }

    public FuzzyPrefs(Context context, int widgetId) {
        mContext = context;
        isPortrait = mContext.getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT;
        mWidgetId = widgetId;
        init();
    }

    private void init() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (mWidgetId == -1) {
            color.minute = prefs.getInt(DREAM_COLOR_MINUTE, android.R.color.white);
            color.hour = prefs.getInt(DREAM_COLOR_HOUR, android.R.color.white);
            color.separator = prefs.getInt(DREAM_COLOR_SEPARATOR, android.R.color.holo_blue_light);
            String fontPref = isPortrait ? DREAM_FONT_SIZE_PORT : DREAM_FONT_SIZE_LAND;
            size = prefs.getFloat(fontPref,
                    (float) mContext.getResources().getInteger(R.integer.fuzzy_font_size_default));
        } else {
            String id = String.valueOf(mWidgetId);
            color.minute = prefs.getInt(WIDGET_COLOR_MINUTE+id, android.R.color.white);
            color.hour = prefs.getInt(WIDGET_COLOR_HOUR+id, android.R.color.white);
            color.separator = prefs.getInt(WIDGET_COLOR_SEPARATOR + id, android.R.color.holo_blue_light);
            String fontPref = isPortrait ? WIDGET_FONT_SIZE_PORT+id : WIDGET_FONT_SIZE_LAND+id;
            size = prefs.getFloat(fontPref,
                    (float) mContext.getResources().getInteger(R.integer.fuzzy_font_size_default));
        }
    }

    public void reset() {
        color.minute = color.hour = android.R.color.white;
        color.separator = android.R.color.holo_blue_light;
        size = mContext.getResources().getInteger(R.integer.fuzzy_font_size_default);
    }

    public void save() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (mWidgetId == -1) {
            String fontPref = isPortrait ? DREAM_FONT_SIZE_PORT : DREAM_FONT_SIZE_LAND;
            prefs.edit().putInt(DREAM_COLOR_MINUTE, color.minute)
                    .putInt(DREAM_COLOR_HOUR, color.hour)
                    .putInt(DREAM_COLOR_SEPARATOR, color.separator)
                    .putFloat(fontPref, size).commit();
        } else {
            String id = String.valueOf(mWidgetId);
            String fontPref = isPortrait ? WIDGET_FONT_SIZE_PORT+id : WIDGET_FONT_SIZE_LAND+id;
            prefs.edit().putInt(WIDGET_COLOR_MINUTE + id, color.minute)
                    .putInt(WIDGET_COLOR_HOUR + id, color.hour)
                    .putInt(WIDGET_COLOR_SEPARATOR + id, color.separator)
                    .putFloat(fontPref, size).commit();
        }
    }

    @Override
    public String toString() {
        return String.format("min=%d, hour=%d, sep=%d, size=%.2f",
                color.minute, color.hour, color.separator, size);
    }
}
