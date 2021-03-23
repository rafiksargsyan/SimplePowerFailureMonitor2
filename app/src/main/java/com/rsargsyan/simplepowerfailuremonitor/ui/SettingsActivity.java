package com.rsargsyan.simplepowerfailuremonitor.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.rsargsyan.simplepowerfailuremonitor.R;
import com.rsargsyan.simplepowerfailuremonitor.utils.Constants;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.text.InputType.TYPE_CLASS_PHONE;
import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.PHONE_NUMBER_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.RECIPIENT_EMAIL_ADDRESS_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SENDER_EMAIL_ADDRESS_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SENDER_EMAIL_PASSWORD_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SEND_SMS_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SMS_ALARM_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SMTP_PORT_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.PreferenceUtil.setEditTextPreferenceInputType;

public class SettingsActivity extends AppCompatActivity {
    private static final char DOT = '\u2022';
    private static final int SEND_SMS_PERMISSION_REQUEST_CODE = 7;
    private static final int ALARM_SMS_PERMISSION_REQUEST_CODE = 13;
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
        private SwitchPreferenceCompat sendSmsPref;
        private SwitchPreferenceCompat smsAlarmPref;
        private SharedPreferences sharedPreferences;
        private EditTextPreference alarmSoundPref;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            syncPermissions();
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

            sendSmsPref = findPreference(SEND_SMS_KEY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && sendSmsPref != null) {
                sendSmsPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    Boolean boolValue = (Boolean) newValue;
                    if (boolValue == null) return true;
                    if (boolValue) {
                        if (getContext().checkSelfPermission(Manifest.permission.SEND_SMS)
                                == PackageManager.PERMISSION_GRANTED) {
                            return true;
                        }
                        requestPermissions(new String[] { Manifest.permission.SEND_SMS },
                                SEND_SMS_PERMISSION_REQUEST_CODE);
                        return false;
                    }
                    return true;
                });
            }
            smsAlarmPref = findPreference(SMS_ALARM_KEY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && smsAlarmPref != null) {
                smsAlarmPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    Boolean boolValue = (Boolean) newValue;
                    if (boolValue == null) return true;
                    if (boolValue) {
                        if (getContext().checkSelfPermission(Manifest.permission.RECEIVE_SMS)
                                == PackageManager.PERMISSION_GRANTED) {
                            return true;
                        }
                        requestPermissions(new String[] { Manifest.permission.RECEIVE_SMS },
                                ALARM_SMS_PERMISSION_REQUEST_CODE);
                        return false;
                    }
                    return true;
                });
            }
        }

        private void addAlarmSoundPreference() {
            alarmSoundPref = new RingtonePickerPreference(this.requireContext());
            alarmSoundPref.setKey(Constants.ALARM_SOUND_KEY);
            alarmSoundPref.setTitle(R.string.alarm_sound_title);
            alarmSoundPref.setDefaultValue(Settings.System.DEFAULT_ALARM_ALERT_URI);

            PreferenceCategory alarmSettingsPrefCategory =
                    findPreference(Constants.ALARM_SETTINGS_KEY);
            Objects.requireNonNull(alarmSettingsPrefCategory).addPreference(alarmSoundPref);

            alarmSoundPref.setSummary(RingtoneManager.getRingtone(getContext(),
                    Uri.parse(alarmSoundPref.getText())).getTitle(getContext()));
        }

        @Override
        public void onRequestPermissionsResult(int requestCode,
                                               @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            if (requestCode == SEND_SMS_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendSmsPref.setChecked(true);
                }
            }
            if (requestCode == ALARM_SMS_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    smsAlarmPref.setChecked(true);
                }
            }
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

        // handling the scenario when permissions are revoked without our knowledge
        private void syncPermissions() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (getContext().checkSelfPermission(Manifest.permission.SEND_SMS)
                        == PackageManager.PERMISSION_DENIED) {
                    sharedPreferences.edit().putBoolean(SEND_SMS_KEY, false).apply();
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (getContext().checkSelfPermission(Manifest.permission.RECEIVE_SMS)
                        == PackageManager.PERMISSION_DENIED) {
                    sharedPreferences.edit().putBoolean(SMS_ALARM_KEY, false).apply();
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