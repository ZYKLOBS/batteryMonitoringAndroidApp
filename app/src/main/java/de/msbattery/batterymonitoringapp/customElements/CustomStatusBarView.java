/*
 * MIT License
 *
 * Copyright (c) 2024 RUB-SE-LAB-2024
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.msbattery.batterymonitoringapp.customElements;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import de.msbattery.batterymonitoringapp.R;

public class CustomStatusBarView extends View {

    protected Paint barPaint;
    protected Paint linePaint;
    protected Paint borderPaint;
    protected float linePosition;
    protected LinearGradient gradient;

    protected int color1;
    protected int color2;
    protected int color3;
    protected int lineColor;

    protected float low;
    protected float high;
    protected float cornerRadius;
    protected float borderWidth;
    protected float indicatorLineWidth;
    protected final float lowerBound = 0.05f;
    protected final float upperBound = 1f - lowerBound;

    public CustomStatusBarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Read custom attributes defined in res/values/attrs.xml
        TypedArray styleAttrs = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CustomStatusBarView,
                0, 0
        );
        try {
            color1 = styleAttrs.getColor(R.styleable.CustomStatusBarView_color1, Color.rgb(255, 0, 0));
            color2 = styleAttrs.getColor(R.styleable.CustomStatusBarView_color2, Color.rgb(0, 128, 0));
            color3 = styleAttrs.getColor(R.styleable.CustomStatusBarView_color3, Color.rgb(255, 0, 0));
            low = styleAttrs.getFloat(R.styleable.CustomStatusBarView_lower_barrier, 0);
            high = styleAttrs.getFloat(R.styleable.CustomStatusBarView_upper_barrier, 1);
            cornerRadius = styleAttrs.getDimension(R.styleable.CustomStatusBarView_cornerRadius, 16);
            borderWidth = styleAttrs.getDimension(R.styleable.CustomStatusBarView_borderWidth, 4);
            lineColor = styleAttrs.getColor(R.styleable.CustomStatusBarView_lineColor, Color.BLACK);
            indicatorLineWidth = styleAttrs.getDimension(R.styleable.CustomStatusBarView_indicatorLineWidth, 10);
        } finally {
            styleAttrs.recycle();
            init();
        }
    }

    private void init() {
        barPaint = new Paint();
        barPaint.setStyle(Paint.Style.FILL);

        linePaint = new Paint();
        linePaint.setColor(lineColor);
        linePaint.setStrokeWidth(indicatorLineWidth);
        linePaint.setAntiAlias(true);

        borderPaint = new Paint();
        borderPaint.setColor(lineColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setAntiAlias(true);

        linePosition = 0.5f; // Default position (50% of width)
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Create the gradient once, when the size is known
        int[] colors = {color1, color2, color3};
        float[] positions = {0f, 0.5f, 1f};
        gradient = new LinearGradient(0, 0, getWidth(), 0,
                colors, positions,
                Shader.TileMode.CLAMP);
        barPaint.setShader(gradient);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        float halfBorderWidth = borderWidth / 2;

        // Draw the rounded background bars
        RectF rect = new RectF(halfBorderWidth, halfBorderWidth, width - halfBorderWidth, height - halfBorderWidth);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, barPaint);

        // Draw the border
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint);

        // Draw the vertical line
        float lineX = linePosition * width;
        canvas.drawLine(lineX, halfBorderWidth, lineX, height - halfBorderWidth, linePaint);
    }

    public void setLinePosition(float value) {
        this.linePosition = determineLinePosition(value);
        invalidate(); // Redraw the view
    }

    protected float determineLinePosition(float value) {
        float result;
        if (value > this.high) result = 1;
        else if (value < this.low) result = 0;
        else result = (value - this.low) / (this.high - this.low);

        /*lines drawn too close to 1 or too close to 0 are hard to recognize on screen
        * -> set a bit further away from the 0 or 1*/
        if (result < lowerBound) result = lowerBound;
        if (result > upperBound) result = upperBound;

        return result;
    }
}
