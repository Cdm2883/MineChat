package vip.cdms.minechat.activity.chat;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import vip.cdms.minechat.R;

public class FloatingChatService extends Service implements View.OnTouchListener {
    private WindowManager windowManager;
    private RecyclerView recyclerView;
    private MessageAdapter adapter;

    private final IBinder mBinder = new Binder();
    public class Binder extends android.os.Binder {
        FloatingChatService getService() {
            return FloatingChatService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private int screenWidth;
    private int screenHeight;
    private WindowManager.LayoutParams params;
    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (recyclerView != null) return START_STICKY;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        recyclerView = (RecyclerView) LayoutInflater.from(this)
                .inflate(R.layout.layout_floating_chat, null);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MessageAdapter(linearLayoutManager, true);
        recyclerView.setAdapter(adapter);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;

        windowManager.addView(recyclerView, params);
        recyclerView.setOnTouchListener(this);

        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recyclerView != null)
            windowManager.removeView(recyclerView);
    }
    public void print(CharSequence message) {
        adapter.addMessage(5000, false, message);
    }

    private float initialX, initialY;
    private float initialTouchX, initialTouchY;
    private boolean isDragging = false;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                initialX = params.x;
                initialY = params.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                isDragging = false;
//                isLongPressing = false;
//                longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_DURATION);
            }
            case MotionEvent.ACTION_MOVE -> {
                float deltaX = event.getRawX() - initialTouchX;
                float deltaY = event.getRawY() - initialTouchY;
                if (Math.abs(deltaX) < 10 && Math.abs(deltaY) < 10) break;

                isDragging = true;
                params.x = (int) (initialX + deltaX);
                params.y = (int) (initialY + deltaY);

                if (params.x < 0) params.x = 0;
                else if (params.x + v.getWidth() > screenWidth) params.x = screenWidth - v.getWidth();

                if (params.y < 0) params.y = 0;
                else if (params.y + v.getHeight() > screenHeight) params.y = screenHeight - v.getHeight();

                windowManager.updateViewLayout(recyclerView, params);
//                longPressHandler.removeCallbacks(longPressRunnable);
            }
            case MotionEvent.ACTION_UP -> {
                if (/*!isLongPressing && */!isDragging) v.performClick();
//                longPressHandler.removeCallbacks(longPressRunnable);
            }
        }
        return true;
    }
}
