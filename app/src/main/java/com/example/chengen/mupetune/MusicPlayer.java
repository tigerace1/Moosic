package com.example.chengen.mupetune;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class MusicPlayer extends Fragment implements View.OnClickListener,MediaPlayer.OnCompletionListener {
    private static ArrayList<SongsDatas> mySongs;
    private static int position;
    private static MediaPlayer mp;
    private Bitmap loop,repeat,random;
    private SeekBar seekBar;
    private ImageButton mode,foreward,backward;
    private ImageView photo;
    private TextView currentTime, totalTime, name,artist;
    private ToggleButton playAndStop;
    private static int MODE_CODE=0;
    private boolean isRunning=false;
    String text;
    private Handler myHandler = new Handler();
    View v;
    public void getData(int pos,ArrayList<SongsDatas>songs){
        position=pos;
        mySongs=songs;
        if (mySongs!=null) {
            if (mp != null) {
                mp.stop();
                mp.release();
                mp = null;
            }
            playSongs(position);
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        v = inflater.inflate(R.layout.activity_music_player, container, false);
        loop = BitmapFactory.decodeResource(getResources(), R.drawable.looping);
        repeat = BitmapFactory.decodeResource(getResources(), R.drawable.repeating);
        random = BitmapFactory.decodeResource(getResources(),R.drawable.randoming);
        seekBar = (SeekBar)v.findViewById(R.id.seekBar);
        photo = (ImageView)v.findViewById(R.id.ivSongImage);
        mode = (ImageButton)v.findViewById(R.id.ibMode);
        foreward = (ImageButton)v.findViewById(R.id.iBForward);
        backward = (ImageButton)v.findViewById(R.id.iBBackward);
        playAndStop = (ToggleButton)v.findViewById(R.id.tBPlayStop);
        currentTime = (TextView)v.findViewById(R.id.tvCurrentTime);
        totalTime = (TextView)v.findViewById(R.id.tvTotalTime);
        name = (TextView)v.findViewById(R.id.tvSongsN);
        artist = (TextView)v.findViewById(R.id.tvSongsA);
        foreward.setOnClickListener(this);
        backward.setOnClickListener(this);
        playAndStop.setOnClickListener(this);
        mode.setOnClickListener(this);
        SharedPreferences sharedPref =getActivity().getSharedPreferences("mode", Context.MODE_PRIVATE);
        if (sharedPref.contains("modeInt"))
           MODE_CODE = sharedPref.getInt("modeInt",0);
        if(MODE_CODE==0){
            mode.setBackground(new BitmapDrawable(getResources(), loop));
        }else if(MODE_CODE==1){
            mode.setBackground(new BitmapDrawable(getResources(), repeat));
        }else if(MODE_CODE==2){
            mode.setBackground(new BitmapDrawable(getResources(), random));
        }
        if(mp==null){
            photo.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(),R.drawable.musicnote)));
            foreward.setClickable(false);
            playAndStop.setClickable(false);
            backward.setClickable(false);
            seekBar.setVisibility(View.INVISIBLE);
            text = "00:00:00";
            totalTime.setText(text);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stopping);
            playAndStop.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
        }else {
            seekBar.setMax(mp.getDuration());
            currentTime.setText(getTimeString(mp.getCurrentPosition()));
            text = "| "+getTimeString((mp.getDuration()));
            totalTime.setText(text);
            if (mp.isPlaying()){
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.going);
                playAndStop.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
                myHandler.postDelayed(UpdateSongTime, 100);
                isRunning = true;
            }else {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stopping);
                playAndStop.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
            }
            changeData(position);
        }
        return v;
    }
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            long startTime = mp.getCurrentPosition();
            currentTime.setText(getTimeString(startTime));
            seekBar.setProgress((int) startTime);
            if(isRunning)
              myHandler.postDelayed(UpdateSongTime, 100);
            else
              myHandler.removeCallbacks(UpdateSongTime);
        }
    };
    private void playSongs(int position) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.going);
        backward.setClickable(true);
        playAndStop.setClickable(true);
        foreward.setClickable(true);
        seekBar.setVisibility(View.VISIBLE);
        playAndStop.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
        Uri uri = Uri.parse(mySongs.get(position).getSongPath());
        mp = MediaPlayer.create(getActivity().getApplicationContext(), uri);
        mp.setOnCompletionListener(this);
        mp.start();
        isRunning=true;
        myHandler.postDelayed(UpdateSongTime, 100);
        seekBar.setMax(mp.getDuration());
        text = "| "+getTimeString(mp.getDuration());
        totalTime.setText(text);
        changeData(position);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(seekBar.getProgress());
            }
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tBPlayStop:
                if (mp.isPlaying()) {
                    mp.pause();
                    myHandler.removeCallbacks(UpdateSongTime);
                    isRunning=false;
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stopping);
                    playAndStop.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
                } else {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.going);
                    playAndStop.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
                    mp.start();
                    isRunning=true;
                    myHandler.postDelayed(UpdateSongTime,100);
                }
                break;
            case R.id.iBForward:
                mp.stop();
                mp.release();
                isRunning=false;
                myHandler.removeCallbacks(UpdateSongTime);
                position = (position + 1) % mySongs.size();
                Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.going);
                playAndStop.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap2));
                playSongs(position);
                break;
            case R.id.iBBackward:
                mp.stop();
                mp.release();
                isRunning=false;
                myHandler.removeCallbacks(UpdateSongTime);
                position = (position - 1 < 0) ? mySongs.size() - 1 : position - 1;
                Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.going);
                playAndStop.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap3));
                playSongs(position);
                break;
            case R.id.ibMode:
                PopupMenu popup2 = new PopupMenu(getActivity(),mode);
                popup2.getMenuInflater().inflate(R.menu.playing_mode, popup2.getMenu());
                popup2.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getTitle().equals("Loop")) {
                            MODE_CODE = 0;
                            mode.setBackground(new BitmapDrawable(getResources(), loop));
                        } else if(item.getTitle().equals("Repeat")) {
                            MODE_CODE = 1;
                            mode.setBackground(new BitmapDrawable(getResources(), repeat));
                        }else if(item.getTitle().equals("Random")) {
                            MODE_CODE = 2;
                            mode.setBackground(new BitmapDrawable(getResources(), random));
                        }
                        return true;
                     }
                    });
                popup2.show();
                break;
        }
    }
    @Override
    public void onCompletion(MediaPlayer player) {
        mp.stop();
        mp.release();
        myHandler.removeCallbacks(UpdateSongTime);
        isRunning=false;
        if(MODE_CODE==0) {
            position = (position + 1) % mySongs.size();
            playSongs(position);
        }else if(MODE_CODE==1){
            playSongs(position);
        }else if(MODE_CODE==2){
            position = (int)(Math.random()*mySongs.size());
            playSongs(position);
        }
    }
    private String getTimeString(long millis) {
        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);
        return (String.format("%02d", hours))+":"+String.format("%02d", minutes)+":"+
                String.format("%02d", seconds);
    }
    private void changeData(int position){
        name.setText(mySongs.get(position).getSongNames());
        artist.setText(mySongs.get(position).getSongArtist());
        photo.setBackground(null);
        photo.setBackground(new BitmapDrawable(getResources(),stringToBitMap(mySongs.get(position).getSongCovers())));
    }
    public Bitmap stringToBitMap(String encodedString){
        try{
            byte [] encodeByte= Base64.decode(encodedString, Base64.URL_SAFE);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        }catch(Exception e){
            e.getMessage();
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPref = getActivity().getSharedPreferences("mode", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("modeInt",MODE_CODE);
        editor.apply();
    }
}
