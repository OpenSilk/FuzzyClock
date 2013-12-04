package org.opensilk.fuzzyclock;

import java.util.Calendar;

public class FuzzyLogic {

    private Calendar mCalendar;
    private int minutes, hours;
    private int previousMinutes, previousHours;
    private boolean m24HourFormat = false;

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
        m24HourFormat = is24hour;
    }

    public boolean getDateFormat() {
        return m24HourFormat;
    }

    public void updateTime() {
        previousMinutes = minutes;
        minutes = mCalendar.get(Calendar.MINUTE);
        previousHours = hours;
        hours = mCalendar.get(getDateFormat() ? Calendar.HOUR_OF_DAY : Calendar.HOUR);
    }

    public boolean hasChanged() {
        return getMinutesState(previousMinutes) != getMinutesState(minutes)
                && previousHours != hours;
    }

    private int getMinutesState(int minutes) {
        int minState;
        if        (minutes >= 56) { minState = 1;
        } else if (minutes >= 51) { minState = 2;
        } else if (minutes >= 46) { minState = 3;
        } else if (minutes >= 41) { minState = 4;
        } else if (minutes >= 36) { minState = 5;
        } else if (minutes >= 25) { minState = 6;
        } else if (minutes >= 20) { minState = 7;
        } else if (minutes >= 15) { minState = 8;
        } else if (minutes >= 10) { minState = 9;
        } else if (minutes >= 5)  { minState = 10;
        } else                    { minState = 1;
        }
        return minState;
    }

    public long getNextIntervalMilli() {
        long nextMilli = 0;
        if        (minutes >= 56) { hours += 1; minutes = 5;
        } else if (minutes >= 51) { minutes = 56;
        } else if (minutes >= 46) { minutes = 51;
        } else if (minutes >= 41) { minutes = 46;
        } else if (minutes >= 36) { minutes = 41;
        } else if (minutes >= 25) { minutes = 36;
        } else if (minutes >= 20) { minutes = 25;
        } else if (minutes >= 15) { minutes = 20;
        } else if (minutes >= 10) { minutes = 15;
        } else if (minutes >= 5)  { minutes = 10;
        } else                    { minutes = 5;
        }
        nextMilli += (hours - previousHours) * 60 * 60 * 1000;
        nextMilli += (minutes - previousMinutes) * 60 * 1000;
        return nextMilli;
    }

    public FuzzyTime getFuzzyTime() {
        int timeM, timeH, separator;

        if        (minutes >= 56) { timeM = -1; // O'CLOCK
        } else if (minutes >= 51) { timeM = R.string.fuzzy_five;
        } else if (minutes >= 46) { timeM = R.string.fuzzy_ten;
        } else if (minutes >= 41) { timeM = R.string.fuzzy_quarter;
        } else if (minutes >= 36) { timeM = R.string.fuzzy_twenty;
        } else if (minutes >= 25) { timeM = R.string.fuzzy_half;
        } else if (minutes >= 20) { timeM = R.string.fuzzy_twenty;
        } else if (minutes >= 15) { timeM = R.string.fuzzy_quarter;
        } else if (minutes >= 10) { timeM = R.string.fuzzy_ten;
        } else if (minutes >= 5)  { timeM = R.string.fuzzy_five;
        } else                    { timeM = -1; // O'CLOCK
        }

        // Adjust for next hour
        if (minutes >= 36) {
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
                if (minutes >= 36) {
                    timeH = (mCalendar.get(Calendar.AM_PM) == Calendar.PM) ?
                            R.string.fuzzy_midnight : R.string.fuzzy_noon;
                } else {
                    timeH = (mCalendar.get(Calendar.AM_PM) == Calendar.AM) ?
                            R.string.fuzzy_midnight : R.string.fuzzy_noon;
                }
            }
        }

        // Final shuffle
        if (minutes >= 56) {
            separator = (hours > 12) ? R.string.fuzzy_hundred : R.string.fuzzy_oclock;
            if (hours == 0 || (m24HourFormat && (hours == 12))) {
                separator = timeH;
                timeM = -1;
            } else {
                timeM = timeH;
            }
            timeH = -1;
        } else if (minutes >= 36) {
            separator = R.string.fuzzy_to;
        } else if (minutes >= 5) {
            separator = R.string.fuzzy_past;
        } else {
            separator = (hours > 12) ? R.string.fuzzy_hundred : R.string.fuzzy_oclock;
            if (hours == 0 || (m24HourFormat && (hours == 12))) {
                separator = timeH;
                timeM = -1;
            } else {
                timeM = timeH;
            }
            timeH = -1;
        }
        return new FuzzyTime(timeM, timeH, separator);
    }

    public static class FuzzyTime {
        public int minute, hour, separator;
        public FuzzyTime(int min, int hour, int sep) {
            this.minute = min;
            this.hour = hour;
            this.separator = sep;
        }
    }

}
