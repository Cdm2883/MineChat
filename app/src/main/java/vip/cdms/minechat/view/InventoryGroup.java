package vip.cdms.minechat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class InventoryGroup extends LinearLayout {
    public InventoryGroup(Context context) {
        this(context, null);
    }
    public InventoryGroup(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public InventoryGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    public InventoryGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setOrientation(VERTICAL);
    }

    public InventoryView addInventory() {
        return null;
    }
}
