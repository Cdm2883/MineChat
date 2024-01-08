package vip.cdms.mcoreui.util;

import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;

/**
 * 像素Drawable包装器 (关闭抗锯齿)
 * @author Cdm2883
 */
public class PixelDrawable extends DrawableWrapper {
    private PixelDrawable(Drawable drawable) {
        super(drawable);
//        mutate();
    }
    public static PixelDrawable instance(Drawable drawable) {
        return drawable instanceof PixelDrawable pixelDrawable ? pixelDrawable : new PixelDrawable(drawable);
    }

    public boolean isEmpty() {
        return getDrawable() == null;
    }

    @Override
    public void draw(Canvas canvas) {
        DrawFilter drawFilter = canvas.getDrawFilter();
        canvas.setDrawFilter(
                new PaintFlagsDrawFilter(Paint.FILTER_BITMAP_FLAG, 0)
        );
        super.draw(canvas);
        canvas.setDrawFilter(drawFilter);
    }
}
