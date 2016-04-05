package com.example.chengen.mupetune;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SongDataAdapter extends ArrayAdapter {
    List<Object> adapterList = new ArrayList<>();
    public SongDataAdapter(Context context, int resource){
        super(context, resource);
    }
    private static class Handler{
        ImageView songImage;
        TextView songName;
        TextView songArtist;
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
            row = inflater.inflate(R.layout.activity_song_data_adapter,parent,false);
            handler = new Handler();
            handler.songImage = (ImageView)row.findViewById(R.id.ivSongImage);
            handler.songName = (TextView)row.findViewById(R.id.tvSongName);
            handler.songArtist = (TextView)row.findViewById(R.id.tvartist);
            row.setTag(handler);
        }else{
            handler=(Handler)row.getTag();
        }
        SongDataProvider songDataProvider;
        songDataProvider = (SongDataProvider)this.getItem(position);
        handler.songImage.setBackground(songDataProvider.getSongImage());
        handler.songImage.setAdjustViewBounds(true);
        handler.songName.setText(songDataProvider.getSongName());
        handler.songArtist.setText(songDataProvider.getSongArtist());
        return row;
    }
}
