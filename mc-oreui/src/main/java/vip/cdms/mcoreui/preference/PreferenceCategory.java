package vip.cdms.mcoreui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;

import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.PixelFont;

public class PreferenceCategory extends androidx.preference.PreferenceCategory {
    public PreferenceCategory(@NonNull Context context) {
        this(context, null);
    }
    @SuppressLint("RestrictedApi")
    public PreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, androidx.preference.R.attr.preferenceCategoryStyle,
                android.R.attr.preferenceCategoryStyle));
    }
    public PreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    public PreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setBackgroundColor(0xff48494a);

        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        titleView.setTextColor(0xffffffff);
        titleView.setText(new PixelFont(titleView.getText(), true));
        titleView.setPadding(
                titleView.getPaddingLeft(),
                titleView.getPaddingTop() + MathUtils.dp2px(titleView.getContext(), 8),
                titleView.getPaddingRight(),
                titleView.getPaddingBottom()
        );

        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        layoutParams.setMargins(0, 0, 0, 0);
        holder.itemView.setLayoutParams(layoutParams);
    }
}
