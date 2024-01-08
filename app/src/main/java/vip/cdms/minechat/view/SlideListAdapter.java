package vip.cdms.minechat.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.PixelDrawable;
import vip.cdms.mcoreui.util.ResourcesUtils;
import vip.cdms.mcoreui.util.SoundPlayer;
import vip.cdms.mcoreui.util.TimeUtils;
import vip.cdms.mcoreui.view.input.Switch;
import vip.cdms.mcoreui.view.show.ImageView;
import vip.cdms.mcoreui.view.show.TextView;
import vip.cdms.minechat.R;

// todo 迁移到UI库
public class SlideListAdapter extends RecyclerView.Adapter<SlideListAdapter.Holder> {
    public record Slide(Drawable icon, View.OnClickListener listener) {
        public Slide {
            if (icon != null) {
                icon = icon.mutate();
                icon.setTintList(ColorStateList.valueOf(0xffffffff));
            }
        }
    }
    // todo 支持选择批量操作 (点击图标进入选择模式)
    public static class DataItem {
        private Drawable icon;
        public CharSequence num;
        public CharSequence title;
        private CharSequence summary;
        private View.OnClickListener onClickListener;
        private final ArrayList<Slide> slides;
        private Drawable topIcon = null;
        private CharSequence topText = null;

        private Switch.OnCheckedChangeListener onCheckedChangeListener;
        private boolean checked = false;

        public DataItem(CharSequence title, CharSequence summary) {
            this(null, title, summary, null);
        }
        public DataItem(CharSequence title, CharSequence summary, View.OnClickListener listener) {
            this(null, title, summary, listener);
        }
        public DataItem(Context context, int id, CharSequence title, CharSequence summary, View.OnClickListener listener) {
            this(ResourcesUtils.getPixelDrawable(context, id), title, summary, listener);
        }
        public DataItem(Drawable icon, CharSequence title, CharSequence summary, View.OnClickListener listener) {
            this(icon, title, summary, listener, new ArrayList<>());
        }
        public DataItem(Drawable icon, CharSequence title, CharSequence summary, View.OnClickListener listener, ArrayList<Slide> slides) {
            this.icon = icon;
            this.title = title;
            this.summary = summary;
            onClickListener = listener;
            this.slides = slides;
        }

        public DataItem setIcon(Drawable icon) {
            this.icon = icon;
            return this;
        }

        public DataItem setNumber(CharSequence num) {
            this.num = num;
            return this;
        }
        public DataItem setNumber(Number num) {
            this.num = String.valueOf(num);
            return this;
        }

        public DataItem setTitle(CharSequence title) {
            this.title = title;
            return this;
        }
        public DataItem setSummary(CharSequence summary) {
            this.summary = summary;
            return this;
        }
        public DataItem setOnClickListener(View.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
            return this;
        }
        public DataItem setTopIcon(Drawable topIcon) {
            this.topIcon = topIcon;
            return this;
        }
        public DataItem setTopText(CharSequence topText) {
            this.topText = topText;
            return this;
        }

        public DataItem setSwitch(Switch.OnCheckedChangeListener onCheckedChangeListener, boolean defaultChecked) {
            this.onCheckedChangeListener = onCheckedChangeListener;
            this.checked = defaultChecked;
            return this;
        }
        public DataItem setSwitch(Switch.OnCheckedChangeListener onCheckedChangeListener) {
            return setSwitch(onCheckedChangeListener, false);
        }

        public DataItem addSlide(Context context, int id, View.OnClickListener listener) {
            return addSlide(ResourcesUtils.getPixelDrawable(context, id), listener);
        }
        public DataItem addSlide(Drawable icon, View.OnClickListener listener) {
            slides.add(new Slide(icon, listener));
            return this;
        }
    }
    public static class Holder extends RecyclerView.ViewHolder {
        LinearLayout root;
        ConstraintLayout content;
        ImageView icon;
        TextView num;
        TextView title;
        TextView summary;

        TextView topText;
        android.widget.ImageView topIcon;

        Switch switchWidget;
        LinearLayout actions;
        LinearLayout slider;
        public Holder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            content = itemView.findViewById(android.R.id.content);
            icon = itemView.findViewById(android.R.id.icon);
            num = itemView.findViewById(R.id.num);
            title = itemView.findViewById(android.R.id.title);
            summary = itemView.findViewById(android.R.id.summary);

            topText = itemView.findViewById(R.id.top_info_text);
            topIcon = itemView.findViewById(R.id.top_info_icon);

            switchWidget = itemView.findViewById(R.id.switch_widget);
            actions = itemView.findViewById(R.id.actions);
            slider = itemView.findViewById(R.id.slider);
        }
    }

    private final ArrayList<DataItem> localDataSet = new ArrayList<>();
    public SlideListAdapter addDataItem(DataItem dataItem) {
        localDataSet.add(dataItem);
        notifyItemInserted(localDataSet.size() - 1);
        return this;
    }
    public SlideListAdapter removeDataItem(int position) {
        localDataSet.remove(position);
        notifyItemRemoved(position);
        return this;
    }
    public SlideListAdapter clearDataItems() {
        int beforeSize = localDataSet.size();
        localDataSet.clear();
        notifyItemRangeRemoved(0, beforeSize);
        return this;
    }
    public SlideListAdapter changeDataItem(int position, DataItem dataItem) {
        localDataSet.set(position, dataItem);
        return post(() -> notifyItemChanged(position));
    }
    public void moveDataItem(int fromPosition, int toPosition) {
        Collections.swap(localDataSet, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }
    public SlideListAdapter post(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
        return this;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_slide, parent, false));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        DataItem dataItem = localDataSet.get(position);
        Context context = holder.root.getContext();

        final boolean[] clicked = {false};
        holder.root.setOnClickListener(v -> {  // 节流
            if (clicked[0]) return;
            if (dataItem.onClickListener != null) dataItem.onClickListener.onClick(v);
            clicked[0] = true;
            TimeUtils.setTimeout(() -> clicked[0] = false, 1000);
        });
        holder.root.setOnTouchListener(SoundPlayer::playClickSound);
        holder.title.setText(dataItem.title);
        holder.summary.setText(dataItem.summary);

        if (dataItem.icon == null) {
            holder.icon.setVisibility(View.GONE);
        } else {
            holder.icon.setImageDrawable(dataItem.icon);
            holder.icon.setVisibility(View.VISIBLE);
        }
        if (dataItem.num == null) {
            holder.num.setVisibility(View.GONE);
        } else {
            holder.num.setText(dataItem.num);
            holder.num.setVisibility(View.VISIBLE);
        }

        if (dataItem.topText == null) {
            holder.topText.setVisibility(View.GONE);
        } else {
            holder.topText.setText(dataItem.topText);
            holder.topText.setVisibility(View.VISIBLE);
        }
        if (dataItem.topIcon == null) {
            holder.topIcon.setVisibility(View.GONE);
        } else {
            holder.topIcon.setImageDrawable(PixelDrawable.instance(dataItem.topIcon));
            holder.topIcon.setVisibility(View.VISIBLE);
        }

        if (dataItem.onCheckedChangeListener == null) {
            holder.switchWidget.setVisibility(View.GONE);
            holder.switchWidget.setOnCheckedChangeListener(null);
        } else {
            holder.switchWidget.setVisibility(View.VISIBLE);
            holder.switchWidget.setOnCheckedChangeListener((switchWeight, isChecked) -> {
                DataItem dataItem1 = localDataSet.get(position);
                if (dataItem1.checked == isChecked) return;
                dataItem1.checked = isChecked;
                localDataSet.set(position, dataItem1);
                dataItem.onCheckedChangeListener.onCheckedChanged(switchWeight, isChecked);
            });
            holder.switchWidget.setChecked(dataItem.checked, false);
        }

        holder.slider.removeAllViews();
        holder.actions.removeAllViews();

        holder.content.setTranslationX(0);
        holder.slider.setTranslationX(0);

        if (dataItem.slides.isEmpty()) return;
        holder.root.post(() -> {
//            int width = holder.root.getHeight();
            int width = MathUtils.dp2px(context, 55);
            for (int i = 0; i < dataItem.slides.size(); i++) {
                var slide = dataItem.slides.get(i);
                var icon = slide.icon();
                var listener = slide.listener();
                if (icon == null) continue;
                ImageView imageView = new ImageView(context);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT));
                imageView.setBackgroundResource(R.drawable.list_border_background);
                imageView.setImageDrawable(icon);
                imageView.setOnClickListener(listener);
                imageView.setOnTouchListener((v, event) -> true);
                if (i == 0) holder.actions.addView(imageView);
                else holder.slider.addView(imageView);
            }
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.slider.getLayoutParams();
            layoutParams.width = width * (dataItem.slides.size() - 1);
            holder.slider.setLayoutParams(layoutParams);
        });
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
