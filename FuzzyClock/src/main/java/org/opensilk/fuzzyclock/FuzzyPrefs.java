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

    private static final String DREAM_COLOR_MINUTE_PORT = "dream_color_minute_port";
    private static final String DREAM_COLOR_MINUTE_LAND = "dream_color_minute_land";
    private static final String DREAM_COLOR_SEPARATOR_PORT = "dream_color_separator_port";
    private static final String DREAM_COLOR_SEPARATOR_LAND = "dream_color_separator_land";
    private static final String DREAM_COLOR_HOUR_PORT = "dream_color_hour_port";
    private static final String DREAM_COLOR_HOUR_LAND = "dream_color_hour_land";
    private static final String DREAM_FONT_SIZE_PORT = "dream_font_size_port";
    private static final String DREAM_FONT_SIZE_LAND = "dream_font_size_land";
    private static final String DREAM_STYLE_PORT = "dream_style_port";
    private static final String DREAM_STYLE_LAND = "dream_style_land";

    private static final String WIDGET_COLOR_MINUTE_PORT = "widget_%d_color_minute_port";
    private static final String WIDGET_COLOR_MINUTE_LAND = "widget_%d_color_minute_land";
    private static final String WIDGET_COLOR_SEPARATOR_PORT = "widget_%d_color_separator_port";
    private static final String WIDGET_COLOR_SEPARATOR_LAND = "widget_%d_color_separator_land";
    private static final String WIDGET_COLOR_HOUR_PORT = "widget_%d_color_hour_port";
    private static final String WIDGET_COLOR_HOUR_LAND = "widget_%d_color_hour_land";
    private static final String WIDGET_FONT_SIZE_PORT = "widget_%d_font_size_port";
    private static final String WIDGET_FONT_SIZE_LAND = "widget_%d_font_size_land";
    private static final String WIDGET_STYLE_PORT = "widget_%d_style_port";
    private static final String WIDGET_STYLE_LAND = "widget_%d_style_land";

    public static final int STYLE_HORIZONTAL = 0;
    public static final int STYLE_VERTICAL = 1;
    public static final int STYLE_STAGGERED = 2;
    public static final int STYLE_DEFAULT = STYLE_HORIZONTAL;

    private final Context mContext;
    private final int mWidgetId;

    private String mPrefMin;
    private String mPrefSep;
    private String mPrefHour;
    private String mPrefSize;
    private String mPrefStyle;

    public final FuzzyColor color = new FuzzyColor();
    public float size;
    public int style;

    public class FuzzyColor {
        public int minute;
        public int separator;
        public int hour;
    }

    public FuzzyPrefs(Context context) {
        this(context, -1);
    }

    public FuzzyPrefs(Context context, int widgetId) {
        mContext = context;
        mWidgetId = widgetId;
        init();
    }

    private void init() {
        init(mContext.getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT);
    }

    private void init(boolean isPortrait) {
        if (mWidgetId == -1) {
            mPrefMin = isPortrait ? DREAM_COLOR_MINUTE_PORT : DREAM_COLOR_MINUTE_LAND;
            mPrefSep = isPortrait ? DREAM_COLOR_SEPARATOR_PORT : DREAM_COLOR_SEPARATOR_LAND;
            mPrefHour = isPortrait ? DREAM_COLOR_HOUR_PORT : DREAM_COLOR_HOUR_LAND;
            mPrefSize = isPortrait ? DREAM_FONT_SIZE_PORT : DREAM_FONT_SIZE_LAND;
            mPrefStyle = isPortrait ? DREAM_STYLE_PORT : DREAM_STYLE_LAND;
        } else {
            mPrefMin = String.format(isPortrait ? WIDGET_COLOR_MINUTE_PORT : WIDGET_COLOR_MINUTE_LAND, mWidgetId);
            mPrefSep = String.format(isPortrait ? WIDGET_COLOR_SEPARATOR_PORT : WIDGET_COLOR_SEPARATOR_LAND, mWidgetId);
            mPrefHour = String.format(isPortrait ? WIDGET_COLOR_HOUR_PORT : WIDGET_COLOR_HOUR_LAND, mWidgetId);
            mPrefSize = String.format(isPortrait ? WIDGET_FONT_SIZE_PORT : WIDGET_FONT_SIZE_LAND, mWidgetId);
            mPrefStyle = String.format(isPortrait ? WIDGET_STYLE_PORT : WIDGET_STYLE_LAND, mWidgetId);
        }
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        color.minute = prefs.getInt(mPrefMin, android.R.color.white);
        color.separator = prefs.getInt(mPrefSep, android.R.color.holo_blue_light);
        color.hour = prefs.getInt(mPrefHour, android.R.color.white);
        size = prefs.getFloat(mPrefSize, (float) mContext.getResources().getInteger(R.integer.fuzzy_font_size_default));
        style = prefs.getInt(mPrefStyle, STYLE_DEFAULT);
    }

    public void reset() {
        color.minute = color.hour = android.R.color.white;
        color.separator = android.R.color.holo_blue_light;
        size = mContext.getResources().getInteger(R.integer.fuzzy_font_size_default);
        style = STYLE_DEFAULT;
    }

    public void save() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.edit()
                .putInt(mPrefMin, color.minute)
                .putInt(mPrefSep, color.separator)
                .putInt(mPrefHour, color.hour)
                .putFloat(mPrefSize, size)
                .putInt(mPrefStyle, style)
                .commit();
    }

    public void remove() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        init(true); //Portrait prefs
        prefs.edit()
                .remove(mPrefMin)
                .remove(mPrefSep)
                .remove(mPrefHour)
                .remove(mPrefSize)
                .remove(mPrefStyle)
                .apply();
        init(false); //Landscape prefs
        prefs.edit()
                .remove(mPrefMin)
                .remove(mPrefSep)
                .remove(mPrefHour)
                .remove(mPrefSize)
                .remove(mPrefStyle)
                .apply();
    }

    @Override
    public String toString() {
        return String.format("min=%d, hour=%d, sep=%d, size=%.2f style=%d",
                color.minute, color.hour, color.separator, size, style);
    }
}
