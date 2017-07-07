package net.ddns.peder.jaktlaget.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.support.v7.widget.AppCompatImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;

import net.ddns.peder.jaktlaget.Constants;

import java.util.Locale;

public class ScaleBar extends AppCompatImageView {

    // Dimension variables in dp units
    float mYOffset = 5;
    float mLineWidth = 3;
    int mTextSize = 24;
    float scalewidth = Constants.SCALE_WIDTH-mLineWidth;

    int mColor = Color.BLACK;

    private GoogleMap mMap;

    float mXdpi;
    float mDensity;

    public ScaleBar(Context context, GoogleMap map) {
        super(context);

        mMap = map;

        //mXdpi = context.getResources().getDisplayMetrics().xdpi;

        // Convert to resolution independent sizes
        mDensity = context.getResources().getDisplayMetrics().xdpi/160f;
        mXdpi = mDensity*scalewidth;
        mLineWidth = mLineWidth*mDensity;
        mTextSize = mTextSize*(int)mDensity;
        mYOffset = mYOffset*mDensity;

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override public void onCameraMove() {
                invalidate();
            }
        });
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();

        drawScaleBarPicture(canvas);

        canvas.restore();
    }

    private void drawScaleBarPicture(Canvas canvas) {
        // We want the scale bar to be as long as the closest round-number miles/kilometers
        // to 1-inch at the latitude at the current center of the screen.

        final Paint barPaint = new Paint();
        barPaint.setColor(mColor);
        barPaint.setAntiAlias(true);
        barPaint.setStrokeWidth(mLineWidth);

        final Paint textPaint = new Paint();
        textPaint.setColor(mColor);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(mTextSize);

        drawXMetric(canvas, textPaint, barPaint);
    }

    private void drawXMetric(Canvas canvas, Paint textPaint, Paint barPaint) {
        Projection projection = mMap.getProjection();

        LatLng p1 = projection.fromScreenLocation(new Point((int) ((getWidth() / 2) - (mXdpi / 2)), getHeight() / 2));
        LatLng p2 = projection.fromScreenLocation(new Point((int) ((getWidth() / 2) + (mXdpi / 2)), getHeight() / 2));

        Location locationP1 = new Location("ScaleBar location p1");
        Location locationP2 = new Location("ScaleBar location p2");

        locationP1.setLatitude(p1.latitude);
        locationP2.setLatitude(p2.latitude);
        locationP1.setLongitude(p1.longitude);
        locationP2.setLongitude(p2.longitude);

        float xMetersPerInch = locationP1.distanceTo(locationP2);

        String xMsg = scaleBarLengthText(xMetersPerInch);
        Rect xTextRect = new Rect();
        textPaint.getTextBounds(xMsg, 0, xMsg.length(), xTextRect);

        int textSpacing = (int) (xTextRect.height() / 5.0);

        canvas.drawRect(0, mYOffset, mXdpi, mYOffset + mLineWidth, barPaint);

        canvas.drawRect(mXdpi, mYOffset, mXdpi + mLineWidth, mYOffset +
                xTextRect.height() + mLineWidth + textSpacing, barPaint);
        canvas.drawRect(0, mYOffset, mLineWidth, mYOffset +
                xTextRect.height() + mLineWidth + textSpacing, barPaint);

        canvas.drawText(xMsg, (mXdpi / 2 - xTextRect.width() / 2),
                (mYOffset + xTextRect.height() + mLineWidth + textSpacing), textPaint);
    }

    private String scaleBarLengthText(float meters) {
        if (meters >= 1000) {
            return String.format(Locale.US, "%.1f", ((meters / 1000))) + "km";
        } else if (meters > 100) {
            return String.format(Locale.US, "%.1f", ((meters / 100.0) / 10.0)) + "km";
        } else {
            return String.format(Locale.US, "%.1f", meters) + "m";
        }
    }
}