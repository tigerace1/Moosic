package com.example.chengen.mupetune;

public class SongsDatas {
    private String songNames,songArtist,songPath,songCovers;

    public SongsDatas(String songNames, String songArtist,String songPath, String songCovers) {
        this.songNames = songNames;
        this.songArtist = songArtist;
        this.songPath = songPath;
        this.songCovers = songCovers;
    }

    public String getSongNames() {
        return songNames;
    }

    public String getSongPath() {
        return songPath;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public String getSongCovers() {
        return songCovers;
    }
}
