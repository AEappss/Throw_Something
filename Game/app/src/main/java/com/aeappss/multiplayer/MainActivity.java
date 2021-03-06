package com.aeappss.multiplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;


import android.os.Handler;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aeappss.multiplayer.hitObjects.Animation;
import com.aeappss.multiplayer.hitObjects.Adapter;
import com.aeappss.multiplayer.hitObjects.RecyclerView;
import com.aeappss.multiplayer.hitObjects.AnimationStation;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.xw.repo.BubbleSeekBar;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.view.View.INVISIBLE;
import static com.aeappss.multiplayer.R.id.imageView;
import static java.lang.Math.abs;
import static java.lang.Thread.sleep;
import android.os.Vibrator;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener, RealTimeMessageReceivedListener,
        RoomStatusUpdateListener, RoomUpdateListener, OnInvitationReceivedListener, LocationListener, SensorEventListener,  DiscreteScrollView.ScrollStateChangeListener<Adapter.ViewHolder>,
        DiscreteScrollView.OnItemChangedListener<Adapter.ViewHolder>{

    private LocationManager locationManager;
    private String provider;
    private double lat;
    private double lng;
    public static double[] randomObjCoord = new double[2]; // lat - 0, lng - 1
    public static double angleRandomObj;

    final static String TAG = "TAG";

    // Request codes for the UIs that we show with startActivityForResult:
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = true;

    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    String mRoomId = null;

    // Are we playing in multiplayer mode?
    boolean mMultiplayer = false;

    // The participants in the currently active game
    ArrayList<Participant> mParticipants = null;

    // My participant ID in the currently active game
    String mMyId = null;

    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    String mIncomingInvitationId = null;

    // Message buffer for sending messages
    byte[] mMsgBuf = new byte[100];
    private Location location;

    public static ArrayList<Player> players = new ArrayList<>(); //all opponents except player
    public static Player player;

    //To DO add distance to Player class
    //float mDistance = -1; //for check -1
    float mDistance = -1;
    public static double [] mOpponentCoord = new double[2];
    LocationRequest mLocationRequest;
    public int hit = -1; //default
    TextView arText;
    ImageView personImage;
    ImageButton inviteButton;
    ImageButton seeInvitationsButton;
    ImageButton cameraGame;
    ImageButton exitButton;
    //ImageButton backButton;

    Button settingsButton;
    TextView inviteText;
    TextView seeInvitationText;
    TextView improveSkills;

    TextView text;
    ImageButton mapButton;
    public static int playerVisibleOnCam = -1;
    private static int visibleOnCam = -1;
    private List<Animation> forecasts;

    //private ForecastView forecastView;
    private DiscreteScrollView picker;

    private RecyclerView view;

    BubbleSeekBar bubbleSeekBar;
    TextView seekbarTextView;
    ImageButton changeLocationBtn;

    ImageButton addButton;
    public static Vibrator v;

    @SuppressLint("WrongViewCast")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //width = getWindowManager().getDefaultDisplay().getWidth();
        //height = getWindowManager().getDefaultDisplay().getHeight();
        switchToMainScreen();

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        epicDialog = new Dialog(this);


        addButton = (ImageButton) findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(getApplicationContext(), Camera2Activity.class);
                startActivity(mainIntent);
            }
        });

        // Create the Google Api Client with access to Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addApi(LocationServices.API)
                .setViewForPopups(findViewById(android.R.id.content))
                .build();

        // set up a click listener for everything we care about
        for (int id : CLICKABLES) {
            findViewById(id).setOnClickListener(this);
        }

        inviteButton = (ImageButton) findViewById(R.id.button_invite_players);
        seeInvitationsButton = (ImageButton) findViewById(R.id.button_see_invitations);
        cameraGame = (ImageButton) findViewById(R.id.button_camera_game);
        settingsButton = (Button) findViewById(R.id.button_settings);
        exitButton = (ImageButton) findViewById(R.id.exit_button);
        //backButton = (ImageButton) findViewById(R.id.button_back);

        inviteText = (TextView) findViewById(R.id.invite_text);
        seeInvitationText = (TextView) findViewById(R.id.see_invitation_text);
        improveSkills = (TextView) findViewById(R.id.improve_skills);

        Typeface myFont = Typeface.createFromAsset(getAssets(), "fonts/rocko.ttf");

        text = (TextView) findViewById(R.id.textView2);
        text.setTypeface(myFont);

        mTextureView = (TextureView) findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);


        bubbleSeekBar = (BubbleSeekBar) findViewById(R.id.seekBar);
        seekbarTextView = (TextView) findViewById(R.id.txtView);
        seekbarTextView.setTypeface(myFont);
        changeLocationBtn = (ImageButton) findViewById(R.id.changeLocationBtn);


        changeLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bubbleSeekBar.setVisibility(View.VISIBLE);
                bubbleSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
                    @Override
                    public void onProgressChanged(int progress, float progressFloat) {
                        seekbarTextView.setText((String.valueOf(progressFloat)) + " m");
                    }

                    @Override
                    public void getProgressOnActionUp(int progress, float progressFloat) {
                        seekbarTextView.setText((String.valueOf(progressFloat)) + " m");
                        android.os.SystemClock.sleep(1000);
                        bubbleSeekBar.setVisibility(INVISIBLE);
                    }

                    @Override
                    public void getProgressOnFinally(int progress, float progressFloat) {

                    }
                });
            }
        });


        view = (RecyclerView) findViewById(R.id.forecast_view);

        forecasts = AnimationStation.get().getForecasts();
        picker = (DiscreteScrollView) findViewById(R.id.forecast_city_picker);
        picker.setSlideOnFling(true);
        picker.setAdapter(new Adapter(forecasts));
        picker.addOnItemChangedListener(this);
        picker.addScrollStateChangeListener(this);
        picker.scrollToPosition(2);
        picker.setItemTransitionTimeMillis(DiscreteScrollViewOptions.getTransitionTime());
        picker.setItemTransformer(new ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .build());

        //forecastView.setForecast(forecasts.get(0));
        Log.i("msg", "po scrolo");
    }


    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        Log.i("BACKAS", "Nuspausta atgal");
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.start_button:
                switchToScreen(R.id.screen_main);
                inviteButton.setVisibility(View.VISIBLE);
                seeInvitationsButton.setVisibility(View.VISIBLE);
                cameraGame.setVisibility(View.VISIBLE);
                //settingsButton.setVisibility(View.VISIBLE);
                //backButton.setVisibility(View.VISIBLE);

                inviteText.setVisibility(View.VISIBLE);
                seeInvitationText.setVisibility(View.VISIBLE);
                improveSkills.setVisibility(View.VISIBLE);
                break;
            case R.id.exit_button:
                System.exit(0);
                break;
            case R.id.button_invite_players:
                // show list of invitable players
                intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 3);
                switchToScreen(R.id.screen_wait);
                startActivityForResult(intent, RC_SELECT_PLAYERS);
                break;
            case R.id.button_see_invitations:
                // show list of pending invitations
                intent = Games.Invitations.getInvitationInboxIntent(mGoogleApiClient);
                switchToScreen(R.id.screen_wait);
                startActivityForResult(intent, RC_INVITATION_INBOX);
                break;
            case R.id.button_accept_popup_invitation:
                // user wants to accept the invitation shown on the invitation popup
                // (the one we got through the OnInvitationReceivedListener).
                acceptInviteToRoom(mIncomingInvitationId);
                mIncomingInvitationId = null;
                break;

            case R.id.button_camera_game:
                Log.i("CAMERA", "clicked");

                Intent cameraIntent = new Intent(getApplicationContext(), Camera2Activity.class);
                //Intent cameraIntent = new Intent(getApplicationContext(), LoadModel.class);

                startActivity(cameraIntent);
                finish();
                break;
            case R.id.button_settings:
                Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingsIntent);
                finish();
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {

        super.onActivityResult(requestCode, responseCode, intent);
        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                // got the result from the "select players" UI -- ready to create the room
                handleSelectPlayersResult(responseCode, intent);
                break;
            case RC_INVITATION_INBOX:
                handleInvitationInboxResult(responseCode, intent);
                break;
            case RC_WAITING_ROOM:
                // got the result from the "waiting room" UI.
                if (responseCode == Activity.RESULT_OK) {
                    // ready to start playing
                    Log.d(TAG, "Starting game (waiting room returned OK).");
                    startGame(true);
                } else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    // player indicated that they want to leave the room
                    leaveRoom();
                } else if (responseCode == Activity.RESULT_CANCELED) {
                    // Dialog was cancelled (user pressed back key, for instance). In our game,
                    // this means leaving the room too. In more elaborate games, this could mean
                    // something else (like minimizing the waiting room UI).
                    leaveRoom();
                }
                break;

            case RC_SIGN_IN:
                Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                        + responseCode + ", intent=" + intent);
                mSignInClicked = false;
                mResolvingConnectionFailure = false;
                if (responseCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                } else {
                    BaseGameUtils.showActivityResultError(this, requestCode, responseCode, R.string.signin_other_error);
                }
                break;
        }
        super.onActivityResult(requestCode, responseCode, intent);
    }

    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            switchToScreen(R.id.screen_main);
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.d(TAG, "Invitee count: " + invitees.size());

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        Log.d(TAG, "Creating room...");
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.addPlayersToInvite(invitees);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        resetGameVars();
        Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            switchToScreen(R.id.screen_main);
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        acceptInviteToRoom(inv.getInvitationId());
    }

    // Accept the given invitation.
    void acceptInviteToRoom(String invId) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        resetGameVars();
        Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());
    }

    // Activity is going to the background. We have to leave the current room.
    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");
        super.onStop();
    }

    @Override
    public void onStart() {
        if (!mGoogleApiClient.isConnected()) {
            Log.d(TAG, "Connecting client.");
            //switchToScreen(R.id.screen_wait);
            mGoogleApiClient.connect();
        } else {
            Log.w(TAG,
                    "GameHelper: client was already connected on onStart()");
        }
        super.onStart();
    }

    Dialog exitDialog;
    ImageView closePopupImg;
    ImageButton buttonCheck;
    ImageButton closeButton;
    // Handle back key to make sure we cleanly leave a game if we are in the middle of one
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        exitDialog = new Dialog(this);
        if (keyCode == KeyEvent.KEYCODE_BACK && mCurScreen == R.id.screen_game) {

            exitDialog.setContentView(R.layout.exit_dialog_layout);
            closePopupImg = (ImageView) exitDialog.findViewById(R.id.closeImg);
            closeButton = (ImageButton) exitDialog.findViewById(R.id.closeButton);
            buttonCheck = (ImageButton) exitDialog.findViewById(R.id.checkButton);
            buttonCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exitDialog.dismiss();
                    leaveRoom();
                }
            });

            closePopupImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exitDialog.dismiss();
                }
            });

            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exitDialog.dismiss();
                }
            });
            exitDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            exitDialog.show();

            //leaveRoom();
            return true;
        }
        return super.onKeyDown(keyCode, e);
    }

    // Leave the room.
    void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        mSecondsLeft = 0;
        stopKeepingScreenOn();
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            mRoomId = null;
            switchToScreen(R.id.screen_wait);
        } else {
            switchToMainScreen();
        }
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, MIN_PLAYERS);

        // show waiting room UI
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    // Called when we get an invitation to play a game. We react by showing that to the user.
    @Override
    public void onInvitationReceived(Invitation invitation) {

        mIncomingInvitationId = invitation.getInvitationId();
        ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
                invitation.getInviter().getDisplayName() + " " +
                        getString(R.string.is_inviting_you));
        switchToScreen(mCurScreen); // This will show the invitation popup
    }

    @Override
    public void onInvitationRemoved(String invitationId) {

        if (mIncomingInvitationId.equals(invitationId) && mIncomingInvitationId != null) {
            mIncomingInvitationId = null;
            switchToScreen(mCurScreen); // This will hide the invitation popup
        }

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected() called. Sign in successful!");

        Log.d(TAG, "Sign-in succeeded.");

        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

        if (connectionHint != null) {
            Log.d(TAG, "onConnected: connection hint provided. Checking for invite.");
            Invitation inv = connectionHint
                    .getParcelable(Multiplayer.EXTRA_INVITATION);
            if (inv != null && inv.getInvitationId() != null) {
                // retrieve and cache the invitation ID
                Log.d(TAG, "onConnected: connection hint has a room invite!");
                acceptInviteToRoom(inv.getInvitationId());
                return;
            }
        }
        switchToMainScreen();


        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location != null) {
            lat = (float) location.getLatitude();
            lng = (float) location.getLongitude();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient,
                    connectionResult, RC_SIGN_IN, getString(R.string.signin_other_error));
        }

        //switchToScreen(R.id.screen_sign_in);
    }

    public static boolean onConnectedToRoom = false;
    @Override
    public void onConnectedToRoom(Room room) {
        Log.d(TAG, "onConnectedToRoom.");

        //get participants and my ID:
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));
        Log.i("onConnectedToRoom", mMyId);
        // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
        if(mRoomId==null)
            mRoomId = room.getRoomId();

        initialisePlayersData();
        onConnectedToRoom = true;

    }

    // Called when successfully left the room (this happens a result of voluntarily leaving
    // via a call to leaveRoom(). If get disconnected, we get onDisconnectedFromRoom()).
    @Override
    public void onLeftRoom(int statusCode, String roomId) {
        // we have left the room; return to main screen.
        Log.d(TAG, "onLeftRoom, code " + statusCode);
        switchToMainScreen();
    }

    // Called when we get disconnected from the room. We return to the main screen.
    @Override
    public void onDisconnectedFromRoom(Room room) {
        mRoomId = null;
        showGameError();
    }

    // Show error message about game being cancelled and return to main screen.
    void showGameError() {
        BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
        switchToMainScreen();
    }

    // Called when room has been created
    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            showGameError();
            return;
        }

        // save room ID so we can leave cleanly before the game starts.
        mRoomId = room.getRoomId();

        // show the waiting room UI
        showWaitingRoom(room);
    }

    // Called when room is fully connected.
    @Override
    public void onRoomConnected(int statusCode, Room room) {
        Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }
        updateRoom(room);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
    }

    @Override
    public void onPeerDeclined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onP2PDisconnected(String participant) {
    }

    @Override
    public void onP2PConnected(String participant) {
    }

    @Override
    public void onPeerJoined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> peersWhoLeft) {
        updateRoom(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        updateRoom(room);
    }

    @Override
    public void onRoomConnecting(Room room) {
        updateRoom(room);
    }

    @Override
    public void onPeersConnected(Room room, List<String> peers) {
        updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        updateRoom(room);
    }

    void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
        if (mParticipants != null) {
            updatePeerScoresDisplay();
        }
    }

    // Current state of the game:
    int mSecondsLeft = -1; // how long until the game ends (seconds)
    final static int GAME_DURATION = 36000; // game duration, seconds.
    int mScore = 0; // user's current score

    // Reset game variables in preparation for a new game.
    void resetGameVars() {
        mSecondsLeft = GAME_DURATION;
        mScore = 0;
        mParticipantScore.clear();
        mFinishedParticipants.clear();
    }

    public String setTeam(String id){
        for(int i = 0; i < mParticipants.size(); i++){
            if(id.equals(mParticipants.get(i).getParticipantId())){
                if(i % 2 == 0){
                    return "A";
                }else{
                    return  "B";
                }
            }
        }
        return "NULL";
    }
    //This method is used to create Players.
    void initialisePlayersData(){
        Log.i("mParticipantsSize", " "  + mParticipants.size());
        for(int i = 0; i < mParticipants.size(); i++){
            if(mMyId.equals(mParticipants.get(i).getParticipantId())){
                player = new Player();
                player.setName(mParticipants.get(i).getDisplayName());
                player.setImageUrl(mParticipants.get(i).getIconImageUrl());
                player.setId(mParticipants.get(i).getParticipantId());
                player.setTeam(setTeam(mMyId));
                Log.i("InitPlayerData", player.toString());
            }else {
                Player player = new Player();
                player.setName(mParticipants.get(i).getDisplayName());
                player.setImageUrl(mParticipants.get(i).getIconImageUrl());
                player.setId(mParticipants.get(i).getParticipantId());
                player.setTeam(setTeam(mParticipants.get(i).getParticipantId()));
                players.add(player);
            }
        }
    }

    //This method is used to update Player coordinates.
    void initialisePlayersData(double lat, double longittude, String id){
        for (int i = 0; i < players.size(); i++){
            if (players.get(i).getId().equals(id)){
                players.get(i).setLatitude(lat);
                players.get(i).setLongitude(longittude);
            }
        }
    }

    ImageView radar;
    // Start the gameplay phase of the game.
    void startGame(boolean multiplayer) {
        mMultiplayer = multiplayer;
        updateScoreDisplay();
        broadcastScore(false);

        randomObjCoord[0] = lat + 0.000010; // lat - 0, lng - 1
        randomObjCoord [1] = lng + 0.000010;

        rlMain = (RelativeLayout ) findViewById(R.id.layout);

        radar = new ImageView(this);
        radar.setImageResource(R.drawable.radar1);
        params = new RelativeLayout.LayoutParams(1090, 1090);// mastelis figuros
        params.topMargin = 10;
        params.width = 500;
        params.height = 400;
        radar.setMinimumWidth(200);
        params.rightMargin = 0; // per viduri ekrano,jei 0laipsniu paklaida
        rlMain.addView(radar, params);


        switchToScreen(R.id.screen_game);
        //findViewById(R.id.button_click_me).setVisibility(View.VISIBLE);
        arText = (TextView) findViewById(R.id.ARtext);
        personImage = (ImageView) findViewById(imageView);

        // run the gameTick() method every second to update the game.
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSecondsLeft <= 0)
                    return;
                gameTick();
                h.postDelayed(this, 1000);
            }
        }, 1000);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senRotationVect = senSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        senSensorManager.registerListener(this, senRotationVect, SensorManager.SENSOR_DELAY_GAME);

        firstBar = (ProgressBar)findViewById(R.id.firstBar);
        textViewThrowing = (TextView) findViewById(R.id.textViewThrowing);

        heart1 = (ImageView) findViewById(R.id.heart1);
        heart2 = (ImageView) findViewById(R.id.heart2);
        heart3 = (ImageView) findViewById(R.id.heart3);
        Typeface myFont = Typeface.createFromAsset(getAssets(), "fonts/rocko.ttf");
        ballCounterText = (TextView) findViewById(R.id.textView);
        ballCounterText.setTypeface(myFont);

        imageButton = (ImageButton) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibleOnCam = playerVisibleOnCam;
                int ballNum = Integer.valueOf((String) ballCounterText.getText());
                if(ballNum > 0){
                    --ballNum;
                    ballCounterText.setText(String.valueOf(ballNum));
                    //ballCounterText1.setText(String.valueOf(ballNum));
                    firstBar.setVisibility(View.VISIBLE);
                    firstBar.setMax(35);
                    firstBar.setProgress(0);
                    //firstBar.setProgress(3);
                    imageButton.setVisibility(INVISIBLE);
                    ballCounterText.setVisibility(INVISIBLE);
                    textViewThrowing.setVisibility(INVISIBLE);
                    pressedThrow = true;
                }
                if(ballNum == 0){
                    // galbut zaidimo eigoje gaus kamuoliu daugiau, tada atsetinti
                    imageButton.setClickable(false);
                }
            }
        });

        final FloatingActionButton mapAction = (FloatingActionButton) findViewById(R.id.map);
        mapAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /// MAP INTENT
                Intent mapsIntent = new Intent(getApplicationContext(), MapsActivity.class);
                ///mapsIntent.putExtra("OpponentCordinate", mOpponentCoord);
                startActivity(mapsIntent);
            }
        });
        mapButton = (ImageButton) findViewById(R.id.action_map);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /// MAP INTENT
                Intent homeIntent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(homeIntent);
            }
        });
    }

    void gameTick() {
        if (mSecondsLeft > 0)
            --mSecondsLeft;

        if (mSecondsLeft <= 0) {
            // finish game
            findViewById(R.id.button_click_me).setVisibility(View.GONE);
            broadcastScore(true);
        }
    }

    // indicates the player scored one point
    void scoreOnePoint() {
        if (mSecondsLeft <= 0)
            return; // too late!
        ++mScore;
        updateScoreDisplay();
        updatePeerScoresDisplay();

        // broadcast our new score to our peers
        broadcastScore(false);
    }

    // Score of other participants. We update this as we receive their scores
    // from the network.
    Map<String, Integer> mParticipantScore = new HashMap<String, Integer>();

    // Coordinte of participants
    Map<String, Integer> mParticipantCoord = new HashMap<String, Integer>();

    // Participants who sent us their final score.
    Set<String> mFinishedParticipants = new HashSet<String>();

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        hit = 'N';
        byte[] buf = rtm.getMessageData();
        String sender = rtm.getSenderParticipantId();
        Log.d(TAG, "Message received: " + (char) buf[0] + "/" + (int) buf[1]);

        if (buf[0] == 'F' || buf[0] == 'U') {
            // score update.
            int existingScore = mParticipantScore.containsKey(sender) ? mParticipantScore.get(sender) : 0;
            int thisScore = (int) buf[1];

            byte [] temp = new byte[8];
            for (int i = 2; i <  buf.length; i++) {
                if(i == 10){
                    break;
                }
                temp[i - 2] = buf[i];
            }

            // Get parcipiant coordinate (longitude) from buffer
            byte [] temp2 = new byte[8];
            for (int i = 10; i <  buf.length - 2; i++) {
                if(i == 18){
                    break;
                }
                temp2[i - 10] = buf[i];
            }

            //Check if player was hit
            if(getMyIndex(buf[18])){
                Log.i("HIT", "Player was hit" + buf[18]);
                wasHit();
            }else{
                Log.i("HIT", "Player missed shot" + buf[18]);
            }

            //Get id player to recognized it.
            String id = "";
            for(int i = 19; i < buf.length; i++){
                if(i == mMyId.length() + 19){
                    break;
                }
                id += (char) buf[i];
            }

            // Get player team
            String team = "";
            team += (char) buf[mMyId.length() + 19];

            for(int i = 0; i < players.size(); i++){
                Log.i("ZaidejuInfo", players.get(i).toString());
            }
            Log.i("ZaidejuInfo", player.toString());

            mOpponentCoord[0] = ByteBuffer.wrap(temp).getDouble(); // Lattitude
            mOpponentCoord[1] = ByteBuffer.wrap(temp2).getDouble();// Longitude

            Log.i("Koordinates", "Mano" + lat +  " " + lng +  " Oponento "  + mOpponentCoord[0] +  " "  + mOpponentCoord[1]);
            initialisePlayersData(ByteBuffer.wrap(temp).getDouble(),ByteBuffer.wrap(temp2).getDouble(), id);

            if(onConnectedToRoom){
                player.setLatitude(lat);
                player.setLongitude(lng);
                for(int i = 0; i < players.size(); i++){
                    Log.i("LokacijaP", " " + i  +  " "+ players.get(i).getLatitude() + " " + players.get(i).getLongitude());
                    Location location2 = new Location("locationB");
                    location2.setLatitude(players.get(i).getLatitude());
                    location2.setLongitude(players.get(i).getLongitude());
                    mDistance  = location.distanceTo(location2);
                    players.get(i).setDistance(location.distanceTo(location2));
                    Log.i("ATSTUMAS", players.get(i).getName() + " "+ "lat " +
                            players.get(i).getLatitude() + " lng " + players.get(i).getLongitude() + "\n" +
                            "AS " + player.getName() + " lat " + player.getLatitude() + "lng " + player.getLongitude()
                            + "\n" + "Atstumas"  + players.get(i).getDistance());
                }
            }

            if (thisScore > existingScore) {

                mParticipantScore.put(sender, thisScore);
            }

            // update the scores on the screen
            updatePeerScoresDisplay();

            // if it's a final score, mark this participant as having finished
            // the game
            if ((char) buf[0] == 'F') {
                Log.i("OPPONENT score", String.valueOf(thisScore) + mParticipantScore.get(sender));
                mFinishedParticipants.add(rtm.getSenderParticipantId());

                if (thisScore > mScore){
                    Log.i("LAIMEJO - ", "TU");
                } else {
                    Log.i("LAIMEJO - ", "AS");
                }
            }
        }
    }

    public boolean getMyIndex(int index){
        for (int i = 0;  i < mParticipants.size(); i++){
            if(mMyId.equals(mParticipants.get(i).getParticipantId())){
                if (index == i){
                    return true;
                }
            }
        }
        return false;
    }

    // Broadcast my score to everybody else.
    void broadcastScore(boolean finalScore) {
        if (!mMultiplayer)
            return; // playing single-player mode

        // First byte in message indicates whether it's a final score or not
        mMsgBuf[0] = (byte) (finalScore ? 'F' : 'U');

        // Second byte is the score.
        mMsgBuf[1] = (byte) mScore;

        // Add paricipiant coordinate (lattitude) to buffer.
        byte[] tempBytes = new byte[8];
        ByteBuffer.wrap(tempBytes).putDouble(lat);
        for (int i = 0; i <  tempBytes.length; i++){
            mMsgBuf[i + 2] = tempBytes[i];
        }

        // Add paricipiant coordinate (longitude) to buffer.
        ByteBuffer.wrap(tempBytes).putDouble(lng);
        for (int i = 0; i <  tempBytes.length; i++){
            mMsgBuf[i + 10] = tempBytes[i];
        }

        // if player hit opponent
        mMsgBuf[18] = (byte) hit;

        // player ID
        byte [] bytes = mMyId.getBytes();
        for(int i = 0; i < bytes.length; i++){
            if(i == mMyId.length()){
                break;
            }
            mMsgBuf[19 + i] = bytes[i];
        }

        // player team
        String team = player.getTeam();
        byte [] bytes2 = team.getBytes();
        for(int i = 0; i < bytes2.length; i++){
            mMsgBuf[19 + mMyId.length() + i] = bytes2[i];
        }

        // Send to every other participant.
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            if (finalScore) {
                Log.i("MY score", String.valueOf(mScore));

                // final score notification must be sent via reliable message
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, mMsgBuf,
                        mRoomId, p.getParticipantId());
            } else {
                // it's an interim score notification, so we can use unreliable
                Games.RealTimeMultiplayer.sendUnreliableMessage(mGoogleApiClient, mMsgBuf, mRoomId,
                        p.getParticipantId());
            }
        }
        hit = -1;
    }

    final static int[] CLICKABLES = {
            R.id.button_accept_popup_invitation, R.id.button_invite_players,
            R.id.button_see_invitations, R.id.button_click_me, R.id.button_camera_game,
            R.id.button_settings, R.id.start_button, R.id.exit_button
    };

    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {
            R.id.screen_game, R.id.screen_menu, R.id.screen_main,
            R.id.screen_wait
    };
    int mCurScreen = -1;

    void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others.
        for (int id : SCREENS) {
            findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
        }
        mCurScreen = screenId;

        // should we show the invitation popup?
        boolean showInvPopup;
        if (mIncomingInvitationId == null) {
            // no invitation, so no popup
            showInvPopup = false;
        } else if (mMultiplayer) {
            // if in multiplayer, only show invitation on main screen
            showInvPopup = (mCurScreen == R.id.screen_main);
        } else {
            // single-player: show on main screen and gameplay screen
            showInvPopup = (mCurScreen == R.id.screen_main || mCurScreen == R.id.screen_game);
        }
        findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
    }

    void switchToMainScreen() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            switchToScreen(R.id.screen_menu);
            text.setVisibility(View.VISIBLE);
        }
    }

    // updates the label that shows my score
    void updateScoreDisplay() {
        ((TextView) findViewById(R.id.my_score)).setText(formatScore(mScore));
    }

    // formats a score as a three-digit number
    String formatScore(int i) {
        if (i < 0)
            i = 0;
        String s = String.valueOf(i);
        return s.length() == 1 ? "00" + s : s.length() == 2 ? "0" + s : s;
    }

    double opponentLat;
    double opponentLong;
    // updates the screen with the scores from our peers
    public static  boolean peer = false;
    void updatePeerScoresDisplay() {
        String teams = "";

        if(onConnectedToRoom){
            //((TextView) findViewById(R.id.score0)).setText("\n" + formatScore(mScore) + " - Me " + player.getTeam());
        }

        int[] arr = {
                R.id.score1, R.id.score2, R.id.score3
        };
        int i = 0;

        if (mRoomId != null) {
            //printMap(player.getDistPlayers());
            for (Participant p : mParticipants) {
                String pid = p.getParticipantId();
                if (pid.equals(mMyId))
                    continue;
                if (p.getStatus() != Participant.STATUS_JOINED)
                    continue;
                int pl = 2;
                int score = mParticipantScore.containsKey(pid) ? mParticipantScore.get(pid) : 0;

                if(onConnectedToRoom){

                }

                ++i;

            }
        }

        for (; i < arr.length; ++i) {
            ((TextView) findViewById(arr[i])).setText("");
        }
    }

    public static void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            //System.out.println(pair.getKey() + " = " + pair.getValue());
            Log.i("PrintMap", pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onLocationChanged(Location location) {
        broadcastScore(false);
        lat =  location.getLatitude();
        lng =  location.getLongitude();

        for(int i = 0; i < players.size(); i++){
            players.get(i).setAngle(bearing(lat, lng, players.get(i).getLatitude(), players.get(i).getLongitude()));

        }

        //bearing star
        angleRandomObj = bearing(lat, lng,randomObjCoord[0],randomObjCoord[1]);

    }

    public static boolean mapOpen = false;
    @Override
    public void onResume() {
        super.onResume();
        if (mTextureView.isAvailable() || mapOpen) {
            Log.i("OnResume", "OpenCamera");
            openCamera();
        } else {
            mTextureView.setSurfaceTextureListener(
                    mSurfaceTextureListener);
        }
        //senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private CameraDevice mCameraDevice = null;
    private CaptureRequest.Builder mCaptureRequestBuilder = null;
    private CameraCaptureSession mCameraCaptureSession  = null;
    private TextureView mTextureView = null;
    private Size mPreviewSize = null;

    private SensorManager senSensorManager;
    private static ProgressBar firstBar = null;
    private Sensor senAccelerometer;
    private Sensor senRotationVect;

    private static TextView textViewThrowing;
    private static ImageButton imageButton; //  ball
    private static TextView ballCounterText;

    private long lastUpdate = 0;
    private float last_x = 0, last_y = 0, last_z = 0;
    private static boolean pressedThrow = false;

    private int heartNum = 3;
    private ImageView heart1;
    private ImageView heart2;
    private ImageView heart3;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try{
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class) [0];
            manager.openCamera(cameraId, mStateCallback, null);
        } catch(CameraAccessException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void startPreview(CameraCaptureSession session) {
        mCameraCaptureSession = session;
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread backgroundThread = new HandlerThread("CameraPreview");
        backgroundThread.start();
        Handler backgroundHandler = new Handler(backgroundThread. getLooper());
        try {
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        //senSensorManager.unregisterListener(this);
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            if (texture == null) {
                return;
            }
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface surface = new Surface(texture);
            try {
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            } catch (CameraAccessException e){
                e.printStackTrace();
            }
            mCaptureRequestBuilder.addTarget(surface);
            try {
                mCameraDevice.createCaptureSession(Arrays.asList(surface), mPreviewStateCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onError(CameraDevice camera, int error) {}
        @Override
        public void onDisconnected(CameraDevice camera) {

        }
    };

    private int mAzimuth = 0; // degree
    float[] orientation = new float[3];
    float[] rMat = new float[9];
    //double angle = 0;

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            double uLat = 55.00509565857462; //54.9823894;
            double uLng = 25.795183178270236; //25.76502240000002;
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }
    };

    // TEORINIS AZIMUTAS
    protected static double bearing(double lat1, double lon1, double lat2, double lon2) {
        double longDiff = Math.toRadians(lon2 - lon1);
        double la1 = Math.toRadians(lat1);
        double la2 = Math.toRadians(lat2);
        double y = Math.sin(longDiff) * Math.cos(la2);
        double x = Math.cos(la1) * Math.sin(la2) - Math.sin(la1) * Math.cos(la2) * Math.cos(longDiff);

        double result = Math.toDegrees(Math.atan2(y, x));
        return (result+360.0d)%360.0d;
    }


    private CameraCaptureSession.StateCallback mPreviewStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            startPreview(session);
        }
        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };


    float maxDown = 0;
    boolean doneDown = false;
    boolean doneUp = false;
    boolean was = false;
    boolean was1 = false;
    float downX = 0;
    float upX = 0;
    long curTimeThrow;
    long allThrowingTime;
    float accelerometerDistance;
    float accelerometerSpeed;
    boolean throwUp = false;
    boolean wasUp = false;
    final int MAX_THROW_DISTANCE = 200; // max throwing distance in game
    float MAX_DEVICE_SPEED; // max device range * max device speed coefficient
    int MAX_DEVICE_SPEED_COEFF = 2; // max device range * 2
    int THROW_SPEED_WITH_COEFFICIENT;  // received speed * coefficient
    float MAX_DEVICE_ERROR; // paklaida
    float myDeviceMinError = (float) 0.882; // creator's device
    float myDeviceMaxError = (float) 10.95707; // creator's device
    float myDeviceMaxSpeed = 64; // creator's device
    float myDeviceMinSpeed = (float) 10.411; // creator's device
    float coeff; //
    float errorSum; // paklaidu suma min + max
    float ERROR;
    float accelerometerMinSpeed; // with ERRROR
    float accelerometerMaxSpeed; // with ERRROR
    float distanceBetweenMyOpponent; // rasti kas yra toje kriptyje, imti kuris yra arciausiai arba klausi zaidejo i kuri taikomasi
    float throwingMinDistance; // atstumas paskaiciuotas taip, kad max negaletu buti daugiau nei 200 (MAX_THROW_DISTANCE)
    float throwingMaxDistance;
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            if(last_x > x && (last_x <= 0 && x <= 0 && last_x - x > 0.5) && !doneDown && pressedThrow && !throwUp && !wasUp){ // atgal
                firstBar.setProgress(Math.abs(Math.round(Math.abs(x))));
                lastUpdate = curTime;
                was = true;
                last_x = x;
                last_y = y;
                last_z = z;
            } // priesingu atveju jei padaugeja paklaida einant i kita puse, nustojama, issaugomi duomenys ir vykdoma i prieki
            else if(last_x < x && (last_x <= 0 && x <= 0 && (abs(last_x) - abs(x) > 2) || abs(last_x) - abs(x) > 0.5 && (curTime/1000 - lastUpdate/1000 > 0.75)) && !doneDown && was && pressedThrow && !throwUp && !wasUp){   // laika iki 0.5 gal sumazint???
                accelerometerSpeed = (abs(last_x) + abs(last_y) + abs(last_z));  // ar nereikejo last_x vetoj x ir kitu likusiu?
                lastUpdate = curTime;
                doneDown = true;
                downX = last_x;
                curTimeThrow = System.currentTimeMillis();   // nustatome laika, kai pasiekiame galines koord
            }

            if(doneDown && last_x < x && abs(last_x) - abs(x) > 0.5 && abs(last_x) - abs(x) < 2 && !doneUp && pressedThrow && !throwUp && !wasUp){
                lastUpdate = curTime;
                was1 = true;
                last_x = x;
            }else if(abs(x) - abs(last_x) > 5 && !doneUp && was1 && pressedThrow && !throwUp && !wasUp){
                lastUpdate = curTime;
                doneUp = true;
                upX = last_x;
                allThrowingTime = System.currentTimeMillis() - curTimeThrow;
            }else if((curTime/1000 - lastUpdate/1000 > 0.75) && !doneUp && was1 && pressedThrow && !throwUp && !wasUp){ // laukiama 0.75 sek
                lastUpdate = curTime;
                doneUp = true;
                upX = last_x;
                allThrowingTime = System.currentTimeMillis() - curTimeThrow;
            }

            if(doneDown && doneUp && !wasUp){
                accelerometerDistance = abs(downX) + abs(upX);
                Log.i("PASUKIMAS ", "GREITIS " + accelerometerSpeed + "m/s");
                //Toast.makeText(this, "GREITIS " + accelerometerSpeed, Toast.LENGTH_LONG).show();
                pressedThrow = false;
                doneDown = false;
                doneUp = false;
                firstBar.setVisibility(INVISIBLE);
                //throwingButton.setVisibility(View.VISIBLE);
                ballCounterText.setVisibility(View.VISIBLE);
                last_x = 0;
                // CALCULATED or hit
                MAX_DEVICE_SPEED = mySensor.getMaximumRange() * 2;
                MAX_DEVICE_ERROR = (myDeviceMaxError * mySensor.getMaximumRange() * 2) / myDeviceMaxSpeed; // kiekvieno irenginio max paklaida randama
                // min device error not found for all devices
                coeff = (myDeviceMinSpeed + MAX_DEVICE_SPEED) / accelerometerSpeed;
                errorSum = myDeviceMinError + MAX_DEVICE_ERROR;
                ERROR = errorSum / coeff;
                // speed bounds
                accelerometerMinSpeed = accelerometerSpeed - ERROR;
                accelerometerMaxSpeed = accelerometerSpeed + ERROR;

                throwingMinDistance = (MAX_THROW_DISTANCE * accelerometerMinSpeed) / MAX_DEVICE_SPEED;
                throwingMaxDistance = (MAX_THROW_DISTANCE * accelerometerMaxSpeed) / MAX_DEVICE_SPEED;

                // patikrinti ar neateina cia
                if (throwingMinDistance > MAX_THROW_DISTANCE){
                    throwingMinDistance = MAX_THROW_DISTANCE;
                }else if (throwingMaxDistance > MAX_THROW_DISTANCE){
                    throwingMaxDistance = MAX_THROW_DISTANCE;
                }

                distanceBetweenMyOpponent = 100;//mDistance;  // atstumas turi irgi paklaida
                if (throwingMinDistance <= distanceBetweenMyOpponent && throwingMaxDistance >= distanceBetweenMyOpponent){
                    // print info. oppenent is shoot

                    Log.i("METIMAS", "Pataikyta min = " + throwingMinDistance + ", max = " + throwingMaxDistance);
                    Log.i("METIMAS", "Pataikyta tikrasis greitis " + accelerometerSpeed);

                    hit = visibleOnCam;
                    broadcastScore(false);
                    showDialog();

                    ImageView tempImageView;

                    tempImageView = (ImageView) findViewById(R.id.imageView);
                    tempImageView.setImageResource(AnimationStation.get().getForecasts().get(Adapter.index).getIcon());

                    setScale(tempImageView);

                }else{
                    // print info. not shoot
                    Log.i("METIMAS", "Nepataikyta min = " + throwingMinDistance + ", max = " + throwingMaxDistance);
                    Log.i("METIMAS", "Nepataikyta tikrasis greitis " + accelerometerSpeed);

                    ImageView tempImageView;
                    tempImageView = (ImageView) findViewById(R.id.imageView);
                    tempImageView.setImageResource(AnimationStation.get().getForecasts().get(Adapter.index).getIcon());

                    setScale(tempImageView);


                }


            }
        }else if (mySensor.getType() == Sensor.TYPE_ROTATION_VECTOR && mCurScreen == R.id.screen_game) {
            //-----------------------------------------------------------------------------------------------
            //-------------------------------------SU VISAIS ZAIDEJAIS---------------------------------------
            // calculate th rotation matrix
            for(int i = 0; i < players.size(); i++) {
                Log.i("ZAIDEJU SK", String.valueOf(players.size()));
                SensorManager.getRotationMatrixFromVector(rMat, sensorEvent.values);
                // get the azimuth value (orientation[0]) in degree
                mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
                Log.i("AZIMUTAS ", "onChanged metode = " + mAzimuth);
                //teorinio
                double minAzimuth = players.get(i).getAngle() - accurancy;
                double maxAzimuth = players.get(i).getAngle() + accurancy;

                if (minAzimuth < 0) {
                    minAzimuth += 360;
                }
                if (maxAzimuth > 360) {
                    maxAzimuth -= 360;
                }

                double temp;
                if (minAzimuth > maxAzimuth) {
                    temp = minAzimuth;
                    minAzimuth = maxAzimuth;
                    maxAzimuth = temp;
                }

                if(onConnectedToRoom){
                    if (isBetween(minAzimuth, maxAzimuth, mAzimuth, i)) {
                        playerVisibleOnCam = i;
                    } else {

                    }
                }

                double xPos, yPos;
                double dist = -1;

                xPos = Math.sin(Math.toRadians(mAzimuth)) * dist;
                yPos = Math.sqrt(Math.pow(dist, 2) - Math.pow(xPos, 2));
                if (mAzimuth > 90 && mAzimuth < 270)
                    yPos *= -1;

                if (System.currentTimeMillis() - blipTime > 100) {
                    blipTime = System.currentTimeMillis();
                    if (blipInView) {
                        rlMain.removeView(blip);
                    }
                    blipInView = true;
                    blip = new ImageView(this);
                    if (player.getTeam().equals(players.get(i).getTeam())){
                        blip.setImageResource(R.drawable.blip);
                    }else {
                        blip.setImageResource(R.drawable.redblip);
                    }

                    params = new RelativeLayout.LayoutParams(20, 20);// mastelis figuros
                    // center
                    Log.i("XPOS", String.valueOf(centerY - yPos * 10));
                    Log.i("XPOS", String.valueOf(centerX + xPos * 10));
                    params.topMargin = (int) (centerY - yPos * 100); // y koord
                    params.leftMargin = (int) (centerX + xPos * 100); // x koord
                    Log.i("Koord ", "x = " + (centerX + xPos * 100));
                    Log.i("Koord ", "y = " + (centerY - yPos * 100));
                    rlMain.addView(blip, params);

                }
            }
            //--------------------------------------------------------------------------------------
        }
    }
   // scroll
    @Override
   public void onCurrentItemChanged(@Nullable Adapter.ViewHolder holder, int position) {
        //viewHolder will never be null, because we never remove items from adapter's list
            if (holder != null) {
                view.setForecast(forecasts.get(position));
                holder.showText();
            }

   }

    @Override
    public void onScrollStart(@NonNull Adapter.ViewHolder holder, int position) {
        holder.hideText();
    }

    @Override
    public void onScroll(
            float position,
            int currentIndex, int newIndex,
            @Nullable Adapter.ViewHolder currentHolder,
            @Nullable Adapter.ViewHolder newHolder) {
        Animation current = forecasts.get(currentIndex);

        if (newIndex >= 0 && newIndex < picker.getAdapter().getItemCount()) {
            Animation next = forecasts.get(newIndex);
            view.onScroll(1f - Math.abs(position), current, next);
        }

    }


    @Override
    public void onScrollEnd(@NonNull Adapter.ViewHolder holder, int position) {

    }

// scroll
    private class GetXMLTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap map = null;
            for (String url : urls) {
                map = downloadImage(url);
            }
            return map;
        }

        // Sets the Bitmap returned by doInBackground
        @Override
        protected void onPostExecute(Bitmap result) {
            //playerView.setImageBitmap(MapsActivity.getCircularBitmap(result));
            person.setImageBitmap(MapsActivity.getCircularBitmap(result));
        }

        // Creates Bitmap from InputStream and returns it
        private Bitmap downloadImage(String url) {
            Bitmap bitmap = null;
            InputStream stream = null;
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 1;

            try {
                stream = getHttpConnection(url);
                bitmap = BitmapFactory.
                        decodeStream(stream, null, bmOptions);
                stream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return bitmap;
        }

        // Makes HttpURLConnection and returns InputStream
        private InputStream getHttpConnection(String urlString)
                throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            try {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();

                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return stream;
        }
    }
    //----------------------------------------------------------------------------------------------

    double centerX = 240;
    double centerY = 200;
    long blipTime = 0;
    ImageView blip;
    ImageView playerView;
    boolean blipInView = false;
    boolean playerInView = false;
    double accurancy = 30;
    int width;
    int height;
    RelativeLayout rlMain;
    RelativeLayout.LayoutParams params;
    ImageView person;
    double coord;
    boolean personInView = false;
    long myTime = 0;

    private boolean isBetween(double minAngle, double maxAngle, double azimuth, int i) {
        if (minAngle > maxAngle) {

        } else {
            if (azimuth > minAngle && azimuth < maxAngle) {

                myTime = System.currentTimeMillis();
                if (personInView) {
                    rlMain.removeView(person);
                }
                personInView = true;
                person = new ImageView(this);
                //person.setImageResource(R.drawable.person);
                GetXMLTask task = new GetXMLTask();
                // Execute the task
                task.execute(new String[] { players.get(i).getImageUrl()});
                params = new RelativeLayout.LayoutParams(1090, 1090);// mastelis figuros
                params.topMargin = 650 + i*100;
                coord = (azimuth - minAngle) * 1090 / (accurancy * 2);
                params.width = 200;
                params.height = 200;
                person.setMinimumWidth(200);
                params.leftMargin = (int) (coord - (545 / 2)); // per viduri ekrano,jei 0laipsniu paklaida
                rlMain.addView(person, params);
                return true;
            }
        }
        return false;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void wasHit(){
        ImageView tempImageView;
        tempImageView = (ImageView) findViewById(R.id.imageView);
        tempImageView.setImageResource(AnimationStation.get().getForecasts().get(1).getIcon());
        setScaleHit(tempImageView);

        if (heartNum == 3){
            heartNum--;
            heart1.setVisibility(View.GONE);
        }else if (heartNum == 2){
            heartNum--;
            heart2.setVisibility(View.GONE);
        }else if (heartNum == 1){
            heartNum--;
            heart3.setVisibility(View.GONE);
        }else{

        }
    }

    private void drawPerson(){

    }

    public static void ballThrowing(){
        visibleOnCam = playerVisibleOnCam;
        int ballNum = Integer.valueOf((String) ballCounterText.getText());
        if(ballNum > 0){
            --ballNum;
            ballCounterText.setText(String.valueOf(ballNum));
            //ballCounterText1.setText(String.valueOf(ballNum));
            firstBar.setVisibility(View.VISIBLE);
            firstBar.setMax(35);
            firstBar.setProgress(0);
            //firstBar.setProgress(3);
            imageButton.setVisibility(INVISIBLE);
            ballCounterText.setVisibility(INVISIBLE);
            textViewThrowing.setVisibility(INVISIBLE);
            pressedThrow = true;
        }
        if(ballNum == 0){
            // galbut zaidimo eigoje gaus kamuoliu daugiau, tada atsetinti
            imageButton.setClickable(false);
        }

    }


    public static  void  setScale(ImageView image){
        image.setVisibility(View.VISIBLE);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1f,0f,1f,0f, image.getWidth() / 2.0f, image.getHeight() / 2.0f);
        scaleAnimation.setDuration(3000);
        image.startAnimation(scaleAnimation);

        scaleAnimation.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            public void onAnimationEnd(android.view.animation.Animation animation) {
                Log.d("ScaleActivity", "Scale started...");
            }

            public void onAnimationRepeat(android.view.animation.Animation animation) {

            }

            public void onAnimationStart(android.view.animation.Animation animation) {
                Log.d("ScaleActivity", "Scale ended...");
            }
        });
        image.setVisibility(INVISIBLE);
    }


    public static  void  setScaleHit(ImageView image){
        image.setVisibility(View.VISIBLE);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1f,4.0f,1f,4.0f, image.getWidth() / 2.0f, image.getHeight() / 2.0f);
        scaleAnimation.setDuration(3000);
        image.startAnimation(scaleAnimation);

        scaleAnimation.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            public void onAnimationEnd(android.view.animation.Animation animation) {
                Log.d("ScaleActivity", "Scale started...");
            }

            public void onAnimationRepeat(android.view.animation.Animation animation) {

            }

            public void onAnimationStart(android.view.animation.Animation animation) {
                Log.d("ScaleActivity", "Scale ended...");
            }
        });
        image.setVisibility(INVISIBLE);
        // Get instance of Vibrator from current Context

        android.os.SystemClock.sleep(1000);
        // Vibrate for 400 milliseconds
        v.vibrate(200);
        android.os.SystemClock.sleep(300);
        v.vibrate(200);
        android.os.SystemClock.sleep(300);
        v.vibrate(200);

    }

    public static void addBalls(int correctShoot){
        int ballNum = Integer.valueOf((String) ballCounterText.getText());
        ballNum = ballNum + correctShoot;
        ballCounterText.setText(String.valueOf(ballNum));
    }

    public Dialog epicDialog;
    public ImageView closePopupPositiveImg;
    public ImageView personImg;
    public TextView scoreText;

    public void showDialog(){
        epicDialog.setContentView(R.layout.found_live_layout); // pakeisti
        closePopupPositiveImg = (ImageView) epicDialog.findViewById(R.id.closePopupPositiveImg);

        closePopupPositiveImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                epicDialog.dismiss();
            }
        });

        epicDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        epicDialog.show();

    }
}

