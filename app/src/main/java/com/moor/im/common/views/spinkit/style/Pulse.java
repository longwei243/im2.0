package com.moor.im.common.views.spinkit.style;

import android.animation.ValueAnimator;

import com.moor.im.common.views.spinkit.animation.SpriteAnimatorBuilder;
import com.moor.im.common.views.spinkit.sprite.CircleSprite;

/**
 * Created by ybq.
 */
public class Pulse extends CircleSprite {

    public Pulse() {
        setScale(0f);
    }

    @Override
    public ValueAnimator onCreateAnimation() {
        float fractions[] = new float[]{0f, 1f};
        return new SpriteAnimatorBuilder(this).
                scale(fractions, 0f, 1f).
                alpha(fractions, 255, 0).
                duration(1000).
                easeInOut(fractions)
                .build();
    }
}
