package com.ethan.mysurfaceview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

/**
 * 擦除
 * Created by wangkangfei on 16/3/14.
 */
public class DrawEraserTouch extends DrawTouch {
    public Path path;
    public Paint paint;

    DrawEraserTouch(float x, float y, Paint p) {
        path = new Path();
        paint = p;
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        path.moveTo(x, y);
        tempX = x;
        tempY = y;
    }

    float tempX;
    float tempY;

    public void move(float mx, float my, Canvas mCanvas) {
        float dx = Math.abs(mx - tempX);
        float dy = Math.abs(tempY - my);
        if (dx >= 4 || dy >= 4) {
            path.quadTo(tempX, tempY, (mx + tempX) / 2, (my + tempY) / 2);
            tempX = mx;
            tempY = my;
        }
        mCanvas.drawPath(path, paint);
    }
}
