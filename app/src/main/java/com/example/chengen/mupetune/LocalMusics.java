package com.example.chengen.mupetune;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

public class LocalMusics extends Fragment implements View.OnClickListener {
    private static ArrayList<File> defaultSongs;
    private static final String path = "/storage/extSdCard/music";
    private ListView musicList;
    private static SongDataAdapter adapter;
    private Communicator comm;
    View v;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        v = inflater.inflate(R.layout.local_music, container, false);
        musicList = (ListView) v.findViewById(R.id.musicList);
        ImageButton love = (ImageButton) v.findViewById(R.id.ibLove);
        ImageButton singer = (ImageButton) v.findViewById(R.id.ibSingers);
        ImageButton recent = (ImageButton) v.findViewById(R.id.ibRecent);
        ImageButton albums = (ImageButton) v.findViewById(R.id.ibAlbums);
        ImageButton custom = (ImageButton) v.findViewById(R.id.ibCustom);
        ImageButton shuffle = (ImageButton)v.findViewById(R.id.ibShuffle);
        comm = (Communicator)getActivity();
        if (adapter != null) {
            musicList.setAdapter(adapter);
        } else {
            updatePlayList();
        }
        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MusicPlayer.class);
                intent.putExtra("pos",(int)(Math.random()*defaultSongs.size())).putExtra("songlist", defaultSongs);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
            }
        });
        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Intent intent = new Intent(getActivity(), MusicPlayer.class);
                comm.respond(position,defaultSongs);
                intent.putExtra("pos", position).putExtra("songlist", defaultSongs);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
            }
        });
        love.setOnClickListener(this);
        singer.setOnClickListener(this);
        recent.setOnClickListener(this);
        albums.setOnClickListener(this);
        custom.setOnClickListener(this);
        return v;
    }
    private void updatePlayList() {
        defaultSongs = new ArrayList<>();
        File home = new File(path);
        if (home.listFiles(new Mp3Filter()).length > 0) {
            Collections.addAll(defaultSongs, home.listFiles(new Mp3Filter()));
        }
        Collections.sort(defaultSongs);
        adapter = new SongDataAdapter(getActivity(),
                R.layout.activity_song_data_adapter);
        Bitmap bitmap = null;
        for (int i = 0; i < defaultSongs.size(); i++) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(defaultSongs.get(i).getPath());
            byte[] artBytes = mmr.getEmbeddedPicture();
            String albumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String singer = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String songName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (artBytes != null) {
                InputStream is = new ByteArrayInputStream(mmr.getEmbeddedPicture());
                bitmap = BitmapFactory.decodeStream(is);
            } else {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.musics);
            }
            if (singer == null || songName == null) {
                singer = "";
                songName = defaultSongs.get(i).getName().replace("mp3", "").replace("wav", "").replace("m4a", "");
            }
            SongDataProvider provider = new SongDataProvider(new BitmapDrawable(getResources(), resizeBitmap(bitmap, 80, 80))
                    , songName, singer);
            adapter.add(provider);
            System.out.println(adapter.getCount());
        }
        musicList.setAdapter(adapter);
        bitmap.recycle();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

    class Mp3Filter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String filename) {
            return (filename.endsWith(".mp3") || filename.endsWith(".wav") || filename.endsWith("m4a"));
        }
    }
    public Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        float ratioX = newWidth / (float) bitmap.getWidth();
        float ratioY = newHeight / (float) bitmap.getHeight();
        float middleX = newWidth / 2.0f;
        float middleY = newHeight / 2.0f;
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
        return scaledBitmap;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
