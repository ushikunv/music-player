package com.ushikun.musicplayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2018/12/29.
 */

public class Playlist {
    List<Long> ids;
    String name;
    static int playlistNum = 0;
    int id;
    Playlist() {
        ids = new ArrayList<>();
        name = "Playlist "+playlistNum+" ";
        id = playlistNum;
        playlistNum++;
    }

    void addToList(long id) {
        if(!ids.contains(id)) {
            ids.add(id);
        }
    }

//    byte[] getByteData(){
//        List<Byte>byteList = new ArrayList<>();
//        byteList.add(ids.get(0).byteValue());
//
//        return ;
//    }
    void test(){
    }

}
