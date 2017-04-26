package com.example.csanchez.ift2905_wordonthestreet;

import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;
import android.util.TypedValue;
import android.os.Parcelable;
import android.os.Parcel;

public class CustomTypefaceSpan extends TypefaceSpan {
    private final Typeface newType;
    private final int newSize;
    private int mData;

    public CustomTypefaceSpan(String family, Typeface type, int size) {
        super(family);
        newType = type;
        newSize = size;
    }
    private CustomTypefaceSpan(Parcel in) {
        this("",null,0);
        mData = in.readInt();
    }
    public static final Parcelable.Creator<CustomTypefaceSpan> CREATOR
            = new Parcelable.Creator<CustomTypefaceSpan>() {
        public CustomTypefaceSpan createFromParcel(Parcel in) {
            return new CustomTypefaceSpan(in);
        }

        public CustomTypefaceSpan[] newArray(int size) {
            return new CustomTypefaceSpan[size];
        }
    };
    @Override
    public void updateDrawState(TextPaint ds) {
        applyCustomTypeFace(ds, newType, newSize);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        applyCustomTypeFace(paint, newType, newSize);
    }

    private static void applyCustomTypeFace(Paint paint, Typeface tf, int size) {

        try {
            int oldStyle;
            Typeface old = paint.getTypeface();
            if (old == null) {
                oldStyle = 0;
            } else {
                oldStyle = old.getStyle();
            }

            int fake = oldStyle & ~tf.getStyle();
            if ((fake & Typeface.BOLD) != 0) {
                paint.setFakeBoldText(true);
            }

            if ((fake & Typeface.ITALIC) != 0) {
                paint.setTextSkewX(-0.25f);
            }

            paint.setTextSize(getPixelsFromDip(size));
            paint.setTypeface(tf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static float getPixelsFromDip(float dip) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dip,
                Resources.getSystem().getDisplayMetrics()
        );
    }
}