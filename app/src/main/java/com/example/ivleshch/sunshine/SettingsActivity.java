package com.example.ivleshch.sunshine;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Ivleshch on 01.11.2016.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        PrefsFragment mPrefsFragment = new PrefsFragment();
        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();

    }


    public static class PrefsFragment extends PreferenceFragment {

        private EditTextPreference mEditTextPreference;
        private ListPreference mListPreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }


        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            mEditTextPreference = (EditTextPreference) getPreferenceManager().findPreference(getString(R.string.pref_location_key));
            mEditTextPreference.setSummary(PreferenceManager.getDefaultSharedPreferences(mEditTextPreference.getContext()).getString(mEditTextPreference.getKey(), ""));
            mEditTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String stringValue = newValue.toString();
                    preference.setSummary(stringValue);
                    return true;
                }
            });

            mListPreference = (ListPreference) getPreferenceManager().findPreference(getString(R.string.pref_units_key));
            mListPreference.setSummary(PreferenceManager.getDefaultSharedPreferences(mListPreference.getContext()).getString(mListPreference.getKey(), ""));
            mListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String stringValue = newValue.toString();


                    ListPreference listPreference = (ListPreference) preference;
                    int prefIndex = listPreference.findIndexOfValue(stringValue);
                    if (prefIndex >= 0) {
                        preference.setSummary(listPreference.getEntries()[prefIndex]);
                    }

                    return true;
                }
            });
        }
    }
}
