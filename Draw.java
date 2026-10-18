package pl.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public final class Draw {
    public static void rrect(DrawContext c, int x, int y, int w, int h, int r, int col) {
        r = Math.min(r, Math.min(w, h) / 2);
        c.fill(x, y + r, x + w, y + h - r, col);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = r - (int) Math.round(Math.sqrt(r * r - dy * dy));
            c.fill(x + inset, y + i, x + w - inset, y + i + 1, col);
            c.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, col);
        }
    }

    public static void border(DrawContext c, int x, int y, int w, int h, int r, int th, int borderCol, int fillCol) {
        rrect(c, x - th, y - th, w + 2 * th, h + 2 * th, r + th, borderCol);
        rrect(c, x, y, w, h, r, fillCol);
    }

    public static int lerpColor(int c1, int c2, float t) {
        int a = (int) MathHelper.lerp(t, (c1 >>> 24) & 255, (c2 >>> 24) & 255);
        int r = (int) MathHelper.lerp(t, (c1 >> 16) & 255, (c2 >> 16) & 255);
        int g = (int) MathHelper.lerp(t, (c1 >> 8) & 255, (c2 >> 8) & 255);
        int b = (int) MathHelper.lerp(t, c1 & 255, c2 & 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int alpha(int col, int a) {
        return (Math.max(0, Math.min(255, a)) << 24) | (col & 0xFFFFFF);
    }

    private Draw() {}
}
