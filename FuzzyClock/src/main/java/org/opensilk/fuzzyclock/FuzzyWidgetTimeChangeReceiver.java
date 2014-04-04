package org.opensilk.fuzzyclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by drew on 4/4/14.
 */
public class FuzzyWidgetTimeChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())
                || Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
            context.startService(new Intent(context, FuzzyWidgetService.class));
        }
    }
}
