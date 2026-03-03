package com.microbinsink.app

import android.os.Bundle
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.microbinsink.app.util.PreferencesHelper

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<EditTextPreference>("server_url")?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                val url = newValue as? String ?: return@setOnPreferenceChangeListener false
                if (PreferencesHelper.isValidUrl(url)) {
                    true
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Invalid URL. Please enter a valid MicroBin server address.",
                        Toast.LENGTH_LONG
                    ).show()
                    false
                }
            }
            summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        }

        findPreference<ListPreference>("privacy")?.summaryProvider =
            ListPreference.SimpleSummaryProvider.getInstance()
        findPreference<ListPreference>("expiration")?.summaryProvider =
            ListPreference.SimpleSummaryProvider.getInstance()
        findPreference<ListPreference>("burn_after")?.summaryProvider =
            ListPreference.SimpleSummaryProvider.getInstance()
    }
}
