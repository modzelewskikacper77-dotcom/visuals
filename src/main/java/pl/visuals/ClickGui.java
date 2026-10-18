package pl.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

/** Menu klienta: kategorie po lewej, karty modulow z ustawieniami. */
public class ClickGui extends Screen {
    static final int PANEL = 0xF0101016, CARD = 0xFF17171F, HEAD = 0xFF1E1E28,
            TXT = 0xFFE8E8F0, DIM = 0xFF8A8A99, LINE = 0xFF2A2A36;

    record Hit(int x, int y, int w, int h, boolean content, Runnable left, Runnable right) {
        boolean in(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }
    record Slider(Mod.Num n, int x, int y, int w, int h) {}

    Mod.Category cat = Mod.Category.VISUALS;
    double scroll = 0;
    final List<Hit> hits = new ArrayList<>();
    final List<Slider> sliders = new ArrayList<>();
    Slider dragging;
    int cx, cy, cw, ch;

    public ClickGui() { super(Text.literal("Visuals")); }

    @Override public boolean shouldPause() { return false; }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        hits.clear();
        sliders.clear();
        MinecraftClient mc = MinecraftClient.getInstance();
        int acc = Theme.accent();

        ctx.fill(0, 0, width, height, 0x99000000);

        int pw = Math.min(width - 30, 560), ph = Math.min(height - 30, 340);
        int px = (width - pw) / 2, py = (height - ph) / 2;
        Draw.border(ctx, px, py, pw, ph, 6, 1, 0xFF2A2A36, PANEL);

        // ----- gorny pasek -----
        int ty = py + 6;
        String title = cat.title;
        int tw = textRenderer.getWidth(title) + 20;
        Draw.rrect(ctx, px + 44, ty, tw, 22, 5, HEAD);
        ctx.drawText(textRenderer, title, px + 54, ty + 7, TXT, true);

        String user = mc.getSession().getUsername();
        int uw = Math.max(textRenderer.getWidth(user), textRenderer.getWidth("USER")) + 34;
        int ux = px + pw - uw - 8;
        Draw.border(ctx, ux, ty, uw, 22, 5, 1, LINE, HEAD);
        if (mc.player != null) PlayerSkinDrawer.draw(ctx, mc.player.getSkinTextures(), ux + 4, ty + 3, 16);
        ctx.drawText(textRenderer, user, ux + 25, ty + 3, TXT, false);
        ctx.drawText(textRenderer, "USER", ux + 25, ty + 12, acc, false);

        ctx.fill(px + 40, py + 34, px + pw - 8, py + 35, LINE);
        ctx.fill(px + 40, py + 34, px + 41, py + ph - 6, LINE);

        // ----- sidebar -----
        Mod.Category[] cats = Mod.Category.values();
        for (int i = 0; i < cats.length; i++) {
            Mod.Category c = cats[i];
            int bx = px + 8, by = py + 40 + i * 34;
            boolean sel = c == cat;
            Draw.border(ctx, bx, by, 26, 26, 5, 1, sel ? acc : LINE, HEAD);
            ctx.drawCenteredTextWithShadow(textRenderer, c.icon, bx + 13, by + 9, sel ? acc : DIM);
            hits.add(new Hit(bx, by, 26, 26, false, () -> { cat = c; scroll = 0; }, null));
        }

        // ----- tresc -----
        cx = px + 48; cy = py + 40; cw = pw - 48 - 10; ch = ph - 40 - 8;
        int colW = (cw - 8) / 2;
        ctx.enableScissor(cx - 2, cy, cx + cw + 2, cy + ch);

        int y0 = cy - (int) scroll;
        int[] colY = {y0, y0};
        for (Mod m : Mods.ALL) {
            if (m.cat != cat) continue;
            int col = colY[0] <= colY[1] ? 0 : 1;
            int x = cx + col * (colW + 8);
            int y = colY[col];
            int rows = 1 + (m.expanded ? m.settings.size() : 0);
            int h = 22 + rows * 22 + 2;

            Draw.border(ctx, x, y, colW, h, 5, 1, m.enabled ? acc : LINE, CARD);
            Draw.rrect(ctx, x, y, colW, 22, 5, HEAD);
            ctx.fill(x, y + 12, x + colW, y + 22, HEAD);
            ctx.drawText(textRenderer, m.expanded ? "v" : ">", x + 8, y + 7, DIM, false);
            ctx.drawText(textRenderer, m.name, x + 20, y + 7, TXT, true);
            Draw.border(ctx, x + colW - 30, y + 5, 24, 12, 3, 1, LINE, HEAD);
            ctx.drawText(textRenderer, "N/A", x + colW - 27, y + 7, DIM, false);
            hits.add(new Hit(x, y, colW, 22, true, () -> m.expanded = !m.expanded, null));

            int ry = y + 22;
            ctx.drawText(textRenderer, "Enable", x + 8, ry + 7, TXT, false);
            checkbox(ctx, x + colW - 18, ry + 6, m.enabled, acc);
            hits.add(new Hit(x, ry, colW, 22, true, m::toggle, null));
            ry += 22;

            if (m.expanded) {
                for (Mod.Setting s : m.settings) {
                    ctx.drawText(textRenderer, s.name, x + 8, ry + 7, DIM, false);
                    if (s instanceof Mod.Bool b) {
                        checkbox(ctx, x + colW - 18, ry + 6, b.v, acc);
                        hits.add(new Hit(x, ry, colW, 22, true, () -> b.v = !b.v, null));
                    } else if (s instanceof Mod.Mode md) {
                        int bw = 70, bx = x + colW - bw - 8;
                        Draw.border(ctx, bx, ry + 3, bw, 16, 3, 1, LINE, HEAD);
                        ctx.drawCenteredTextWithShadow(textRenderer, md.get(), bx + bw / 2, ry + 7, TXT);
                        hits.add(new Hit(bx, ry + 3, bw, 16, true, () -> md.cycle(1), () -> md.cycle(-1)));
                    } else if (s instanceof Mod.Num n) {
                        int tx = x + colW - 76, tw2 = 68;
                        String val = n.text();
                        ctx.drawText(textRenderer, val, tx - 4 - textRenderer.getWidth(val), ry + 7, TXT, false);
                        ctx.fill(tx, ry + 9, tx + tw2, ry + 13, 0xFF2A2A36);
                        float f = (float) ((n.v - n.min) / (n.max - n.min));
                        ctx.fill(tx, ry + 9, tx + (int) (tw2 * f), ry + 13, acc);
                        Draw.rrect(ctx, tx + (int) (tw2 * f) - 2, ry + 6, 5, 10, 2, 0xFFFFFFFF);
                        sliders.add(new Slider(n, tx, ry + 4, tw2, 14));
                    }
                    ry += 22;
                }
            }
            colY[col] += h + 8;
        }

        int contentH = Math.max(colY[0], colY[1]) - y0;
        double maxScroll = Math.max(0, contentH - ch);
        scroll = MathHelper.clamp(scroll, 0, maxScroll);
        ctx.disableScissor();

        ctx.drawText(textRenderer, "Open Menu: " + VisualsClient.openKey.getBoundKeyLocalizedText().getString()
                + "  |  LPM/PPM zmienia tryb", px + 48, py + ph - 12, 0xFF55556A, false);
    }

    void checkbox(DrawContext ctx, int x, int y, boolean on, int acc) {
        Draw.border(ctx, x, y, 10, 10, 2, 1, on ? acc : 0xFF4A4A5A, on ? acc : HEAD);
        if (on) ctx.fill(x + 3, y + 3, x + 7, y + 7, 0xFF08210F);
    }

    void setSlider(Slider s, double mx) {
        double f = MathHelper.clamp((mx - s.x()) / s.w(), 0, 1);
        s.n().set(s.n().min + f * (s.n().max - s.n().min));
    }

    boolean inContent(double my) { return my >= cy && my <= cy + ch; }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && inContent(my)) {
            for (Slider s : sliders) {
                if (mx >= s.x() - 3 && mx <= s.x() + s.w() + 3 && my >= s.y() && my <= s.y() + s.h()) {
                    dragging = s; setSlider(s, mx); return true;
                }
            }
        }
        for (Hit h : hits) {
            if (!h.in(mx, my)) continue;
            if (h.content() && !inContent(my)) continue;
            if (button == 1 && h.right() != null) { h.right().run(); return true; }
            if (button == 0 && h.left() != null) { h.left().run(); return true; }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null) { setSlider(dragging, mx); return true; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        scroll -= v * 20;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (VisualsClient.openKey.matchesKey(keyCode, scanCode)) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() { Config.save(); }
}
