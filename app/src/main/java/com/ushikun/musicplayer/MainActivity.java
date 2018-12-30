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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<MusicItem> mItems;
    static ArrayList<String> artists;
    static ArrayList<String> albums;
    List<MusicItem> playingList;
    List<MusicItem> selectingList;
    ListView listView;
    ListView listView2;
    CustomAdapter adapter;
    CustomAdapterArtists adapterArtists;
    CustomAdapterPlaylist adapterPlaylists;
    int playingPosition = 0;
    int selectiongPosition = 0;
    MediaPlayer mediaPlayer;
    int playingTimePosition;
    boolean playerMode = false;
    boolean randomPlayMode = false;
    final Handler mSeekbarUpdateHandler = new Handler();
    Runnable mUpdateSeekbar;
    int selectedList = 0;
    public static final int ARTISTS_SELECTED = 0;
    public static final int ALBUMS_SELECTED = 1;
    public static final int PLAYLIST_SELECTED = 2;
    static List<Playlist> playlists;

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
        //MusicItem.getItemsで音楽ファイルを取得する際にartistのリストも取得する
        //そのために初期化しておく
        artists = new ArrayList<>();
        albums = new ArrayList<>();

        playlists = new ArrayList<>();
        playlists.add(new Playlist());
        playlists.add(new Playlist());
        playlists.add(new Playlist());
        playlists.add(new Playlist());
        try {
//            FileOutputStream fos = openFileOutput("test", Context.MODE_PRIVATE);
//            fos.write(playlists.get(0).ids.get(0).byteValue());
//            fos.close();
        }catch (Exception e){

        }

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
        //リストとして表示するためにlistviewとadapaterを設定、artistのリストを表示
        listView = findViewById(R.id.listView);
        adapterArtists = new CustomAdapterArtists(getApplicationContext(), R.layout.textview, artists);
        listView.setAdapter(adapterArtists);
        //クリックリスナー
        listView.setOnItemClickListener(artistsMessageClickedHandler);

        initMediaPlayer();

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
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
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
        listView.setOnItemClickListener(artistsMessageClickedHandler);
        listView.setSelectionFromTop(selectiongPosition, 0);
    }

    void setListToAlbums() {
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        listView = findViewById(R.id.listView);
        adapterArtists = new CustomAdapterArtists(getApplicationContext(), R.layout.textview, albums);
        listView.setAdapter(adapterArtists);
        listView.setOnItemClickListener(albumsMessageClickedHandler);
        listView.setSelectionFromTop(selectiongPosition, 0);
    }

    void setListToPlaylists() {
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        listView = findViewById(R.id.listView);
        adapterPlaylists= new CustomAdapterPlaylist(getApplicationContext(), R.layout.textview, playlists);
        listView.setAdapter(adapterPlaylists);
        listView.setOnItemClickListener(playlistsMessageClickedHandler);
        listView.setSelectionFromTop(selectiongPosition, 0);
    }

    void setListToAddToPlaylist() {
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        listView = findViewById(R.id.listView);
        adapterPlaylists= new CustomAdapterPlaylist(getApplicationContext(), R.layout.textview, playlists);
        listView.setAdapter(adapterPlaylists);
        listView.setOnItemClickListener(addToPlaylistMessageClickedHandler);
        listView.setSelectionFromTop(selectiongPosition, 0);
    }

    void setListToCurentList() {
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        listView = findViewById(R.id.listView);
        adapter = new CustomAdapter(getApplicationContext(), R.layout.textview, selectingList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(mMessageClickedHandler);
    }



    // Create a message handling object as an anonymous class.
    private AdapterView.OnItemClickListener artistsMessageClickedHandler = new AdapterView.OnItemClickListener() {
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

            //artistから曲へひとつ深い階層にいくので戻るボタンを表示
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            selectiongPosition = position;
        }
    };

    private AdapterView.OnItemClickListener albumsMessageClickedHandler = new AdapterView.OnItemClickListener() {
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

            //artistから曲へひとつ深い階層にいくので戻るボタンを表示
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            selectiongPosition = position;
        }
    };

    private AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            if (playingList.equals(selectingList) && playingPosition == position) {
                mediaPlayer.start();
            } else {
                playingList.clear();
                playingList.addAll(selectingList);
                try {
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


    private AdapterView.OnItemClickListener playlistsMessageClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            //selectingListを初期化
            selectingList.clear();

            for (int i = 0; i < mItems.size(); i++) {
                if (playlists.get(position).ids.contains(mItems.get(i).id)) {
                    selectingList.add(mItems.get(i));
                }
            }
            //リストの表示を新しいselectingListに変更
            setListToCurentList();

            //artistから曲へひとつ深い階層にいくので戻るボタンを表示
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    };

    private AdapterView.OnItemClickListener addToPlaylistMessageClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            playlists.get(position).addToList(playingList.get(playingPosition).id);
            setPlayer();
            updateTitle();
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
    }

    public void playNext() {
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
                    setContentView(R.layout.activity_main);
                    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    listView = findViewById(R.id.listView);
                    adapter = new CustomAdapter(getApplicationContext(), R.layout.textview, playingList);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(mMessageClickedHandler);
                    listView.setSelectionFromTop(playingPosition, 0);
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
                try {
                    mediaPlayer.release();
                } catch (Exception e) {
                }
                playingList.clear();
                playingList.addAll(mItems);
                Collections.shuffle(playingList);
                selectingList.clear();
                selectingList.addAll(playingList);
                setContentView(R.layout.activity_main);
                playingPosition = 0;
                setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                listView = findViewById(R.id.listView);
                adapter = new CustomAdapter(getApplicationContext(), R.layout.textview, playingList);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(mMessageClickedHandler);
                listView.setSelectionFromTop(playingPosition, 0);

                initMediaPlayer();
                mediaPlayer.start();

                return true;
            case R.id.artistsButton:
                selectiongPosition = 0;
                setListToArtists();
                selectedList = ARTISTS_SELECTED;
                return true;
            case R.id.albumsButton:
                selectiongPosition = 0;
                setListToAlbums();
                selectedList = ALBUMS_SELECTED;
                return true;
            case R.id.playlistsButton:
                selectiongPosition = 0;
                setListToPlaylists();
                selectedList = PLAYLIST_SELECTED;
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

