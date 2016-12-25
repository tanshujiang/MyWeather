package pku.ss.cyy.myweather;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pku.ss.cyy.bean.City;
import pku.ss.cyy.db.CityDB;


/**
 * The configuration screen for the {@link WeatherWidget WeatherWidget} AppWidget.
 */
public class WeatherWidgetConfigureActivity extends Activity {

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
//    EditText mAppWidgetText;
    private CityDB mCityDB;
    ListView cityLv;
    private SimpleAdapter simpleAdapter;
    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private List<Map<String, Object>> mCityList = new ArrayList<Map<String, Object>>();
    private Set<String> set;

    private static final String PREFS_NAME = "pku.ss.cyy.myweather.WeatherWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";

    public WeatherWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.weather_widget_configure);
//        mAppWidgetText = (EditText) findViewById(R.id.appwidget_text);

        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        mCityDB = openCityDB();
        cityLv = (ListView) findViewById(R.id.city_list);
        simpleAdapter = new SimpleAdapter(WeatherWidgetConfigureActivity.this,prepareCityList(),R.layout.city_item,
                new String[] { "cityFull" }, new int[] { R.id.city_name });
        cityLv.setAdapter(simpleAdapter);
//        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        cityLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String cityCode = mCityList.get(position).get("number").toString();
                saveTitlePref(WeatherWidgetConfigureActivity.this, mAppWidgetId, cityCode);

                // It is the responsibility of the configuration activity to update the app widget
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(WeatherWidgetConfigureActivity.this);
                WeatherWidget.updateAppWidget(WeatherWidgetConfigureActivity.this, appWidgetManager, mAppWidgetId);

                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

//        mAppWidgetText.setText(loadTitlePref(WeatherWidgetConfigureActivity.this, mAppWidgetId));
    }

    private List<Map<String, Object>> prepareCityList() {
        String[] cityList = getCityCodeList();
        editor.putString("remindCode",cityList[0]);
        editor.commit();
        Map<String, Object> map;
        for (String cityCode : cityList) {
            City city = mCityDB.searchCityById(cityCode);
            map = new HashMap<String, Object>();
            map.put("cityFull",city.getProvince() + " - " + city.getCity());
            map.put("province",city.getProvince());
            map.put("city",city.getCity());
            map.put("number",city.getNumber());
            mCityList.add(map);
        }
        return mCityList;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private String[] getCityCodeList() {
        set = sharedPreferences.getStringSet("cityList",null);
        String obj[] = set.toArray(new String[]{});
        Arrays.sort(obj);
        return obj;
    }

    private CityDB openCityDB() {
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath()
                + File.separator + getPackageName()
                + File.separator + "databases"
                + File.separator;
        return new CityDB(this, path);
    }

/*    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = WeatherWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            String widgetText = mAppWidgetText.getText().toString();
            saveTitlePref(context, mAppWidgetId, widgetText);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            WeatherWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };*/

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.commit();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return "101010100";
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.commit();
    }
}



