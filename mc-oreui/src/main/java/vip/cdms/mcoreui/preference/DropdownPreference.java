package vip.cdms.mcoreui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceViewHolder;

import java.util.ArrayList;

import vip.cdms.mcoreui.R;
import vip.cdms.mcoreui.view.input.Dropdown;

public class DropdownPreference extends ListPreference {
    public DropdownPreference(@NonNull Context context) {
        this(context, null);
    }
    @SuppressLint("RestrictedApi")
    public DropdownPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, androidx.preference.R.attr.dialogPreferenceStyle, android.R.attr.dialogPreferenceStyle));
    }
    public DropdownPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    @SuppressLint("RestrictedApi")
    public DropdownPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setLayoutResource(R.layout.preference_dropdown);

        TypedArray attributes = context.obtainStyledAttributes(attrs, androidx.preference.R.styleable.ListPreference, defStyleAttr, defStyleRes);
        setEntries(TypedArrayUtils.getTextArray(attributes, androidx.preference.R.styleable.ListPreference_entries, androidx.preference.R.styleable.ListPreference_android_entries));
        setEntryValues(TypedArrayUtils.getTextArray(attributes, androidx.preference.R.styleable.ListPreference_entryValues, androidx.preference.R.styleable.ListPreference_android_entryValues));
    }

    private Dropdown dropdown = null;
    private int value = 0;
    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        dropdown = (Dropdown) holder.findViewById(R.id.dropdown);
        dropdown.setItemList(entries);
        if (value >= 0) dropdown.select(value);
        dropdown.setOnItemSelectedListener((view, position, selectNothing) -> setValueIndex(position));
    }

    private CharSequence[] entries;
    @Override
    public void setEntries(CharSequence[] entries) {
        super.setEntries(entries);
        this.entries = entries;
        if (dropdown != null)
            dropdown.setItemList(entries);
    }
    @Override
    public CharSequence[] getEntries() {
        return entries;
    }

    private CharSequence[] entryValues;
    @Override
    public void setEntryValues(CharSequence[] entryValues) {
        super.setEntryValues(entryValues);
        this.entryValues = entryValues;
    }
    @Override
    public CharSequence[] getEntryValues() {
        return entryValues;
    }

    @Override
    public void setValue(String value) {
        if (onPreferenceChangeListener != null) onPreferenceChangeListener.onPreferenceChange(this, value);

        super.setValue(value);
        this.value = findIndexOfValue(value);
        if (dropdown != null)
            dropdown.select(this.value);
    }

    @Override
    protected void onClick() {
//        super.onClick(); DO NOT SUPER CALL
    }

    private OnPreferenceChangeListener onPreferenceChangeListener = null;
    @Override
    public void setOnPreferenceChangeListener(@Nullable OnPreferenceChangeListener onPreferenceChangeListener) {
        this.onPreferenceChangeListener = onPreferenceChangeListener;
    }
    @Nullable
    @Override
    public OnPreferenceChangeListener getOnPreferenceChangeListener() {
        return onPreferenceChangeListener;
    }
}
