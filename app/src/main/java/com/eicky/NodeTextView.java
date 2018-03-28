package com.eicky;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: Wei xiao min
 * Email: weixiaomin02@maoyan.com
 * Date: 2018/3/28
 */

public class NodeTextView extends View {
    private Paint mTextPaint;
    private final List<Node> mNodeList;
    private final int[] mLocation = new int[]{0, 0};
    private final int mTextSize;

    public NodeTextView(Context context) {
        this(context, null);
    }

    public NodeTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NodeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mNodeList = new ArrayList<>();
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.GREEN);
        mTextSize = dp2px(context, 11);
        mTextPaint.setTextSize(mTextSize);
    }

    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        getLocationOnScreen(mLocation);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Node node : mNodeList) {
            canvas.drawText(node.text, node.xOnScreen - mLocation[0], node.yOnScreen + mTextSize - mLocation[1], mTextPaint);
            canvas.drawCircle(node.xOnScreen - mLocation[0], node.yOnScreen - mLocation[1], 4, mTextPaint);
//            Log.i("onDraw", "onDraw: node = " + node + "\nmLocation = " + Arrays.toString(mLocation));
        }
    }

    public void setNodeList(List<Node> list) {
        mNodeList.clear();
        mNodeList.addAll(list);
    }

    public void addNode(Node node) {
        mNodeList.add(node);
    }

    public void notifyDataSetChange() {
        invalidate();
    }

    public void clear() {
        mNodeList.clear();
    }

    public static final class Node {
        public final String text;
        public final float xOnScreen;
        public final float yOnScreen;

        public Node(String text, float xOnScreen, float yOnScreen) {
            this.text = text;
            this.xOnScreen = xOnScreen;
            this.yOnScreen = yOnScreen;
        }

        @Override
        public String toString() {
            return "Node{" + "text='" + text + '\'' +
                    ", xOnScreen=" + xOnScreen +
                    ", yOnScreen=" + yOnScreen +
                    '}';
        }
    }
}
