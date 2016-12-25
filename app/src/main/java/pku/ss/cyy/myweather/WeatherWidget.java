package pku.ss.cyy.myweather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;

import pku.ss.cyy.app.MyApplication;
import pku.ss.cyy.bean.City;
import pku.ss.cyy.bean.TodayWeather;
import pku.ss.cyy.db.CityDB;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WeatherWidgetConfigureActivity WeatherWidgetConfigureActivity}
 */
public class WeatherWidget extends AppWidgetProvider {
    public static final String UPDATE_ACTION = "pku.ss.cyy.myweather.action.APPWIDGET_UPDATE";
    static Bundle bundle;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(UPDATE_ACTION)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName mComponentName = new ComponentName(context, WeatherWidget.class);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(mComponentName);
//                for (int i : ids) {
//                    Log.e("app", "id: " + i);
//                }
//
//                int[] appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
//                if (appWidgetIds != null && appWidgetIds.length > 0) {
//                    this.onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
//                }
                final int N = appWidgetIds.length;
                for (int i = 0; i < N; i++) {
                    CharSequence widgetText = WeatherWidgetConfigureActivity.loadTitlePref(context, appWidgetIds[i]);
                    String cityCode = widgetText.toString();
                    String targetCity = extras.getString("targetCity");
                    String city = extras.getString("city");
                    String type = extras.getString("type");
                    String temperature = extras.getString("temperature");
                    String quality = extras.getString("quality");
                    if (cityCode.equals(targetCity)) {
                        // Construct the RemoteViews object
                        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
                        views.setTextViewText(R.id.city, city);
                        views.setTextViewText(R.id.type, type);
                        views.setTextViewText(R.id.temperature, temperature + "℃");
                        views.setTextViewText(R.id.quality, "空气质量  " + (quality==null?"无":quality));
                        views.setImageViewResource(R.id.weather_img, getWeatherImage(type));
                        // Instruct the widget manager to update the widget
                        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetIds[i], views);
                    }
                }
            }
//            Toast.makeText(context, "receiver", Toast.LENGTH_SHORT).show();
//            bundle = intent.getExtras();
//            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(MyApplication.getContext());
//            int[] mAppWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
//            WeatherWidget.updateAppWidget(MyApplication.getContext(), appWidgetManager, mAppWidgetId);

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
////                    Bitmap srcbBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.clear_fan);
////                    for (int i = 0; i < 20; i++) {
////                        float degree = (i * 90)%360;
////                        mRemoteViews.setImageViewBitmap(R.id.imageView1, rotateBitmap(context, srcbBitmap, degree));
////                        Intent intentClick = new Intent();
////                        intentClick.setAction(CLICK_ACTION);
////                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentClick, 0);
////                        mRemoteViews.setOnClickPendingIntent(R.id.imageView1, pendingIntent);
////                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
////                        appWidgetManager.updateAppWidget(
////                                new ComponentName(context, MyAppWidgetProvider.class), mRemoteViews);
////                        try {
////                            Thread.sleep(100);
////                        } catch (InterruptedException e) {
////                            e.printStackTrace();
////                        }
////                    }
//
//                }
//            }).start();

        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            WeatherWidgetConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
        views.setOnClickPendingIntent(R.id.parent, pendingIntent);
        if (bundle!=null) {
            CharSequence widgetText = WeatherWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
            String cityCode = widgetText.toString();
            String targetCity = bundle.getString("targetCity");
            String city = bundle.getString("city");
            String type = bundle.getString("type");
            String temperature = bundle.getString("temperature");
            String quality = bundle.getString("quality");
            if (cityCode.equals(targetCity)) {
                // Construct the RemoteViews object
                views.setTextViewText(R.id.city, city);
                views.setTextViewText(R.id.type, type);
                views.setTextViewText(R.id.temperature, temperature);
                views.setTextViewText(R.id.quality, "空气质量  " + (quality == null ? "无" : quality));
                views.setImageViewResource(R.id.weather_img, getWeatherImage(quality));

            }
        }
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static CityDB openCityDB() {
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath()
                + File.separator + MyApplication.getContext().getPackageName()
                + File.separator + "databases"
                + File.separator;
        return new CityDB(MyApplication.getContext(), path);
    }

    private static int getWeatherImage(String type) {
        int id;
        switch (type) {
            case "晴":
                id = R.drawable.biz_plugin_weather_qing;
                break;
            case "阴":
                id = R.drawable.biz_plugin_weather_yin;
                break;
            case "雾":
            case "霾":
                id = R.drawable.biz_plugin_weather_wu;
                break;
            case "多云":
                id = R.drawable.biz_plugin_weather_duoyun;
                break;
            case "沙尘暴":
            case "扬沙":
            case "浮尘":
                id = R.drawable.biz_plugin_weather_shachenbao;
                break;
            case "雷阵雨":
                id = R.drawable.biz_plugin_weather_leizhenyu;
                break;
            case "雷阵雨冰雹":
                id = R.drawable.biz_plugin_weather_leizhenyubingbao;
                break;
            case "雨夹雪":
                id = R.drawable.biz_plugin_weather_yujiaxue;
                break;
            case "阵雪":
                id = R.drawable.biz_plugin_weather_zhenxue;
                break;
            case "小雪":
                id = R.drawable.biz_plugin_weather_xiaoxue;
                break;
            case "中雪":
                id = R.drawable.biz_plugin_weather_zhongxue;
                break;
            case "大雪":
                id = R.drawable.biz_plugin_weather_daxue;
                break;
            case "暴雪":
                id = R.drawable.biz_plugin_weather_baoxue;
                break;
            case "阵雨":
                id = R.drawable.biz_plugin_weather_zhenyu;
                break;
            case "小雨":
                id = R.drawable.biz_plugin_weather_xiaoyu;
                break;
            case "中雨":
                id = R.drawable.biz_plugin_weather_zhongyu;
                break;
            case "大雨":
                id = R.drawable.biz_plugin_weather_dayu;
                break;
            case "暴雨":
                id = R.drawable.biz_plugin_weather_baoyu;
                break;
            case "大暴雨":
                id = R.drawable.biz_plugin_weather_dabaoyu;
                break;
            case "特大暴雨":
                id = R.drawable.biz_plugin_weather_tedabaoyu;
                break;
            default:
                id = R.drawable.biz_plugin_weather_qing;
                break;
        }
        return id;
    }
}


