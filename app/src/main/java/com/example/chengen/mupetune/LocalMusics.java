package com.example.chengen.mupetune;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LocalMusics extends Fragment implements View.OnClickListener,AdapterView.OnItemClickListener {
    private static final String path = Environment.getDataDirectory().getPath();
    private static ArrayList<SongsDatas> songsDatases;
    private ListView musics;
    private static SongDataAdapter adapter;
    private Communicator comm;
    private TinyDB tinyDB;
    private ImageButton search;
    View v;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        v = inflater.inflate(R.layout.local_music, container, false);
        ImageButton love = (ImageButton) v.findViewById(R.id.ibLove);
        ImageButton singer = (ImageButton) v.findViewById(R.id.ibSingers);
        ImageButton recent = (ImageButton) v.findViewById(R.id.ibRecent);
        ImageButton albums = (ImageButton) v.findViewById(R.id.ibAlbums);
        ImageButton custom = (ImageButton) v.findViewById(R.id.ibCustom);
        ImageButton shuffle = (ImageButton)v.findViewById(R.id.ibShuffle);
        search = (ImageButton)v.findViewById(R.id.ibFindSongs);
        musics = (ListView) v.findViewById(R.id.list_of_music);
        comm = (Communicator)getActivity();
        tinyDB = new TinyDB(getContext());
        if (adapter != null) {
            musics.setAdapter(adapter);
        }else{
            adapter = new SongDataAdapter(getActivity().getApplication(),
                    R.layout.activity_song_data_adapter);
            if(tinyDB.getListObject("defaultSongData", SongsDatas.class)!=null) {
                songsDatases = tinyDB.getListObject("defaultSongData",SongsDatas.class);
                for(int i=0;i<songsDatases.size();i++){
                    SongDataProvider provider = new SongDataProvider(
                            new BitmapDrawable(getResources(),
                                    Bitmap.createScaledBitmap(stringToBitMap(songsDatases.get(i).getSongCovers()), 80, 80,true)),
                            songsDatases.get(i).getSongNames(),songsDatases.get(i).getSongArtist());
                    adapter.add(provider);
                }
                musics.setAdapter(adapter);
            }
        }
        shuffle.setOnClickListener(this);
        love.setOnClickListener(this);
        singer.setOnClickListener(this);
        recent.setOnClickListener(this);
        albums.setOnClickListener(this);
        custom.setOnClickListener(this);
        search.setOnClickListener(this);
        musics.setOnItemClickListener(this);
        return v;
    }
    private void updateDefaultList() {
        ArrayList<File> songNames = new ArrayList<>();
        songsDatases = new ArrayList<>();
        File home = new File(path);
        adapter = new SongDataAdapter(getActivity().getApplication(),
                R.layout.activity_song_data_adapter);
        if (home.listFiles(new Mp3Filter()).length > 0)
            Collections.addAll(songNames, home.listFiles(new Mp3Filter()));
        Bitmap bitmap=null;
        for (int i = 0; i < songNames.size(); i++) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(songNames.get(i).getPath());
            byte[] artBytes = mmr.getEmbeddedPicture();
            String singer = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String songName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (artBytes != null) {
                InputStream is = new ByteArrayInputStream(mmr.getEmbeddedPicture());
                bitmap = BitmapFactory.decodeStream(is);
            } else {
                bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.musicnote);
            }
            if (singer == null || songName == null) {
                singer = " ";
                songName = songNames.get(i).getName().replace("mp3", "").replace("wav", "").replace("m4a", "");
            }
            SongsDatas songsDatas =new SongsDatas(songName,singer, songNames.get(i).getPath(),bitMapToString(bitmap));
            songsDatases.add(songsDatas);
        }
        Collections.sort(songsDatases, new Comparator<SongsDatas>() {
            @Override
            public int compare(SongsDatas lhs, SongsDatas rhs) {
                return (lhs).getSongNames().compareToIgnoreCase(rhs.getSongNames());
            }
        });
        for(int i=0;i<songsDatases.size();i++){
            SongDataProvider provider = new SongDataProvider(new BitmapDrawable(getResources(),
                    Bitmap.createScaledBitmap(stringToBitMap(songsDatases.get(i).getSongCovers()), 80, 80,true))
                    , songsDatases.get(i).getSongNames(), songsDatases.get(i).getSongArtist());
            adapter.add(provider);
        }
        tinyDB.putObject("defaultSongData", songsDatases);
        musics.setAdapter(adapter);
        if(bitmap!=null)
           bitmap.recycle();
        search.clearAnimation();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibShuffle:
                comm.respond((int) (Math.random() * songsDatases.size()),songsDatases);
                break;
            case R.id.ibFindSongs:
                final Animation an = AnimationUtils.loadAnimation(getActivity().getBaseContext(), R.anim.spin);
                search.startAnimation(an);
                updateDefaultList();
        }
    }
    private String bitMapToString(Bitmap bitmap){
        System.gc();
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte [] b=baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }
    public Bitmap stringToBitMap(String encodedString){
        try{
            byte [] encodeByte=Base64.decode(encodedString,Base64.URL_SAFE);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        }catch(Exception e){
            e.getMessage();
            return null;
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        comm.respond(position, songsDatases);
    }
    class Mp3Filter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String filename) {
           return (filename.endsWith(".mp3") || filename.endsWith(".wav") || filename.endsWith("m4a"));
        }
    }
}
