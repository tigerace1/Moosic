package com.example.chengen.mupetune;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ImageView iv = (ImageView) findViewById(R.id.ivLoading);
        final Animation an = AnimationUtils.loadAnimation(getBaseContext(), R.anim.anim_loading);
        final Animation an2 = AnimationUtils.loadAnimation(getBaseContext(), R.anim.anim_load);
        iv.startAnimation(an);
        an.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd (Animation animation){
                SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                if (sharedPref.contains("username")) {
                    Intent intent = new Intent(MainActivity.this, Tabs.class).putExtra("count",0);
                    iv.startAnimation(an2);
                    startActivity(intent);
                    finish();
                }else{
                    Intent intent = new Intent(MainActivity.this, LoginPage.class);
                    iv.startAnimation(an2);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
