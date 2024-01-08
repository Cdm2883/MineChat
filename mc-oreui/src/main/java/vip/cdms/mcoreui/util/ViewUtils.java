package vip.cdms.mcoreui.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.ScaleGestureDetectorCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;

import vip.cdms.mcoreui.R;

/**
 * 视图工具
 * @author Cdm2883
 */
public class ViewUtils {
    public static void forEach(ViewGroup viewGroup, Consumer<View> callback) {
//        if (viewGroup == null) return;
//        int childCount = viewGroup.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            View childView = viewGroup.getChildAt(i);
//            callback.accept(childView);
//            if (childView instanceof ViewGroup childViewGroup)
//                forEach(childViewGroup, callback);
//        }
        forEachBreak(viewGroup, view -> {
            callback.accept(view);
            return true;
        });
    }
    public static void forEachBreak(ViewGroup viewGroup, Function<View, Boolean> callback) {
        if (viewGroup == null) return;
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = viewGroup.getChildAt(i);
            if (callback.apply(childView) && childView instanceof ViewGroup childViewGroup)
                forEachBreak(childViewGroup, callback);
        }
    }
    public static void forEachParent(View view, Function<View, Boolean> callback) {
        View parent = (View) view.getParent();
        if (parent != null && callback.apply(parent)) {
            forEachParent(parent, callback);
        }
    }

    /** 通过坐标获取view */
    public static View findViewByCoordinates(ViewGroup viewGroup, float x, float y) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = viewGroup.getChildAt(i);
            if (isPointInsideView(childView, x, y)) {
                if (childView instanceof ViewGroup) {
                    // 如果点击事件发生在一个ViewGroup上, 递归地继续查找子View
                    return findViewByCoordinates((ViewGroup) childView, x, y);
                } else {
                    // 如果点击事件发生在一个普通View上, 直接返回该View
                    return childView;
                }
            }
        }
        return null;
    }
    /** 判断坐标点是否在view上 */
    public static boolean isPointInsideView(View view, float x, float y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();

        return (x >= viewX && x <= (viewX + viewWidth) &&
                y >= viewY && y <= (viewY + viewHeight));
    }

    /** 添加点击事件而不是替换/设置 */
    public static void addOnClickListener(View view, @NonNull View.OnClickListener newClickListener) {
        // 获取之前设置的点击事件
        View.OnClickListener previousClickListener;
        try {
            Object listenerInfo = ReflectionUtils.invoke(View.class, "getListenerInfo", view);
            previousClickListener = (View.OnClickListener) ReflectionUtils.get("android.view.View$ListenerInfo", "mOnClickListener", listenerInfo);
        } catch (Throwable e) {
            e.printStackTrace();
            view.setOnClickListener(newClickListener);
            return;
        }

        // 设置新的点击事件
        View.OnClickListener finalPreviousClickListener = previousClickListener;
        view.setOnClickListener(v -> {
            // 调用之前设置的点击事件
            if (finalPreviousClickListener != null) finalPreviousClickListener.onClick(v);
            // 调用新设置的点击事件
            newClickListener.onClick(v);
        });
    }

    /** 添加触摸事件而不是替换/设置 */
    @SuppressLint("ClickableViewAccessibility")
    public static void addOnTouchListener(View view, @NonNull View.OnTouchListener newTouchListener, boolean canConsume) {
        View.OnTouchListener previousTouchListener;
        try {
            Object listenerInfo = ReflectionUtils.invoke(View.class, "getListenerInfo", view);
            previousTouchListener = (View.OnTouchListener) ReflectionUtils.get("android.view.View$ListenerInfo", "mOnTouchListener", listenerInfo);
        } catch (Throwable e) {
            e.printStackTrace();
            view.setOnTouchListener(newTouchListener);
            return;
        }
        View.OnTouchListener finalPreviousTouchListener = previousTouchListener;
        if (canConsume) view.setOnTouchListener((v, event) ->
                (finalPreviousTouchListener != null && finalPreviousTouchListener.onTouch(v, event)) || newTouchListener.onTouch(v, event));
        else view.setOnTouchListener((v, event) -> {
            if (finalPreviousTouchListener != null) finalPreviousTouchListener.onTouch(v, event);
            return newTouchListener.onTouch(v, event);
        });
    }

    public static void setTooltip(View view, String text, Runnable then) {
        new Thread(() -> {
            CharSequence texted = MCFontWrapper.WRAPPER.wrap(text);
            view.post(() -> {
                TooltipCompat.setTooltipText(view, texted);
                if (then != null) then.run();
            });
        }).start();
    }
    @SuppressLint("SoonBlockedPrivateApi")
    public static void showTooltip(View view) {
        int [] outLocation = new int[2];
        view.getLocationOnScreen(outLocation);

        try {
            ReflectionUtils.invoke(View.class, "showTooltip", ReflectionUtils.A_P_T, view,
                    outLocation[0], 0, false);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    public static void hideTooltip(View view) {
        TooltipCompat.setTooltipText(view, null);
        try {
            ReflectionUtils.invoke(View.class, "hideTooltip", view);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static class DragTouchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener implements View.OnTouchListener {
        final boolean draggable;
        final boolean scalable;
        public DragTouchListener(boolean draggable, boolean scalable) {
            this.draggable = draggable;
            this.scalable = scalable;
        }
        
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public final boolean onTouch(View v, MotionEvent event) {
            return scale(v, event) | drag(v, event);
        }

        float startX;
        float startY;
        boolean isMoving;
        final boolean drag(View v, MotionEvent event) {
            if (!draggable) return false;
            float nowX = event.getX();
            float nowY = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    startX = nowX;
                    startY = nowY;
                    isMoving = false;
                }
                case MotionEvent.ACTION_MOVE -> {
                    float diffX = nowX - startX;
                    float diffY = nowY - startY;
                    if (diffX == 0 && diffY == 0) break;
                    addX(v, diffX);
                    addY(v, diffY);
                    isMoving = true;
                }
            }
            return isMoving;
        }
        public void addX(View view, float diffX) {
            view.setTranslationX(view.getTranslationX() + diffX);
        }
        public void addY(View view, float diffY) {
            view.setTranslationY(view.getTranslationY() + diffY);
        }

        float nowScaleLevel = 1;
        float startScaleLevel = -1;
        float startFingerDis = -1;
        final boolean scale(View v, MotionEvent event) {
            if (!scalable) return false;
            if (event.getAction() == MotionEvent.ACTION_UP) startScaleLevel = startFingerDis = -1;
            if (event.getPointerCount() != 2 || event.getAction() != MotionEvent.ACTION_MOVE) return false;

            float nowFingerDis = getFingerDistance(event);
            if (startScaleLevel == -1) {
                startScaleLevel = nowScaleLevel;
                return false;
            }
            if (startFingerDis == -1) {
                startFingerDis = nowFingerDis;
                return false;
            }

            nowScaleLevel = startScaleLevel * (nowFingerDis / startFingerDis);
//            nowScaleLevel = Math.min(nowScaleLevel, 2);

            scale(v, nowScaleLevel);
            return true;
        }
        public void scale(View view, float level) {
            view.setScaleX(level);
            view.setScaleY(level);
        }
        MathUtils.FloatAverageFilter scaleFiler = new MathUtils.FloatAverageFilter(5);
        final float getFingerDistance(MotionEvent event) {
            float deltaX = event.getX(0) - event.getX(1);
            float deltaY = event.getY(0) - event.getY(1);
            return scaleFiler.filter((float) Math.sqrt(deltaX * deltaX + deltaY * deltaY));
        }
    }

    /** 给view设置OREUI风格的垂直滚动条 */
    public static void setOreUIVerticalScrollBar(View scrollView) {
        Drawable thumb = ResourcesUtils.getPixelDrawable(scrollView.getContext(), R.drawable.scrollbar_thumb_vertical);
        Drawable track = ResourcesUtils.getPixelDrawable(scrollView.getContext(), R.drawable.scrollbar_track_vertical);

//        scrollView.setVerticalScrollBarEnabled(true);
//        scrollView.setScrollbarFadingEnabled(false);
//        scrollView.setScrollBarFadeDuration(0);
//        scrollView.setScrollBarSize(1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            scrollView.setVerticalScrollbarThumbDrawable(thumb);
            scrollView.setVerticalScrollbarTrackDrawable(track);
            return;
        }

        try {
            Object scrollCache = ReflectionUtils.get(View.class, "mScrollCache", scrollView);
            if (scrollCache == null) return;
            Object scrollBar = ReflectionUtils.get("scrollBar", scrollCache);
            if (scrollBar == null) return;
            ReflectionUtils.invoke("setVerticalThumbDrawable", ReflectionUtils.D_U_P_T, scrollBar, Drawable.class, thumb);
            ReflectionUtils.invoke("setVerticalTrackDrawable", ReflectionUtils.D_U_P_T, scrollBar, Drawable.class, track);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void animateHide(View view) {
        animateHide(view, null);
    }
    public static void animateHide(View view, Runnable then) {
        view.animate().alpha(0).setDuration(150).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.animate().setListener(null);
                view.setVisibility(View.GONE);
                if (then != null) then.run();
            }
        }).start();
    }
    public static void animateShow(View view) {
        view.setVisibility(View.VISIBLE);
        view.animate().alpha(1).setDuration(150).start();
    }
}
