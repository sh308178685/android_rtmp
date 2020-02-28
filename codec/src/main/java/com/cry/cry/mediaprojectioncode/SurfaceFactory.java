package com.cry.cry.mediaprojectioncode;

//import android.support.annotation.Nullable;
import android.view.Surface;

import androidx.annotation.Nullable;

public interface SurfaceFactory {

    @Nullable
    Surface createSurface(int width, int height);

    void stop();
}
