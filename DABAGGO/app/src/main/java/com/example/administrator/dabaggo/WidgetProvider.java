package com.example.administrator.dabaggo;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Log.e("Widget", "onReceive");

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        ComponentName thisWidget = new ComponentName(context.getApplicationContext(), WidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        Log.e("onReceive", "" + appWidgetIds.length);

        for (int appWidgetId : appWidgetIds) {
            Log.e("onReceive app id", "" + appWidgetId);
            refresh(context, appWidgetId, appWidgetManager);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.i("TESTETST", "onUpdate");
        Log.i("TESTETST", "" + appWidgetIds.length);

        for (int appWidgetId : appWidgetIds) {
            Log.e("onUpdate app id", "" + appWidgetId);
            refresh(context, appWidgetId, appWidgetManager);
        }

    }


    @SuppressLint("ResourceAsColor")
    private void refresh(Context context, int appWidgetId, AppWidgetManager appWidgetManager) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("com.example.administrator.dabaggo.sharedPreferences", Context.MODE_PRIVATE);
        int size = sharedPreferences.getInt("status_size", 0);

        String result = "";
        String separator = "";
        String source = sharedPreferences.getString("txt_lang", "");
        for (int i = 0; i < size; i++) {
            result += separator + sharedPreferences.getString("status_lang_" + i, "")
                    + "  " + sharedPreferences.getString("status_cnt_" + i, "");
            separator = "\n";
        }
        Log.e("result", result + "");

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_item);
        remoteViews.setOnClickPendingIntent(R.id.layout, pendingIntent);


        //  remoteViews.setTextColor(R.id.widget_txt_cnt, R.color.colorMainGreen);
        remoteViews.setTextViewText(R.id.widget_txt_lang, source);
        remoteViews.setTextViewText(R.id.widget_txt_cnt, result);


        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

        Log.e("refresh", "");
    }


    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }
}