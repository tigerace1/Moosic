package com.example.chengen.mupetune;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CreateRoom extends Activity {
    private EditText roomname;
    private EditText roompass;
    private Button create;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomname = (EditText)findViewById(R.id.etRoomName);
        roompass = (EditText)findViewById(R.id.etRoomPassword);
        create = (Button)findViewById(R.id.btnCreateRoom);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //upload room info
                startActivity(new Intent(CreateRoom.this,Tabs.class).putExtra("count",0));
                finish();
            }
        });
    }
}
