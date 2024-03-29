package com.rsargsyan.simplepowerfailuremonitor.utils;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;

public class PreferenceUtil {
    public static void setEditTextPreferenceInputType(@Nullable EditTextPreference pref,
                                                      int inputType) {
        if (pref != null) {
            pref.setOnBindEditTextListener(editText ->
                    editText.setInputType(inputType));
        }
    }
}
