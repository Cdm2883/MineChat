package vip.cdms.mcoreui.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.FontRes;
import androidx.core.content.res.ResourcesCompat;

/**
 * 资源小工具
 * @author Cdm2883
 */
public class ResourcesUtils {
    public static Drawable getDrawable(Context context, @DrawableRes int id) {
        if (id == -1) return null;
        return ResourcesCompat.getDrawable(context.getResources(), id, null);
    }
    public static Drawable getPixelDrawable(Context context, @DrawableRes int id) {
        return PixelDrawable.instance(getDrawable(context, id));
    }

    public static Typeface getTypeface(Context context, @FontRes int id) {
        return ResourcesCompat.getFont(context, id);
    }

    public static int getColor(Context context, @ColorRes int id) {
        return ResourcesCompat.getColor(context.getResources(), id, context.getTheme());
    }
}
