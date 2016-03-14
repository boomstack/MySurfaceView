package com.ethan.mysurfaceview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private MainSurfaceView sv;
    private SeekBar seekBar;
    private TextView widthTv;

    public static int screenW;
    public static int screenH;
    public static int width;
    public static int height;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        screenW = wm.getDefaultDisplay().getWidth();
        screenH = wm.getDefaultDisplay().getHeight();
        setContentView(R.layout.main);
        sv = (MainSurfaceView) this.findViewById(R.id.sv);
        ViewTreeObserver vto = sv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {
                sv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                width = sv.getWidth();
                height = sv.getHeight();
                System.out.println("hola: width: " + width + " height: " + height);

            }
        });

        widthTv = (TextView) findViewById(R.id.paint_width);

        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setMax(100);
        seekBar.setProgress(5);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                widthTv.setText("粗细: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sv.setCurPaintWidth(seekBar.getProgress());
            }
        });

    }

    public void undo(View view) {
        sv.undo();
    }

    public void redo(View view) {
        sv.redo();
    }

    public void setModeDraw(View view) {
        Toast.makeText(this,"当前可以绘图",Toast.LENGTH_SHORT).show();
        sv.setDraw();
    }

    public void setModeEraser(View view) {
        Toast.makeText(this,"当前可以擦除",Toast.LENGTH_SHORT).show();
        sv.setEraser();
    }

    public void changeColor(View view) {
        new ColorPickerDialog(MainActivity.this, new ColorPickerDialog.OnColorChangedListener() {
            @Override
            public void colorChanged(int color) {
                sv.setPaintColor(color);
            }
        }, Color.RED).show();
    }

    @Override
    protected void onDestroy() {
        try {
            sv.getThreadInstance().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
