package com.eicky;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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
    private static final LayoutParams FULL_LAYOUT_PARAMS;
    private FloatView mFloatView;
    private NodeTextView mNodeTextView;
    private State mSate = State.NONE;
    private Interactor mInteractor;

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

        LayoutParams fullLP = new LayoutParams();
        fullLP.x = 0;
        fullLP.y = 0;
        fullLP.width = LayoutParams.MATCH_PARENT;
        fullLP.height = LayoutParams.MATCH_PARENT;
        fullLP.type = LayoutParams.TYPE_PHONE;
        fullLP.format = PixelFormat.RGBA_8888;
        layoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
        FULL_LAYOUT_PARAMS = fullLP;
    }

    private FloatWindowUtils() {

    }


    public FloatWindowUtils(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public State getState() {
        return mSate;
    }

    public void setInteractor(Interactor interactor) {
        this.mInteractor = interactor;
    }

    public void showSimpleInfoView() {
        if (mNodeTextView != null) {
            mNodeTextView.setVisibility(GONE);
        }
        if (mFloatView == null) {
            mFloatView = new FloatView(mContext);
            mWindowManager.addView(mFloatView, LAYOUTPARAMS);
            mFloatView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        showNodeInfoView();
                    }
                }
            });
        } else if (mFloatView.getVisibility() != VISIBLE) {
            mFloatView.setVisibility(VISIBLE);
        }
        mSate = State.SIMPLE;
        if (mInteractor != null) {
            mInteractor.onSimpleViewShow();
        }
    }

    public void removeFloatView() {
        if (mFloatView != null) {
            mWindowManager.removeView(mFloatView);
            mFloatView = null;
        }

        if (mNodeTextView != null) {
            mWindowManager.removeView(mNodeTextView);
            mNodeTextView = null;
        }

        mSate = State.NONE;
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void showNodeInfoView() {
        if (mFloatView != null && mFloatView.getVisibility() == VISIBLE) {
            mFloatView.setVisibility(GONE);
        }
        if (mNodeTextView == null) {
            mNodeTextView = new NodeTextView(mContext);
            mWindowManager.addView(mNodeTextView, FULL_LAYOUT_PARAMS);
            mNodeTextView.setBackgroundColor(Color.parseColor("#80000000"));
            mNodeTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSimpleInfoView();
                }
            });
        } else if (mNodeTextView.getVisibility() != VISIBLE) {
            mNodeTextView.setVisibility(VISIBLE);
        }
        mSate = State.NODE_INFO;
        if (mInteractor != null) {
            mInteractor.onNodeInfoViewShow();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void addNodeInfo(AccessibilityNodeInfo nodeInfo) {
        NodeTextView nodeInfoView = mNodeTextView;
        if (nodeInfoView == null) {
            return;
        }
        Rect rect = new Rect();
        nodeInfo.getBoundsInScreen(rect);
        String idName = nodeInfo.getViewIdResourceName();
        if (!TextUtils.isEmpty(idName) && !"null".equals(idName)) {
            String[] split = idName.split(":");
            NodeTextView.Node node = new NodeTextView.Node(split[split.length - 1], rect.left, rect.top);
            nodeInfoView.addNode(node);
        }
    }

    public void clearNodeInfo() {
        NodeTextView nodeInfoView = mNodeTextView;
        if (nodeInfoView == null) {
            return;
        }
        nodeInfoView.clear();
    }

    public void notifyNodeInfoSetChange(){
        NodeTextView nodeInfoView = mNodeTextView;
        if (nodeInfoView == null) {
            return;
        }
        nodeInfoView.notifyDataSetChange();
    }

    public enum State {
        SIMPLE, NODE_INFO, NONE
    }

    public interface Interactor {
        void onSimpleViewShow();

        void onNodeInfoViewShow();
    }
}
