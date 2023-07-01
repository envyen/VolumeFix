package com.envyen.volume;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.ToggleButton;
import android.media.AudioManager;

import androidx.appcompat.app.AppCompatActivity;

import com.envyen.volume.YourService;
import com.envyen.volume.PreferencesManager;

public class MainActivity extends AppCompatActivity {

    ToggleButton mScreenTrackingToggleButton;

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_main);

        PreferencesManager.init(getApplicationContext());
        mScreenTrackingToggleButton = findViewById(R.id.screen_tracking_toggle_button);

        if(PreferencesManager.getInstance().getBoolean("track_screen"))
        {
            mScreenTrackingToggleButton.setChecked(true);
            this.startService(new Intent(this, YourService.class));
        }

        mScreenTrackingToggleButton.setOnCheckedChangeListener(
                ((compoundButton, b) -> {

                    if(b)
                    {
                        PreferencesManager.getInstance().putBoolean("track_screen",true);
                        Intent intent = new Intent(this, YourService.class);
                        startService(intent);
                    }
                    else
                    {
                        PreferencesManager.getInstance().putBoolean("track_screen",false);
                        Intent intent = new Intent(this, YourService.class);
                        intent.putExtra("close",true);
                        startService(intent);
                    }
                })
        );
    }


    private void setMediaVolume(Context context, int volume) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }
}