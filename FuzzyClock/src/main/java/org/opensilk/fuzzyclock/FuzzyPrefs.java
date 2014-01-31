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
import android.graphics.Typeface;
import android.preference.PreferenceManager;

import java.util.Locale;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class FuzzyPrefs {

    private static final String DREAM_COLOR_MINUTE_PORT = "dream_color_minute_port";
    private static final String DREAM_COLOR_MINUTE_LAND = "dream_color_minute_land";
    private static final String DREAM_COLOR_SEPARATOR_PORT = "dream_color_separator_port";
    private static final String DREAM_COLOR_SEPARATOR_LAND = "dream_color_separator_land";
    private static final String DREAM_COLOR_HOUR_PORT = "dream_color_hour_port";
    private static final String DREAM_COLOR_HOUR_LAND = "dream_color_hour_land";
    private static final String DREAM_FONT_SIZE_MINUTE_PORT = "dream_font_size_minute_port";
    private static final String DREAM_FONT_SIZE_MINUTE_LAND = "dream_font_size_minute_land";
    private static final String DREAM_FONT_SIZE_SEPARATOR_PORT = "dream_font_size_separator_port";
    private static final String DREAM_FONT_SIZE_SEPARATOR_LAND = "dream_font_size_separator_land";
    private static final String DREAM_FONT_SIZE_HOUR_PORT = "dream_font_size_hour_port";
    private static final String DREAM_FONT_SIZE_HOUR_LAND = "dream_font_size_hour_land";
    private static final String DREAM_FONT_STYLE_MINUTE_PORT = "dream_font_style_minute_port";
    private static final String DREAM_FONT_STYLE_MINUTE_LAND = "dream_font_style_minute_land";
    private static final String DREAM_FONT_STYLE_SEPARATOR_PORT = "dream_font_style_separator_port";
    private static final String DREAM_FONT_STYLE_SEPARATOR_LAND = "dream_font_style_separator_land";
    private static final String DREAM_FONT_STYLE_HOUR_PORT = "dream_font_style_hour_port";
    private static final String DREAM_FONT_STYLE_HOUR_LAND = "dream_font_style_hour_land";
    private static final String DREAM_STYLE_PORT = "dream_style_port";
    private static final String DREAM_STYLE_LAND = "dream_style_land";
    private static final String DREAM_LOGIC_PORT = "dream_logic_port";
    private static final String DREAM_LOGIC_LAND = "dream_logic_land";

    private static final String WIDGET_COLOR_MINUTE_PORT = "widget_%d_color_minute_port";
    private static final String WIDGET_COLOR_MINUTE_LAND = "widget_%d_color_minute_land";
    private static final String WIDGET_COLOR_SEPARATOR_PORT = "widget_%d_color_separator_port";
    private static final String WIDGET_COLOR_SEPARATOR_LAND = "widget_%d_color_separator_land";
    private static final String WIDGET_COLOR_HOUR_PORT = "widget_%d_color_hour_port";
    private static final String WIDGET_COLOR_HOUR_LAND = "widget_%d_color_hour_land";
    private static final String WIDGET_FONT_SIZE_MINUTE_PORT = "widget_%d_font_size_minute_port";
    private static final String WIDGET_FONT_SIZE_MINUTE_LAND = "widget_%d_font_size_minute_land";
    private static final String WIDGET_FONT_SIZE_SEPARATOR_PORT = "widget_%d_font_size_separator_port";
    private static final String WIDGET_FONT_SIZE_SEPARATOR_LAND = "widget_%d_font_size_separator_land";
    private static final String WIDGET_FONT_SIZE_HOUR_PORT = "widget_%d_font_size_hour_port";
    private static final String WIDGET_FONT_SIZE_HOUR_LAND = "widget_%d_font_size_hour_land";
    private static final String WIDGET_FONT_STYLE_MINUTE_PORT = "widget_%d_font_style_minute_port";
    private static final String WIDGET_FONT_STYLE_MINUTE_LAND = "widget_%d_font_style_minute_land";
    private static final String WIDGET_FONT_STYLE_SEPARATOR_PORT = "widget_%d_font_style_separator_port";
    private static final String WIDGET_FONT_STYLE_SEPARATOR_LAND = "widget_%d_font_style_separator_land";
    private static final String WIDGET_FONT_STYLE_HOUR_PORT = "widget_%d_font_style_hour_port";
    private static final String WIDGET_FONT_STYLE_HOUR_LAND = "widget_%d_font_style_hour_land";
    private static final String WIDGET_STYLE_PORT = "widget_%d_style_port";
    private static final String WIDGET_STYLE_LAND = "widget_%d_style_land";
    private static final String WIDGET_LOGIC_PORT = "widget_%d_logic_port";
    private static final String WIDGET_LOGIC_LAND = "widget_%d_logic_land";

    public static final int TEXT_COLOR_DEF_MIN = android.R.color.white;
    public static final int TEXT_COLOR_DEF_SEP = android.R.color.holo_blue_light;
    public static final int TEXT_COLOR_DEF_HOUR = android.R.color.white;

    public static final int TEXT_STYLE_REGULAR = 0;
    public static final int TEXT_STYLE_REGULAR_BOLD = 1;
    public static final int TEXT_STYLE_CONDENSED = 2;
    public static final int TEXT_STYLE_CONDENSED_BOLD = 3;
    public static final int TEXT_STYLE_THIN = 4;
    public static final int TEXT_STYLE_THIN_BOLD = 5;
    public static final int TEXT_STYLE_DEFAULT = TEXT_STYLE_CONDENSED;

    public static final int CLOCK_STYLE_HORIZONTAL = 0;
    public static final int CLOCK_STYLE_VERTICAL = 1;
    public static final int CLOCK_STYLE_STAGGERED = 2;
    public static final int CLOCK_STYLE_DEFAULT = CLOCK_STYLE_HORIZONTAL;

    public static final int CLOCK_LOGIC_FAST = 0;
    public static final int CLOCK_LOGIC_PRECISE = 1;
    public static final int CLOCK_LOGIC_SLOW = 2;
    public static final int CLOCK_LOGIC_WARPED = 3;
    public static final int CLOCK_LOGIC_DEFAULT = CLOCK_LOGIC_WARPED;

    private final Context mContext;
    private final int mWidgetId;

    private String mPrefMinColor;
    private String mPrefSepColor;
    private String mPrefHourColor;
    private String mPrefMinSize;
    private String mPrefSepSize;
    private String mPrefHourSize;
    private String mPrefMinStyle;
    private String mPrefSepStyle;
    private String mPrefHourStyle;
    private String mPrefClockStyle;
    private String mPrefClockLogic;

    public final Settings minute = new Settings();
    public final Settings separator = new Settings();
    public final Settings hour = new Settings();
    public int clockStyle;
    public int clockLogic;

    public static class Settings {
        public int color;
        public float size;
        public int style;
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
            mPrefMinColor = isPortrait ? DREAM_COLOR_MINUTE_PORT : DREAM_COLOR_MINUTE_LAND;
            mPrefMinSize = isPortrait ? DREAM_FONT_SIZE_MINUTE_PORT : DREAM_FONT_SIZE_MINUTE_LAND;
            mPrefMinStyle = isPortrait ? DREAM_FONT_STYLE_MINUTE_PORT : DREAM_FONT_STYLE_MINUTE_LAND;
            mPrefSepColor = isPortrait ? DREAM_COLOR_SEPARATOR_PORT : DREAM_COLOR_SEPARATOR_LAND;
            mPrefSepSize = isPortrait ? DREAM_FONT_SIZE_SEPARATOR_PORT : DREAM_FONT_SIZE_SEPARATOR_LAND;
            mPrefSepStyle = isPortrait ? DREAM_FONT_STYLE_SEPARATOR_PORT : DREAM_FONT_STYLE_SEPARATOR_LAND;
            mPrefHourColor = isPortrait ? DREAM_COLOR_HOUR_PORT : DREAM_COLOR_HOUR_LAND;
            mPrefHourSize = isPortrait ? DREAM_FONT_SIZE_HOUR_PORT : DREAM_FONT_SIZE_HOUR_LAND;
            mPrefHourStyle = isPortrait ? DREAM_FONT_STYLE_HOUR_PORT : DREAM_FONT_STYLE_HOUR_LAND;
            mPrefClockStyle = isPortrait ? DREAM_STYLE_PORT : DREAM_STYLE_LAND;
            mPrefClockLogic = isPortrait ? DREAM_LOGIC_PORT : DREAM_LOGIC_LAND;
        } else {
            mPrefMinColor = String.format(Locale.US, isPortrait ? WIDGET_COLOR_MINUTE_PORT : WIDGET_COLOR_MINUTE_LAND, mWidgetId);
            mPrefMinSize = String.format(Locale.US, isPortrait ? WIDGET_FONT_SIZE_MINUTE_PORT : WIDGET_FONT_SIZE_MINUTE_LAND, mWidgetId);
            mPrefMinStyle = String.format(Locale.US, isPortrait ? WIDGET_FONT_STYLE_MINUTE_PORT : WIDGET_FONT_STYLE_MINUTE_LAND, mWidgetId);
            mPrefSepColor = String.format(Locale.US, isPortrait ? WIDGET_COLOR_SEPARATOR_PORT : WIDGET_COLOR_SEPARATOR_LAND, mWidgetId);
            mPrefSepSize = String.format(Locale.US, isPortrait ? WIDGET_FONT_SIZE_SEPARATOR_PORT : WIDGET_FONT_SIZE_SEPARATOR_LAND, mWidgetId);
            mPrefSepStyle = String.format(Locale.US, isPortrait ? WIDGET_FONT_STYLE_SEPARATOR_PORT : WIDGET_FONT_STYLE_SEPARATOR_LAND, mWidgetId);
            mPrefHourColor = String.format(Locale.US, isPortrait ? WIDGET_COLOR_HOUR_PORT : WIDGET_COLOR_HOUR_LAND, mWidgetId);
            mPrefHourSize = String.format(Locale.US, isPortrait ? WIDGET_FONT_SIZE_HOUR_PORT : WIDGET_FONT_SIZE_HOUR_LAND, mWidgetId);
            mPrefHourStyle = String.format(Locale.US, isPortrait ? WIDGET_FONT_STYLE_HOUR_PORT : WIDGET_FONT_STYLE_HOUR_LAND, mWidgetId);
            mPrefClockStyle = String.format(Locale.US, isPortrait ? WIDGET_STYLE_PORT : WIDGET_STYLE_LAND, mWidgetId);
            mPrefClockLogic = String.format(Locale.US, isPortrait ? WIDGET_LOGIC_PORT : WIDGET_LOGIC_LAND, mWidgetId);
        }
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        minute.color = prefs.getInt(mPrefMinColor, TEXT_COLOR_DEF_MIN);
        minute.size = prefs.getFloat(mPrefMinSize, (float) mContext.getResources().getInteger(R.integer.fuzzy_font_size_default));
        minute.style = prefs.getInt(mPrefMinStyle, TEXT_STYLE_DEFAULT);
        separator.color = prefs.getInt(mPrefSepColor, TEXT_COLOR_DEF_SEP);
        separator.size = prefs.getFloat(mPrefSepSize, (float) mContext.getResources().getInteger(R.integer.fuzzy_font_size_default));
        separator.style = prefs.getInt(mPrefSepStyle, TEXT_STYLE_DEFAULT);
        hour.color = prefs.getInt(mPrefHourColor, TEXT_COLOR_DEF_HOUR);
        hour.size = prefs.getFloat(mPrefHourSize, (float) mContext.getResources().getInteger(R.integer.fuzzy_font_size_default));
        hour.style = prefs.getInt(mPrefHourStyle, TEXT_STYLE_DEFAULT);
        clockStyle = prefs.getInt(mPrefClockStyle, CLOCK_STYLE_DEFAULT);
        clockLogic = prefs.getInt(mPrefClockLogic, CLOCK_LOGIC_DEFAULT);
    }

    public void reset() {
        minute.color = TEXT_COLOR_DEF_MIN;
        separator.color = TEXT_COLOR_DEF_SEP;
        hour.color = TEXT_COLOR_DEF_HOUR;
        minute.size = separator.size = hour.size =
                mContext.getResources().getInteger(R.integer.fuzzy_font_size_default);
        minute.style = separator.style = hour.style =
                TEXT_STYLE_DEFAULT;
        clockStyle = CLOCK_STYLE_DEFAULT;
        clockLogic = CLOCK_LOGIC_DEFAULT;
    }

    public void save() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.edit()
                .putInt(mPrefMinColor, minute.color)
                .putFloat(mPrefMinSize, minute.size)
                .putInt(mPrefMinStyle, minute.style)
                .putInt(mPrefSepColor, separator.color)
                .putFloat(mPrefSepSize, separator.size)
                .putInt(mPrefSepStyle, separator.style)
                .putInt(mPrefHourColor, hour.color)
                .putFloat(mPrefHourSize, hour.size)
                .putInt(mPrefHourStyle, hour.style)
                .putInt(mPrefClockStyle, clockStyle)
                .putInt(mPrefClockLogic, clockLogic)
                .commit();
    }

    public void remove() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        init(true); //Portrait prefs
        prefs.edit()
                .remove(mPrefMinColor)
                .remove(mPrefMinSize)
                .remove(mPrefMinStyle)
                .remove(mPrefSepColor)
                .remove(mPrefSepSize)
                .remove(mPrefSepStyle)
                .remove(mPrefHourColor)
                .remove(mPrefHourSize)
                .remove(mPrefHourStyle)
                .remove(mPrefClockStyle)
                .remove(mPrefClockLogic)
                .apply();
        init(false); //Landscape prefs
        prefs.edit()
                .remove(mPrefMinColor)
                .remove(mPrefMinSize)
                .remove(mPrefMinStyle)
                .remove(mPrefSepColor)
                .remove(mPrefSepSize)
                .remove(mPrefSepStyle)
                .remove(mPrefHourColor)
                .remove(mPrefHourSize)
                .remove(mPrefHourStyle)
                .remove(mPrefClockStyle)
                .remove(mPrefClockLogic)
                .apply();
    }

    public static Typeface createTypeface(int style) {
        switch (style) {
            case FuzzyPrefs.TEXT_STYLE_REGULAR:
                return Typeface.create("sans-serif", Typeface.NORMAL);
            case FuzzyPrefs.TEXT_STYLE_REGULAR_BOLD:
                return Typeface.create("sans-serif", Typeface.BOLD);
            case FuzzyPrefs.TEXT_STYLE_CONDENSED:
                return Typeface.create("sans-serif-condensed", Typeface.NORMAL);
            case FuzzyPrefs.TEXT_STYLE_CONDENSED_BOLD:
                return Typeface.create("sans-serif-condensed", Typeface.BOLD);
            case FuzzyPrefs.TEXT_STYLE_THIN:
                return Typeface.create("sans-serif-thin", Typeface.NORMAL);
            case FuzzyPrefs.TEXT_STYLE_THIN_BOLD:
                return Typeface.create("sans-serif-thin", Typeface.BOLD);
            default:
                return Typeface.defaultFromStyle(Typeface.NORMAL);
        }
    }

    public static FuzzyLogic createLogic(int type) {
        switch (type) {
            case CLOCK_LOGIC_FAST:
                return new FuzzyLogicFast();
            case CLOCK_LOGIC_PRECISE:
                return new FuzzyLogicPrecise();
            case CLOCK_LOGIC_SLOW:
                return new FuzzyLogicSlow();
            case CLOCK_LOGIC_DEFAULT:
            default:
                return new FuzzyLogicWarped();
        }
    }

}
