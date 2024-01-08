package vip.cdms.mcoreui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import java.util.ArrayList;

import vip.cdms.mcoreui.util.SoundPlayer;
import vip.cdms.mcoreui.util.TimeUtils;
import vip.cdms.mcoreui.util.ViewUtils;
import vip.cdms.mcoreui.view.another.Appbar;

/**
 * 为了使用一些功能, 请务必使用此Activity
 */
public class OreUIActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_OreUI);
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(0xff202020);

//        getWindow()/*.getDecorView()*/.setBackgroundDrawable(new ColorDrawable(0x30000000));
//        getWindow().setBackgroundDrawable(new ColorDrawable(0xff101010));

        getSupportFragmentManager()
                .addFragmentOnAttachListener((fragmentManager, fragment) ->
                        TimeUtils.setTimeout(() ->
                                runOnUiThread(() ->
                                        stylized((ViewGroup) getWindow().getDecorView())), 200));
    }
    public void hideStatusBar() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        stylized();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private final ArrayList<View> addedClickSoundViews = new ArrayList<>();
    public void stylized() {
        stylized((ViewGroup) getWindow().getDecorView());
    }
    @SuppressLint("ClickableViewAccessibility")
    private void stylized(ViewGroup viewGroup) {
        if (viewGroup == null) return;
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = viewGroup.getChildAt(i);
            if (childView instanceof ViewGroup childViewGroup) stylized(childViewGroup);
            if (childView instanceof Appbar appbar1 && appbar == null) appbar = appbar1;
            if (
                    childView instanceof ScrollView
                    || childView instanceof NestedScrollView
            ) ViewUtils.setOreUIVerticalScrollBar(childView);
            if (childView instanceof SoundPlayer.ClickSound clickSound && !addedClickSoundViews.contains(childView)) {
                ViewUtils.addOnTouchListener(childView, (v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                        SoundPlayer.play(this, clickSound.getClickSoundRawResId());
                    return false;
                }, false);
                addedClickSoundViews.add(childView);
            }
        }
    }

    private Appbar appbar = null;
    @Override
    public void setTitle(CharSequence title) {
        appbar.setTitle(title);
        super.setTitle(title);
    }

    private final ArrayList<View> hasClickSoundViews = new ArrayList<>();
    public void addClickSound(View view) {
        if (hasClickSoundViews.contains(view)) return;
        hasClickSoundViews.add(view);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) return super.dispatchTouchEvent(event);

        View clickedView = ViewUtils.findViewByCoordinates(
                (ViewGroup) getWindow().getDecorView(),
                event.getX(),
                event.getY()
        );
        if (clickedView == null) return super.dispatchTouchEvent(event);

        if (clickedView.isEnabled() && !addedClickSoundViews.contains(clickedView)) {
            if (clickedView instanceof SoundPlayer.ClickSound clickSound)
                SoundPlayer.play(this, clickSound.getClickSoundRawResId());
            else if (hasClickSoundViews.contains(clickedView))
                SoundPlayer.playClickSound(this);
            addedClickSoundViews.add(clickedView);
        }


        return super.dispatchTouchEvent(event);
    }
}
