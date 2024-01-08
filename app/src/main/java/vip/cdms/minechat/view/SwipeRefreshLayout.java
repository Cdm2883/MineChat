package vip.cdms.minechat.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import vip.cdms.mcoreui.util.ImageUtils;
import vip.cdms.mcoreui.util.PixelDrawable;
import vip.cdms.mcoreui.util.ReflectionUtils;
import vip.cdms.mcoreui.util.ResourcesUtils;
import vip.cdms.minechat.R;

public class SwipeRefreshLayout extends androidx.swiperefreshlayout.widget.SwipeRefreshLayout {
    public SwipeRefreshLayout(@NonNull Context context) throws Throwable {
        this(context, null);
    }
    public SwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) throws Throwable {
        super(context, attrs);

        // 去除背景 (透明)
        setProgressBackgroundColorSchemeResource(android.R.color.transparent);

        // 修改进度条 (应该说"进度圈"?)
        ImageView circleView = ReflectionUtils.get(
                androidx.swiperefreshlayout.widget.SwipeRefreshLayout.class,
                "mCircleView",
                this
        );
        circleView.setImageDrawable(getLoadingDrawable(context));  // 进度条动画
        circleView.setElevation(0);  // 去除阴影
    }

    public static Drawable getLoadingDrawable(@NonNull Context context) {
        final int totalFrames = 10;

        AnimationDrawable animationDrawable = new AnimationDrawable();
        Bitmap allFrames = ImageUtils.drawableToBitmap(ResourcesUtils.getDrawable(context, R.drawable.loading_spin));
        int width = allFrames.getWidth();
        int height = allFrames.getHeight();
        int frameWidth = width / totalFrames;
        for (int i = 0; i < totalFrames; i++) {
            Bitmap frameBitmap = Bitmap.createBitmap(allFrames, i * frameWidth, 0, frameWidth, height);
            Drawable frameDrawable = new BitmapDrawable(context.getResources(), frameBitmap);
            animationDrawable.addFrame(frameDrawable, 80);
        }

        animationDrawable.start();
        animationDrawable.setOneShot(false);

        return PixelDrawable.instance(animationDrawable);
    }
}
