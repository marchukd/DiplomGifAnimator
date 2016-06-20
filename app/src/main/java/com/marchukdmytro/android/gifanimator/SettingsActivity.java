package com.marchukdmytro.android.gifanimator;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.Locale;

public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener {
    public static final String COUNT_OF_FRAMES = "countOfFrames";
    public static final String LOCALE = "language_preference";
    public static String DEFAULT_COUNT_OF_FRAMES = "5";
    public static String DEFAULT_LOCALE = "uk_UA";
    public static String LANGUAGE_PREFERENCES_KEY = "language_preference";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        String countOfFrames = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(COUNT_OF_FRAMES, DEFAULT_COUNT_OF_FRAMES);
        findPreference(COUNT_OF_FRAMES).setOnPreferenceChangeListener(this);
        getPreferenceScreen().findPreference(COUNT_OF_FRAMES).setSummary(countOfFrames);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(COUNT_OF_FRAMES)) {
            String value = sharedPreferences.getString(COUNT_OF_FRAMES, DEFAULT_COUNT_OF_FRAMES);
            getPreferenceScreen().findPreference(COUNT_OF_FRAMES).setSummary(String.valueOf(value));
        } else if (key.equals(LOCALE)) {
            String lang = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.LANGUAGE_PREFERENCES_KEY, SettingsActivity.DEFAULT_LOCALE);
            Locale locale = new Locale(lang.split("_")[0], lang.split("_")[1]);
            Locale.setDefault(locale);
            Configuration configuration = new Configuration();
            configuration.locale = locale;
            getBaseContext().getResources().updateConfiguration(configuration, null);
            recreate();
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = Integer.parseInt(String.valueOf(newValue));
        if ((value <= 100) && (value >= 2))
            return true;
        Toast.makeText(SettingsActivity.this, R.string.prefs_min_max, Toast.LENGTH_SHORT).show();
        return false;
    }
}
