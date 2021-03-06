package com.example.chengen.mupetune;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MoosicRoom extends AppCompatActivity implements View.OnClickListener {
    private static ArrayList<SongsDatas> mySongs;
    private ListView songs;
    private static LinkedList<String> songName,singers;
    private static SongDataAdapter adapter, adapter2;
    private static ArrayList<File> songData;
    private static final String path = "/storage/extSdCard/music";
    private static final String url = "http://45.55.182.4:8080/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moosic_room);
        ListView rooms = (ListView) findViewById(R.id.listRooms);
        songs = (ListView) findViewById(R.id.listSongs);
        Button add = (Button) findViewById(R.id.btnAdd);
        Button update = (Button) findViewById(R.id.btnUpdate);
        add.setOnClickListener(this);
        update.setOnClickListener(this);
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
        }
        updatePlayList();
        songs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MoosicRoom.this);
                builder.setCancelable(false);
                builder.setMessage("Do you want to add the song?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Thread t = new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                upload(songName.get(position), singers.get(position), fileToString(songData.get(position)));
                            }
                        };
                        t.start();
                        songs.setVisibility(View.GONE);
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
            case R.id.btnUpdate:
                updateList();
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
                String songUrl = decompress(songObject.getString("Url"));
                SongsDatas songsDatas  = new SongsDatas(songName,artist,songUrl,"");
                mySongs.add(songsDatas);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void upload(String songName, String artist, String fileS) {
        try {
            URL obj = new URL(url + "uploadSong");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            SharedPreferences groupPref = getSharedPreferences("groupInfo", Context.MODE_PRIVATE);
            String groupID = groupPref.getString("groupID", "");
            wr.writeUTF("songName=" + songName + "&artist" + artist + "&file=" + fileS + "&groupID=" + groupID);
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
    private String fileToString(File file) {
        FileInputStream fin = null;
        String s = null;
        try {
            fin = new FileInputStream(file);
            byte fileContent[] = new byte[(int) file.length()];
            fin.read(fileContent);
            s = new String(fileContent);
            return compress(s);
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while reading file " + ioe);
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }
        return s;
    }

    public static String compress(String string) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
        GZIPOutputStream gos = new GZIPOutputStream(os);
        gos.write(string.getBytes());
        gos.close();
        byte[] compressed = os.toByteArray();
        os.close();
        return compressed.toString();
    }

    public static String decompress(String zipText) throws IOException {
        byte[] compressed = Base64.decode(zipText, 1);
        if (compressed.length > 4)
        {
            GZIPInputStream gzipInputStream = new GZIPInputStream(
                    new ByteArrayInputStream(compressed, 4,
                            compressed.length - 4));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int value = 0; value != -1;) {
                value = gzipInputStream.read();
                if (value != -1) {
                    baos.write(value);
                }
            }
            gzipInputStream.close();
            baos.close();
            String sReturn = new String(baos.toByteArray(), "UTF-8");
            return sReturn;
        } else {
            return "";
        }
    }
}