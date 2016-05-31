/*
 * Copyright (C) 2016 CaMnter yuanyu.camnter@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moor.im.common.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.moor.im.R;

public class GlideUtils {

    private static final String TAG = "GlideUtils";


    public static void displayNative(final ImageView view, @DrawableRes int resId) {
        // 不能崩
        if (view == null) {
            Log.e(TAG, "GlideUtils -> display -> imageView is null");
            return;
        }
        Context context = view.getContext();
        // View你还活着吗？
        if (context instanceof Activity) {
            if (((Activity) context).isFinishing()) {
                return;
            }
        }

        try {
            Glide.with(context)
                 .load(resId)
                 .asBitmap()
                 .into(view)
                 .getSize(new SizeReadyCallback() {
                     @Override public void onSizeReady(int width, int height) {
                         view.setVisibility(View.VISIBLE);
                     }
                 });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void displayNet(final ImageView view, String imgUrl) {
        // 不能崩
        if (view == null) {
            Log.e(TAG, "GlideUtils -> display -> imageView is null");
            return;
        }
        Context context = view.getContext();
        // View你还活着吗？
        if (context instanceof Activity) {
            if (((Activity) context).isFinishing()) {
                return;
            }
        }

        try {
            Glide.with(context)
                    .load(imgUrl)
                    .asBitmap()
                    .placeholder(R.drawable.img_default_head)
                    .into(view)
                    .getSize(new SizeReadyCallback() {
                        @Override public void onSizeReady(int width, int height) {
                            view.setVisibility(View.VISIBLE);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void displayNetForErp(final ImageView view, String imgUrl) {
        // 不能崩
        if (view == null) {
            Log.e(TAG, "GlideUtils -> display -> imageView is null");
            return;
        }
        Context context = view.getContext();
        // View你还活着吗？
        if (context instanceof Activity) {
            if (((Activity) context).isFinishing()) {
                return;
            }
        }

        try {
            Glide.with(context)
                    .load(imgUrl)
                    .asBitmap()
                    .placeholder(R.drawable.erp_user_default)
                    .into(view)
                    .getSize(new SizeReadyCallback() {
                        @Override public void onSizeReady(int width, int height) {
                            view.setVisibility(View.VISIBLE);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
