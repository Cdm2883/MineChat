package vip.cdms.mcoreui.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import androidx.annotation.Nullable;

/**
 * 图像小工具
 * @author Cdm2883
 */
public class ImageUtils {
    public static Drawable setBounds(Drawable drawable) {
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        return drawable;
    }

    /** 只是用来标记 */
    public static class RotatedDrawable extends BitmapDrawable {
        public RotatedDrawable(Bitmap bitmap) {
            super(null, bitmap);
        }
    }
    public static RotatedDrawable rotateDrawable(Drawable originalDrawable, float degrees) {
        Bitmap originalBitmap = drawableToBitmap(originalDrawable);
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);

        Bitmap rotatedBitmap = Bitmap.createBitmap(
                originalBitmap,
                0,
                0,
                originalBitmap.getWidth(),
                originalBitmap.getHeight(),
                matrix,
                true
        );

        RotatedDrawable rotatedDrawable = new RotatedDrawable(rotatedBitmap);
        rotatedDrawable.setBounds(0, 0, originalDrawable.getMinimumWidth(), originalDrawable.getMinimumHeight());
        return rotatedDrawable;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static Drawable bitmapToDrawable(@Nullable Context context, Bitmap bitmap) {
        return PixelDrawable.instance(new BitmapDrawable(context == null ? null : context.getResources(), bitmap));
    }
    public static Bitmap base64ToBitmap(String base64) {
        byte[] bytes = Base64.decode(base64.split(",")[1], Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    public static Drawable base64ToDrawable(Context context, String base64) {
        return bitmapToDrawable(context, base64ToBitmap(base64));
    }
}
