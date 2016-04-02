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

import org.json.JSONArray;
import org.json.JSONObject;

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
        user.setHint("Username");
        password.setHint("Password");
        isLogin=true;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnlogin:
                String username = user.getText().toString();
                String pass = password.getText().toString();
                final String args = "username="+username+"&password="+pass;
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
                    Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_LONG).show();
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
    private void postLogin(String urlParameters){
        try {
            //url here
            URL obj = new URL("http://192.168.0.115:3000/login");
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
            if(response.toString().equals("success")){
                isLogin=true;
                getUserInfo(user.getText().toString());
            }else {
                isLogin=false;
            }
            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void getUserInfo(String user){
        try {
            URL obj = new URL("http://192.168.0.115:3000/getUserInfo/" + user);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            String userInfo = response.toString();
            JSONArray jArray = new JSONArray(userInfo);
            JSONObject json_obj = jArray.getJSONObject(0);
            String username = json_obj.getString("username");
            String firstName = json_obj.getString("firstName");
            String lastName = json_obj.getString("lastName");
            String email = json_obj.getString("email");
            SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("username", username);
            editor.putString("firstName", firstName);
            editor.putString("lastName", lastName);
            editor.putString("email", email);
            editor.apply();
            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}