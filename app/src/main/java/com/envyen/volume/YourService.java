package com.envyen.volume;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import android.media.AudioManager;

import com.envyen.volume.PreferencesManager;

public class YourService extends Service {
    private static BroadcastReceiver m_ScreenOffReceiver = null;
    private AudioManager audioManager;
    private int previousVolume;
    private Handler handler;
    private Runnable volumeCheckRunnable;
    private VolumeChangeReceiver volumeChangeReceiver;
    private static final int VOLUME_CHECK_INTERVAL = 1000; // 1sec

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());

        if (m_ScreenOffReceiver == null) {
//            Toast.makeText(getApplicationContext(), "Registering Receiver",Toast.LENGTH_LONG).show();
        }

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        // Start monitoring volume changes
        volumeChangeReceiver = new VolumeChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(volumeChangeReceiver, intentFilter);

        // Start the volume check runnable
        handler = new Handler();
        volumeCheckRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d("VolumeControlService", "checkVolume() called");
                checkVolume();
                handler.postDelayed(this, VOLUME_CHECK_INTERVAL);
            }
        };
        handler.postDelayed(volumeCheckRunnable, VOLUME_CHECK_INTERVAL);

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        boolean shouldClose = intent.getBooleanExtra("close", false);
        if (shouldClose) {
            stopSelf();
        } else {
            // Continue to action here
        }

//        return START_STICKY;
        return START_REDELIVER_INTENT;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if(PreferencesManager.getInstance().getBoolean("track_screen"))
        {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("restartservice");
            broadcastIntent.setClass(this, Restarter.class);
            this.sendBroadcast(broadcastIntent);
        }
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkVolume() {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume != previousVolume) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume, 0);
//            Toast.makeText(this, "Reset Volume", Toast.LENGTH_SHORT).show();
        }
    }

    private class VolumeChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("VolumeControlService", "VolumeChangeReceiver()");

            if (intent.getAction() != null && intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                checkVolume();
            }
        }
    }
}