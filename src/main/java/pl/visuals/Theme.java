package pl.visuals;

import net.minecraft.util.math.MathHelper;

public final class Theme {
    public static int hue(double deg) {
        return 0xFF000000 | MathHelper.hsvToRgb((float) (deg / 360.0), 0.65f, 1f);
    }

    public static int accent() {
        if (Mods.THEME_RAINBOW.v) {
            float h = (System.currentTimeMillis() % 6000) / 6000f;
            return 0xFF000000 | MathHelper.hsvToRgb(h, 0.6f, 1f);
        }
        return hue(Mods.THEME_HUE.v);
    }

    public static int pick(boolean useTheme, double hue) {
        return useTheme ? accent() : hue(hue);
    }

    private Theme() {}
}
