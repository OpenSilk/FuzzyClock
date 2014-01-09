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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import hugo.weaving.DebugLog;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

public class FuzzyWidgetSettings extends FuzzySettings {

    private int mAppWidgetId = INVALID_APPWIDGET_ID;
    private final Intent mResult = new Intent();

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID);
        }
        mResult.putExtra(EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_CANCELED, mResult);
    }

    @DebugLog
    @Override
    protected void onStart() {
        super.onStart();
        mFuzzyPrefs = new FuzzyPrefs(this, mAppWidgetId);
    }

    @DebugLog
    @Override
    public void onClick(View v) {
        if (v == mButtonDone) {
            mFuzzyPrefs.save();
            // Force refresh;
            startService(new Intent("not_null", null, this, FuzzyWidgetService.class));
            setResult(RESULT_OK, mResult);
        }
        super.onClick(v);
    }
}
