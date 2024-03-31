package com.android.simplemusic.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.android.simplemusic.utils.ColorUtils;

public class BarGraphView extends View {
    public static final String TAG = BarGraphView.class.getSimpleName();
    private int numBar;
    private int space;
    private long delay;
    private byte[] data;
    private Paint paint;

    public BarGraphView(Context context) {
        super(context);
        init(context);
    }

    public BarGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BarGraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public BarGraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        numBar = 20;
        space = 10;
        delay = 200;
        paint = new Paint();
        if (context != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            paint.setColor(ColorUtils.analyzeColor(context, sharedPreferences.getString("theme_color", "red")));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i(TAG, "onDraw");
        super.onDraw(canvas);
        int oneWidth = (getWidth() - space * (numBar - 1)) / numBar;
        if (data != null) {
            for (int i = 0, j = 0; i < numBar && j < data.length; i++, j += data.length / numBar) {
                double oneHeight = ((double) data[j] + 128) / 256 * getHeight();
                int oneHeight1 = (int) oneHeight;
                canvas.drawRect(oneWidth * i + space * i, getHeight() - oneHeight1, oneWidth * (i + 1) + space * i, getHeight(), paint);
            }
            /*int[] allHeight = calcBarHeight(data, numBar);
            for (int i = 0; i < numBar && i < allHeight.length; i++) {
                int oneHeight1 = allHeight[i];
                canvas.drawRect(oneWidth * i + space * i, getHeight() - oneHeight1, oneWidth * (i + 1) + space * i, getHeight(), paint);
            }*/
        }
        postInvalidateDelayed(delay);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int widthMeasureSpec) {
        int width;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            width = size;
        } else {
            width = 300;
            if (specMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, size);
            }
        }
        Log.i(TAG, "Width:" + width);
        return width;
    }

    private int measureHeight(int heightMeasureSpec) {
        int height;
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            height = size;
        } else {
            height = 300;
            if (specMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, size);
            }
        }
        Log.i(TAG, "Height:" + height);
        return height;
    }

    public boolean setNumBar(int numBar) {
        if (numBar > 0) {
            this.numBar = numBar;
            return true;
        }
        return false;
    }

    public boolean setSpace(int space) {
        if (space > 0) {
            this.space = space;
            return true;
        }
        return false;
    }

    public boolean setDelay(long delay) {
        if (delay > (long) 0) {
            this.delay = delay;
            return true;
        }
        return false;
    }

    public void onReceiveByte(byte[] bytes) {
        data = bytes;
    }

    public int getNumBar() {
        return numBar;
    }

    public int getSpace() {
        return space;
    }

    public long getDelay() {
        return delay;
    }

    protected int[] calcBarHeight(byte[] data, int numBar) {
        int average = data.length / numBar;
        int[] result = new int[numBar];
        for (int i = 0; i < numBar; i++) {
            double d = 0;
            for (int j = 0; j < average; j++) {
                d += (double) data[i * average + j];
            }
            d /= average;
            d = (d + 128) / 256 * getHeight();
            result[i] = (int) d;
        }
        return result;
    }
}
