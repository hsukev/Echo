package com.stream.jerye.queue.room;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.firebase.iid.FirebaseInstanceId;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.squareup.picasso.Picasso;
import com.stream.jerye.queue.PreferenceUtility;
import com.stream.jerye.queue.R;
import com.stream.jerye.queue.firebase.FirebaseEventBus;
import com.stream.jerye.queue.lobby.LobbyActivity;
import com.stream.jerye.queue.lobby.User;
import com.stream.jerye.queue.profile.SpotifyProfileAsyncTask;
import com.stream.jerye.queue.room.messagePage.MessageFragment;
import com.stream.jerye.queue.room.musicPage.MusicFragment;
import com.stream.jerye.queue.room.musicPage.SimpleTrack;

import butterknife.BindView;
import butterknife.ButterKnife;
import kaaes.spotify.webapi.android.models.UserPrivate;

public class RoomActivity extends AppCompatActivity implements
        MusicPlayerListener,
        FirebaseEventBus.FirebaseQueueAdapterHandler,
        FirebaseEventBus.FirebaseUserAdapterHandler,
        SpotifyProfileAsyncTask.SpotifyProfileCallback {
    private EchoPlayer mPlayer;
    private String TAG = "MainActivity.java";
    private String mToken, mRoomTitle, intentAction;
    private AnimatedVectorDrawable playToPause, pauseToPlay;
    private FirebaseEventBus.MusicDatabaseAccess mMusicDatabaseAccess;
    private FirebaseEventBus.UserDatabaseAccess mUserDatabaseAccess;
    private UserAdapter mUserAdapter;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service Connected");
            mPlayer = ((PlayerService.PlayerBinder) service).getService(RoomActivity.this, RoomActivity.this, mToken);
            Log.d(TAG, "peeking from activity result");
//            mMusicDatabaseAccess.peek();
            mMusicDatabaseAccess.addChildListener();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Service DisConnected");

            mPlayer = null;
        }
    };

    @BindView(R.id.room_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.room_toolbar_profile)
    ImageView mToolbarProfile;
    @BindView(R.id.room_toolbar_title)
    TextView mToolbarTitle;
    @BindView(R.id.view_pager)
    ViewPager mPager;
    @BindView(R.id.previous_button)
    ImageView mPreviousButton;
    @BindView(R.id.play_button)
    ImageView mPlayButton;
    @BindView(R.id.next_button)
    ImageView mNextButton;
    @BindView(R.id.music_seekbar)
    SeekBar mSeekBar;
    @BindView(R.id.music_current_title)
    TextView mCurrentMusicTitle;
    @BindView(R.id.music_current_artist)
    TextView mCurrentMusicArtist;
    @BindView(R.id.current_album_image)
    ImageView mCurrentAlbumImage;
    @BindView(R.id.music_duration)
    TextView mMusicDuration;
    @BindView(R.id.music_progress)
    TextView mMusicProgress;
    @BindView(R.id.profile_drawer)
    DrawerLayout mDrawer;
    @BindView(R.id.profile_name)
    TextView mProfileName;
    @BindView(R.id.profile_picture)
    ImageView mProfilePicture;
    @BindView(R.id.profile_logout)
    TextView mProfileLogoutButton;
    @BindView(R.id.users_list)
    RecyclerView mUsersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_activity);
        ButterKnife.bind(this);

        PreferenceUtility.initialize(this);
        mToken = PreferenceUtility.getPreference(PreferenceUtility.SPOTIFY_TOKEN);

        Intent intent = getIntent();
        intentAction = intent.getAction();


        mMusicDatabaseAccess = new FirebaseEventBus.MusicDatabaseAccess(this, this);
        mUserDatabaseAccess = new FirebaseEventBus.UserDatabaseAccess(this, this);

        playToPause = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_play_to_pause);
        pauseToPlay = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_pause_to_play);

        mPager.setAdapter(new SimpleFragmentPageAdapter(getSupportFragmentManager()));

        SpotifyProfileAsyncTask asyncTask = new SpotifyProfileAsyncTask(this, this, mToken);
        asyncTask.execute();
        mUserDatabaseAccess.getUsers();

        if (LobbyActivity.ACTION_NEW_USER.equals(intentAction)) {
            Bundle bundle = intent.getExtras();
            mRoomTitle = bundle.getString("room title");
        } else if (LobbyActivity.ACTION_EXISTING_USER.equals(intentAction)) {
            mRoomTitle = PreferenceUtility.getPreference(PreferenceUtility.ROOM_TITLE);
        }

        setActionBar(mToolbar);
        mToolbarTitle.setText(mRoomTitle);

    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(PlayerService.getIntent(this), mServiceConnection, Activity.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "FirebaseInstanceId Token: " + FirebaseInstanceId.getInstance().getToken());
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "play button clicked");

                if (mPlayer == null) {
                    Log.d("MainActivity.java", "mPlayer is null");
                    return;
                }

                if (mPlayer.isPaused()) {
                    mPlayer.resume();
                    mPlayButton.setImageDrawable(playToPause);
                    playToPause.start();
                } else if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                    mPlayButton.setImageDrawable(pauseToPlay);
                    pauseToPlay.start();
                } else {
                    mPlayer.play();
                    mPlayButton.setImageDrawable(playToPause);
                    playToPause.start();
                }
            }
        });


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPlayer.resume();
            }
        });

        mUserAdapter = new UserAdapter(this);
        mUsersList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        mUsersList.setAdapter(mUserAdapter);

    }


    @Override
    public void createProfile(UserPrivate userPrivate) {
        String profileName = userPrivate.display_name;
        String profilePicture = userPrivate.images.get(0).url;
        String profileId = userPrivate.id;

        String[] profile = {profileName, profilePicture, profileId};

        PreferenceUtility.setPreference(PreferenceUtility.PROFILE_GENERIC, profile);

        mProfileName.setText(profileName);
        Picasso.with(this).load(profilePicture).into(mProfilePicture);

        // Check if user is unique first
        if (LobbyActivity.ACTION_NEW_USER.equals(intentAction)) {
            User newUser = new User(profileName, profileId, FirebaseInstanceId.getInstance().getToken(), profilePicture);
            mUserDatabaseAccess.push(newUser);
        }
    }

    @Override
    public void getUser(User user) {
        mUserAdapter.addUser(user);
    }

    private class SimpleFragmentPageAdapter extends FragmentStatePagerAdapter {

        private SimpleFragmentPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return MusicFragment.newInstance();
            } else {
                return MessageFragment.newInstance();
            }

        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

    }


//    @Override
//    public void peekedResult(List<SimpleTrack> list) {
//        mPlayer.setNextTrack(list);
//    }

    @Override
    public void getSongProgress(int positionInMs) {
        mSeekBar.setProgress(positionInMs);
        int totalInS = positionInMs / 1000;
        int minutes = totalInS / 60;
        int seconds = totalInS % 60;
        mMusicProgress.setText(minutes + ":" + (seconds < 10 ? "0" : "") + seconds);
    }

    @Override
    public void getSongDuration(int durationInMs) {
        Log.d(TAG, "setting max: " + durationInMs);
        mSeekBar.setMax(durationInMs);

        int totalInS = durationInMs / 1000;
        int minutes = totalInS / 60;
        int seconds = totalInS % 60;
        mMusicDuration.setText(minutes + ":" + (seconds < 10 ? "0" : "") + seconds);
    }

    @Override
    public void displayCurrentTrack(SimpleTrack simpleTrack) {
        mCurrentMusicTitle.setText(simpleTrack.getName());
        mCurrentMusicArtist.setText(simpleTrack.getArtistName());
        Picasso.with(this).load(simpleTrack.getAlbumImage()).into(mCurrentAlbumImage);
    }

    //    @Override
//    public void queueNextSong(SimpleTrack oldTrackToRemove) {
//        Log.d(TAG, "queueNextSong called from player context end");
//        mMusicDatabaseAccess.remove(oldTrackToRemove);
//        mMusicDatabaseAccess.peek();
//    }

    @Override
    public void enqueue(SimpleTrack simpleTrack) {
        mPlayer.addTrack(simpleTrack);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mServiceConnection);

    }

    public void profileLogout(View v) {
        AuthenticationClient.clearCookies(this);
//        prefs.edit()
//                .remove("token")
//                .remove("profile picture")
//                .remove("profile name")
//                .remove("profile id")
//                .remove("room key")
//                .apply();

        Intent exit = new Intent(this, LobbyActivity.class);
        startActivity(exit);
    }


    public void openProfileDrawer(View v) {
        mDrawer.openDrawer(Gravity.START);
    }

    public void playNext(View v) {
        if (mPlayer == null) {
            Log.d("MainActivity.java", "mPlayer is null");
            return;
        }
        mPlayer.next();
    }

    public void playPrevious(View v) {
        if (mPlayer == null) {
            Log.d("MainActivity.java", "mPlayer is null");
            return;
        }
        mPlayer.previous();
    }

}
