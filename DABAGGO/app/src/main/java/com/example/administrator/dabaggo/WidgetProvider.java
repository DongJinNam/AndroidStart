package com.example.administrator.dabaggo;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.List;

public class WidgetProvider extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.i("TESTETST", "receiveBroadcast");
        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context
                    .getPackageName(), R.layout.dabaggo_widget);

            Intent intent = new Intent(context, MainActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            remoteViews.setOnClickPendingIntent(R.id.btn_search, pendingIntent);

            this.refresh(context, remoteViews);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

        }

    }


    private void refresh(Context context, RemoteViews remoteViews) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("com.example.administrator.dabaggo.sharedPreferences", Context.MODE_PRIVATE);

        int size = sharedPreferences.getInt("status_size", 0);

        List<LangVO> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new LangVO(0,sharedPreferences.getString("status_lang_" + i, null), sharedPreferences.getString("status_cnt_" + i, null), true));
        }

        Log.i("TESTETST", "ui " + size);
        for (int i = 0; i < size; i++) {
            // ui 업데이트
            Log.i("TESTETST", "-------------");
            Log.i("TESTETST", list.get(i).lang + " " + list.get(i).content);
//            Log.i("TESTETST idx :: ", sharedPreferences.getString("status_lang_" + i, null));
//            Log.i("TESTETST cnt :: ", sharedPreferences.getString("status_cnt_" + i, null));
        }
        //ArrayAdapter<LangVO> adapter = new ArrayAdapter<>(this, R.layout.widget_item, list);
        // remoteViews.setRemoteAdapter(adapter);
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
