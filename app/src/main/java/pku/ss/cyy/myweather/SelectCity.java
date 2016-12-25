package pku.ss.cyy.myweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pku.ss.cyy.bean.City;
import pku.ss.cyy.db.CityDB;


public class SelectCity extends Activity implements View.OnClickListener {
    SharedPreferences sharedPreferences;
    private static final String TAG = "MyAPP";
    private ImageView mBackBtn;
    private TextView title;
    private EditText citySearch;
    private ListView mCityListLV;
    private List<City> mList;
    private CityDB mCityDB;
    private String cityCode, cityName;
    private List<Map<String, Object>> mCityList = new ArrayList<Map<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        mBackBtn = (ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);

        title = (TextView) findViewById(R.id.title_name);
//        title.setText("当前城市：" + sharedPreferences.getString("main_city_name", "北京"));
        title.setText("请选择城市");

        mCityDB = openCityDB();
        mCityListLV = (ListView)findViewById(R.id.city_list);
        SimpleAdapter simpleAdapter = new SimpleAdapter(SelectCity.this,prepareCityList(),R.layout.city_item,
                new String[] { "cityFull" }, new int[] { R.id.city_name });
        mCityListLV.setAdapter(simpleAdapter);

        mCityListLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cityCode = mCityList.get(position).get("number").toString();
                Intent intent = getIntent();
                intent.putExtra("code", cityCode);
                setResult(CityList.RESULT_ADD_CITY,intent);
                finish();
            }
        });

        mCityListLV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                citySearch.setFocusableInTouchMode(true);
                return false;
            }
        });

        citySearch = (EditText) findViewById(R.id.city_search);
        citySearch.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private int editStart;
            private int editEnd;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                temp = charSequence;
                Log.d("myapp", "beforeTextChanged:" + temp);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                //mTextView.setText(charSequence);
                Log.d("myapp", "onTextChanged:" + charSequence);
                mCityList.clear();
                SimpleAdapter simpleAdapter = new SimpleAdapter(SelectCity.this, prepareCityList(charSequence.toString()), R.layout.city_item,
                        new String[]{"cityFull"}, new int[]{R.id.city_name});
                mCityListLV.setAdapter(simpleAdapter);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (temp.length() > 10) {
                    Toast.makeText(SelectCity.this,
                            "你输⼊的字数已经超过了限制！ ", Toast.LENGTH_SHORT)
                            .show();
                    citySearch.setText(citySearch.getText().toString().substring(0, 10));
                    citySearch.setSelection(10);
                }
                Log.d("myapp", "afterTextChanged:");
            }
        });
    }

    private CityDB openCityDB() {
        String name= CityDB.CITY_DB_NAME;
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath()
                + File.separator + getPackageName()
                + File.separator + "databases"
                + File.separator;
        File FileDir = new File(path);//仅创建路径的File对象
        if(!FileDir.exists()){
            FileDir.mkdir();//如果路径不存在就先创建路径
        }
        File db = new File(FileDir,name);
//        File db = new File(path);
        Log.d(TAG, path);
        if (!db.exists()) {
            Log.i(TAG,"db is not exits");
            try {
                InputStream is = getAssets().open("city.db");
                FileOutputStream fos = new FileOutputStream(db);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    fos.flush();
                }
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(this, path);
    }

    private List<Map<String, Object>> prepareCityList() {
        Map<String, Object> map;
        mList = mCityDB.getAllCityByName();
        for (City city : mList) {
            map = new HashMap<String, Object>();
            map.put("cityFull",city.getProvince() + " - " + city.getCity());
            map.put("province",city.getProvince());
            map.put("city",city.getCity());
            map.put("number",city.getNumber());
            mCityList.add(map);
        }
        return mCityList;
    }
    private List<Map<String, Object>> prepareCityList(String cityName) {
        Map<String, Object> map;
        mList = mCityDB.searchCityByName(cityName);
        for (City city : mList) {
            map = new HashMap<String, Object>();
            map.put("cityFull",city.getProvince() + " - " + city.getCity());
            map.put("province",city.getProvince());
            map.put("city",city.getCity());
            map.put("number",city.getNumber());
            mCityList.add(map);
        }
        return mCityList;
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.title_back) {
            finish();
        }
    }
}
