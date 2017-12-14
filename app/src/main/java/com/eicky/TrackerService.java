package com.eicky;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.text.TextUtils;
import android.text.style.TtsSpan;
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

    public enum Type {
        OPEN(1), CLOSE(0);

        public int code;

        Type(int code) {
            this.code = code;
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mFloatWindowUtils == null)
            mFloatWindowUtils = new FloatWindowUtils(getApplicationContext());
        type = intent.getIntExtra(TYPE_KEY, -1);
        if (type != -1) {
            if (type == Type.OPEN.code) {
                mFloatWindowUtils.addFloatView();
            } else if (type == Type.CLOSE.code) {
                mFloatWindowUtils.removeFloatView();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageNameStr = event.getPackageName().toString();
            String classNameStr = event.getClassName().toString();
            Log.i(TAG, "==state==:" + classNameStr);

            if (classNameStr.startsWith(packageNameStr)) {
                classNameStr = classNameStr.substring(packageNameStr.length());
            }
            if (mFloatWindowUtils != null) {
                mFloatWindowUtils.updateDisplay(classNameStr, null);
            }

        } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED
                || event.getEventType() == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                AccessibilityNodeInfo source = event.getSource();
                StringBuilder sb = new StringBuilder();
                String idName = source.getViewIdResourceName();
                if (TextUtils.isEmpty(idName)) {
                    int childCount = source.getChildCount();
                    if (childCount > 0) {
                        int index = 0;
                        do {
                            AccessibilityNodeInfo child = source.getChild(index);
                            String name = child.getViewIdResourceName();
                            if (!TextUtils.isEmpty(name)) {
                                sb.append("child:");
                                sb.append(child.getClassName());
                                sb.append("\n");
                                sb.append("viewId:");
                                sb.append(name);
                                break;
                            }
                            index++;
                        } while (index < childCount);
                    }
                } else {
                    sb.append("source:");
                    sb.append(source.getClassName());
                    sb.append("\n");
                    sb.append("viewId:");
                    sb.append(idName);
                }
                if (TextUtils.isEmpty(sb.toString())) {
                    sb.append("source:");
                    sb.append(source.getClassName());
                }
                if (mFloatWindowUtils != null) {
                    mFloatWindowUtils.updateDisplay(null, sb.toString());
                }
                Log.i(TAG, "==click==:" + sb.toString());
            }
        }
    }

    @Override
    public void onInterrupt() {

    }
}
