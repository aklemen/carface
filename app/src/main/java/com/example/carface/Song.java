package com.example.carface;


public class Song {
    private int id;
    private String songName;
    private String songArtist;
    private String songAlbum;
    private int songResourceId;

    public Song(int id, String songName, String songArtist, String songAlbum, int songResourceId) {
        this.id = id;
        this.songName = songName;
        this.songArtist = songArtist;
        this.songAlbum = songAlbum;
        this.songResourceId = songResourceId;
    }

    public int getId() {
        return id;
    }

    public String getSongName() {
        return songName;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public String getSongAlbum() {
        return songAlbum;
    }

    public int getSongResourceId() {
        return songResourceId;
    }
}