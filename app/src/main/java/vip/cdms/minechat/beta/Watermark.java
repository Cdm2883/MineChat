package vip.cdms.minechat.beta;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.ResourcesUtils;

public class Watermark {
    private static String text;
    public static void show(Activity activity) {
        try {
            if (text == null) {
                PackageManager packageManager = activity.getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageInfo(activity.getPackageName(), 0);
                if (packageInfo.versionName.contains("beta")) text = "MINECHAT BETA";
                else if (packageInfo.versionName.contains("dev")) text = "MINECHAT DEV";
            }

            ViewGroup rootView = activity.findViewById(android.R.id.content);
            FrameLayout layout = new FrameLayout(activity);
            layout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            WatermarkDrawable drawable = new WatermarkDrawable(activity, layout);
            layout.setBackground(drawable);
            rootView.addView(layout);
        } catch (Exception ignored) {}
    }

    private static class WatermarkDrawable extends Drawable {
        private final Context context;
        private final Paint paint;
        private WatermarkDrawable(Context context, View view) {
            this.context = context;
            paint = new Paint();
        }
        @Override
        public void draw(@NonNull Canvas canvas) {
            int width = getBounds().right;
            int height = getBounds().bottom;
            int diagonal = (int) Math.sqrt(width * width + height * height);

            paint.setColor(0x08ffffff);
            paint.setTextSize(MathUtils.dp2px(context, 20));
            paint.setAntiAlias(true);
            paint.setTypeface(ResourcesUtils.getTypeface(context, vip.cdms.mcoreui.R.font.minecraftten));
            float textWidth = paint.measureText(text);

            canvas.drawColor(0x00000000);
            canvas.rotate(-15);

            int index = 0;
            float fromX;
            for (int positionY = diagonal / 10; positionY <= diagonal; positionY += diagonal / 10) {
                fromX = -width + (index++ % 2) * textWidth;
                for (float positionX = fromX; positionX < width; positionX += textWidth * 2)
                    canvas.drawText(text, positionX, positionY, paint);
            }

            canvas.save();
            canvas.restore();
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}
