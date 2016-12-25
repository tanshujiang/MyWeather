package pku.ss.cyy.myweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.umeng.update.UmengUpdateAgent;

import java.util.Timer;
import java.util.TimerTask;


public class StartPage extends Activity {
    private final int DELAY_TIME = 1000;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {

                sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                boolean isFirst = sharedPreferences.getBoolean("isFirst", true);
                if (isFirst) {
                    editor = sharedPreferences.edit();
                    editor.putBoolean("isFirst", false);
                    editor.commit();
                    Intent intent = new Intent(StartPage.this, Guide.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Intent intent = new Intent(StartPage.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, DELAY_TIME);
    }

    //屏蔽后退键，禁止退出
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }
}
