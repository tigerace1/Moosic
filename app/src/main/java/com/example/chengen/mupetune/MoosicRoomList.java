package com.example.chengen.mupetune;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MoosicRoomList extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private ListView roomslist;
    private MoosicRoomsAdapter moosicRoomsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final String url = "http://45.55.182.4:8080/";
    View v;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        v = inflater.inflate(R.layout.activity_moosic_room_list,container,false);
        roomslist = (ListView)v.findViewById(R.id.list_of_rooms);
        ImageButton create = (ImageButton) v.findViewById(R.id.iBCreate);
        moosicRoomsAdapter = new MoosicRoomsAdapter(getActivity(),R.layout.activity_moosic_rooms_adapter);
        Thread t = new Thread(){
            @Override
            public void run() {
                super.run();
                getRoomsFromDB();
            }
        };
        t.start();
        roomslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MoosicRoomsProvider moosicRoomsProvider = (MoosicRoomsProvider) moosicRoomsAdapter.getItem(position);
                String groupID = moosicRoomsProvider.getRoomID();
                String roomName = moosicRoomsProvider.getRoomName();
                String roomHost = moosicRoomsProvider.getRoomHost();
                SharedPreferences groupPref = getContext().getSharedPreferences("groupInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = groupPref.edit();
                editor.putString("groupID", groupID);
                editor.putString("roomName", roomName);
                editor.putString("roomHost", roomHost);
                editor.apply();
                startActivity(new Intent(MoosicRoomList.this.getActivity().getApplicationContext(), MoosicRoom.class));

            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createRoom = new Intent(MoosicRoomList.this.getActivity().getApplicationContext(), CreateRoom.class);
                getActivity().startActivity(createRoom);
                getActivity().finish();
            }
        });
        swipeRefreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorScheme(
                ContextCompat.getColor(getActivity().getApplicationContext(), android.R.color.holo_blue_bright),
                ContextCompat.getColor(getActivity().getApplicationContext(), android.R.color.holo_green_light),
                ContextCompat.getColor(getActivity().getApplicationContext(), android.R.color.holo_orange_light),
                ContextCompat.getColor(getActivity().getApplicationContext(), android.R.color.holo_red_light));
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return v;
    }
    private void getRoomsFromDB(){
        try {
            URL obj = new URL(url+"getAllGroups");
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
            for(int i = 0; i < jArray.length(); i++) {
                JSONObject json_obj = jArray.getJSONObject(i);
                String id = json_obj.getString("_id");
                String roomName = json_obj.getString("roomName");
                String roomHost = json_obj.getString("createdBy");
                MoosicRoomsProvider moosicRoomsProvider = new MoosicRoomsProvider(id, roomName, roomHost);
                moosicRoomsAdapter.add(moosicRoomsProvider);
            }
            roomslist.setAdapter(moosicRoomsAdapter);
            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void onRefresh() {
        Thread t= new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    URL obj = new URL(url+"getAllGroups");
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    moosicRoomsAdapter.clear();
                    String userInfo = response.toString();
                    JSONArray jArray = new JSONArray(userInfo);
                    for(int i = 0; i < jArray.length(); i++) {
                        JSONObject json_obj = jArray.getJSONObject(i);
                        String id = json_obj.getString("_id");
                        String roomName = json_obj.getString("roomName");
                        String roomHost = json_obj.getString("createdBy");
                        MoosicRoomsProvider moosicRoomsProvider = new MoosicRoomsProvider(id, roomName, roomHost);
                        moosicRoomsAdapter.add(moosicRoomsProvider);
                    }
                    moosicRoomsAdapter.notifyDataSetChanged();
                    in.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        swipeRefreshLayout.setRefreshing(false);
    }
}