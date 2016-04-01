package com.example.chengen.mupetune;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginPage extends AppCompatActivity implements View.OnClickListener{
    private EditText user,password;
    private Button login;
    private TextView forget,signUp;
    private boolean isLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        user = (EditText)findViewById(R.id.etusername);
        password  =(EditText)findViewById(R.id.etpassword);
        login = (Button)findViewById(R.id.btnlogin);
        forget = (TextView)findViewById(R.id.tvforget);
        signUp = (TextView)findViewById(R.id.tvsignup);
        login.setOnClickListener(this);
        signUp.setOnClickListener(this);
        user.setHint("UserName/e-mail");
        password.setHint("Password");
        isLogin=true;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnlogin:
                String username = user.getText().toString();
                String password = user.getText().toString();
                final String args = "username="+username+"&password="+password;
                //need to put anything about network into a thread
                Thread t = new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        postLogin(args);
                    }
                };
                t.start();
                login.setClickable(false);
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(isLogin){
                    startActivity(new Intent(this,Tabs.class));
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(),"Login failed",Toast.LENGTH_LONG).show();
                    login.setClickable(true);
                }
                break;
            case R.id.tvforget:
                break;
            case R.id.tvsignup:
                startActivity(new Intent(this,SignUpLayout.class));
                finish();
                break;
        }
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
            if(response.toString().equals("succees")){
                isLogin=true;
                SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("username", user.getText().toString());
                editor.putString("password", password.getText().toString());
                editor.apply();
            }else {
                isLogin=false;
            }
            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
