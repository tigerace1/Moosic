package com.example.chengen.mupetune;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

public class MoosicRoomList extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private ListView roomslist;
    private ImageButton create;
    private MoosicRoomsAdapter moosicRoomsAdapter;
    View v;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        v = inflater.inflate(R.layout.activity_moosic_room_list,container,false);
        roomslist = (ListView)v.findViewById(R.id.list_of_rooms);
        create = (ImageButton)v.findViewById(R.id.iBCreate);
        moosicRoomsAdapter = new MoosicRoomsAdapter(getActivity(),R.layout.activity_moosic_rooms_adapter);
        getRoomsFromDB();
        roomslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MoosicRoomsProvider moosicRoomsProvider = (MoosicRoomsProvider) moosicRoomsAdapter.getItem(position);
                String groupId = moosicRoomsProvider.getRoomID();
                //Do whatever you want
            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create room
            }
        });
        return v;
    }
    private void getRoomsFromDB(){
        //get JSON list from database
        /***
          for(json size
         {
            jsonArray....
            MoosicRoomsProvider moosicRoomsProvider = new MoosicRoomsProvider(Id,roomName,rooomHost,roomPeopleNum);
            moosicRoomsAdapter.add(moosicRoomsProvider);
         }*/
        roomslist.setAdapter(moosicRoomsAdapter);
    }
    @Override
    public void onRefresh() {
        //refresh the list
    }
}
