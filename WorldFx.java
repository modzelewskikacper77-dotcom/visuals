package pl.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Efekty rysowane w swiecie 3D (linie). */
public final class WorldFx {
    record Circle(double x, double y, double z, long t) {}
    static final List<Circle> circles = new ArrayList<>();

    static void addCircle(double x, double y, double z) {
        circles.add(new Circle(x, y, z, System.currentTimeMillis()));
    }

    static void render(WorldRenderContext ctx) {
        MatrixStack ms = ctx.matrixStack();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (ms == null || ctx.consumers() == null || mc.player == null || mc.world == null) return;

        float td = ctx.tickCounter().getTickDelta(false);
        Vec3d cam = ctx.camera().getPos();
        VertexConsumer vc = ctx.consumers().getBuffer(RenderLayer.getLines());
        MatrixStack.Entry e = ms.peek();
        double t = (System.nanoTime() / 1.0e9) % 100000.0;

        // ----- Target Render -----
        if (Mods.TARGET_RENDER.enabled) {
            Entity tg = VisualsClient.renderTarget();
            if (tg != null) {
                double x = MathHelper.lerp(td, tg.prevX, tg.getX()) - cam.x;
                double y = MathHelper.lerp(td, tg.prevY, tg.getY()) - cam.y;
                double z = MathHelper.lerp(td, tg.prevZ, tg.getZ()) - cam.z;
                int col = Theme.pick(Mods.TR_THEME.v, Mods.TR_HUE.v);
                switch (Mods.TR_TYPE.get()) {
                    case "Ring" -> {
                        for (int k = 0; k < 4; k++) {
                            double r = 0.55 + k * 0.05 + Math.sin(t * 3) * 0.03;
                            ring(vc, e, x, y + 0.02 + k * 0.004, z, r, Draw.alpha(col, 230 - k * 50), t, true);
                        }
                        double phase = (t * 0.8) % 1.0;
                        int a = Math.max(0, (int) (220 * (1.0 - Math.abs(phase - 0.5) * 1.6)));
                        for (int k = 0; k < 2; k++)
                            ring(vc, e, x, y + phase * tg.getHeight(), z, 0.5 + k * 0.04, Draw.alpha(col, a), t * 2, true);
                    }
                    case "Halo" -> {
                        for (int k = 0; k < 3; k++)
                            ring(vc, e, x, y + tg.getHeight() + 0.25 + k * 0.01, z, 0.4, Draw.alpha(col, 240 - k * 60), t * 2, false);
                    }
                    default -> ghosts(vc, e, x, y, z, tg.getHeight(), col, Mods.TR_COUNT.i(), Mods.TR_SPEED.v, t);
                }
            }
        }

        // ----- Jump Circles -----
        long now = System.currentTimeMillis();
        for (Iterator<Circle> it = circles.iterator(); it.hasNext(); ) {
            Circle c = it.next();
            double p = (now - c.t()) / 900.0;
            if (p >= 1) { it.remove(); continue; }
            int col = Theme.hue(Mods.JC_HUE.v);
            int a = (int) (240 * (1 - p));
            double r = Mods.JC_RADIUS.v * (1 - Math.pow(1 - p, 2));
            for (int k = 0; k < 3; k++)
                ring(vc, e, c.x() - cam.x, c.y() - cam.y + 0.02, c.z() - cam.z, r * (1 - k * 0.07), Draw.alpha(col, a - k * 40), 0, false);
        }

        // ----- China Hat -----
        if (Mods.CHINA_HAT.enabled) {
            boolean first = mc.options.getPerspective().isFirstPerson();
            for (PlayerEntity p : mc.world.getPlayers()) {
                boolean self = p == mc.player;
                if (self && first) continue;
                if (!self && !Mods.HAT_OTHERS.v) continue;
                double x = MathHelper.lerp(td, p.prevX, p.getX()) - cam.x;
                double y = MathHelper.lerp(td, p.prevY, p.getY()) - cam.y + p.getHeight();
                double z = MathHelper.lerp(td, p.prevZ, p.getZ()) - cam.z;
                hat(vc, e, x, y, z, Theme.accent());
            }
        }
    }

    // ---------- ksztalty ----------
    static void line(VertexConsumer vc, MatrixStack.Entry e, double x1, double y1, double z1,
                     double x2, double y2, double z2, int argb) {
        float dx = (float) (x2 - x1), dy = (float) (y2 - y1), dz = (float) (z2 - z1);
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0e-6f) return;
        dx /= len; dy /= len; dz /= len;
        int a = argb >>> 24, r = (argb >> 16) & 255, g = (argb >> 8) & 255, b = argb & 255;
        vc.vertex(e, (float) x1, (float) y1, (float) z1).color(r, g, b, a).normal(e, dx, dy, dz);
        vc.vertex(e, (float) x2, (float) y2, (float) z2).color(r, g, b, a).normal(e, dx, dy, dz);
    }

    static void ring(VertexConsumer vc, MatrixStack.Entry e, double cx, double cy, double cz,
                     double r, int argb, double spin, boolean dashed) {
        int seg = 48;
        for (int i = 0; i < seg; i++) {
            if (dashed && ((i + (int) (spin * 4)) & 7) == 7) continue;
            double a1 = Math.PI * 2 * i / seg + spin, a2 = Math.PI * 2 * (i + 1) / seg + spin;
            line(vc, e, cx + Math.cos(a1) * r, cy, cz + Math.sin(a1) * r,
                    cx + Math.cos(a2) * r, cy, cz + Math.sin(a2) * r, argb);
        }
    }

    /** "Duchy": swiecace smugi krazace wokol gracza (jak na screenie z wirem). */
    static void ghosts(VertexConsumer vc, MatrixStack.Entry e, double bx, double by, double bz,
                       float h, int accent, int count, double speed, double t) {
        int len = 40;
        for (int g = 0; g < count; g++) {
            double off = g * (Math.PI * 2 / count);
            double px = 0, py = 0, pz = 0;
            for (int j = 0; j < len; j++) {
                double tt = t - j * 0.022;
                double ang = tt * speed + off;
                double yy = (0.5 + 0.5 * Math.sin(tt * 1.7 + off * 1.3)) * h;
                double rad = 0.62 + 0.06 * Math.sin(tt * 2 + off);
                double x = bx + Math.cos(ang) * rad, y = by + yy, z = bz + Math.sin(ang) * rad;
                if (j > 0) {
                    float f = j / (float) len;
                    int a = (int) (255 * Math.pow(1 - f, 1.4));
                    int col = Draw.alpha(Draw.lerpColor(0xFFFFFFFF, accent, f), a);
                    for (int k = 0; k < 3; k++)
                        line(vc, e, px, py + k * 0.02, pz, x, y + k * 0.02, z, Draw.alpha(col, a - k * 60));
                }
                px = x; py = y; pz = z;
            }
        }
    }

    static void hat(VertexConsumer vc, MatrixStack.Entry e, double x, double y, double z, int col) {
        int sides = Mods.HAT_SIDES.i();
        double r = Mods.HAT_SIZE.v;
        double by = y + 0.02, ty = y + 0.38;
        int c = Draw.alpha(col, 230);
        for (int i = 0; i < sides; i++) {
            double a1 = Math.PI * 2 * i / sides, a2 = Math.PI * 2 * (i + 1) / sides;
            double x1 = x + Math.cos(a1) * r, z1 = z + Math.sin(a1) * r;
            double x2 = x + Math.cos(a2) * r, z2 = z + Math.sin(a2) * r;
            line(vc, e, x1, by, z1, x2, by, z2, c);
            line(vc, e, x, ty, z, x1, by, z1, Draw.alpha(col, 150));
        }
    }

    private WorldFx() {}
}
