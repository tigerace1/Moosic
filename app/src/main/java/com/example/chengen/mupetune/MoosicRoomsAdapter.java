package com.example.chengen.mupetune;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MoosicRoomsAdapter extends ArrayAdapter {
    List<Object> adapterList = new ArrayList<>();
    public MoosicRoomsAdapter(Context context, int resource){
        super(context, resource);
    }
    private static class Handler{
        TextView RoomID;
        TextView RoomName;
        TextView RoomHost;
        TextView RoomPeopleNum;
    }
    @Override
    public void add(Object object) {
        super.add(object);
        adapterList.add(object);
    }
    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        row = convertView;
        final  Handler handler;
        if(convertView==null){
            final LayoutInflater inflater=(LayoutInflater)this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.activity_moosic_rooms_adapter,parent,false);
            handler = new Handler();
            handler.RoomID = (TextView)row.findViewById(R.id.tvRoomID);
            handler.RoomName = (TextView)row.findViewById(R.id.tvRoomName);
            handler.RoomHost = (TextView)row.findViewById(R.id.tvRoomHost);
            handler.RoomPeopleNum = (TextView)row.findViewById(R.id.tvRoomPeopleNum);
            row.setTag(handler);
        }else{
            handler=(Handler)row.getTag();
        }
        MoosicRoomsProvider moosicRoomsProvider;
        moosicRoomsProvider = (MoosicRoomsProvider)this.getItem(position);
        handler.RoomID.setText(moosicRoomsProvider.getRoomID());
        handler.RoomName.setText(moosicRoomsProvider.getRoomName());
        handler.RoomHost.setText(moosicRoomsProvider.getRoomHost());
        handler.RoomPeopleNum.setText(moosicRoomsProvider.getRoomPeopleNum());
        return row;
    }
}

