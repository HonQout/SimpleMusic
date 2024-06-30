package com.android.simplemusic.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.simplemusic.R;

public class DockBar extends RelativeLayout {
    private ImageView imageView;
    private TextView textView1;
    private TextView textView2;
    private ImageButton imageButton1;
    private ImageButton imageButton2;
    private ImageButton imageButton3;
    private RelativeLayout dockbar_root;

    public DockBar(Context context) {
        super(context);
        initView(context);
    }

    public DockBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public DockBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public DockBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dock_bar, this, true);
        imageView = (ImageView) view.findViewById(R.id.dock_bar_pic);
        textView1 = (TextView) view.findViewById(R.id.dock_bar_title);
        textView2 = (TextView) view.findViewById(R.id.dock_bar_artist);
        imageButton1 = (ImageButton) view.findViewById(R.id.dock_bar_prev);
        imageButton2 = (ImageButton) view.findViewById(R.id.dock_bar_play);
        imageButton3 = (ImageButton) view.findViewById(R.id.dock_bar_next);
        dockbar_root = (RelativeLayout) view.findViewById(R.id.dock_bar);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
    }

    public int getViewWidth(LayoutParams layoutParams) {
        return MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY);
    }

    public int getViewHeight(LayoutParams layoutParams) {
        return MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY);
    }

    public void setImageViewDrawable(Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }

    public void setImageViewBitmap(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    public void setImageViewOnClickListener(OnClickListener onClickListener) {
        imageView.setOnClickListener(onClickListener);
    }

    public void setTextView1Text(String text) {
        textView1.setText(text);
    }

    public void setTextView1OnClickListener(OnClickListener onClickListener) {
        textView1.setOnClickListener(onClickListener);
    }

    public void setTextView2Text(String text) {
        textView2.setText(text);
    }

    public void setTextView2OnClickListener(OnClickListener onClickListener) {
        textView2.setOnClickListener(onClickListener);
    }

    public void setImageButton1Drawable(Drawable drawable) {
        imageButton1.setImageDrawable(drawable);
    }

    public void setImageButton1OnClickListener(OnClickListener onClickListener) {
        imageButton1.setOnClickListener(onClickListener);
    }

    public void setImageButton2Drawable(Drawable drawable) {
        imageButton2.setImageDrawable(drawable);
    }

    public void setImageButton2OnClickListener(OnClickListener onClickListener) {
        imageButton2.setOnClickListener(onClickListener);
    }

    public void setImageButton3Drawable(Drawable drawable) {
        imageButton3.setImageDrawable(drawable);
    }

    public void setImageButton3OnClickListener(OnClickListener onClickListener) {
        imageButton3.setOnClickListener(onClickListener);
    }
}
