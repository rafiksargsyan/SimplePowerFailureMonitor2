package com.rsargsyan.simplepowerfailuremonitor.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.PasswordTransformationMethod;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.rsargsyan.simplepowerfailuremonitor.R;
import com.rsargsyan.simplepowerfailuremonitor.utils.Constants;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

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
    private static final int RINGTONE_PICKER_REQUEST_CODE = 17;

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
        private EditTextPreference alarmSoundPref;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            addAlarmSoundPreference();

            setEditTextPreferenceInputType(findPreference(PHONE_NUMBER_KEY), TYPE_CLASS_PHONE);
            setEditTextPreferenceInputType(findPreference(RECIPIENT_EMAIL_ADDRESS_KEY),
                    TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            setEditTextPreferenceInputType(findPreference(SMTP_PORT_KEY), TYPE_CLASS_PHONE);
            setEditTextPreferenceInputType(findPreference(SENDER_EMAIL_ADDRESS_KEY),
                    TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            setupSenderEmailPassword();
        }

        private void addAlarmSoundPreference() {
            alarmSoundPref = new RingtonePickerPreference(this.requireContext());
            alarmSoundPref.setKey(Constants.ALARM_SOUND_KEY);
            alarmSoundPref.setTitle(R.string.alarm_sound_title);
            alarmSoundPref.setDefaultValue(Settings.System.DEFAULT_ALARM_ALERT_URI.toString());

            PreferenceCategory alarmSettingsPrefCategory =
                    findPreference(Constants.ALARM_SETTINGS_KEY);
            Objects.requireNonNull(alarmSettingsPrefCategory).addPreference(alarmSoundPref);

            alarmSoundPref.setSummary(RingtoneManager.getRingtone(getContext(),
                    Uri.parse(alarmSoundPref.getText())).getTitle(getContext()));
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            if (resultCode == Activity.RESULT_OK && requestCode == RINGTONE_PICKER_REQUEST_CODE
                    && data != null) {
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

                if (uri != null) {
                    alarmSoundPref.setText(uri.toString());
                    Ringtone ringtone = RingtoneManager.getRingtone(getContext(), uri);
                    String title = ringtone.getTitle(getContext());
                    alarmSoundPref.setSummary(title);
                }
            }
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
                    final int length = (password == null ? 0 : password.length());
                    if (0 == length) {
                        return preference.getContext().getString(R.string.not_set);
                    }
                    return StringUtils.repeat(DOT, length);
                });
            }
        }

        private class RingtonePickerPreference extends EditTextPreference {
            public RingtonePickerPreference(Context context) {
                super(context);
            }

            @Override
            protected void onClick() {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
                        getString(R.string.select_alarm_sound));
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                        Settings.System.DEFAULT_ALARM_ALERT_URI);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                        Uri.parse(alarmSoundPref.getText()));
                startActivityForResult(intent, RINGTONE_PICKER_REQUEST_CODE);
            }
        }
    }
}