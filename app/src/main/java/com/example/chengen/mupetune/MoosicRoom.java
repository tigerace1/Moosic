package com.example.chengen.mupetune;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
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
 public class MoosicRoom extends Fragment implements View.OnClickListener{
        private ListView rooms,songs;
        private Button add,update;
        private static LinkedList<String> songName,singers,onlineName,onlineSingers;
        private static SongDataAdapter adapter,adapter2;
        private static ArrayList<File>songData;
        private static ArrayList<File>onlineSongs;
        private static final String path = "/storage/extSdCard/music";
        private Communicator comm;
        private static final String url = "http://10.28.18.19:3000/";
        View v;
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            v = inflater.inflate(R.layout.activity_moosic_room, container, false);
            rooms = (ListView)v.findViewById(R.id.listRooms);
            songs = (ListView)v.findViewById(R.id.listSongs);
            add = (Button)v.findViewById(R.id.btnAdd);
            update = (Button)v.findViewById(R.id.btnUpdate);
            comm = (Communicator)getActivity();
            add.setOnClickListener(this);
            update.setOnClickListener(this);
            adapter = new SongDataAdapter(getActivity().getApplicationContext(),
                    R.layout.activity_song_data_adapter);
            songName = new LinkedList<>();
            singers = new LinkedList<>();
            songData = new ArrayList<>();
            onlineName = new LinkedList<>();
            onlineSingers = new LinkedList<>();
            onlineSongs = new ArrayList<>();
            Thread t = new Thread(){
                @Override
                public void run() {
                    super.run();
                    SharedPreferences groupPref = getContext().getSharedPreferences("groupInfo", Context.MODE_PRIVATE);
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
            for(int i=0;i<onlineName.size();i++) {
                // ignore the null
                SongDataProvider provider = new SongDataProvider(null,onlineName.get(i),onlineSingers.get(i));
                adapter.add(provider);
            }
            rooms.setAdapter(adapter);
            if(adapter2!=null){
                songs.setAdapter(adapter2);
            }
            updatePlayList();
            songs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(false);
                    builder.setMessage("Do you want to add the song?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Thread t = new Thread() {
                                @Override
                                public void run() {
                                    super.run();
                                    upload(songName.get(position), singers.get(position),songData.get(position));
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
            rooms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    comm.respond(position,onlineSongs);
                }
            });
            return v;
        }
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btnAdd:
                    if(songs.isShown())
                        songs.setVisibility(View.GONE);
                    else
                        songs.setVisibility(View.VISIBLE);
                    break;
                case R.id.btnUpdate:
                    updateList();
                    break;
            }
        }
        private void getFromDB(String groupID){
            // songName=...;
            //singers=....;
            //onlineSongData=;
            //get
            try {
                URL obj = new URL(url+"getSongForGroup/" + groupID);
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
                    JSONObject songObject = jArray.getJSONObject(i);
                    String songName = songObject.getString("songName");
                    String artist = songObject.getString("artist");
                    //Chengen can you get file ------------------------------------------------------------------------------------------------
                }
                in.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        private void upload(String songName, String artist,File fileS){
            try {
                //add url here
                URL obj = new URL(url+"postASong");
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                SharedPreferences groupPref = getContext().getSharedPreferences("groupInfo", Context.MODE_PRIVATE);
                String groupID = groupPref.getString("groupID", "");
                wr.writeUTF("songName="+songName+"&artist"+artist+"&file="+fileS+"&groupID="+groupID);
                wr.flush();
                wr.close();
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        private void updateList()  {
            Thread t = new Thread(){
                @Override
                public void run() {
                    super.run();
                    SharedPreferences groupPref = getContext().getSharedPreferences("groupInfo", Context.MODE_PRIVATE);
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
            onlineName.clear();
            onlineSingers.clear();
            adapter.clear();
            for(int i=0;i<onlineName.size();i++) {
                SongDataProvider provider = new SongDataProvider(null,onlineName.get(i), onlineSingers.get(i));
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
            adapter2 = new SongDataAdapter(getActivity().getApplication(),
                    R.layout.activity_song_data_adapter);
            for (int i = 0; i < songData.size(); i++) {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(songData.get(i).getPath());
                String art = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String name = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                if (art == null || name == null) {
                    singers.add("");
                    songName.add(songData.get(i).getName().replace("mp3", "").replace("wav", "").replace("m4a", ""));
                    name=songData.get(i).getName().replace("mp3", "").replace("wav", "").replace("m4a", "");
                }else {
                    singers.add(art);
                    songName.add(name);
                }
                SongDataProvider provider = new SongDataProvider(null,name,art);
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
            String s=null;
            try {
                fin = new FileInputStream(file);
                byte fileContent[] = new byte[(int) file.length()];
                fin.read(fileContent);
                s = new String(fileContent);
                System.out.println("File content: " + s);
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
    }