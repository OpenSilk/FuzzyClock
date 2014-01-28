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
 * Fuzzy time that runs precisely on time
 */
public class FuzzyLogicPrecise extends FuzzyLogic {

    public FuzzyLogicPrecise() {
        super();
    }

    public boolean hasChanged() {
        return mPrevMinutes != mMinutes || mPrevHours != mHours;
    }

    public long getNextIntervalMilli() {
        long nextMilli = 0;
        nextMilli += (mMinutes + 1) * 60000;
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

        switch (minutes) {
            case 0: timeM = -1; break; // O'CLOCK
            case 1: timeM = R.string.fuzzy_one; break;
            case 2: timeM = R.string.fuzzy_two; break;
            case 3: timeM = R.string.fuzzy_three; break;
            case 4: timeM = R.string.fuzzy_four; break;
            case 5: timeM = R.string.fuzzy_five; break;
            case 6: timeM = R.string.fuzzy_six; break;
            case 7: timeM = R.string.fuzzy_seven; break;
            case 8: timeM = R.string.fuzzy_eight; break;
            case 9: timeM = R.string.fuzzy_nine; break;
            case 10: timeM = R.string.fuzzy_ten; break;
            case 11: timeM = R.string.fuzzy_eleven; break;
            case 12: timeM = R.string.fuzzy_twelve; break;
            case 13: timeM = R.string.fuzzy_thirteen; break;
            case 14: timeM = R.string.fuzzy_fourteen; break;
            case 15: timeM = R.string.fuzzy_quarter; break;
            case 16: timeM = R.string.fuzzy_sixteen; break;
            case 17: timeM = R.string.fuzzy_seventeen; break;
            case 18: timeM = R.string.fuzzy_eighteen; break;
            case 19: timeM = R.string.fuzzy_nineteen; break;
            case 20: timeM = R.string.fuzzy_twenty; break;
            case 21: timeM = R.string.fuzzy_twenty_one; break;
            case 22: timeM = R.string.fuzzy_twenty_two; break;
            case 23: timeM = R.string.fuzzy_twenty_three; break;
            case 24: timeM = R.string.fuzzy_twenty_four; break;
            case 25: timeM = R.string.fuzzy_twenty_five; break;
            case 26: timeM = R.string.fuzzy_twenty_six; break;
            case 27: timeM = R.string.fuzzy_twenty_seven; break;
            case 28: timeM = R.string.fuzzy_twenty_eight; break;
            case 29: timeM = R.string.fuzzy_twenty_nine; break;
            case 30: timeM = R.string.fuzzy_half; break;
            case 31: timeM = R.string.fuzzy_twenty_nine; break;
            case 32: timeM = R.string.fuzzy_twenty_eight; break;
            case 33: timeM = R.string.fuzzy_twenty_seven; break;
            case 34: timeM = R.string.fuzzy_twenty_six; break;
            case 35: timeM = R.string.fuzzy_twenty_five; break;
            case 36: timeM = R.string.fuzzy_twenty_four; break;
            case 37: timeM = R.string.fuzzy_twenty_three; break;
            case 38: timeM = R.string.fuzzy_twenty_two; break;
            case 39: timeM = R.string.fuzzy_twenty_one; break;
            case 40: timeM = R.string.fuzzy_twenty; break;
            case 41: timeM = R.string.fuzzy_nineteen; break;
            case 42: timeM = R.string.fuzzy_eighteen; break;
            case 43: timeM = R.string.fuzzy_seventeen; break;
            case 44: timeM = R.string.fuzzy_sixteen; break;
            case 45: timeM = R.string.fuzzy_quarter; break;
            case 46: timeM = R.string.fuzzy_fourteen; break;
            case 47: timeM = R.string.fuzzy_thirteen; break;
            case 48: timeM = R.string.fuzzy_twelve; break;
            case 49: timeM = R.string.fuzzy_eleven; break;
            case 50: timeM = R.string.fuzzy_ten; break;
            case 51: timeM = R.string.fuzzy_nine; break;
            case 52: timeM = R.string.fuzzy_eight; break;
            case 53: timeM = R.string.fuzzy_seven; break;
            case 54: timeM = R.string.fuzzy_six; break;
            case 55: timeM = R.string.fuzzy_five; break;
            case 56: timeM = R.string.fuzzy_four; break;
            case 57: timeM = R.string.fuzzy_three; break;
            case 58: timeM = R.string.fuzzy_two; break;
            case 59: timeM = R.string.fuzzy_one; break;
            default: timeM = -1; break; //Err
        }

        // Adjust for next hour
        if (minutes > 30) {
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
                if (minutes > 30) {
                    timeH = (mCalendar.get(Calendar.AM_PM) == Calendar.PM) ?
                            R.string.fuzzy_midnight : R.string.fuzzy_noon;
                } else {
                    timeH = (mCalendar.get(Calendar.AM_PM) == Calendar.AM) ?
                            R.string.fuzzy_midnight : R.string.fuzzy_noon;
                }
            }
        }

        // Final shuffle
        if (minutes == 0) {
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
        } else if (minutes <= 30) {
            separator = R.string.fuzzy_past;
        } else /* minutes > 30 */ {
            separator = R.string.fuzzy_to;
        }

        return new FuzzyLogic.FuzzyTime(timeM, timeH, separator);
    }
}
