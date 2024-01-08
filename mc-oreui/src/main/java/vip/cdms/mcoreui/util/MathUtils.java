package vip.cdms.mcoreui.util;

import android.content.Context;

import java.util.ArrayList;

/**
 * 计算工具
 * @author Cdm2883
 */
public class MathUtils {
    /**
     * 除法
     * @param dividend 被除数
     * @param divisor 除数
     * @param accuracy 保留几位小数
     */
    public static double divide(int dividend, int divisor, int accuracy) {
        return Math.round(dividend * Math.pow(10, accuracy) / divisor) / Math.pow(10.0, accuracy);
    }

    /**
     * 根据手机的分辨率从 dp(相对大小) 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        // 获取屏幕密度
        final float scale = context.getResources().getDisplayMetrics().density;
        // 结果+0.5是为了int取整时更接近
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp(相对大小)
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 用于平滑数值抖动的滤波器
     */
    public static class FloatAverageFilter {
        final int bufferSize;
        final ArrayList<Float> buffer = new ArrayList<>();
        /**
         * @param bufferSize 缓冲区大小
         */
        public FloatAverageFilter(int bufferSize) {
            this.bufferSize = bufferSize;
        }
        public float filter(float newValue) {
            buffer.add(newValue);
            if (buffer.size() > bufferSize) buffer.remove(0);
            float sum = (float) buffer.stream().mapToDouble(Float::floatValue).sum();
            return sum / buffer.size();
        }

//        private final float[] buffer;
//        private final int bufferSize;
//        private int currentIndex;
//        private float sum;
//
//        /**
//         * @param bufferSize 缓冲区大小
//         */
//        public FloatAverageFilter(int bufferSize) {
//            this.bufferSize = bufferSize;
//            buffer = new float[bufferSize];
//            currentIndex = 0;
//            sum = 0;
//        }
//        public float filter(float newValue) {
//            sum -= buffer[currentIndex];
//            buffer[currentIndex] = newValue;
//            sum += newValue;
//            currentIndex = (currentIndex + 1) % bufferSize;
//
//            System.out.println(buffer.length);
//
//            return sum / bufferSize;
//        }
    }
}
