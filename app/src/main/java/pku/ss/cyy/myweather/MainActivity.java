package pku.ss.cyy.myweather;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.umeng.socialize.bean.RequestType;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.TencentWBSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.WeiXinShareContent;
import com.umeng.update.UmengUpdateAgent;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import pku.ss.cyy.bean.City;
import pku.ss.cyy.bean.TodayWeather;
import pku.ss.cyy.db.CityDB;
import pku.ss.cyy.receiver.LockReceiver;
import pku.ss.cyy.receiver.RemindReceiver;
import pku.ss.cyy.util.NetUtil;


public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private static final int UPDATE_TODAY_WEATHER = 1;
    private static final int UPDATE_NO_DATA = 2;
    private static final int UPDATE_LOCATION = 3;
    private ImageView mUpdateBtn, mCitySelect, mLocation, mShareBtn;
    private ProgressBar mUpdateProgress;
    private TextView titleCityTv;
    private ViewPager viewPager;
    private List<Fragment> fragmentList;
    private static final String TAG = "MyAPP";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String cityCode, cityName;
    public static final int RESULT_CITY = 1;
    private int day = 0;
    private CityDB mCityDB;
    private TodayWeather weather = null, todayWeather;

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private String province = "", city = "", district = "";
    private City locationCity;
    private String[] cityList, titleList;
    private List<String> tagList;

    private boolean isSwitch = false;
    static boolean isRegisterReceiver = false;
    private LockReceiver lockReceiver = new LockReceiver();

    // 首先在您的Activity中添加如下成员变量
    final UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share");
//// 设置分享内容
//    mController.setShareContent("友盟社会化组件（SDK）让移动应用快速整合社交分享功能，http://www.umeng.com/social");
//// 设置分享图片, 参数2为图片的url地址
//    mController.setShareMedia(new UMImage(getActivity(),
//    "http://www.umeng.com/images/pic/banner_module_social.png"));

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UmengUpdateAgent.update(this);
        setContentView(R.layout.activity_main);

        registerLockReceiver();
        setReminder(true);
        intiSocial();

        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数

        mUpdateProgress = (ProgressBar) findViewById(R.id.title_update_progress);
        mUpdateBtn = (ImageView)findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        cityCode = sharedPreferences.getString("main_city_code","101010100");
        editor = sharedPreferences.edit();
//        Set<String> set = new TreeSet<>();
//        Collections.addAll(set, cityList);
//        editor.putStringSet("cityList",set);
        editor.commit();
        cityList = getCityCodeList();

        mCitySelect = (ImageView)findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);

        mLocation = (ImageView)findViewById(R.id.title_location);
        mLocation.setOnClickListener(this);

        mShareBtn = (ImageView)findViewById(R.id.title_share);
        mShareBtn.setOnClickListener(this);

        mCityDB = openCityDB();

        Log.d("myWeather", cityCode);
        if(NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
            Log.d("MyWeather", "网络正常！");
//            queryWeatherCode(cityCode);
//            mUpdateBtn.setVisibility(View.VISIBLE);
//            mUpdateProgress.setVisibility(View.GONE);
        }
        else {
            Log.d("MyWeather", "网络错误！");
            mUpdateBtn.setVisibility(View.VISIBLE);
            mUpdateProgress.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(),"网络未连接！",Toast.LENGTH_LONG).show();
        }

        initView();
    }

    public void registerLockReceiver() {
        if (!isRegisterReceiver) {
            isRegisterReceiver = true;

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            registerReceiver(lockReceiver, filter);
        }
    }

    public void unRegisterLockReceiver() {
        if (isRegisterReceiver) {
            isRegisterReceiver = false;
            unregisterReceiver(lockReceiver);
        }
    }

    private void setReminder(boolean on) {
        AlarmManager am= (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pi= PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(this,RemindReceiver.class), 0);

        if(on){
            Calendar c=Calendar.getInstance();
            if (c.get(Calendar.HOUR_OF_DAY)>8) {
                c.add(Calendar.DATE,1);
            }
            c.set(Calendar.HOUR_OF_DAY,8);
            c.set(Calendar.MINUTE,0);
            c.set(Calendar.SECOND,0);
            am.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 24*3600*1000, pi);
        }
        else{
            am.cancel(pi);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_CITY:
                if (CityList.changed) {
                    cityList = getCityCodeList();
                    fragmentList = new ArrayList<>();
                    for (String cityCode : cityList) {
                        CityDetail city = new CityDetail();
                        Bundle bundle = new Bundle();
                        bundle.putString("cityCode", cityCode);
                        city.setArguments(bundle);
                        fragmentList.add(city);
                    }
                    pagerAdapter.notifyDataSetChanged();
                }
        }
//        /**使用SSO授权必须添加如下代码 */
//        UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode) ;
//        if(ssoHandler != null){
//            ssoHandler.authorizeCallBack(requestCode, resultCode, data);
//        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private String[] getCityCodeList() {
        String[] obj;
        Set<String> set = sharedPreferences.getStringSet("cityList", null);
        if (set!=null) {
            obj = set.toArray(new String[]{});
            Arrays.sort(obj);
        } else {
            obj = new String[]{"101010100"};
            set = new HashSet<>();
            Collections.addAll(set, obj);
            editor.putStringSet("cityList",set);
            editor.commit();
        }
        return obj;
    }

    private void intiSocial() {
        String appID = "wx4d240f1223159b98";
        String appSecret = "6320cd08c95f7dac86effd88918bddf6";
        // 添加微信平台
        UMWXHandler wxHandler = new UMWXHandler(MainActivity.this,appID,appSecret);
        wxHandler.addToSocialSDK();
        // 添加微信朋友圈
        UMWXHandler wxCircleHandler = new UMWXHandler(MainActivity.this,appID,appSecret);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();

        //参数1为当前Activity，参数2为开发者在QQ互联申请的APP ID，参数3为开发者在QQ互联申请的APP kEY.
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(MainActivity.this, "100424468",
                "c7394704798a158208a74ab60104f0ba");
        qqSsoHandler.addToSocialSDK();

        //参数1为当前Activity，参数2为开发者在QQ互联申请的APP ID，参数3为开发者在QQ互联申请的APP kEY.
        QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(MainActivity.this, "100424468",
                "c7394704798a158208a74ab60104f0ba");
        qZoneSsoHandler.addToSocialSDK();

        //设置新浪SSO handler
        mController.getConfig().setSsoHandler(new SinaSsoHandler());

        //设置腾讯微博SSO handler
        mController.getConfig().setSsoHandler(new TencentWBSsoHandler());
    }

    public CityDB openCityDB() {
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

    FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        @Override
        public Fragment getItem(int i) {
            return fragmentList.get(i);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
//            return super.instantiateItem(container, position);

            CityDetail fragment = (CityDetail) super.instantiateItem(container, position);
            fragment.update(cityList[position]);
            return fragment;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    };

    void initView() {
        titleCityTv = (TextView) findViewById(R.id.title_city_name);

        viewPager = (ViewPager) findViewById(R.id.city_info);

        fragmentList = new ArrayList<>();// 将要分页显示的View装入数组中
        for (String cityCode : cityList) {
            CityDetail city = new CityDetail();
            Bundle bundle = new Bundle();
            bundle.putString("cityCode", cityCode);
            city.setArguments(bundle);
            fragmentList.add(city);
        }
        viewPager.setAdapter(pagerAdapter);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                weather = ((CityDetail)fragmentList.get(i)).getWeather();

                isSwitch = true;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

//        titleCityTv.setText(sharedPreferences.getString("main_city_name_home", "北京天气"));
        titleCityTv.setText("天气预报");
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.title_update_btn) {
            Log.d("myWeather",cityCode);
            mUpdateBtn.setVisibility(View.INVISIBLE);
            mUpdateProgress.setVisibility(View.VISIBLE);
            if(NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
                Log.d("MyWeather", "网络正常！");
//                queryWeatherCode(cityCode);
            }
            else {
                Log.d("MyWeather", "网络错误！");
                mUpdateBtn.setVisibility(View.VISIBLE);
                mUpdateProgress.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),"网络未连接！",Toast.LENGTH_LONG).show();
            }
        }
        if(v.getId() == R.id.title_city_manager) {
            Intent intent = new Intent(getApplicationContext(),CityList.class);
            startActivityForResult(intent, RESULT_CITY);
//            UmengUpdateAgent.forceUpdate(MainActivity.this);
        }
        if(v.getId() == R.id.title_location) {
            InitLocation();
            mLocationClient.start();
        }
        if(v.getId() == R.id.title_share) {
            if (!isSwitch) {
                weather = ((CityDetail)fragmentList.get(0)).getWeather();
            }
            if (weather!=null) {
                String content = weather.getCity() + "今天" + weather.getType() + "，" + weather.getLow()
                        + "，" + weather.getHigh() + "，风力" + weather.getFengli() + "，湿度" + weather.getShidu();
                if (weather.getPm25()!=null) {
                    content += "，空气质量" + weather.getQuality();
                }
                content += "。";
                mController.setShareContent(content);
                mController.setAppWebSite("http://www.goto985.com");
                mController.openShare(MainActivity.this, false);
            } else {
                Toast.makeText(MainActivity.this,"该城市无天气详情，不能分享！",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void InitLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("gcj02");
        int span=1000;
        option.setScanSpan(span);
        option.setIsNeedAddress(true);
        option.setAddrType("all");
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        mLocationClient.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unRegisterLockReceiver();
        super.onDestroy();
    }

    //    @Override
//    public void OnWeatherListener(TodayWeather weather) {
//        this.weather = weather;
//    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
//            StringBuffer sb = new StringBuffer(256);
//            sb.append("time : ");
//            sb.append(location.getTime());
//            sb.append("\nerror code : ");
//            sb.append(location.getLocType());
//            sb.append("\nlatitude : ");
//            sb.append(location.getLatitude());
//            sb.append("\nlontitude : ");
//            sb.append(location.getLongitude());
//            sb.append("\nradius : ");
//            sb.append(location.getRadius());
//            if (location.getLocType() == BDLocation.TypeGpsLocation){
//                sb.append("\nspeed : ");
//                sb.append(location.getSpeed());
//                sb.append("\nsatellite : ");
//                sb.append(location.getSatelliteNumber());
//                sb.append("\ndirection : ");
//                sb.append("\naddr : ");
//                sb.append(location.getAddrStr());
//                sb.append(location.getDirection());
//            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
//                sb.append("\naddr : ");
//                sb.append(location.getAddrStr());
//                //��Ӫ����Ϣ
//                sb.append("\noperationers : ");
//                sb.append(location.getOperators());
//            }
//            Log.i("BaiduLocationApiDem", sb.toString());
//            Log.i("BaiduLocationApiDem", location.getLocType()+" "+location.getTime()+" "+location.getProvince()+" "+location.getCity()+" "+location.getDistrict());
            if (location.getLocType() == BDLocation.TypeGpsLocation || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                province = location.getProvince();
                city = location.getCity();
                district = location.getDistrict();
                Toast.makeText(MainActivity.this,"定位成功！",Toast.LENGTH_SHORT).show();
                mLocationClient.stop();
                Message msg = new Message();
                msg.what = UPDATE_LOCATION;
                handler.sendMessage(msg);
            }
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
//                    updateTodayWeather((TodayWeather)msg.obj);
                    break;
                case UPDATE_NO_DATA:
//                    updateTodayWeather();
                    break;
                case UPDATE_LOCATION:
                    if (matchCity()) {
                        editor.putString("main_city_code",locationCity.getNumber());
                        editor.putString("main_city_name",locationCity.getCity());
                        editor.commit();
                        if(NetUtil.getNetworkState(MainActivity.this) != NetUtil.NETWORK_NONE) {
//                            queryWeatherCode(locationCity.getNumber());
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"网络未连接！",Toast.LENGTH_LONG).show();
                        }
                        //updateTodayWeather();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private boolean matchCity() {
        province = province.substring(0,province.length()-1);
        district = district.substring(0,district.length()-1);
        City city1 = mCityDB.getCity(province,district);
        if (city1!=null) {
            locationCity = city1;
            return true;
        }
        return false;
    }

    //屏蔽后退键，双击退出
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            //return true;
            exitBy2Click();
        }
        //return super.onKeyDown(keyCode,event);
        return  false;
    }

    private static Boolean isExit = false;
    private void exitBy2Click() {
        Timer tExit = null;
        if (!isExit) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            finish();
            System.exit(0);
        }
    }
}
