package com.ethan.mysurfaceview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * 自由手绘
 * Created by wangkangfei on 16/3/14.
 */
public class DrawFreeTouch extends DrawTouch {
    public Path path;
    public Paint paint;

    public DrawFreeTouch(float x, float y, Paint p) {
        path = new Path();
        paint = p;
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        path.moveTo(x, y);
        tempX = x;
        tempY = y;
    }

    float tempX;
    float tempY;

    public void move(float mx, float my, Canvas mCanvas) {
        float dx = Math.abs(mx - tempX);
        float dy = Math.abs(my - tempY);
        if (dx >= 4 || dy >= 4) {
            path.quadTo(tempX, tempY, (mx + tempX) / 2, (my + tempY) / 2);
            tempX = mx;
            tempY = my;
        }
        mCanvas.drawPath(path, paint);
    }
}
