/*
 *  Copyright (C) 2014 OpenSilk Productions LLC
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

/**
 * Fuzzy time that runs five minutes slow
 */
public class FuzzyLogicSlow extends FuzzyLogic {

    public FuzzyLogicSlow() {
        super();
    }

    public boolean hasChanged() {
        return getMinutesState(mPrevMinutes) != getMinutesState(mMinutes)
                || mPrevHours != mHours;
    }

    private int getMinutesState(int minutes) {
        int minState;
        if        (minutes < 5)  { minState = 1;
        } else if (minutes < 10) { minState = 2;
        } else if (minutes < 15) { minState = 3;
        } else if (minutes < 20) { minState = 4;
        } else if (minutes < 25) { minState = 5;
        } else if (minutes < 30) { minState = 6;
        } else if (minutes < 35) { minState = 7;
        } else if (minutes < 40) { minState = 8;
        } else if (minutes < 45) { minState = 9;
        } else if (minutes < 50) { minState = 10;
        } else if (minutes < 55) { minState = 11;
        } else   /*minutes < 60*/{ minState = 12;
        }
        return minState;
    }

    public long getNextIntervalMilli() {
        long nextMilli = 0;
        int minutes = mMinutes;
        int hours = mHours;
        if        (minutes < 5)  { minutes = 5;
        } else if (minutes < 10) { minutes = 10;
        } else if (minutes < 15) { minutes = 15;
        } else if (minutes < 20) { minutes = 20;
        } else if (minutes < 25) { minutes = 25;
        } else if (minutes < 30) { minutes = 30;
        } else if (minutes < 35) { minutes = 35;
        } else if (minutes < 40) { minutes = 40;
        } else if (minutes < 45) { minutes = 45;
        } else if (minutes < 50) { minutes = 50;
        } else if (minutes < 55) { minutes = 55;
        } else   /*minutes < 60*/{ hours += 1; minutes = 0;
        }
        nextMilli += (hours - mHours) * 3600000;
        nextMilli += (minutes - mMinutes) * 60000;
        nextMilli -= mCalendar.get(Calendar.SECOND) * 1000;
        return nextMilli;
    }

    public FuzzyLogic.FuzzyTime getFuzzyTime() {
        int timeM, timeH, separator;
        int minutes = mMinutes;
        int hours = mHours;

        if (!m24HourFormat) {
            hours %= 12;
        }

        if        (minutes < 5)  { timeM = -1; // O'CLOCK;
        } else if (minutes < 10) { timeM = R.string.fuzzy_five;
        } else if (minutes < 15) { timeM = R.string.fuzzy_ten;
        } else if (minutes < 20) { timeM = R.string.fuzzy_quarter;
        } else if (minutes < 25) { timeM = R.string.fuzzy_twenty;
        } else if (minutes < 30) { timeM = R.string.fuzzy_twenty_five;
        } else if (minutes < 35) { timeM = R.string.fuzzy_half;
        } else if (minutes < 40) { timeM = R.string.fuzzy_twenty_five;
        } else if (minutes < 45) { timeM = R.string.fuzzy_twenty;
        } else if (minutes < 50) { timeM = R.string.fuzzy_quarter;
        } else if (minutes < 55) { timeM = R.string.fuzzy_ten;
        } else   /*minutes < 60*/{ timeM = R.string.fuzzy_five;
        }

        // Adjust for next hour
        if (minutes >= 35) {
            if (m24HourFormat) {
                hours = (hours + 1) % 24;
            } else {
                hours = (hours + 1) % 12;
            }
        }

        timeH = getHourResourceId(hours);

        // Handle Noon and Midnight
        if (m24HourFormat) {
            if (hours == 12) {
                timeH = R.string.fuzzy_noon;
            } else if (hours == 0) {
                timeH = R.string.fuzzy_midnight;
            }
        } else {
            if (hours == 0) {
                if (minutes >= 35) {
                    timeH = (mCalendar.get(Calendar.AM_PM) == Calendar.PM) ?
                            R.string.fuzzy_midnight : R.string.fuzzy_noon;
                } else {
                    timeH = (mCalendar.get(Calendar.AM_PM) == Calendar.AM) ?
                            R.string.fuzzy_midnight : R.string.fuzzy_noon;
                }
            }
        }

        // Final shuffle
        if (minutes < 5) {
            if (hours == 0 || hours == 12) {
                // minutes show noon/midnight
                timeM = timeH;
                timeH = -1;
                separator = -1;
            } else {
                // put hour in minutes place
                timeM = timeH;
                timeH = R.string.fuzzy_oclock;
                separator = -1;
            }
        } else if (minutes < 35) {
            separator = R.string.fuzzy_past;
        } else /* minutes >= 35 */ {
            separator = R.string.fuzzy_to;
        }

        return new FuzzyLogic.FuzzyTime(timeM, timeH, separator);
    }
}
