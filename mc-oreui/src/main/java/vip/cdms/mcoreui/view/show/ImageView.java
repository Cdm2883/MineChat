package vip.cdms.mcoreui.view.show;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import vip.cdms.mcoreui.R;
import vip.cdms.mcoreui.util.PixelDrawable;
import vip.cdms.mcoreui.util.ResourcesUtils;

/**
 * ORE UI风格图片 (只是加个边框和关闭抗锯齿<3)
 * @author Cdm2883
 */
public class ImageView extends AppCompatImageView {
    public ImageView(@NonNull Context context) {
        this(context, null);
    }
    public ImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public ImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray attributes = context.obtainStyledAttributes(attrs, new int[]{ android.R.attr.background });
        Drawable background = attributes.getDrawable(0);
        if (background == null) setBackground(ResourcesUtils.getPixelDrawable(context, R.drawable.border));
        attributes.recycle();
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        // 关闭抗锯齿
        super.setImageDrawable(PixelDrawable.instance(drawable));
    }
}
