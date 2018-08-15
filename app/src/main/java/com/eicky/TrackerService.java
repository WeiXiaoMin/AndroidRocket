package com.eicky;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * @author Eicky
 * @Description:
 * @date: 2017/1/10 15:50
 * @version: V1.0
 */
public class TrackerService extends AccessibilityService {
    public static final String TYPE_KEY = "type_key";
    private FloatWindowUtils mFloatWindowUtils;
    private final String TAG = "Eicky";
    private int type;
    private static boolean mFilterClassName = false;

    public enum Type {
        OPEN(1), CLOSE(0);

        public int code;

        Type(int code) {
            this.code = code;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        type = intent.getIntExtra(TYPE_KEY, -1);
        if (type != -1) {
            if (type == Type.OPEN.code) {
                if (mFloatWindowUtils == null) {
                    mFloatWindowUtils = new FloatWindowUtils(getApplicationContext());
                    mFloatWindowUtils.setInteractor(new FloatWindowUtils.Interactor() {
                        @Override
                        public void onSimpleViewShow() {

                        }

                        @Override
                        public void onNodeInfoViewShow() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                showNodeInfo();
                            }
                        }
                    });
                }
                mFloatWindowUtils.showSimpleInfoView();
            } else if (type == Type.CLOSE.code) {
                if (mFloatWindowUtils != null) {
                    mFloatWindowUtils.removeFloatView();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            // 浮窗显示信息
            if (mFloatWindowUtils != null) {
                String classNameStr = event.getClassName().toString();
                if (isFilterClassName()) {
                    if (!TextUtils.isEmpty(classNameStr) && (classNameStr.contains("Activity")
                            || classNameStr.contains("Fragment") || classNameStr.contains("Dialog"))) {
                        mFloatWindowUtils.updateDisplay(classNameStr, null);
                    }
                } else {
                    mFloatWindowUtils.updateDisplay(classNameStr, null);
                }
//                Log.i(TAG, "onAccessibilityEvent: classNameStr = " + classNameStr);
            }

        } else if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED
                || eventType == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                AccessibilityNodeInfo source = event.getSource();
                StringBuilder sb = new StringBuilder();
                String idName = source != null ? source.getViewIdResourceName() : "";
                if (TextUtils.isEmpty(idName)) {
                    int childCount = source != null ? source.getChildCount() : 0;
                    if (childCount > 0) {
                        int index = 0;
                        do {
                            AccessibilityNodeInfo child = source.getChild(index);
                            if (child != null) {
                                String name = child.getViewIdResourceName();
                                if (!TextUtils.isEmpty(name)) {
                                    sb.append("child:");
                                    sb.append(child.getClassName());
                                    sb.append("\n");
                                    sb.append("viewId:");
                                    sb.append(name);
                                    break;
                                }
                            }
                            index++;
                        } while (index < childCount);
                    }
                } else {
                    sb.append("source:");
                    sb.append(source != null ? source.getClassName() : "");
                    sb.append("\n");
                    sb.append("viewId:");
                    sb.append(idName);
                }
                if (TextUtils.isEmpty(sb.toString())) {
                    sb.append("source:");
                    sb.append(source != null ? source.getClassName() : "");
                }
                if (mFloatWindowUtils != null) {
                    mFloatWindowUtils.updateDisplay(null, sb.toString());
                }
//                Log.i(TAG, "==click==:" + sb.toString());
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void showNodeInfo() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            // 获取当前窗口的控件id
            AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
            if (nodeInfo != null) {
                mFloatWindowUtils.clearNodeInfo();
                checkoutAllViewId(nodeInfo);
                mFloatWindowUtils.notifyNodeInfoSetChange();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void checkoutAllViewId(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo.getChildCount() == 0) {
            // 没有子View
            mFloatWindowUtils.addNodeInfo(nodeInfo);
        } else {
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                if (nodeInfo.getChild(i) != null) {
                    checkoutAllViewId(nodeInfo.getChild(i));
                }
            }
        }
    }

    public static void setFilterClassName(boolean filter) {
        mFilterClassName = filter;
    }

    public static boolean isFilterClassName() {
        return mFilterClassName;
    }
}
