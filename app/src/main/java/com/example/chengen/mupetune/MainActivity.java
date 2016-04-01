package com.example.chengen.mupetune;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private boolean isLogin;
    private Thread t;
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
                SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                if (sharedPref.contains("username")) {
                    String username = sharedPref.getString("username", "");
                    String password = sharedPref.getString("password", "");
                    final String args = "username=" + username + "&password=" + password;
                    t = new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            postLogin(args);
                        }
                    };
                    t.start();
                }else{
                    Intent intent = new Intent(MainActivity.this, LoginPage.class);
                    iv.startAnimation(an2);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onAnimationEnd (Animation animation){
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isLogin) {
                    Intent intent = new Intent(MainActivity.this, Tabs.class);
                    startActivity(intent);
                    iv.startAnimation(an2);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MainActivity.this, LoginPage.class));
                    iv.startAnimation(an2);
                    finish();
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
    private  void postLogin(String urlParameters){
        try {
            //url here
            URL obj = new URL("");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeUTF(urlParameters);
            wr.flush();
            wr.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            isLogin = response.toString().equals("succees");
            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
