package com.ushikun.musicplayer;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<MusicItem> mItems;
    static List<String> artists;
    static List<String> albums;
    List<MusicItem> playingList;
    List<MusicItem> selectingList;
    ListView listView;
    CustomAdapter adapter;
    CustomAdapterArtists adapterArtists;
    CustomAdapterPlaylist adapterPlaylists;
    int playingPosition = 0;
    int selectiongPosition = 0;
    MediaPlayer mediaPlayer;
    int playingTimePosition;
    boolean playerMode = false;
    final Handler mSeekbarUpdateHandler = new Handler();
    Runnable mUpdateSeekbar;
    int selectedList = 3;
    public static final int ARTISTS_SELECTED = 0;
    public static final int ALBUMS_SELECTED = 1;
    public static final int PLAYLIST_SELECTED = 2;
    public static final int RANDOM_SELECTED = 3;
    static List<Playlist> playlists;
    static boolean firstOnCreateBool = true;
    static int currentPlaylistNum =0;

    // BroadcastReceiver
    private BroadcastReceiver mReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //レイアウト
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        //ストレージへのアクセスとキーガードのpermissionを取る
        permission();

        //プレイリストを暫定的に4つ
        playlists = new ArrayList<>();
        playlists.add(new Playlist("Playlist0"));
        playlists.add(new Playlist("Playlist1"));
        playlists.add(new Playlist("Playlist2"));
        playlists.add(new Playlist("Playlist3"));

        //保存してあるプレイリストを読み込む（テスト
        for(int i =0;i<4;i++) {
            try {
                  FileInputStream fos = openFileInput("Playlist"+i);
                int length = fos.available();
                byte[] bytes = new byte[length];
                fos.read(bytes);
                fos.close();
                playlists.get(0).setByteData(bytes);
            } catch (Exception e) {
            }
        }


        if(firstOnCreateBool) {
            //MusicItem.getItemsで音楽ファイルを取得する際にartistのリストも取得する
            //そのために初期化しておく
            artists = new ArrayList<>();
            albums = new ArrayList<>();
            //初期化
            playingList = new LinkedList<MusicItem>();
            selectingList = new LinkedList<MusicItem>();
            //ストレージから音楽ファイルを取得
            mItems = MusicItem.getItems(getApplicationContext());
            //プレイリスト(現在再生しているリスト)に追加
            playingList.addAll(mItems);
            //ランダムにシャッフル
            Collections.shuffle(playingList);
            //artistリストをソート
            Collections.sort(artists);
            Collections.sort(albums);

            selectingList.addAll(playingList);
            playingPosition = 0;
            initMediaPlayer();
            setPlayer();
            updateTitle();
            mediaPlayer.start();

            firstOnCreateBool = false;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() == Intent.ACTION_POWER_CONNECTED) {
                    try {
                        mediaPlayer.start();
                    } catch (Exception e) {
                        initMediaPlayer();
                        mediaPlayer.seekTo(playingTimePosition);
                        mediaPlayer.start();
                        setPlayer();
                        updateTitle();
                    }
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else if (intent.getAction() == Intent.ACTION_POWER_DISCONNECTED) {
                    playingTimePosition = mediaPlayer.getCurrentPosition();
                    mediaPlayer.release();
                    mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                }
            }
        };
        this.registerReceiver(mReceiver, filter);
    }

    private void initMediaPlayer() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getApplicationContext(), playingList.get(playingPosition).getURI());
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer thisMediaPlayer) {
                    playNext();
                }
            });
        } catch (Exception e) {

        }
    }

    public static void addArtist(String artist) {
        if (!artists.contains(artist)) {
            artists.add(artist);
        }
    }

    public static void addAlbum(String album) {
        if (!albums.contains(album)) {
            albums.add(album);
        }
    }



    void setListToArtists() {
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        listView = findViewById(R.id.listView);
        adapterArtists = new CustomAdapterArtists(getApplicationContext(), R.layout.textview, artists);
        listView.setAdapter(adapterArtists);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                        //selectingListを初期化
                        selectingList.clear();
                        for (int i = 0; i < mItems.size(); i++) {
                            if (mItems.get(i).artist.equals(artists.get(position))) {
                                selectingList.add(mItems.get(i));
                            }
                        }
                        //リストの表示を新しいselectingListに変更
                        setListToCurentList();

                        selectiongPosition = position;
            }
        });
        //長押しでプレイリストに追加
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            public boolean onItemLongClick(AdapterView parent,View v,int position,long id){
                selectingList.clear();
                for (int i = 0; i < mItems.size(); i++) {
                    if (mItems.get(i).artist.equals(artists.get(position))) {
                        selectingList.add(mItems.get(i));
                    }
                }
                setListToAddToPlaylist();
                listView.setOnItemClickListener(addListToPlaylistMessageClickedHandler);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                selectiongPosition = position;
                return true;
            }
        });
        listView.setSelectionFromTop(selectiongPosition, 0);
    }

    void setListToAlbums() {
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        listView = findViewById(R.id.listView);
        adapterArtists = new CustomAdapterArtists(getApplicationContext(), R.layout.textview, albums);
        listView.setAdapter(adapterArtists);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                //selectingListを初期化
                selectingList.clear();

                for (int i = 0; i < mItems.size(); i++) {
                    if (mItems.get(i).album.equals(albums.get(position))) {
                        selectingList.add(mItems.get(i));
                    }
                }
                //リストの表示を新しいselectingListに変更
                setListToCurentList();

                selectiongPosition = position;
            }
        });
        //長押しでプレイリストに追加
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            public boolean onItemLongClick(AdapterView parent,View v,int position,long id){
                selectingList.clear();

                for (int i = 0; i < mItems.size(); i++) {
                    if (mItems.get(i).album.equals(albums.get(position))) {
                        selectingList.add(mItems.get(i));
                    }
                }
                setListToAddToPlaylist();
                listView.setOnItemClickListener(addListToPlaylistMessageClickedHandler);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                selectiongPosition = position;
                return true;
            }
        });
        listView.setSelectionFromTop(selectiongPosition, 0);
    }

    void setListToPlaylists() {
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        listView = findViewById(R.id.listView);
        adapterPlaylists= new CustomAdapterPlaylist(getApplicationContext(), R.layout.textview, playlists);
        listView.setAdapter(adapterPlaylists);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                //selectingListを初期化
                selectingList.clear();
                for (int i = 0; i < mItems.size(); i++) {
                    if (playlists.get(position).ids.contains(mItems.get(i).id)) {
                        selectingList.add(mItems.get(i));
                    }
                }
                currentPlaylistNum=position;
                //リストの表示を新しいselectingListに変更
                setListToCurentList();

            }
        });
        //長押しでプレイリストを初期化
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            public boolean onItemLongClick(AdapterView parent,View v,int position,long id){
                //playlistをクリアして保存
                playlists.get(position).clearList();
                try {
                    FileOutputStream fos = openFileOutput("Playlist"+position, Context.MODE_PRIVATE);
                    fos.write(playlists.get(position).getByteData());
                    fos.close();
                }catch (Exception e){
                }
                return true;
            }
        });
        listView.setSelectionFromTop(selectiongPosition, 0);
    }

    void setListToAddToPlaylist() {
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        listView = findViewById(R.id.listView);
        adapterPlaylists= new CustomAdapterPlaylist(getApplicationContext(), R.layout.textview, playlists);
        listView.setAdapter(adapterPlaylists);
        listView.setSelectionFromTop(selectiongPosition, 0);
    }

    void setListToCurentList() {
        //selectingListを表示する
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        listView = findViewById(R.id.listView);
        adapter = new CustomAdapter(getApplicationContext(), R.layout.textview, selectingList);
        listView.setAdapter(adapter);
        //選択したら再生するClickListener
        listView.setOnItemClickListener(currentListClickedHandler);
        if(selectedList == PLAYLIST_SELECTED) {
            listView.setOnItemLongClickListener(removeFromPlaylistMessageLongClickedHandler);
        }
        final Button shuffle = findViewById(R.id.shuffle);
        shuffle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Collections.shuffle(selectingList);
                setListToCurentList();
            }
        });
        shuffle.setVisibility(View.VISIBLE);
        if(! (selectedList==RANDOM_SELECTED)) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }




    private AdapterView.OnItemClickListener currentListClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            if (playingList.equals(selectingList) && playingPosition == position) {
                mediaPlayer.start();
            } else {
                try {
                    playingList.clear();
                    playingList.addAll(selectingList);
                    mediaPlayer.release();
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(getApplicationContext(), playingList.get(position).getURI());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer thisMediaPlayer) {
                            playNext();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                playingPosition = position;
            }
            //playerを表示
            setPlayer();
            updateTitle();
        }
    };

    private AdapterView.OnItemClickListener addListToPlaylistMessageClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            for(int i = 0;i<selectingList.size();i++){
                playlists.get(position).addToList(selectingList.get(i).id);
            }
            //プレイリストを保存
            try {
                FileOutputStream fos = openFileOutput("Playlist"+position, Context.MODE_PRIVATE);
                fos.write(playlists.get(position).getByteData());
                fos.close();
            }catch (Exception e){
            }
        if(selectedList == ARTISTS_SELECTED){
            setListToArtists();
        }else{
            setListToAlbums();
        }

        }
    };

    private AdapterView.OnItemClickListener addToPlaylistMessageClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            playlists.get(position).addToList(playingList.get(playingPosition).id);
            setPlayer();
            updateTitle();

            //プレイリストを保存
            try {
                FileOutputStream fos = openFileOutput("Playlist"+position, Context.MODE_PRIVATE);
                fos.write(playlists.get(position).getByteData());
                fos.close();
            }catch (Exception e){
            }
        }
    };

    private AdapterView.OnItemLongClickListener removeFromPlaylistMessageLongClickedHandler = new AdapterView.OnItemLongClickListener() {
        public boolean onItemLongClick(AdapterView parent, View v, int position, long id) {

            //selectingListを初期化
            playlists.get(currentPlaylistNum).removeFromListById(selectingList.get(position).id);
            selectingList.remove(position);

            setListToCurentList();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //プレイリストを保存
            try {
                FileOutputStream fos = openFileOutput("Playlist"+currentPlaylistNum, Context.MODE_PRIVATE);
                fos.write(playlists.get(currentPlaylistNum).getByteData());
                fos.close();
            }catch (Exception e){
            }
            return true;
        }
    };




    private void updateTitle() {
        final TextView title = findViewById(R.id.playingTitle);
        title.setText(playingList.get(playingPosition).title);
        final TextView artist = findViewById(R.id.playingArtist);
        artist.setText(playingList.get(playingPosition).artist);
        final SeekBar seekBar = findViewById(R.id.seekBar);
        final TextView playingPositionText = ((TextView) findViewById(R.id.playingPosition));
        seekBar.setMax((int) playingList.get(playingPosition).duration / 1000);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean touched = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (touched) {
                    try {
                        mediaPlayer.seekTo(i * 1000);
                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                touched = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                touched = false;
            }
        });
        mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
        mUpdateSeekbar = new Runnable() {
            String str = "";
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition() / 1000);
                if ((int) (mediaPlayer.getCurrentPosition() / 1000) % 60 < 10) {
                    str = "0";
                } else {
                    str = "";
                }
                playingPositionText.setText(String.valueOf((int) (mediaPlayer.getCurrentPosition() / 60000)) + ":" + str + String.valueOf(mediaPlayer.getCurrentPosition() / 1000 % 60));
                mSeekbarUpdateHandler.postDelayed(this, 50);
            }
        };
        mSeekbarUpdateHandler.post(mUpdateSeekbar);
    }

    private void setPlayer() {
        setContentView(R.layout.player);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final Button play = findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    } else {
                        mediaPlayer.start();
                    }
                } catch (Exception e) {
                    initMediaPlayer();
                    mediaPlayer.seekTo(playingTimePosition);
                    mediaPlayer.start();
                }
                updateTitle();
            }
        });

        final Button next = findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playNext();
            }
        });
        final Button prev = findViewById(R.id.prev);
        prev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playPrev();
            }
        });
        final Button add = findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setListToAddToPlaylist();
                listView.setOnItemClickListener(addToPlaylistMessageClickedHandler);
            }
        });

        playerMode = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void permission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.DISABLE_KEYGUARD)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.DISABLE_KEYGUARD)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.DISABLE_KEYGUARD},
                        0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WAKE_LOCK)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WAKE_LOCK)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WAKE_LOCK},
                        0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }



    }

    public void playNext() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
        wakeLock.acquire();

        try {
            mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            if (playingList.size() != playingPosition + 1) {
                mediaPlayer.setDataSource(getApplicationContext(), playingList.get(playingPosition + 1).getURI());
                playingPosition++;
            } else {
                mediaPlayer.setDataSource(getApplicationContext(), playingList.get(0).getURI());
                playingPosition = 0;
            }
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer thisMediaPlayer) {
                    playNext();
                }
            });
            updateTitle();
        } catch (Exception e) {
        }
        wakeLock.release();

    }

    public void playPrev() {
        try {
            mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            if (playingPosition == 0) {
                mediaPlayer.setDataSource(getApplicationContext(), playingList.get(0).getURI());
            } else {
                mediaPlayer.setDataSource(getApplicationContext(), playingList.get(playingPosition - 1).getURI());
                playingPosition--;
            }
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer thisMediaPlayer) {
                    playNext();
                }
            });
            updateTitle();
        } catch (Exception e) {
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (playerMode) {
                    selectingList.clear();
                    selectingList.addAll(playingList);
                    setListToCurentList();
                    playerMode = false;
                } else {
                    switch (selectedList){
                        case ARTISTS_SELECTED:
                            setListToArtists();
                            break;
                        case ALBUMS_SELECTED:
                            setListToAlbums();
                            break;
                        case PLAYLIST_SELECTED:
                            setListToPlaylists();
                            break;
                    }
                }
                return true;

            case R.id.reset:
                selectedList = RANDOM_SELECTED;
                //randomA;;
                try {
                    mediaPlayer.release();
                } catch (Exception e) {
                }
                playingList.clear();
                playingList.addAll(mItems);
                Collections.shuffle(playingList);
                selectingList.clear();
                selectingList.addAll(playingList);
                playingPosition = 0;
                initMediaPlayer();
                setPlayer();
                updateTitle();
                mediaPlayer.start();
                return true;
            case R.id.artistsButton:
                selectedList = ARTISTS_SELECTED;
                selectiongPosition = 0;
                setListToArtists();
                return true;
            case R.id.albumsButton:
                selectedList = ALBUMS_SELECTED;
                selectiongPosition = 0;
                setListToAlbums();
                return true;
            case R.id.playlistsButton:
                selectedList = PLAYLIST_SELECTED;
                selectiongPosition = 0;
                setListToPlaylists();
                return true;
            case R.id.currentPlaying:
                setPlayer();
                updateTitle();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


}


