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

public interface IFuzzyClockView {
    public void updateColors();

    public void setMinuteColor(int res);

    public void setHourColor(int res);

    public void setSeparatorColor(int res);

    public void setFontSize(float size);

    public void loadPreferences(FuzzyPrefs prefs);

    public void setLive(boolean live);

    public void updateTime(int hour, int minute);

    public boolean is24HourFormat();

    public void registerCallback(TimeChangedListener l);

    public interface TimeChangedListener {
        public void onTimeChanged();
    }
}
