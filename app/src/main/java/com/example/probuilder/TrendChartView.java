package com.example.probuilder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrendChartView extends View {

    private Paint linePaint;
    private Paint fillPaint;
    private Path path;
    private Path fillPath;
    private List<Float> dataPoints;

    public TrendChartView(Context context) {
        super(context);
        init();
    }

    public TrendChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(8f);
        linePaint.setAntiAlias(true);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);

        path = new Path();
        fillPath = new Path();
        dataPoints = new ArrayList<>();
    }

    public void setData(List<Float> data, int color) {
        this.dataPoints = data;
        linePaint.setColor(color);
        
        // Gradient for fill
        fillPaint.setShader(new LinearGradient(0, 0, 0, getHeight(),
                Color.argb(50, Color.red(color), Color.green(color), Color.blue(color)),
                Color.TRANSPARENT, Shader.TileMode.CLAMP));
        
        invalidate();
    }

    // Generate synthetic data based on trend
    public void generateTrendData(String trend, double currentPrice, int color) {
        List<Float> points = new ArrayList<>();
        Random random = new Random();
        float basePrice = (float) currentPrice;
        
        // Start from 7 days ago
        float current = basePrice;
        if (trend.equalsIgnoreCase("increase")) {
            current = basePrice * 0.9f; // Started lower
        } else if (trend.equalsIgnoreCase("decrease")) {
            current = basePrice * 1.1f; // Started higher
        }

        points.add(current);

        for (int i = 0; i < 6; i++) {
            float change = (random.nextFloat() - 0.5f) * (basePrice * 0.05f); // 5% noise
            
            if (trend.equalsIgnoreCase("increase")) {
                change += (basePrice * 0.02f); // Upward bias
            } else if (trend.equalsIgnoreCase("decrease")) {
                change -= (basePrice * 0.02f); // Downward bias
            }
            
            current += change;
            points.add(current);
        }
        
        setData(points, color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (dataPoints == null || dataPoints.isEmpty()) return;

        float width = getWidth();
        float height = getHeight();
        float padding = 20f;
        
        float chartWidth = width - (padding * 2);
        float chartHeight = height - (padding * 2);

        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;

        for (float val : dataPoints) {
            if (val < min) min = val;
            if (val > max) max = val;
        }

        // Avoid divide by zero
        if (max == min) {
            max += 1;
            min -= 1;
        }

        path.reset();
        fillPath.reset();

        float xStep = chartWidth / (dataPoints.size() - 1);
        float range = max - min;

        // Start point
        float startX = padding;
        float startY = padding + chartHeight - ((dataPoints.get(0) - min) / range * chartHeight);
        
        path.moveTo(startX, startY);
        fillPath.moveTo(startX, height); // Bottom corner
        fillPath.lineTo(startX, startY);

        for (int i = 1; i < dataPoints.size(); i++) {
            float x = padding + (i * xStep);
            float y = padding + chartHeight - ((dataPoints.get(i) - min) / range * chartHeight);
            
            // Cubic bezier for smoothness
            float prevX = padding + ((i - 1) * xStep);
            float prevY = padding + chartHeight - ((dataPoints.get(i - 1) - min) / range * chartHeight);
            
            float midX = (prevX + x) / 2;
            path.cubicTo(midX, prevY, midX, y, x, y);
            fillPath.cubicTo(midX, prevY, midX, y, x, y);
        }

        fillPath.lineTo(padding + chartWidth, height); // Bottom right corner
        fillPath.close();

        // Draw fill first
        canvas.drawPath(fillPath, fillPaint);
        // Draw line on top
        canvas.drawPath(path, linePaint);
    }
}
