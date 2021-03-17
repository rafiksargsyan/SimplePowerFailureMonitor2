package com.rsargsyan.simplepowerfailuremonitor.ui;

import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.rsargsyan.simplepowerfailuremonitor.R;

import org.apache.commons.lang3.StringUtils;

import static android.text.InputType.TYPE_CLASS_PHONE;
import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.PHONE_NUMBER_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.RECIPIENT_EMAIL_ADDRESS_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SENDER_EMAIL_ADDRESS_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SENDER_EMAIL_PASSWORD_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SMTP_PORT_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.PreferenceUtil.setEditTextPreferenceInputType;

public class SettingsActivity extends AppCompatActivity {
    private static final char DOT = '\u2022';

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            setEditTextPreferenceInputType(findPreference(PHONE_NUMBER_KEY), TYPE_CLASS_PHONE);
            setEditTextPreferenceInputType(findPreference(RECIPIENT_EMAIL_ADDRESS_KEY),
                    TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            setEditTextPreferenceInputType(findPreference(SMTP_PORT_KEY), TYPE_CLASS_PHONE);
            setEditTextPreferenceInputType(findPreference(SENDER_EMAIL_ADDRESS_KEY),
                    TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            setupSenderEmailPassword();
        }

        private void setupSenderEmailPassword() {
            EditTextPreference pref = findPreference(SENDER_EMAIL_PASSWORD_KEY);
            if (pref != null) {
                pref.setOnBindEditTextListener(editText -> {
                     editText.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
                     editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                });
                pref.setSummaryProvider(preference -> {
                    final String password = pref.getText();
                    final int length = password.length();
                    if (0 == length) {
                        return preference.getContext().getString(R.string.not_set);
                    }
                    return StringUtils.repeat(DOT, length);
                });
            }
        }
    }
}