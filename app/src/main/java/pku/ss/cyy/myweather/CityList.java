package pku.ss.cyy.myweather;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pku.ss.cyy.bean.City;
import pku.ss.cyy.db.CityDB;


public class CityList extends Activity implements View.OnClickListener {
    private ImageView backBtn, addBtn;
    private ListView cityListLv;
    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private CityDB mCityDB;
    private List<City> mList;
    private List<Map<String, Object>> mCityList = new ArrayList<Map<String, Object>>();
//    private String[] cityList;
    private Set<String> set;
    private SimpleAdapter simpleAdapter;
    public static final int RESULT_ADD_CITY = 1;
    public static boolean changed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        mCityDB = openCityDB();

        initView();
    }

    private void initView() {
        backBtn = (ImageView) findViewById(R.id.title_back);
        addBtn = (ImageView) findViewById(R.id.title_add_city);

        backBtn.setOnClickListener(this);
        addBtn.setOnClickListener(this);

//        cityList = getCityCodeList();


        cityListLv = (ListView) findViewById(R.id.city_list);
        simpleAdapter = new SimpleAdapter(CityList.this,prepareCityList(),R.layout.city_item,
                new String[] { "cityFull" }, new int[] { R.id.city_name });
        cityListLv.setAdapter(simpleAdapter);

        cityListLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String cityCode = mCityList.get(position).get("number").toString();
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(CityList.this);
                builder.setMessage("确认要删除该城市吗？")
                    .setCancelable(true)
                    .setPositiveButton("确认",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    changed = true;
                                    set = new HashSet<String>(set);
                                    set.remove(cityCode);
                                    editor.putStringSet("cityList", set);
                                    editor.commit();
                                    mCityList.clear();
                                    mCityList = prepareCityList();
                                    simpleAdapter.notifyDataSetChanged();
                                }
                            })
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                }
                            });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
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

    private CityDB openCityDB() {
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath()
                + File.separator + getPackageName()
                + File.separator + "databases"
                + File.separator;
        return new CityDB(this, path);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private String[] getCityCodeList() {
        set = sharedPreferences.getStringSet("cityList",null);
        String obj[] = set.toArray(new String[]{});
        Arrays.sort(obj);
        return obj;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == resultCode) {
            switch (requestCode) {
                case RESULT_ADD_CITY:
                    changed = true;
                    String code = data.getStringExtra("code");
                    set = new HashSet<String>(set);
                    set.add(code);
                    editor.putStringSet("cityList", set);
                    editor.commit();
                    mCityList.clear();
                    mCityList = prepareCityList();
                    simpleAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_back:
                finish();
                break;
            case R.id.title_add_city:
                Intent intent = new Intent(this,SelectCity.class);
                startActivityForResult(intent, RESULT_ADD_CITY);
                break;
        }
    }
}
