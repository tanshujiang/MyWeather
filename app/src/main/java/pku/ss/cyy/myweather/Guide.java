package pku.ss.cyy.myweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import pku.ss.cyy.adapter.GuideAdapter;


public class Guide extends Activity implements ViewPager.OnPageChangeListener{
    private ViewPager viewPager;
    private GuideAdapter guideAdapter;
    private List<View> views;
    private ImageView[] dots;
    private int[] ids = {R.id.imageView1, R.id.imageView2, R.id.imageView3};
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        initViews();
        initDots();
        button = (Button) views.get(2).findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Guide.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initDots() {
        dots = new ImageView[views.size()];
        for (int i=0; i<views.size(); i++) {
            dots[i] = (ImageView) findViewById(ids[i]);
        }
    }

    private void initViews() {
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<>();
        views.add(inflater.inflate(R.layout.guide1, null));
        views.add(inflater.inflate(R.layout.guide2, null));
        views.add(inflater.inflate(R.layout.guide3, null));
        guideAdapter = new GuideAdapter(views, this);
        viewPager = (ViewPager) findViewById(R.id.guide_pager);
        viewPager.setAdapter(guideAdapter);
        viewPager.setOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i=0; i<ids.length;i++) {
            if (i==position) {
                dots[i].setImageResource(R.drawable.circle);
            } else {
                dots[i].setImageResource(R.drawable.circle_gray);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
