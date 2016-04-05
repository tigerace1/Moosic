package com.example.chengen.mupetune;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class MoosicRooms extends Fragment implements View.OnClickListener{
    private ListView rooms,songs;
    private Button add,update;
    //lists that get from your data base
    private static LinkedList<String> songName,singers,onlineName,onlineSingers;
    private static SongDataAdapter adapter,adapter2;
    private static ArrayList<File>songData;
    private static ArrayList<File>onlineSongs;
    private static final String path = "/storage/extSdCard/music";
    private Communicator comm;
    View v;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        v = inflater.inflate(R.layout.activity_moosic_rooms, container, false);
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
                getFromDB();
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
    private void getFromDB(){
        // songName=...;
        //singers=....;
        //onlineSongData=;
    }
    private void upload(String name, String singer,File fileS){

    }
    private void updateList()  {
        Thread t = new Thread(){
            @Override
            public void run() {
                super.run();
                getFromDB();
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
