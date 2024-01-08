package vip.cdms.mcoreui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreferenceCompat;

import vip.cdms.mcoreui.R;
import vip.cdms.mcoreui.util.SoundPlayer;
import vip.cdms.mcoreui.util.ViewUtils;

public class SwitchPreference extends SwitchPreferenceCompat {
    public SwitchPreference(@NonNull Context context) {
        this(context, null);
    }
    public SwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.switchPreferenceCompatStyle);
    }
    public SwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    public SwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(R.layout.preference_switch);
    }

    boolean firstClick = true;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.findViewById(R.id.switchWidget).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (firstClick) {
                    firstClick = false;
                    return true;
                }
                holder.itemView.callOnClick();
            }
            return !SoundPlayer.playClickSound(v, event);
        });
        holder.itemView.setOnTouchListener(SoundPlayer::playClickSound);
        ViewUtils.addOnClickListener(holder.itemView, v -> firstClick = false);
    }
}
