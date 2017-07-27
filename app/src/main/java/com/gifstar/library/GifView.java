package com.gifstar.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.os.SystemClock;
import android.view.View;

import java.io.InputStream;

public class GifView extends View {

    private Movie mMovie;
    private InputStream mStream;
    private long mMovieStart;

    public GifView(Context context, InputStream stream) {
        super(context);

        mStream = stream;
        mMovie = Movie.decodeStream(mStream);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        super.onDraw(canvas);
        final long now = SystemClock.uptimeMillis();
        if (mMovieStart == 0) {
            mMovieStart = now;
        }
        final int relTime = (int) ((now - mMovieStart) % mMovie.duration());
        mMovie.setTime(relTime);
        mMovie.draw(canvas, 10, 10);
        this.invalidate();
    }
}
