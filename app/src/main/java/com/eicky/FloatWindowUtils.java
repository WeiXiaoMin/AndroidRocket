package com.eicky;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * @author Eicky
 * @Description:
 * @date: 2017/1/16 15:47
 * @version: V1.0
 */
public class FloatWindowUtils {
    private Context mContext;
    private WindowManager mWindowManager;
    private static final LayoutParams LAYOUTPARAMS;
    private FloatView mFloatView;

    static {
        LayoutParams layoutParams = new LayoutParams();
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.type = LayoutParams.TYPE_PHONE;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
        LAYOUTPARAMS = layoutParams;
    }

    private FloatWindowUtils() {

    }


    public FloatWindowUtils(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void addFloatView() {
        if (mFloatView == null) {
            mFloatView = new FloatView(mContext);
            mWindowManager.addView(mFloatView, LAYOUTPARAMS);
        }
    }

    public void removeFloatView() {
        if (mFloatView != null) {
            mWindowManager.removeView(mFloatView);
            mFloatView = null;
        }
    }

    public void updateDisplay(@Nullable String text1, @Nullable String text2) {
        if (mFloatView != null)
            if (text1 != null && text2 != null) {
                mFloatView.updateDisplay(text1, text2);
            } else if (text1 != null) {
                mFloatView.updateText1(text1);
            } else if (text2 != null) {
                mFloatView.updateText2(text2);
            }
    }
}
