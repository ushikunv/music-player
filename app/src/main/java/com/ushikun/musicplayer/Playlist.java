package com.ushikun.musicplayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2018/12/29.
 */

public class Playlist {
    List<Long> ids;
    String name;


    Playlist(String title) {
        ids = new ArrayList<>();
        name = title;
    }

    void addToList(long id) {
        if(!ids.contains(id)) {
            ids.add(id);
        }
    }

    void clearList(){
        ids.clear();
    }

   byte[] getByteData(){
        byte[] bytes = new byte[ids.size()*2];
       for(int i = 0;i<ids.size();i++) {
           bytes[2*i] = (byte) ids.get(i).longValue();
           bytes[2*i+1] = (byte) (ids.get(i).longValue() >> 8);
       }
       return bytes;
   }

   void setByteData(byte[] bytes){
       int mask = 255;
       for(int i = 0;i<bytes.length/2;i++){
           ids.add((long) (bytes[2*i] & mask | (bytes[2*i+1] << 8)));
       }
   }

}
