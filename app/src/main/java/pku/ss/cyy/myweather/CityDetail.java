package pku.ss.cyy.myweather;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import pku.ss.cyy.app.MyApplication;
import pku.ss.cyy.bean.TodayWeather;
import pku.ss.cyy.db.CityDB;
import pku.ss.cyy.util.NetUtil;


public class CityDetail extends Fragment {
    public static final String UPDATE_ACTION = "pku.ss.cyy.myweather.action.APPWIDGET_UPDATE";
    private Context context;
    private View view;
    private LayoutInflater inflater;
    private static final int UPDATE_TODAY_WEATHER = 1;
    private static final int UPDATE_NO_DATA = 2;
    private static final int UPDATE_LOCATION = 3;
    private TextView cityTv = null, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, temperatureTv,
            climateTv, windTv;
    private ImageView weatherImg, pmImg, indexOne, indexTwo;
    private ViewPager viewPager;
    private View view1, view2;
    private List<View> viewList;
    private String[] date = new String[7];
    private String[] type = new String[7];
    private String[] temperatureLow = new String[7];
    private String[] temperatureHigh = new String[7];
    private String[] wind = new String[7];
    private int[] dateTvs, typeTvs, temperatureTvs, windTvs, weatherIvs;
    private static final String TAG = "MyAPP";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String cityCode, cityName;
    public static final int RESULT_CITY = 1;
    private int day = 0;
    private CityDB mCityDB;
    private TodayWeather weather = null;
    private String remindCode;

//    OnWeatherListener mListener;

    public CityDetail() {
        // Required empty public constructor
    }

    public TodayWeather getWeather() {
        return weather;
    }

    private View findViewById(int id) {
        return view.findViewById(id);
    }

//    public interface OnWeatherListener {
//        public void OnWeatherListener (TodayWeather weather);
//    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mListener = (OnWeatherListener ) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + "must implement OnGridViewSelectedListener");
//        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather)msg.obj);
                    TodayWeather todayWeather = (TodayWeather)msg.obj;
                    Toast.makeText(MyApplication.getContext(),"更新成功！",Toast.LENGTH_SHORT).show();
//                    todayWeather = ((WeatherDetail)fragmentList.get(0)).getWeather();
                    if (todayWeather!=null && cityCode.equals(remindCode)) {
                        editor.putString("remind",todayWeather.getCity()+":"+todayWeather.getType()+","
                                +todayWeather.getLow()+"~"+todayWeather.getHigh());
                        editor.putString("city",todayWeather.getCity());
                        editor.putString("type",todayWeather.getType());
                        editor.putString("temperature",todayWeather.getWendu());
                        editor.putString("quality",todayWeather.getQuality());
                        editor.commit();
                    }
                    Intent intent = new Intent();
                    intent.setAction(UPDATE_ACTION);
                    Bundle bundle = new Bundle();
                    bundle.putIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS,new int[] {23});
                    bundle.putString("targetCity",cityCode);
                    bundle.putString("city", todayWeather.getCity());
                    bundle.putString("type", todayWeather.getType());
                    bundle.putString("temperature", todayWeather.getWendu());
                    bundle.putString("quality", todayWeather.getQuality());
                    intent.putExtras(bundle);
                    MyApplication.getContext().sendBroadcast(intent);
//                    mListener.OnWeatherListener((TodayWeather)msg.obj);
                    break;
                case UPDATE_NO_DATA:
                    updateTodayWeather();
                    Toast.makeText(MyApplication.getContext(),"更新失败：无数据！",Toast.LENGTH_SHORT).show();
                    break;
                case UPDATE_LOCATION:
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        SharedPreferences sp = MyApplication.getContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        editor = sp.edit();
        remindCode = sp.getString("remindCode","101010100");
        this.inflater = inflater;
        view = inflater.inflate(R.layout.fragment_city_detail, container, false);
        initView();
        initViewPager();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (weather == null) {
            updateTodayWeather();
        } else {
            updateTodayWeather(weather);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        cityCode = bundle.getString("cityCode");

    }

    void initView() {
        cityTv = (TextView)view.findViewById(R.id.city);
        timeTv = (TextView)view.findViewById(R.id.publish_time);
        humidityTv = (TextView)view.findViewById(R.id.humidity);
        weekTv = (TextView)view.findViewById(R.id.week_today);
        pmDataTv = (TextView)view.findViewById(R.id.pm_data);
        pmQualityTv = (TextView)view.findViewById(R.id.pm2_5_quality);
        temperatureTv = (TextView)view.findViewById(R.id.temperature);
        climateTv = (TextView)view.findViewById(R.id.climate);
        windTv = (TextView)view.findViewById(R.id.wind);
        pmImg = (ImageView)view.findViewById(R.id.pm2_5_img);
        weatherImg = (ImageView)view.findViewById(R.id.weather_img);

        viewPager = (ViewPager) view.findViewById(R.id.six_day_weather);
        indexOne = (ImageView) view.findViewById(R.id.index_one);
        indexTwo = (ImageView) view.findViewById(R.id.index_two);
        LayoutInflater lf = LayoutInflater.from(inflater.getContext());
        view1 = lf.inflate(R.layout.weather_first_page, null);
        view2 = lf.inflate(R.layout.weather_second_page, null);
        viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
        viewList.add(view1);
        viewList.add(view2);
        day = 0;

        dateTvs = new int[] { R.id.date1, R.id.date2, R.id.date3, R.id.date4, R.id.date5, R.id.date6 };
        typeTvs = new int[] { R.id.type1, R.id.type2, R.id.type3, R.id.type4, R.id.type5, R.id.type6 };
        temperatureTvs = new int[] {R.id.temperature1, R.id.temperature2, R.id.temperature3,
                R.id.temperature4, R.id.temperature5, R.id.temperature6 };
        windTvs = new int[] { R.id.wind1, R.id.wind2, R.id.wind3, R.id.wind4, R.id.wind5, R.id.wind6 };
        weatherIvs = new int[] { R.id.weather_img1, R.id.weather_img2, R.id.weather_img3,
                R.id.weather_img4, R.id.weather_img5, R.id.weather_img6 };

//        cityTv.setText("N/A");
//        timeTv.setText("N/A");
//        humidityTv.setText("N/A");
//        pmDataTv.setText("N/A");
//        pmQualityTv.setText("N/A");
//        weekTv.setText("N/A");
//        temperatureTv.setText("N/A");
//        climateTv.setText("N/A");
//        windTv.setText("N/A");
    }

    private void initViewPager() {
        viewPager = (ViewPager) view.findViewById(R.id.six_day_weather);
        view1 = inflater.inflate(R.layout.weather_first_page, null);
        view2 = inflater.inflate(R.layout.weather_second_page, null);
        viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
        viewList.add(view1);
        viewList.add(view2);
        day = 0;
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        indexOne.setImageResource(R.drawable.circle);
                        indexTwo.setImageResource(R.drawable.circle_gray);
                        break;
                    case 1:
                        indexOne.setImageResource(R.drawable.circle_gray);
                        indexTwo.setImageResource(R.drawable.circle);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
//                switch (end) {
//                    case 0:
//                        indexOne.setImageResource(R.drawable.circle);
//                        indexTwo.setImageResource(R.drawable.circle_gray);
//                        break;
//                    case 1:
//                        indexOne.setImageResource(R.drawable.circle_gray);
//                        indexTwo.setImageResource(R.drawable.circle);
//                        break;
//                }
            }
        });
    }

    PagerAdapter pagerAdapter = new PagerAdapter() {

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {

            return arg0 == arg1;
        }

        @Override
        public int getCount() {

            return viewList.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position,
                                Object object) {
            container.removeView(viewList.get(position));

        }

        @Override
        public int getItemPosition(Object object) {

            return super.getItemPosition(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(viewList.get(position));
            if (NetUtil.getNetworkState(MyApplication.getContext()) != NetUtil.NETWORK_NONE && date[0] != null) {
                for (int i = 0; i < 3; i++) {
                    ((TextView) findViewById(dateTvs[day])).setText(date[day]);
                    ((TextView) findViewById(typeTvs[day])).setText(type[day]);
                    ((TextView) findViewById(windTvs[day])).setText(wind[day]);
                    ((TextView) findViewById(temperatureTvs[day]))
                            .setText(temperatureLow[day].substring(3, temperatureLow[day].length()) + " ~ " +
                                    temperatureHigh[day].substring(3, temperatureHigh[day].length()));
                    setWeatherImage(type[day], (ImageView) findViewById(weatherIvs[day]));
                    day++;
                }
            }
            else {
                for (int i = 0; i < 3; i++) {
                    ((TextView) findViewById(dateTvs[day])).setText("N/A");
                    ((TextView) findViewById(typeTvs[day])).setText("N/A");
                    ((TextView) findViewById(windTvs[day])).setText("N/A");
                    ((TextView) findViewById(temperatureTvs[day])).setText("N/A");
                    setWeatherImage("晴", (ImageView) findViewById(weatherIvs[day]));
                    day++;
                }
            }
            return viewList.get(position);
        }

    };

    private void setWeatherImage(String type, ImageView imageView) {
        switch (type) {
            case "晴":
                imageView.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
            case "阴":
                imageView.setImageResource(R.drawable.biz_plugin_weather_yin);
                break;
            case "雾":
            case "霾":
                imageView.setImageResource(R.drawable.biz_plugin_weather_wu);
                break;
            case "多云":
                imageView.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                break;
            case "沙尘暴":
            case "扬沙":
            case "浮尘":
                imageView.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                break;
            case "雷阵雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                break;
            case "雷阵雨冰雹":
                imageView.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                break;
            case "雨夹雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                break;
            case "阵雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                break;
            case "小雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                break;
            case "中雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                break;
            case "大雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_daxue);
                break;
            case "暴雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                break;
            case "阵雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                break;
            case "小雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                break;
            case "中雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                break;
            case "大雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_dayu);
                break;
            case "暴雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                break;
            case "大暴雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                break;
            case "特大暴雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                break;
            default:
                imageView.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
        }
    }

    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(address);
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    if(httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = httpResponse.getEntity();

                        InputStream inputStream = entity.getContent();
                        inputStream = new GZIPInputStream(inputStream);

                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder response = new StringBuilder();
                        String str;
                        while ((str=reader.readLine()) != null) {
                            response.append(str);
                        }
                        String responseStr = response.toString();
                        TodayWeather todayWeather = parseXML(responseStr);
                        weather = todayWeather;
                        if (todayWeather != null) {
                            Message msg = new Message();
                            msg.what = UPDATE_TODAY_WEATHER;
                            msg.obj = todayWeather;
                            handler.sendMessage(msg);
                        }
                        else {
                            Message msg = new Message();
                            msg.what = UPDATE_NO_DATA;
                            handler.sendMessage(msg);
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private TodayWeather parseXML(String xmlData) {
        TodayWeather todayWeather = null;
        try {
            int fengxiangCount = 0;
            int fengliCount = 0;
            int dateCount = 0;
            int highCount = 0;
            int lowCount = 0;
            int typeCount = 0;
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));
            int eventType = xmlPullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
                        }
                        if(xmlPullParser.getName().equals("error")) {
                            eventType = xmlPullParser.next();
                            return null;
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli")) {
                                eventType = xmlPullParser.next();
                                if (fengliCount==0) {
                                    todayWeather.setFengli(xmlPullParser.getText());
                                }
                                if (fengliCount%2==1) {
                                    wind[fengliCount/2] = xmlPullParser.getText();
                                }
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date")) {
                                eventType = xmlPullParser.next();
                                if (dateCount==1) {
                                    todayWeather.setDate(xmlPullParser.getText());
                                }
                                date[dateCount] = xmlPullParser.getText();
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high")) {
                                eventType = xmlPullParser.next();
                                if (highCount==1) {
                                    todayWeather.setHigh(xmlPullParser.getText());
                                }
                                temperatureHigh[highCount] = xmlPullParser.getText();
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low")) {
                                eventType = xmlPullParser.next();
                                if (lowCount==1) {
                                    todayWeather.setLow(xmlPullParser.getText());
                                }
                                temperatureLow[lowCount] = xmlPullParser.getText();
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type")) {
                                eventType = xmlPullParser.next();
                                if (typeCount==2) {
                                    todayWeather.setType(xmlPullParser.getText());
                                }
                                if (typeCount%2==0) {
                                    type[typeCount/2] = xmlPullParser.getText();
                                }
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("fx_1")) {
                                eventType = xmlPullParser.next();
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fl_1")) {
                                eventType = xmlPullParser.next();
                                if (fengliCount%2==1) {
                                    wind[fengliCount/2] = xmlPullParser.getText();
                                }
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date_1")) {
                                eventType = xmlPullParser.next();
                                date[dateCount] = xmlPullParser.getText();
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high_1")) {
                                eventType = xmlPullParser.next();
                                temperatureHigh[highCount] = xmlPullParser.getText();
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low_1")) {
                                eventType = xmlPullParser.next();
                                temperatureLow[lowCount] = xmlPullParser.getText();
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type_1")) {
                                eventType = xmlPullParser.next();
                                if (typeCount%2==0) {
                                    type[typeCount/2] = xmlPullParser.getText();
                                }
                                typeCount++;
                            }
                        }

                        break;

                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return todayWeather;
    }

    void updateTodayWeather(TodayWeather todayWeather) {
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidityTv.setText("湿度：" + todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getLow().substring(3,todayWeather.getLow().length()) + " ~ " + todayWeather.getHigh().substring(3,todayWeather.getHigh().length()));
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力" + todayWeather.getFengli());
        setWeatherImage(todayWeather.getType(), weatherImg);

        initViewPager();

        if (todayWeather.getPm25()!=null) {
            int pm = Integer.parseInt(todayWeather.getPm25());
            if (pm > 200) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
            } else if (pm > 200) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
            } else if (pm > 150) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
            } else if (pm > 100) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
            } else if (pm > 50) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
            } else {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
            }
        }
        else {
            pmDataTv.setText("0");
            pmQualityTv.setText("无");
            pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
        }
    }

    void updateTodayWeather() {
        cityTv.setText("无数据");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");

        initViewPager();

        weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
        pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);

//        mUpdateBtn.setVisibility(View.VISIBLE);
//        mUpdateProgress.setVisibility(View.GONE);
    }

    public void update(String code) {
        if (!code.equals(cityCode)) {
            cityCode = code;
            if(NetUtil.getNetworkState(MyApplication.getContext()) != NetUtil.NETWORK_NONE) {
                Log.d("MyWeather", "网络正常！");
                queryWeatherCode(code);
//            mUpdateBtn.setVisibility(View.VISIBLE);
//            mUpdateProgress.setVisibility(View.GONE);
            }
            else {
                Log.d("MyWeather", "网络错误！");
//            mUpdateBtn.setVisibility(View.VISIBLE);
//            mUpdateProgress.setVisibility(View.GONE);
                Toast.makeText(MyApplication.getContext(), "网络未连接！", Toast.LENGTH_LONG).show();
            }
        }
    }

}
