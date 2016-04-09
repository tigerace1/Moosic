package com.example.chengen.mupetune;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CreateRoom extends AppCompatActivity {
    private EditText roomname;
    private EditText roompass;
    private String groupID;
    private final String url = "http://192.168.0.115:8080/";
    private boolean isCreate,isget;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);
        roomname = (EditText)findViewById(R.id.etRoomName);
        roomname.setHint("Room Name");
        roompass = (EditText)findViewById(R.id.etRoomPassword);
        roompass.setHint("Password");
        final Button create = (Button) findViewById(R.id.btnCreateRoom);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //upload room info
                create.setClickable(false);
                isget=false;
                isCreate=false;
                SharedPreferences userInfo = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                final String username = userInfo.getString("username", "");
                SharedPreferences myGroup = getSharedPreferences("myGroup", Context.MODE_PRIVATE);
                final SharedPreferences.Editor myGroupEditor = myGroup.edit();
                myGroupEditor.putString("creator", username);
                myGroupEditor.putString("roomName", roomname.getText().toString());
                myGroupEditor.putString("roomPass", roompass.getText().toString());
                Thread t1 = new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        myGroupEditor.putString("groupID", getGroupID(username));
                    }
                };
                t1.start();
                Thread t = new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        makeARoom(username, roomname.getText().toString(), roompass.getText().toString());
                    }
                };
                t.start();
                try {
                    t1.join();
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(!isget||!isCreate) {
                    Toast.makeText(CreateRoom.this, "Cannot create this room", Toast.LENGTH_LONG).show();
                    create.setClickable(true);
                }else {
                    myGroupEditor.apply();
                    startActivity(new Intent(CreateRoom.this, MoosicHostRoom.class));
                    finish();
                }
            }
        });
    }
    private void makeARoom(String creator, String roomName, String roomPassword){
        try {
            //add url here
            URL obj = new URL(url+"makeAGroup");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeUTF("creator=" + creator + "&roomName=" + roomName + "&roomPass=" + roomPassword);
            wr.flush();
            wr.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            isCreate=true;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private String getGroupID(String createdBy){
        try {
            URL obj = new URL(url + "getGroupID");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeUTF("createBy=" + createdBy);
            wr.flush();
            wr.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            String userInfo = response.toString();
            JSONObject JsonID = new JSONObject(userInfo);
            groupID = JsonID.getString("groupID");
            in.close();
            if(groupID!=null)
                isget=true;
            return groupID;
        }catch (Exception e){
            e.printStackTrace();
        }
        return groupID;
    }
}