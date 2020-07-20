package com.example.anti_tampering_checker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Save all data to shared prefs
        new DeviceData(getApplicationContext()).saveAllToSharedPref();
        new SettingsFinder(getApplicationContext()).saveAllToSharedPrefs();

        // Wait grace period
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Show shared prefs
        showSharedPrefData();
    }

    /**
     * Get all shared preferences and display them in the text view
     */
    private void showSharedPrefData(){
        EditText editText = findViewById(R.id.editTextTextMultiLine);
        editText.setText("Tamper Detection Checks:");

        File prefsdir = new File(getApplicationInfo().dataDir, "shared_prefs");

        if (prefsdir.exists() && prefsdir.isDirectory()) {
            String[] list_of_preference_names = prefsdir.list();

            if (list_of_preference_names != null) {
                for (String pref : list_of_preference_names) {

                    if (pref.startsWith("com")) {
                        continue;
                    }

                    pref = pref.replace(".xml", "");

                    SharedPreferences prefs = this.getSharedPreferences(pref, Context.MODE_PRIVATE);
                    editText.append("\n\n " + pref + ":");

                    Map<String, ?> allEntries = prefs.getAll();
                    for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                        Log.v(getApplicationContext().getPackageName(), entry.getKey() + "\t:\t" + entry.getValue().toString());
                        editText.append("\n\t " + entry.getKey() + "\t:\t" + entry.getValue().toString());
                    }
                }
            }
        }
    }
}