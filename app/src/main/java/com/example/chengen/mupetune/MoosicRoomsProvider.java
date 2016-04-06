package com.example.chengen.mupetune;
public class MoosicRoomsProvider {
    private String RoomID;
    private String RoomName;
    private String RoomHost;
    private String RoomPeopleNum;
    public MoosicRoomsProvider(String roomID, String roomName, String roomHost, String roomPeopleNum) {
        RoomID = roomID;
        RoomName = roomName;
        RoomHost = roomHost;
        RoomPeopleNum = roomPeopleNum;
    }

    public String getRoomID() {
        return RoomID;
    }

    public void setRoomID(String roomID) {
        RoomID = roomID;
    }

    public String getRoomName() {
        return RoomName;
    }

    public void setRoomName(String roomName) {
        RoomName = roomName;
    }

    public String getRoomHost() {
        return RoomHost;
    }

    public void setRoomHost(String roomHost) {
        RoomHost = roomHost;
    }

    public String getRoomPeopleNum() {
        return RoomPeopleNum;
    }

    public void setRoomPeopleNum(String roomPeopleNum) {
        RoomPeopleNum = roomPeopleNum;
    }
}
