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

import java.util.Calendar;

public abstract class FuzzyLogic {

    protected Calendar mCalendar;
    protected int mMinutes, mHours;
    protected int mPrevMinutes, mPrevHours;
    protected boolean m24HourFormat = false;

    public FuzzyLogic() {
        mCalendar = Calendar.getInstance();
    }

    public void setCalendar(Calendar c) {
        mCalendar = c;
    }

    public Calendar getCalendar() {
        return mCalendar;
    }

    public void setDateFormat(boolean is24hour) {
        // From what i've read countries using 24hour time generally speak the time in 12hour format
        //m24HourFormat = is24hour;
    }

    public boolean is24HourFormat() {
        return m24HourFormat;
    }

    public void updateTime() {
        mPrevMinutes = mMinutes;
        mMinutes = mCalendar.get(Calendar.MINUTE);
        mPrevHours = mHours;
        mHours = mCalendar.get(Calendar.HOUR_OF_DAY);
    }

    public abstract boolean hasChanged();

    public abstract long getNextIntervalMilli();

    public abstract FuzzyTime getFuzzyTime();

    protected int getHourResourceId(int hour) {
        switch (hour) {
            case 0:  return R.string.fuzzy_twelve;
            case 1:  return R.string.fuzzy_one;
            case 2:  return R.string.fuzzy_two;
            case 3:  return R.string.fuzzy_three;
            case 4:  return R.string.fuzzy_four;
            case 5:  return R.string.fuzzy_five;
            case 6:  return R.string.fuzzy_six;
            case 7:  return R.string.fuzzy_seven;
            case 8:  return R.string.fuzzy_eight;
            case 9:  return R.string.fuzzy_nine;
            case 10: return R.string.fuzzy_ten;
            case 11: return R.string.fuzzy_eleven;
            case 12: return R.string.fuzzy_twelve;
            case 13: return R.string.fuzzy_thirteen;
            case 14: return R.string.fuzzy_fourteen;
            case 15: return R.string.fuzzy_fifteen;
            case 16: return R.string.fuzzy_sixteen;
            case 17: return R.string.fuzzy_seventeen;
            case 18: return R.string.fuzzy_eighteen;
            case 19: return R.string.fuzzy_nineteen;
            case 20: return R.string.fuzzy_twenty;
            case 21: return R.string.fuzzy_twenty_one;
            case 22: return R.string.fuzzy_twenty_two;
            case 23: return R.string.fuzzy_twenty_three;
            case 24: return R.string.fuzzy_twenty_four;
            default: return -1;
        }
    }

    // int here are resource id's
    public static class FuzzyTime {
        public int minute, hour, separator;
        public FuzzyTime(int minute, int hour, int separator) {
            this.minute = minute;
            this.hour = hour;
            this.separator = separator;
        }
    }

}
