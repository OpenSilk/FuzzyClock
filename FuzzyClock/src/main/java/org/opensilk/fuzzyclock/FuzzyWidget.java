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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import hugo.weaving.DebugLog;

public class FuzzyWidget extends AppWidgetProvider {

    private static final String TAG = FuzzyWidget.class.getSimpleName();
    private static final boolean LOGV = true;

    @DebugLog
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        pokeService(context);
    }

    @DebugLog
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int id: appWidgetIds) {
            new FuzzyPrefs(context, id).remove();
        }
        pokeService(context);
    }

    @DebugLog
    @Override
    public void onDisabled(Context context) {
    }

    @DebugLog
    private void pokeService(Context context) {
        context.startService(new Intent(context, FuzzyWidgetService.class));
    }
}
