package vip.cdms.mcoreui.view.input;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.BounceInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import vip.cdms.mcoreui.R;
import vip.cdms.mcoreui.util.ReflectionUtils;
import vip.cdms.mcoreui.util.ResourcesUtils;
import vip.cdms.mcoreui.util.SoundPlayer;

/**
 * ORE UI风格开关
 * @author Cdm2883
 */
public class Switch extends SwitchCompat implements SoundPlayer.ClickSound, ValueAnimator.AnimatorUpdateListener {
    public Switch(@NonNull Context context) {
        this(context, null);
    }
    public Switch(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.switchStyle);
    }
    public Switch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackground(null);
        setThumbDrawable(ResourcesUtils.getPixelDrawable(context, R.drawable.switch_thumb));
        setTrackDrawable(ResourcesUtils.getPixelDrawable(context, R.drawable.switch_track));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) { // 重写是为了消耗掉事件, 这样就能走自己的动画逻辑了
        if (!isEnabled()) return true;

        if (event.getAction() == MotionEvent.ACTION_UP) {
//            performClick();
//            ((View) getParent()).callOnClick();
            toggle();
        }
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled == isEnabled()) return;
        super.setEnabled(enabled);

        if (enabled) {
            setThumbDrawable(ResourcesUtils.getPixelDrawable(getContext(), R.drawable.switch_thumb));
            setTrackDrawable(ResourcesUtils.getPixelDrawable(getContext(), R.drawable.switch_track));
        } else {
            setThumbDrawable(ResourcesUtils.getPixelDrawable(getContext(), R.drawable.switch_thumb_disabled));
            setTrackDrawable(ResourcesUtils.getPixelDrawable(getContext(), R.drawable.switch_track_disabled));
        }
    }

    private boolean checked = false;
    @Override
    public boolean isChecked() {
        return checked;
    }
    public void setChecked(boolean checked, boolean animate) {
        if (this.checked == checked) return;
        float start = checked ? 0f : 1f;
        float end = checked ? 1f : 0f;

        ValueAnimator animator = ValueAnimator.ofFloat(start, end);
        animator.setDuration(animate ? 300 : 0);
        animator.setInterpolator(new BounceInterpolator());  // duang duang的效果~
        animator.addUpdateListener(this);
        animator.start();

        this.checked = checked;

        // 防止这个setChecked是从onCheckedChangeListener来的, 最后堆栈溢出
        StackTraceElement call = new Throwable().getStackTrace()[1];
        String callClass = call.getClassName();
        String callMethod = call.getMethodName();
        if (callClass.equals("androidx.preference.SwitchPreferenceCompat$Listener") || callMethod.equals("onCheckedChanged")) return;

        if (onCheckedChangeListener != null) onCheckedChangeListener.onCheckedChanged(this, checked);
    }
    @Override
    public void setChecked(boolean checked) {
        setChecked(checked, true);
    }
    @Override
    public void onAnimationUpdate(@NonNull ValueAnimator animation) {
        setThumbPosition((Float) animation.getAnimatedValue());
    }

    private OnCheckedChangeListener onCheckedChangeListener;
    @Override
    public void setOnCheckedChangeListener(@Nullable OnCheckedChangeListener listener) {
       onCheckedChangeListener = listener;
    }

//    private Method setThumbPositionMethod = null;
    private void setThumbPosition(float position) {
        try {
            ReflectionUtils.invoke(SwitchCompat.class, "setThumbPosition", ReflectionUtils.A_P_T, this, position);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}