package org.opensilk.fuzzyclock;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class FuzzyWidget extends AppWidgetProvider {

    private static final String TAG = FuzzyWidget.class.getSimpleName();
    private static final boolean LOGV = true;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (LOGV) Log.v(TAG, "onUpdate");
        Intent i = new Intent(context, FuzzyWidgetService.class);
        i.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.startService(i);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        if (LOGV) Log.v(TAG, "onDeleted");
        Intent i = new Intent(context, FuzzyWidgetService.class);
        i.setAction(AppWidgetManager.ACTION_APPWIDGET_DELETED);
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.startService(i);
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        if (LOGV) Log.v(TAG, "onDisabled");
        context.stopService(new Intent(context, FuzzyWidgetService.class));
        super.onDisabled(context);
    }
}
