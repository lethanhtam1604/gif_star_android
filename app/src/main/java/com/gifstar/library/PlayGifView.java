package com.gifstar.library;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.gifstar.manager.Global;

import java.io.InputStream;

public class PlayGifView extends View {

    private static final int DEFAULT_MOVIEW_DURATION = 1000;
    private Movie mMovie;

    private long mMovieStart = 0;
    private int mCurrentAnimationTime = 0;

    private int delta = 100 + 270 + 60;
    private int heightFrame = 0;
    private float rate = 0;

    @SuppressLint("NewApi")
    public PlayGifView(Context context, AttributeSet attrs) {
        super(context, attrs);

        /**
         * Starting from HONEYCOMB have to turn off HardWare acceleration to draw
         * Movie on Canvas.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public void setImageResource(InputStream is) {
        mMovie = Movie.decodeStream(is);
        heightFrame = Global.screenHeight - delta;
        rate = (float) ((heightFrame * 1.0) / mMovie.height());
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mMovie != null) {
            if (heightFrame < mMovie.height()) {
                setMeasuredDimension((int) (mMovie.width() * rate), (int) (mMovie.height() * rate));
            } else
                setMeasuredDimension(mMovie.width(), mMovie.height());
        } else {
            setMeasuredDimension(getSuggestedMinimumWidth(), getSuggestedMinimumHeight());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMovie != null) {
            updateAnimtionTime();
            drawGif(canvas);
            invalidate();
        } else {
            drawGif(canvas);
        }
    }

    private void updateAnimtionTime() {
        long now = android.os.SystemClock.uptimeMillis();

        if (mMovieStart == 0) {
            mMovieStart = now;
        }
        int dur = mMovie.duration();
        if (dur == 0) {
            dur = DEFAULT_MOVIEW_DURATION;
        }
        mCurrentAnimationTime = (int) ((now - mMovieStart) % dur);
    }

    private void drawGif(Canvas canvas) {
        mMovie.setTime(mCurrentAnimationTime);
        if (heightFrame < mMovie.height()) {
            canvas.scale(rate, rate); // scale gif to its half size
        }
        mMovie.draw(canvas, 0, 0);
        canvas.restore();
    }

}