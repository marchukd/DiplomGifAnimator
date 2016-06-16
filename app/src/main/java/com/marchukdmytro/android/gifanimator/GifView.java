package com.marchukdmytro.android.gifanimator;

import java.io.FileInputStream;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

public class GifView extends View{

	private InputStream gifInputStream;
	private Movie gifMovie;
	private int movieWidth, movieHeight;
	private long movieDuration;
	private long movieStart;
	
	public GifView(Context context, String path) {
		super(context);
		init(context, path);
	}

	
	public GifView(Context context, AttributeSet attrs, String path) {
		super(context, attrs);
		init(context, path);
	}
	
	public GifView(Context context, AttributeSet attrs, int defStyleAttr, String path) {
		super(context, attrs, defStyleAttr);
		init(context, path);
	}
	
	private void init(Context context, String path) {
		setFocusable(true);
		gifMovie = Movie.decodeFile(path);
		movieWidth = gifMovie.width();
		movieHeight = gifMovie.height();
		movieDuration = gifMovie.duration();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(movieWidth, movieHeight);
	}
	
	public int getMovieWidth() {
		return movieWidth;
	}
	
	public int getMovieHeight() {
		return movieHeight;
	}
	
	public long getMovieDuration() {
		return movieDuration;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		long now = SystemClock.uptimeMillis();
		
		if(movieStart == 0) {
			movieStart = now;
		}
		
		if(gifMovie != null) {
			
			int dur = gifMovie.duration();
			if(dur == 0) {
				dur = 1000;
			}
			
			int relTime = (int)((now - movieStart) % dur);
			
			gifMovie.setTime(relTime);
			
			gifMovie.draw(canvas, 0, 0);
			invalidate();
		}
	}
}
