package com.ethan.mysurfaceview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 主画布
 * Created by wangkangfei on 16/3/14.
 */
public class MainSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder holder;
    private boolean isRunning = false;//绘制线程是否运行中
    private boolean isFreeze = false;//当前窗口是否focus
    List<DrawTouch> undoList;//撤销表
    List<DrawTouch> redoList;//重做表

    private DrawTouch curTouch = null;//当前触摸事件
    private int curPaintMode = 0;//当前画笔模式
    private int curPaintColor = Color.BLUE;//当前画笔颜色
    private int curPaintWidth = 10;//当前画笔粗细

    private int width;
    private int height;

    private Bitmap bgBitmap;
    private Bitmap cacheBitmap;
    private Canvas tmpCanvas;
    private Paint paint;
    private DrawThread dt;


    private final static int MODE_DRAW = 0;
    private final static int MODE_ERASER = 1;

    public MainSurfaceView(Context context) {
        super(context);
    }

    public MainSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        width = MainActivity.screenW;
        height = MainActivity.screenH;
        holder = getHolder();
        holder.addCallback(this);
        setFocusable(true);
        undoList = new ArrayList<>();
        redoList = new ArrayList<>();
        initBg();
    }

    /**
     * 初始化背景
     * */
    public void initBg() {
        Bitmap bm = BitmapFactory.decodeResource(getResources(),
                R.drawable.background);
        bgBitmap = FitTheScreenSizeImage(bm, width, height);
        cacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        tmpCanvas = new Canvas(cacheBitmap);
        tmpCanvas.setDrawFilter(new PaintFlagsDrawFilter(0,
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
    }

    /**
     * 图片适应屏幕
     */
    public static Bitmap FitTheScreenSizeImage(Bitmap m, int ScreenWidth,
                                               int ScreenHeight) {
        float width = (float) ScreenWidth / m.getWidth();
        float height = (float) ScreenHeight / m.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(width, height);
        return Bitmap.createBitmap(m, 0, 0, m.getWidth(), m.getHeight(),
                matrix, true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float mX = event.getX();
        float mY = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setCurTouch(mX, mY);
                break;
            case MotionEvent.ACTION_MOVE:
                if (curTouch != null) {
                    curTouch.move(mX, mY, tmpCanvas);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (curTouch != null) {
                    undoList.add(curTouch);
                    redoList.add(curTouch);
                    curTouch = null;
                }
                break;
            default:
                break;
        }
        return true;
    }

    public void setCurTouch(float mX, float mY) {
        switch (curPaintMode) {
            case MODE_DRAW:
                paint = new Paint();
                paint.setColor(curPaintColor);
                paint.setStrokeWidth(curPaintWidth);
                curTouch = new DrawFreeTouch(mX, mY, paint);
                break;
            case MODE_ERASER:
                paint = new Paint();
                paint.setStrokeWidth(curPaintWidth);
                curTouch = new DrawEraserTouch(mX, mY, paint);
                break;
            default:
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning = true;
        getThreadInstance().start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) {
            isFreeze = true;
        } else {
            isFreeze = false;
        }
        super.onWindowFocusChanged(hasWindowFocus);
    }

    public DrawThread getThreadInstance() {
        if (dt != null) {
            return dt;
        } else {
            return new DrawThread();
        }
    }

    class DrawThread extends Thread {
        @Override
        public void run() {

            while (isRunning) {
                Canvas mCanvas = null;
                try {
                    mCanvas = holder.lockCanvas();
                    synchronized (holder) {
                        drawView(mCanvas);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (mCanvas != null)
                        holder.unlockCanvasAndPost(mCanvas);
                    if (isFreeze) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
    }

    public void drawView(Canvas mCanvas) {
        mCanvas.drawBitmap(bgBitmap, 0, 0, null);
        mCanvas.drawBitmap(cacheBitmap, 0, 0, null);
    }


    /**
     * 撤销
     */
    public void undo() {

        cacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        tmpCanvas.setBitmap(cacheBitmap);
        if (undoList != null && undoList.size() > 0) {
            DrawTouch touch = undoList.get(undoList.size() - 1);
            redoList.add(touch);
            undoList.remove(touch);

            Iterator<DrawTouch> iter = undoList.iterator();
            while (iter.hasNext()) {
                DrawTouch a = iter.next();
                if (a instanceof DrawFreeTouch) {
                    DrawFreeTouch mp = (DrawFreeTouch) a;
                    tmpCanvas.drawPath(mp.path, mp.paint);
                }
                if (a instanceof DrawEraserTouch) {
                    DrawEraserTouch me = (DrawEraserTouch) a;
                    tmpCanvas.drawPath(me.path, me.paint);
                }
            }

        }
    }

    /**
     * 重做
     */
    public void redo() {

        if (redoList.size() < 1)
            return;
        cacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        tmpCanvas.setBitmap(cacheBitmap);
        if (redoList != null && redoList.size() > 0) {
            DrawTouch touch = redoList.get(redoList.size() - 1);
            undoList.add(touch);
            redoList.remove(touch);
            Iterator<DrawTouch> iter = undoList.iterator();
            while (iter.hasNext()) {
                DrawTouch a = iter.next();
                if (a instanceof DrawFreeTouch) {
                    DrawFreeTouch mp = (DrawFreeTouch) a;
                    tmpCanvas.drawPath(mp.path, mp.paint);
                }
                if (a instanceof DrawEraserTouch) {
                    DrawEraserTouch me = (DrawEraserTouch) a;
                    tmpCanvas.drawPath(me.path, me.paint);
                }
            }
        }
    }

    public void setDraw() {
        curPaintMode = MODE_DRAW;
    }

    public void setEraser() {
        curPaintMode = MODE_ERASER;
    }

    public void setPaintColor(int color) {
        curPaintColor = color;
    }

    public void setCurPaintWidth(int width) {
        curPaintWidth = width;
    }
}
