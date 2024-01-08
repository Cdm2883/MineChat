package vip.cdms.mcoreui.view.button;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

import vip.cdms.mcoreui.R;
import vip.cdms.mcoreui.util.PixelDrawable;
import vip.cdms.mcoreui.util.ResourcesUtils;
import vip.cdms.mcoreui.util.SoundPlayer;

public class IconButton extends AppCompatImageButton implements SoundPlayer.ClickSound {
    public IconButton(@NonNull Context context) {
        this(context, null);
    }
    public IconButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.buttonStyle);
    }
    public IconButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setScaleType(ScaleType.CENTER_CROP);
        setBackground(ResourcesUtils.getDrawable(getContext(), R.drawable.button_icon_background_selector));
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(PixelDrawable.instance(drawable));
    }
}
