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

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import hugo.weaving.DebugLog;

public class FuzzyDreamSettings extends FuzzySettings {

    @DebugLog
    @Override
    protected void onStart() {
        mFuzzyPrefs = new FuzzyPrefs(this);
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("firstRun", true)) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.first_run))
                    .setNeutralButton(getString(android.R.string.ok), null)
                    .show();
            prefs.edit().putBoolean("firstRun", false).apply();
        }
    }

}
