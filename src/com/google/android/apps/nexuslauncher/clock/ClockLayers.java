package com.google.android.apps.nexuslauncher.clock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Process;

import androidx.annotation.RequiresApi;

import com.android.launcher3.AdaptiveIconDrawableExt;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.icons.LauncherIcons;

import java.util.Calendar;
import java.util.TimeZone;

public class ClockLayers {
    private final Calendar mCurrentTime;
    public Bitmap iconBitmap;
    Drawable mDrawable;
    int mHourIndex;
    int mMinuteIndex;
    int mSecondIndex;
    int mDefaultHour;
    int mDefaultMinute;
    int mDefaultSecond;
    int offset;
    float scale;
    private LayerDrawable mLayerDrawable;

    ClockLayers() {
        mCurrentTime = Calendar.getInstance();
    }

    @Override
    public ClockLayers clone() {
        ClockLayers ret = null;
        if (mDrawable == null) {
            return null;
        }
        ClockLayers clone = new ClockLayers();
        clone.scale = scale;
        clone.mHourIndex = mHourIndex;
        clone.mMinuteIndex = mMinuteIndex;
        clone.mSecondIndex = mSecondIndex;
        clone.mDefaultHour = mDefaultHour;
        clone.mDefaultMinute = mDefaultMinute;
        clone.mDefaultSecond = mDefaultSecond;
        clone.iconBitmap = iconBitmap;
        clone.offset = offset;
        clone.mDrawable = mDrawable.getConstantState().newDrawable();
        clone.mLayerDrawable = clone.getLayerDrawable();
        if (clone.mLayerDrawable != null) {
            ret = clone;
        }
        return ret;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void setupBackground(Context context) {
        LauncherIcons launcherIcons = LauncherIcons.obtain(context);
        float[] tmp = new float[1];
        Drawable icon = getBackground().getConstantState().newDrawable();
        if (mDrawable instanceof AdaptiveIconDrawableExt) {
            icon = new AdaptiveIconDrawableExt(icon, null);
        }
        iconBitmap = launcherIcons.createBadgedIconBitmap(icon, Process.myUserHandle(), 26, false, tmp).icon;
        scale = tmp[0];
        offset = (int) Math.ceil((double) (0.0104167f * ((float) LauncherAppState.getInstance(context).getInvariantDeviceProfile().iconBitmapSize)));
        launcherIcons.recycle();
    }

    boolean updateAngles() {
        mCurrentTime.setTimeInMillis(System.currentTimeMillis());

        int hour = (mCurrentTime.get(Calendar.HOUR) + (12 - mDefaultHour)) % 12;
        int minute = (mCurrentTime.get(Calendar.MINUTE) + (60 - mDefaultMinute)) % 60;
        int second = (mCurrentTime.get(Calendar.SECOND) + (60 - mDefaultSecond)) % 60;
        int millis = second * 1000 + mCurrentTime.get(Calendar.MILLISECOND);

        boolean hasChanged = false;
        if (mHourIndex != -1 && mLayerDrawable.getDrawable(mHourIndex).setLevel(hour * 60 + mCurrentTime.get(Calendar.MINUTE))) {
            hasChanged = true;
        }
        if (mMinuteIndex != -1 && mLayerDrawable.getDrawable(mMinuteIndex).setLevel(minute + mCurrentTime.get(Calendar.HOUR) * 60)) {
            hasChanged = true;
        }
        if (mSecondIndex != -1 && mLayerDrawable.getDrawable(mSecondIndex).setLevel(millis / 100)) {
            hasChanged = true;
        }
        return hasChanged;
    }

    void setTimeZone(TimeZone timeZone) {
        mCurrentTime.setTimeZone(timeZone);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    LayerDrawable getLayerDrawable() {
        if (mDrawable instanceof LayerDrawable) {
            return (LayerDrawable) mDrawable;
        }
        if (mDrawable instanceof AdaptiveIconDrawableExt) {
            AdaptiveIconDrawableExt adaptiveIconDrawable = (AdaptiveIconDrawableExt) mDrawable;
            if (adaptiveIconDrawable.getForeground() instanceof LayerDrawable) {
                return (LayerDrawable) adaptiveIconDrawable.getForeground();
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    Drawable getBackground() {
        if (mDrawable instanceof AdaptiveIconDrawableExt) {
            return ((AdaptiveIconDrawableExt) mDrawable).getBackground();
        } else {
            return mDrawable;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void clipToMask(Canvas canvas) {
        if (mDrawable instanceof AdaptiveIconDrawableExt) {
            canvas.clipPath(((AdaptiveIconDrawableExt) mDrawable).getIconMask());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void drawForeground(Canvas canvas) {
        if (mDrawable instanceof AdaptiveIconDrawableExt) {
            ((AdaptiveIconDrawableExt) mDrawable).getForeground().draw(canvas);
        } else {
            mDrawable.draw(canvas);
        }
    }
}
