package com.moor.im.common.views.spinkit;

import com.moor.im.common.views.spinkit.sprite.Sprite;
import com.moor.im.common.views.spinkit.style.ChasingDots;
import com.moor.im.common.views.spinkit.style.Circle;
import com.moor.im.common.views.spinkit.style.CubeGrid;
import com.moor.im.common.views.spinkit.style.DoubleBounce;
import com.moor.im.common.views.spinkit.style.FadingCircle;
import com.moor.im.common.views.spinkit.style.FoldingCube;
import com.moor.im.common.views.spinkit.style.MultiplePulse;
import com.moor.im.common.views.spinkit.style.MultiplePulseRing;
import com.moor.im.common.views.spinkit.style.Pulse;
import com.moor.im.common.views.spinkit.style.PulseRing;
import com.moor.im.common.views.spinkit.style.RotatingCircle;
import com.moor.im.common.views.spinkit.style.RotatingPlane;
import com.moor.im.common.views.spinkit.style.ThreeBounce;
import com.moor.im.common.views.spinkit.style.WanderingCubes;
import com.moor.im.common.views.spinkit.style.Wave;

/**
 * Created by ybq.
 */
public class SpriteFactory {

    public static Sprite create(Style style) {
        Sprite sprite = null;
        switch (style) {
            case ROTATING_PLANE:
                sprite = new RotatingPlane();
                break;
            case DOUBLE_BOUNCE:
                sprite = new DoubleBounce();
                break;
            case WAVE:
                sprite = new Wave();
                break;
            case WANDERING_CUBES:
                sprite = new WanderingCubes();
                break;
            case PULSE:
                sprite = new Pulse();
                break;
            case CHASING_DOTS:
                sprite = new ChasingDots();
                break;
            case THREE_BOUNCE:
                sprite = new ThreeBounce();
                break;
            case CIRCLE:
                sprite = new Circle();
                break;
            case CUBE_GRID:
                sprite = new CubeGrid();
                break;
            case FADING_CIRCLE:
                sprite = new FadingCircle();
                break;
            case FOLDING_CUBE:
                sprite = new FoldingCube();
                break;
            case ROTATING_CIRCLE:
                sprite = new RotatingCircle();
                break;
            case MULTIPLE_PULSE:
                sprite = new MultiplePulse();
                break;
            case PULSE_RING:
                sprite = new PulseRing();
                break;
            case MULTIPLE_PULSE_RING:
                sprite = new MultiplePulseRing();
                break;
            default:
                break;
        }
        return sprite;
    }
}
