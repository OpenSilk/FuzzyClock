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
