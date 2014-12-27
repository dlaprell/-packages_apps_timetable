package eu.laprell.timetable.utils;

import android.graphics.Color;

/**
 * Created by david on 07.11.14.
 */
public class ColorUtils {

    public static int setAlphaInColor(int color, int alpha) {
        return Color.argb(
                alpha,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        );
    }

    public static int convertMaterial500To700(int color) {
        color &= 0x00FFFFFF;

        switch (color) {
            case 0x00F44336: return 0xFFD32F2F;
            case 0x00E91E63: return 0xFFC2185B;
            case 0x009C27B0: return 0xFF7B1FA2;
            case 0x00673AB7: return 0xFF512DA8;
            case 0x003F51B5: return 0xFF303F9F;
            case 0x002196F3: return 0xFF1976D2;
            case 0x0003A9F4: return 0xFF0288D1;
            case 0x0000BCD4: return 0xFF0097A7;
            case 0x00009688: return 0xFF00796B;
            case 0x004CAF50: return 0xFF388E3C;
            case 0x008BC34A: return 0xFF689F38;
            case 0x00CDDC39: return 0xFFAFB42B;
            case 0x00FFEB3B: return 0xFFFBC02D;
            case 0x00FFC107: return 0xFFFFA000;
            case 0x00FF9800: return 0xFFF57C00;
            case 0x00FF5722: return 0xFFE64A19;
            case 0x00795548: return 0xFF5D4037;
            case 0x009E9E9E: return 0xFF616161;
            case 0x00607D8B: return 0xFF455A64;
        }

        return color | 0xFF000000;
    }
}

