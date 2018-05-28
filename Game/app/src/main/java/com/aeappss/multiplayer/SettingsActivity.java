package com.aeappss.multiplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;


public class SettingsActivity extends AppCompatActivity {
    ImageButton buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        android.app.Fragment fragment = new SettingsScreen();
        android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if(savedInstanceState == null){
            fragmentTransaction.add(R.id.relative_layout, fragment, "settings_fragment");
            fragmentTransaction.commit();

        }else{
            fragment = getFragmentManager().findFragmentByTag("settings_fragment");
        }

        buttonBack = (ImageButton) findViewById(R.id.imageButton7);
        View.OnClickListener onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                switch (view.getId()) {
                    case R.id.imageButton7:

                        Intent settingsIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(settingsIntent);
                        finish();
                        break;
                }
            }
        };
        buttonBack.setOnClickListener(onClickListener);
    }

    public static  class SettingsScreen extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_screen);
            addPreferencesFromResource(R.xml.settings_screen_shake);

        }

    }

}
