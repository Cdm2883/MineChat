package vip.cdms.mcoreui.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;

import vip.cdms.mcoreui.R;

/**
 * 音效播放小工具
 * @author Cdm2883
 */
public class SoundPlayer {
    public static SoundPool soundPool = null;
    public static HashMap<Integer, Integer> sounds = new HashMap<>();

    public static void init(Context context) {
        if (soundPool != null) return;
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        load(context, R.raw.button_click);
        load(context, R.raw.button_click_pop);
        load(context, R.raw.toast);
    }

    public static void load(Context context, int resId) {
        if (sounds.containsKey(resId)) return;
        sounds.put(resId, soundPool.load(context, resId, resId));
    }

    public static void play(Context context, int resId) {
        load(context, resId);

        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        // 获取当前音量
        float streamVolumeCurrent = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
        // 获取系统最大音量
        float streamVolumeMax = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 计算得到播放音量
        float volume = streamVolumeCurrent / streamVolumeMax;
        // 调用SoundPool的play方法来播放声音文件

        soundPool.play(sounds.get(resId), volume, volume, 1, 0, 1.0f);
    }

    /** 自定义view可以实现此接口以实现在OreUIActivity中响应点击音效 */
    public interface ClickSound {
        /** 获取点击音效的ResId */
        default int getClickSoundRawResId() {
            return R.raw.button_click;
        }
    }

    /** 播放点击音效 */
    public static void playClickSound(Context context) {
        play(context, R.raw.button_click);
    }

    /** @return false */
    public static boolean playClickSound(View view, MotionEvent event) {
        if (view == null || event.getAction() != MotionEvent.ACTION_DOWN) return false;
        Context context = view.getContext();
        if (view instanceof ClickSound clickSound) play(context, clickSound.getClickSoundRawResId());
        else playClickSound(context);
        return false;
    }
}
