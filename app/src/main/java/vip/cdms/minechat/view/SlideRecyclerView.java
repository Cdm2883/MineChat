package vip.cdms.minechat.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.SoundPlayer;
import vip.cdms.mcoreui.util.ViewUtils;
import vip.cdms.mcoreui.view.show.ImageView;
import vip.cdms.mcoreui.view.show.TextView;

public class SlideRecyclerView extends RecyclerView {
    private final LinearLayoutManager layout;
    private final ItemTouchHelper helper;
    private final SlideListAdapter adapter;
    public SlideRecyclerView(@NonNull Context context) {
        this(context, null);
    }
    public SlideRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, androidx.recyclerview.R.attr.recyclerViewStyle);
    }
    public SlideRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutManager(layout = new LinearLayoutManager(context));

        setBackgroundColor(0xff1e1e1f);
        setClipToPadding(false);

        setAdapter(adapter = new SlideListAdapter());
        helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            private int fromPosition = -1;
            private int toPosition = -1;

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START);
            }
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder, @NonNull ViewHolder target) {
//                adapter.moveDataItem(viewHolder.getAdapterPosition(), target.getAdapterPosition());
//                if (onItemMoveListener != null) onItemMoveListener.onItemMoved(target.getAdapterPosition(), viewHolder.getAdapterPosition());
//
//                if (fromPosition == -1) fromPosition = viewHolder.getAdapterPosition();
//                toPosition = target.getAdapterPosition();
                return true;
            }

            @Override
            public void onMoved(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder, int fromPos, @NonNull ViewHolder target, int toPos, int x, int y) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);

                boolean canMove = onItemMoveListener == null || onItemMoveListener.onItemMove(fromPos, toPos);

                if (canMove) {
                    adapter.moveDataItem(fromPos, toPos);
                    if (fromPosition == -1) fromPosition = fromPos;
                    toPosition = toPos;
                }
            }

            @Override
            public void onSwiped(@NonNull ViewHolder viewHolder, int direction) {}

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                if (fromPosition != -1 && toPosition != -1 && onItemMovedListener != null) {
                    onItemMovedListener.onItemMoved(fromPosition, toPosition);
                    fromPosition = -1;
                    toPosition = -1;
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                SlideListAdapter.Holder holder = (SlideListAdapter.Holder) viewHolder;
                if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    return;
                }

                ConstraintLayout content = holder.content;
                LinearLayout slider = holder.slider;
                int translateX = slider.getWidth();

                if (dX < -translateX) dX = -translateX;

                if (dX == 0) {
                    content.animate().setDuration(150).translationX(dX).start();
                    slider.animate().setDuration(150).translationX(dX).start();
                } else {
                    content.setTranslationX(dX);
                    slider.setTranslationX(dX);
                }
            }
        });
        helper.attachToRecyclerView(this);

        ViewUtils.setOreUIVerticalScrollBar(this);
    }

    public void scrollToBottom() {
        layout.scrollToPositionWithOffset(adapter.getItemCount() - 1, Integer.MIN_VALUE);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (e.getAction() != MotionEvent.ACTION_DOWN) return super.onInterceptTouchEvent(e);

        LinearLayout touchItemView = (LinearLayout) findChildViewUnder(e.getX(), e.getY());
        float x = e.getX();
        ViewUtils.forEach(touchItemView, view -> {
            if (!(view instanceof ImageView)) return;
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int viewX = location[0];
            int viewWidth = view.getWidth();
            if (x >= viewX && x <= (viewX + viewWidth)) {
                view.performClick();
                SoundPlayer.playClickSound(getContext());
            }
        });
        return super.onInterceptTouchEvent(e);
    }

    @NonNull
    @Override
    public SlideListAdapter getAdapter() {
        return adapter;
    }

    public interface OnItemMoveListener {
        boolean onItemMove(int fromPosition, int toPosition);
    }
    private OnItemMoveListener onItemMoveListener;
    public void setOnItemMoveListener(OnItemMoveListener onItemMoveListener) {
        this.onItemMoveListener = onItemMoveListener;
    }

    public interface OnItemMovedListener {
        void onItemMoved(int fromPosition, int toPosition);
    }
    private OnItemMovedListener onItemMovedListener;
    public void setOnItemMovedListener(OnItemMovedListener onItemMovedListener) {
        this.onItemMovedListener = onItemMovedListener;
    }
}