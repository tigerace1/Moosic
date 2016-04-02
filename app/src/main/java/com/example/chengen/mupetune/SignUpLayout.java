package com.example.chengen.mupetune;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignUpLayout extends AppCompatActivity implements View.OnClickListener{
    private EditText password,rePass,first,last,email,user;
    private Button signup;
    private boolean success = true;
    private String args;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_layout);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        first = (EditText) findViewById(R.id.etfisrtName);
        last = (EditText) findViewById(R.id.etLastName);
        email = (EditText) findViewById(R.id.etEmail);
        user = (EditText) findViewById(R.id.etUserName);
        password = (EditText)findViewById(R.id.etPassword);
        rePass = (EditText)findViewById(R.id.etRePassword);
        signup = (Button) findViewById(R.id.btnSign);
        first.setHint("Your first name");
        last.setHint("Your last name");
        email.setHint("Your email");
        user.setHint("Your username");
        password.setHint("Your password");
        rePass.setHint("Your password again");
        signup.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSign:
                if (!password.getText().toString().equals(rePass.getText().toString())) {
                    rePass.setHint("Your passwords do not match");
                    rePass.setHintTextColor(ContextCompat.getColor(getApplicationContext(), R.color.alert));
                }
                else if (first.getText().toString().equals("")) {
                    first.setHint("First name is a required field");
                    first.setHintTextColor(ContextCompat.getColor(getApplicationContext(), R.color.alert));
                }
                else if (last.getText().toString().equals("")) {
                    last.setHint("Last name is a required field");
                    last.setHintTextColor(ContextCompat.getColor(getApplicationContext(), R.color.alert));
                }
                else if (email.getText().toString().equals("")) {
                    email.setHint("Please type in your email address");
                    email.setHintTextColor(ContextCompat.getColor(getApplicationContext(), R.color.alert));
                }
                else if (user.getText().toString().equals("")) {
                    user.setHint("Your username here please");
                    user.setHintTextColor(ContextCompat.getColor(getApplicationContext(), R.color.alert));
                }else {
                    args = "firstname="+first.getText().toString()+"&lastname="+last.getText().toString()
                    +"&email="+email.getText().toString()+"&username="+user.getText().toString()+"&password="+password.getText().toString();
                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            postSignUp(args);
                        }
                    };
                    t.start();
                    signup.setClickable(false);
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (success) {
                        startActivity(new Intent(this, Tabs.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Sign up failed, User already exists", Toast.LENGTH_SHORT).show();
                        signup.setClickable(true);
                    }
                    break;
                }
        }
    }
    private void postSignUp(String urlParameters){
        try {
            //add url here
            URL obj = new URL("http://192.168.0.115:3000/signup");
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
            if(response.toString().equals("success")) {
                success = true;
                SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", user.getText().toString());
                editor.putString("firstName", first.getText().toString());
                editor.putString("lastName", last.getText().toString());
                editor.putString("email", email.getText().toString());
                editor.apply();
            }else{
                success = false;
            }
            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, LoginPage.class);
        startActivity(intent);
        finish();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, LoginPage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}