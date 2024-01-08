package vip.cdms.minechat.activity.chat;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

public class TextScale implements RecyclerView.OnItemTouchListener, ScaleGestureDetector.OnScaleGestureListener {
    MessageAdapter adapter;
    RecyclerView recyclerView;
    ScaleGestureDetector scaleDetector;
    float scaleBase = 1;
    float scaleLevel = 1;
    float average = 1;
    float scaledRecord = 0;
    public TextScale(MessageAdapter adapter, RecyclerView recyclerView) {
        this.adapter = adapter;
        this.recyclerView = recyclerView;
    }
    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        scaleDetector = new ScaleGestureDetector(rv.getContext(), this);
        return e.getPointerCount() > 1;
    }
    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        scaleDetector.onTouchEvent(e);
    }
    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        average = (average + detector.getCurrentSpan()) / 2;
        scaleLevel = average / scaleBase;
        if (scaledRecord != 0) scaleLevel *= scaledRecord;

        scale(MessageAdapter.DEFAULT_TEXT_SIZE * scaleLevel);
        return true;
    }

    public void scale(float textSize) {
        adapter.textSize = textSize;
        for (int i = 0; i < adapter.messages.size(); i++) adapter.notifyItemChanged(i);
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        average = scaleBase = detector.getCurrentSpan();
        if (recyclerView.getItemAnimator() != null) {  // 去除notifyItemChanged动画, 减少卡顿
            recyclerView.getItemAnimator().setAddDuration(0);
            recyclerView.getItemAnimator().setChangeDuration(0);
            recyclerView.getItemAnimator().setMoveDuration(0);
            recyclerView.getItemAnimator().setRemoveDuration(0);
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        }
        return true;
    }
    @Override
    public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
        scaledRecord = scaleLevel;
        if (recyclerView.getItemAnimator() != null) {  // 恢复动画
            recyclerView.getItemAnimator().setAddDuration(120);
            recyclerView.getItemAnimator().setChangeDuration(250);
            recyclerView.getItemAnimator().setMoveDuration(250);
            recyclerView.getItemAnimator().setRemoveDuration(120);
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(true);
        }
    }
}
