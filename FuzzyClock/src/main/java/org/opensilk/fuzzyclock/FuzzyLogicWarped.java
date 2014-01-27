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

/**
 * The core logic here aims to warp your perception of time.
 *
 * The first half of the hour runs slow and creeps along, behind
 * the actual time. Giving you a few extra minutes complete your task,
 * effectively making you more productive.
 *
 * The second half of the hour races, skipping ahead... into the future.
 * Giving you some extra minutes to commute, getting you out the door
 * and where you need to be on time.
 */
public class FuzzyLogicWarped extends FuzzyLogic {

    public FuzzyLogicWarped() {
        super();
    }

    public boolean hasChanged() {
        return getMinutesState(mPrevMinutes) != getMinutesState(mMinutes)
                || mPrevHours != mHours;
    }

    private int getMinutesState(int minutes) {
        int minState;
        if        (minutes >= 56) { minState = 1;
        } else if (minutes >= 51) { minState = 2;
        } else if (minutes >= 46) { minState = 3;
        } else if (minutes >= 41) { minState = 4;
        } else if (minutes >= 36) { minState = 5;
        } else if (minutes == 35) { minState = 6;
        } else if (minutes >= 30) { minState = 7;
        } else if (minutes >= 25) { minState = 8;
        } else if (minutes >= 20) { minState = 9;
        } else if (minutes >= 15) { minState = 10;
        } else if (minutes >= 10) { minState = 11;
        } else if (minutes >= 5)  { minState = 12;
        } else                    { minState = 1;
        }
        return minState;
    }

    public long getNextIntervalMilli() {
        long nextMilli = 0;
        int minutes = mMinutes;
        int hours = mHours;
        if        (minutes >= 56) { hours += 1; minutes = 5;
        } else if (minutes >= 51) { minutes = 56;
        } else if (minutes >= 46) { minutes = 51;
        } else if (minutes >= 41) { minutes = 46;
        } else if (minutes >= 36) { minutes = 41;
        } else if (minutes == 35) { minutes = 36;
        } else if (minutes >= 30) { minutes = 35;
        } else if (minutes >= 25) { minutes = 30;
        } else if (minutes >= 20) { minutes = 25;
        } else if (minutes >= 15) { minutes = 20;
        } else if (minutes >= 10) { minutes = 15;
        } else if (minutes >= 5)  { minutes = 10;
        } else                    { minutes = 5;
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

        if        (minutes >= 56) { timeM = -1; // O'CLOCK
        } else if (minutes >= 51) { timeM = R.string.fuzzy_five;
        } else if (minutes >= 46) { timeM = R.string.fuzzy_ten;
        } else if (minutes >= 41) { timeM = R.string.fuzzy_quarter;
        } else if (minutes >= 36) { timeM = R.string.fuzzy_twenty;
        } else if (minutes == 35) { timeM = R.string.fuzzy_twenty_five;
        } else if (minutes >= 30) { timeM = R.string.fuzzy_half;
        } else if (minutes >= 25) { timeM = R.string.fuzzy_twenty_five;
        } else if (minutes >= 20) { timeM = R.string.fuzzy_twenty;
        } else if (minutes >= 15) { timeM = R.string.fuzzy_quarter;
        } else if (minutes >= 10) { timeM = R.string.fuzzy_ten;
        } else if (minutes >= 5)  { timeM = R.string.fuzzy_five;
        } else                    { timeM = -1; // O'CLOCK
        }

        // Adjust for next hour
        if (minutes >= 35) {
            if (m24HourFormat) {
                hours = (hours + 1) % 24;
            } else {
                hours = (hours + 1) % 12;
            }
        }

        switch (hours) {
            case 0:  timeH = R.string.fuzzy_twelve; break;
            case 1:  timeH = R.string.fuzzy_one; break;
            case 2:  timeH = R.string.fuzzy_two; break;
            case 3:  timeH = R.string.fuzzy_three; break;
            case 4:  timeH = R.string.fuzzy_four; break;
            case 5:  timeH = R.string.fuzzy_five; break;
            case 6:  timeH = R.string.fuzzy_six; break;
            case 7:  timeH = R.string.fuzzy_seven; break;
            case 8:  timeH = R.string.fuzzy_eight; break;
            case 9:  timeH = R.string.fuzzy_nine; break;
            case 10: timeH = R.string.fuzzy_ten; break;
            case 11: timeH = R.string.fuzzy_eleven; break;
            case 12: timeH = R.string.fuzzy_twelve; break;
            case 13: timeH = R.string.fuzzy_thirteen; break;
            case 14: timeH = R.string.fuzzy_fourteen; break;
            case 15: timeH = R.string.fuzzy_fifteen; break;
            case 16: timeH = R.string.fuzzy_sixteen; break;
            case 17: timeH = R.string.fuzzy_seventeen; break;
            case 18: timeH = R.string.fuzzy_eighteen; break;
            case 19: timeH = R.string.fuzzy_nineteen; break;
            case 20: timeH = R.string.fuzzy_twenty; break;
            case 21: timeH = R.string.fuzzy_twenty_one; break;
            case 22: timeH = R.string.fuzzy_twenty_two; break;
            case 23: timeH = R.string.fuzzy_twenty_three; break;
            case 24: timeH = R.string.fuzzy_twenty_four; break;
            default: timeH = -1; break;
        }

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
        if (minutes >= 56 || minutes < 5) {
            if (hours == 0 || hours == 12) {
                // Separator show noon/midnight
                separator = timeH;
                timeH = timeM = -1;
            } else {
                // put hour in minutes place
                timeM = timeH;
                timeH = R.string.fuzzy_oclock;
                separator = -1;
            }
        } else if (minutes >= 35) {
            separator = R.string.fuzzy_to;
        } else /* minutes >= 5 */ {
            separator = R.string.fuzzy_past;
        }

        return new FuzzyLogic.FuzzyTime(timeM, timeH, separator);
    }
}