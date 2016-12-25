package pku.ss.cyy.myweather;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import pku.ss.cyy.app.MyApplication;
import pku.ss.cyy.bean.TodayWeather;


public class LockScreen extends Activity implements View.OnClickListener, Runnable {
    Button unlockBtn;
    TextView cityTv, typeTv, temperatureTv, qualityTv, timeTv,dateTv;
    String city, type, temperature, quality;
    private final int UPDATE_TIME = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        SharedPreferences sp = MyApplication.getContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        city = sp.getString("city", null);
        type = sp.getString("type", null);
        temperature = sp.getString("temperature",null);
        quality = sp.getString("quality",null);

        unlockBtn = (Button) findViewById(R.id.button);
        unlockBtn.setOnClickListener(this);

        cityTv = (TextView) findViewById(R.id.city);
        typeTv = (TextView) findViewById(R.id.type);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        qualityTv = (TextView) findViewById(R.id.quality);
        timeTv = (TextView) findViewById(R.id.time);
        dateTv = (TextView) findViewById(R.id.date);

        cityTv.setText(city);
        typeTv.setText(type);
        temperatureTv.setText(temperature + "℃");
        qualityTv.setText("空气质量  " + quality);

        Calendar calendar = Calendar.getInstance();
        timeTv.setText(getTime(calendar));
        dateTv.setText(getDate(calendar));

        Thread thread = new Thread(this);
        thread.start();
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Calendar calendar = Calendar.getInstance();
            switch (msg.what) {
                case UPDATE_TIME:
                    timeTv.setText(getTime(calendar));
                    dateTv.setText(getDate(calendar));
                    break;
                default:
                    break;
            }
        }
    };

    private String getDate(Calendar calendar) {
        String date = (calendar.get(Calendar.MONTH)+1) + "月";
        date += (calendar.get(Calendar.DATE)) + "日";
        return date;
    }

    private String getTime(Calendar calendar) {
        String time = String.format("%02d", (calendar.get(Calendar.HOUR_OF_DAY))) + ":";
        time += String.format("%02d", (calendar.get(Calendar.MINUTE)));
        return time;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_HOME:
                return true;
            }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Message message = new Message();
            message.what = UPDATE_TIME;
            handler.sendMessage(message);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
