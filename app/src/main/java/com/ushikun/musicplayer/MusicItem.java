package com.ushikun.musicplayer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MusicItem implements Comparable<Object> {
    private static final String TAG = "MusicItem";
    final long id;
    final String artist;
    final String title;
    final String album;
    final int truck;
    final long duration;
    final String path;

    public MusicItem(long id, String artist, String title, String album, int truck, long duration, String path) {
        this.id = id;
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.truck = truck;
        this.duration = duration;
        this.path = path;
    }

    public String getTitle() {
    return title;

    }

    public Uri getURI() {
        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
    }

    /**
     * 外部ストレージ上から音楽を探してリストを返す。
     * @param context コンテキスト
     * @return 見つかった音楽のリスト
     */
    public static List<MusicItem> getItems(Context context) {
        List<MusicItem> items = new LinkedList<MusicItem>();

        // ContentResolver を取得
        ContentResolver cr = context.getContentResolver();

        // 外部ストレージから音楽を検索
       Cursor cur = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null);

        if (cur != null) {
            if (cur.moveToFirst()) {

                // 曲情報のカラムを取得
                int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int albumColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID);
                int idTruck = cur.getColumnIndex(MediaStore.Audio.Media.TRACK);
                int dataColmun = cur.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);


                // リストに追加
                do {
                    try {
                        items.add(new MusicItem(cur.getLong(idColumn),
                                cur.getString(artistColumn),
                                cur.getString(titleColumn),
                                cur.getString(albumColumn),
                                cur.getInt(idTruck),
                                cur.getLong(durationColumn),
                                cur.getString(dataColmun)));
                        //MainActivityのartists,albumsに追加
                        MainActivity.addArtist(cur.getString(artistColumn));
                        MainActivity.addAlbum(cur.getString(albumColumn));
                    }catch (Exception e){

                    }
                } while (cur.moveToNext());

            }
            // カーソルを閉じる
            cur.close();
        }

        // 見つかる順番はソートされていないため、アルバム単位でソートする
        Collections.sort(items);
        return items;
    }

    @Override
    //  Collections.sort(items)でのソート設定
    public int compareTo(Object another) {
        if (another == null) {
            return 1;
        }
        MusicItem item = (MusicItem) another;
        int result = this.album.compareTo(item.album);
        if (result != 0) {
            return result;
        }
        return this.truck - item.truck;
    }
}