package com.example.chengen.mupetune;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class MoosicHostRoom extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener {
    private static ArrayList<SongsDatas> mySongs;
    private static int position;
    private ImageButton foreward,backward,playStop;
    private ListView songs;
    private static MediaPlayer mp;
    private static LinkedList<String> songName, singers;
    private static SongDataAdapter adapter, adapter2;
    private static ArrayList<File> songData;
    private static final String path = "/storage/extSdCard/music";
    private static final String url = "http://45.55.182.4:8080/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moosic_host_room_layout);
        ListView rooms = (ListView) findViewById(R.id.listRooms);
        songs = (ListView) findViewById(R.id.listSongs);
        Button add = (Button) findViewById(R.id.btnHostAdd);
        foreward = (ImageButton)findViewById(R.id.ibForeTwo);
        backward = (ImageButton)findViewById(R.id.ibBackTwo);
        playStop = (ImageButton)findViewById(R.id.ibPlayTwo);
        add.setOnClickListener(this);
        adapter = new SongDataAdapter(getApplicationContext(),
                R.layout.activity_song_data_adapter);
        songName = new LinkedList<>();
        singers = new LinkedList<>();
        songData = new ArrayList<>();
        mySongs = new ArrayList<>();
        Thread t = new Thread() {
            @Override
            public void run() {
                super.run();
                SharedPreferences groupPref = getSharedPreferences("groupInfo", Context.MODE_PRIVATE);
                String groupID = groupPref.getString("groupID", "");
                getFromDB(groupID);
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < mySongs.size(); i++) {
            SongDataProvider provider = new SongDataProvider(null, mySongs.get(i).getSongNames(),mySongs.get(i).getSongArtist());
            adapter.add(provider);
        }
        rooms.setAdapter(adapter);
        if (adapter2 != null) {
            songs.setAdapter(adapter2);
        }else
            updatePlayList();
        songs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int pos, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MoosicHostRoom.this);
                builder.setCancelable(false);
                builder.setMessage("Do you want to add the song?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Thread t = new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                upload(songName.get(pos), singers.get(pos), songData.get(pos).getName());
                            }
                        };
                        t.start();
                        songs.setVisibility(View.GONE);
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        updateList();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        songs.setVisibility(View.GONE);
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAdd:
                if (songs.isShown())
                    songs.setVisibility(View.GONE);
                else
                    songs.setVisibility(View.VISIBLE);
                break;
            case R.id.tBPlayStop:
                if (mp.isPlaying()) {
                    mp.pause();
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stopping);
                    playStop.setBackground(new BitmapDrawable(getResources(), bitmap));
                } else {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.going);
                    playStop.setBackground(new BitmapDrawable(getResources(), bitmap));
                    mp.start();
                }
                break;
            case R.id.iBForward:
                mp.stop();
                mp.release();
                position = (position + 1) % mySongs.size();
                Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.going);
                playStop.setBackground(new BitmapDrawable(getResources(), bitmap2));
                playSongs(position);
                break;
            case R.id.iBBackward:
                mp.stop();
                mp.release();
                position = (position - 1 < 0) ? mySongs.size() - 1 : position - 1;
                Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.going);
                playStop.setBackground(new BitmapDrawable(getResources(), bitmap3));
                playSongs(position);
                break;
        }
    }
    private void getFromDB(String groupID) {
        try {
            URL obj = new URL(url + "getSongForGroup/" + groupID);
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
            for (int i = mySongs.size(); i < jArray.length(); i++) {
                JSONObject songObject = jArray.getJSONObject(i);
                String songName = songObject.getString("songName");
                String artist = songObject.getString("artist");
                String SongUrl = songObject.getString("url");
                SongsDatas datas = new SongsDatas(songName,artist,SongUrl,"");
                mySongs.add(datas);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void upload(String songName, String artist, String fileName) {
        try {
            URL obj = new URL(url + "uploadSong");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            SharedPreferences groupPref = getSharedPreferences("groupInfo", Context.MODE_PRIVATE);
            String groupID = groupPref.getString("groupID", "");
            wr.writeUTF("songName=" + songName + "&artist" + artist + "&file=" + fileName + "&groupID=" + groupID);
            wr.flush();
            wr.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void updateList() {
        Thread t = new Thread() {
            @Override
            public void run() {
                super.run();
                SharedPreferences groupPref = getSharedPreferences("groupInfo", Context.MODE_PRIVATE);
                String groupID = groupPref.getString("groupID", "");
                getFromDB(groupID);
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        adapter.clear();
        for (int i = 0; i < mySongs.size(); i++) {
            SongDataProvider provider = new SongDataProvider(null, mySongs.get(i).getSongNames(),mySongs.get(i).getSongArtist());
            adapter.add(provider);
        }
    }
    private void updatePlayList() {
        songData = new ArrayList<>();
        File home = new File(path);
        if (home.listFiles(new Mp3Filter()).length > 0) {
            Collections.addAll(songData, home.listFiles(new Mp3Filter()));
        }
        Collections.sort(songData);
        adapter2 = new SongDataAdapter(getApplication(),
                R.layout.activity_song_data_adapter);
        for (int i = 0; i < songData.size(); i++) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(songData.get(i).getPath());
            String art = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String name = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (art == null || name == null) {
                singers.add("");
                songName.add(songData.get(i).getName().replace("mp3", "").replace("wav", "").replace("m4a", ""));
                name = songData.get(i).getName().replace("mp3", "").replace("wav", "").replace("m4a", "");
            } else {
                singers.add(art);
                songName.add(name);
            }
            SongDataProvider provider = new SongDataProvider(null, name, art);
            adapter2.add(provider);
        }
        songs.setAdapter(adapter2);
    }
    class Mp3Filter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String filename) {
            return (filename.endsWith(".mp3") || filename.endsWith(".wav") || filename.endsWith("m4a"));
        }
    }
    private void playSongs(int position) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.going);
        backward.setClickable(true);
        playStop.setClickable(true);
        foreward.setClickable(true);
        playStop.setBackground(new BitmapDrawable(getResources(), bitmap));
        Uri uri = Uri.parse(mySongs.get(position).getSongPath());
        mp = MediaPlayer.create(getApplicationContext(), uri);
        mp.setOnCompletionListener(this);
        mp.start();
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.stop();
        mp.release();
        position = (position + 1) % mySongs.size();
        playSongs(position);
    }
}
