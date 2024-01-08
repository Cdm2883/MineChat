package vip.cdms.minechat.activity.chat;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.constraintlayout.widget.ConstraintLayout;

public class AppbarExpander implements View.OnTouchListener {
    boolean isExpanded = false;
    int maxHeight;
    float startY;

    ChatActivity activity;
    public AppbarExpander(ChatActivity activity) {
        this.activity = activity;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (activity.client == null) return false;
        float nowY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                maxHeight = activity.binding.appbarMore.getHeight();
                startY = nowY;
            }
            case MotionEvent.ACTION_UP -> {
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) activity.binding.appbar.getLayoutParams();
                int topMargin = layoutParams.topMargin;

                if (topMargin == 0) isExpanded = false;
                else if (topMargin == maxHeight) isExpanded = true;

                if (topMargin == 0 || topMargin == maxHeight) break;
//                        boolean back = topMargin < maxHeight / 2;
                boolean back = nowY - startY < 0;
                isExpanded = !back;
                ValueAnimator animator = ValueAnimator.ofInt(topMargin, back ? 0 : maxHeight);
                animator.setDuration(200);
                animator.setInterpolator(new DecelerateInterpolator());
                animator.addUpdateListener(animation -> {
                    layoutParams.topMargin = (int) animation.getAnimatedValue();
                    activity.binding.appbar.setLayoutParams(layoutParams);
                });
                animator.start();
            }
            case MotionEvent.ACTION_MOVE -> {
                float diffY = nowY - startY;
                if (diffY == 0) break;

                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) activity.binding.appbar.getLayoutParams();
                int topMargin = (int) diffY + layoutParams.topMargin;
                if (topMargin < 0) break;
                if (topMargin > maxHeight) break;

                layoutParams.topMargin = topMargin;
                activity.binding.appbar.setLayoutParams(layoutParams);
            }
        }
        return true;
    }

    public void toggle(boolean expand) {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) activity.binding.appbar.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(layoutParams.topMargin, expand ? maxHeight : 0);
        animator.setDuration(200);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            layoutParams.topMargin = (int) animation.getAnimatedValue();
            activity.binding.appbar.setLayoutParams(layoutParams);
        });
        animator.start();
        isExpanded = expand;
    }
    public void expand() {
        toggle(true);
    }
    public void collapse() {
        toggle(false);
    }
}
