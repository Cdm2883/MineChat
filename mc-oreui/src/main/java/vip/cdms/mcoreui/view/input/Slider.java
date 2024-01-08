package vip.cdms.mcoreui.view.input;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import vip.cdms.mcoreui.R;
import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.ResourcesUtils;
import vip.cdms.mcoreui.util.SoundPlayer;

/**
 * ORE UI风格滑块
 * @author Cdm2883
 */
public class Slider extends View implements SoundPlayer.ClickSound {
    private final Drawable thumbDrawable;
    private final Drawable trackDrawable;
    private final Drawable trackSplitDrawable;
    private final Drawable progressDrawable;
    private final Drawable progressSplitDrawable;
    private final int dp;
    private boolean touchable = true;

    private int min;
    private int max;
    private int step;
    private int percentProgress = 0;

    private int howManySteps;
    private float aOffset;
    private float offset = 0;

    public Slider(Context context) {
        this(context, null);
    }
    public Slider(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    @SuppressLint("PrivateResource")
    public Slider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        thumbDrawable = ResourcesUtils.getPixelDrawable(context, R.drawable.switch_thumb);
        trackDrawable = ResourcesUtils.getPixelDrawable(context, R.drawable.slider_track);
        trackSplitDrawable = ResourcesUtils.getPixelDrawable(context, R.drawable.slider_track_split);
        progressDrawable = ResourcesUtils.getPixelDrawable(context, R.drawable.slider_progress);
        progressSplitDrawable = ResourcesUtils.getPixelDrawable(context, R.drawable.slider_progress_split);
        dp = MathUtils.dp2px(getContext(), 1);

        TypedArray attributes = context.obtainStyledAttributes(attrs, com.google.android.material.R.styleable.Slider);
        min = (int) attributes.getFloat(com.google.android.material.R.styleable.Slider_android_valueFrom, 0);
        max = (int) attributes.getFloat(com.google.android.material.R.styleable.Slider_android_valueTo, 6);
        step = (int) attributes.getFloat(com.google.android.material.R.styleable.Slider_android_stepSize, 1);
        attributes.recycle();
    }

    private void calculate() {
        howManySteps = (max - min) / step + 1;
        aOffset = (float) MathUtils.divide(getWidth() - getHeight(), howManySteps - 1, 10);
    }

    public boolean isTouchable() {
        return touchable;
    }
    public void setTouchable(boolean touchable) {
        this.touchable = touchable;
        invalidate();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!touchable)
            return super.dispatchTouchEvent(event);

        ViewParent parent = getParent();
        if (parent == null)
            return super.dispatchTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> parent.requestDisallowInterceptTouchEvent(true);
            case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    parent.requestDisallowInterceptTouchEvent(false);
        }
        return super.dispatchTouchEvent(event);
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if (touchable)
                    setPercentProgress(getClosestPercentProgress(event.getX()));
                break;
            case MotionEvent.ACTION_UP:
                performClick();
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }
    private int getClosestPercentProgress(float offset) {
        calculate();
        offset = Math.max(0, Math.min(getWidth() - getHeight(), offset));

        int closest = 0;
        float diff = Float.MAX_VALUE;
        for (int i = 0; i < howManySteps; i++) {
            float stepOffset = i * aOffset;
            float stepDiff = Math.abs(offset - stepOffset);
            if (stepDiff < diff) {
                closest = i;
                diff = stepDiff;
            }
        }

        return closest;
    }
    private void setPercentProgress(int percentProgress) {
        if (this.percentProgress == percentProgress) return;

        calculate();
        percentProgress = Math.max(0, Math.min(howManySteps, percentProgress));
        this.percentProgress = percentProgress;

        offset = percentProgress * aOffset;
        for (OnChangeListener listener : onChangeListeners)
            listener.onValueChange(this, getValue());
        invalidate();
    }

    public int getValue() {
        return min + percentProgress * step;
    }
    public void setValue(int progress) {
        setPercentProgress((progress - min) / step);
    }

    public int getMin() {
        return min;
    }
    public void setMin(int min) {
        this.min = min;
        invalidate();
    }

    public int getMax() {
        return max;
    }
    public void setMax(int max) {
        this.max = max;
        invalidate();
    }

    public int getStep() {
        return step;
    }
    public void setStep(int step) {
        this.step = step;
        invalidate();
    }

    public interface OnChangeListener {
        void onValueChange(@NonNull Slider slider, int value);
    }
    private final ArrayList<OnChangeListener> onChangeListeners = new ArrayList<>();
    public void addOnChangeListener(@NonNull OnChangeListener listener) {
        onChangeListeners.add(listener);
    }
    public void removeOnChangeListener(@NonNull OnChangeListener listener) {
        onChangeListeners.remove(listener);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), 32 * dp);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        calculate();

        // track
        trackDrawable.setBounds(0, 0, getWidth(), getHeight());
        trackDrawable.draw(canvas);

        // progress
        canvas.save();
        canvas.clipRect(0, 0, offset, getHeight());
        progressDrawable.setBounds(0, 0, getWidth(), getHeight());
        progressDrawable.draw(canvas);
        canvas.restore();

        // split
        for (int i = 0; i < howManySteps; i++) {
            float stepOffset = i * aOffset + getHeight() * 0.5f;
            Drawable split = stepOffset >= offset ? trackSplitDrawable : progressSplitDrawable;
            split.setBounds((int) (stepOffset), 0, (int) (stepOffset + dp * 2), getHeight());
            split.draw(canvas);
        }

        // thumb
        if (!touchable) return;
        thumbDrawable.setBounds((int) offset, 0, (int) (offset + getHeight()), getHeight());
        thumbDrawable.draw(canvas);
    }
}
