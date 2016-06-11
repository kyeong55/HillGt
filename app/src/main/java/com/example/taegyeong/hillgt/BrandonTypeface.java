package com.example.taegyeong.hillgt;

import android.content.res.AssetManager;
import android.graphics.Typeface;

/**
 * Created by taegyeong on 16. 6. 11..
 */
public class BrandonTypeface {
    public static Typeface branBlack;
    public static Typeface branBold;
    public static Typeface branRegular;
    public static Typeface branLight;

    public static void setupTypeface(AssetManager mgr) {
        branBlack = Typeface.createFromAsset(mgr, "brandon_blk.otf");
        branBold = Typeface.createFromAsset(mgr, "brandon_bld.otf");
        branRegular = Typeface.createFromAsset(mgr, "brandon_med.otf");
        branLight = Typeface.createFromAsset(mgr, "brandon_reg.otf");
    }
}
