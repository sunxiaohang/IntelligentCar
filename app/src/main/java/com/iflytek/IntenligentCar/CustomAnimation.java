package com.iflytek.IntenligentCar;

import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by m1320 on 2016/8/31.
 *
 * 输入框输入检测动画，当输入不符合正则规范时左右摇头
 * CustomAnimation继承自Animation
 */
public class CustomAnimation extends Animation {
    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    /***
     * @param interpolatedTime 转换时间
     * @param t                转换对象
     */
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
//        t.setAlpha(interpolatedTime);
//        t.getMatrix().setTranslate(200*interpolatedTime,200*interpolatedTime);
        /***
         * 设置插值器为正弦值变换
         */
        t.getMatrix().setTranslate((float) Math.sin(interpolatedTime * 30) * 20, 0);
        super.applyTransformation(interpolatedTime, t);
    }
}
