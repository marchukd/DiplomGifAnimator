package com.marchukdmytro.android.gifanimator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String COUNT_OF_FRAMES = "countOfFrames";
    public static final String LOCALE = "language_preference";
    public static String DEFAULT_COUNT_OF_FRAMES = "5";
    public static String DEFAULT_LOCALE = "uk_UA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        String countOfFrames = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(COUNT_OF_FRAMES, DEFAULT_COUNT_OF_FRAMES);
        getPreferenceScreen().findPreference(COUNT_OF_FRAMES).setSummary(countOfFrames);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(COUNT_OF_FRAMES)) {
            String value = sharedPreferences.getString(COUNT_OF_FRAMES, DEFAULT_COUNT_OF_FRAMES);
            getPreferenceScreen().findPreference(COUNT_OF_FRAMES).setSummary(String.valueOf(value));
        } else if (key.equals(LOCALE)) {
            String value = sharedPreferences.getString(LOCALE, DEFAULT_LOCALE);
            Preference localePreference = getPreferenceScreen().findPreference(LOCALE);
            switch (value) {
                case "en_US":
                    value = "Англійська";
                    break;
                case "uk_UA":
                    value = "Українська";
                    break;
            }
            localePreference.setSummary(value);
        }
    }
}
