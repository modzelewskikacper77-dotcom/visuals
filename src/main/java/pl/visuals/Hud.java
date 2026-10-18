package pl.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Hud {
    static final int BG = 0xE0121218;
    static final int DIM = 0xFFB0B8C0;
    static final Item[] OVERLAY = {Items.TOTEM_OF_UNDYING, Items.ENDER_PEARL, Items.GOLDEN_APPLE, Items.END_CRYSTAL};
    static final Item[] WATCHED = {Items.ENDER_PEARL, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE,
            Items.CHORUS_FRUIT, Items.SHIELD, Items.WIND_CHARGE, Items.TRIDENT};

    static float shownHp = -1;
    static AbstractClientPlayerEntity shownFor;

    static void render(DrawContext ctx, RenderTickCounter tc) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden || mc.currentScreen instanceof ClickGui) return;
        float td = tc.getTickDelta(false);
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();

        if (Mods.WATERMARK.enabled) watermark(ctx, mc);
        if (Mods.MODULES_LIST.enabled) modulesList(ctx, mc, sw);
        if (Mods.PLAYER_INFO.enabled) playerInfo(ctx, mc, sw, sh);
        if (Mods.ITEM_OVERLAY.enabled) itemOverlay(ctx, mc, sh);
        if (Mods.CONSUMABLE.enabled) consumables(ctx, mc, sh, td);
        if (Mods.CROSSHAIR.enabled) crosshair(ctx, sw, sh);
        if (Mods.TARGET_HUD.enabled && VisualsClient.targetActive()) targetHud(ctx, mc, sw, sh);
    }

    // ---------- Target HUD ----------
    static void targetHud(DrawContext ctx, MinecraftClient mc, int sw, int sh) {
        AbstractClientPlayerEntity t = VisualsClient.target;
        int acc = Theme.accent();
        int w = 150, h = 52;
        int x = sw / 2 + 25, y = sh / 2 - h / 2;

        float maxHp = Math.max(1f, t.getMaxHealth());
        float hp = t.getHealth() + t.getAbsorptionAmount();
        if (shownFor != t || shownHp < 0) { shownHp = hp; shownFor = t; }
        shownHp += (hp - shownHp) * 0.15f;
        float frac = MathHelper.clamp(shownHp / maxHp, 0f, 1f);

        Draw.border(ctx, x, y, w, h, 5, 1, Draw.alpha(acc, 140), BG);

        ctx.fill(x + 5, y + 5, x + 39, y + 39, acc);
        PlayerSkinDrawer.draw(ctx, t.getSkinTextures(), x + 6, y + 6, 32);

        ctx.drawText(mc.textRenderer, t.getName().getString(), x + 44, y + 6, 0xFFFFFFFF, true);
        ctx.drawText(mc.textRenderer, String.format("HP %.1f / %.1f", hp, maxHp), x + 44, y + 17, DIM, false);

        String dist = String.format("%.1fm", mc.player.distanceTo(t));
        int dw = mc.textRenderer.getWidth(dist) + 8;
        Draw.rrect(ctx, x + w - dw - 5, y + 5, dw, 12, 4, 0xFF3FD060);
        ctx.drawText(mc.textRenderer, dist, x + w - dw - 1, y + 7, 0xFF08210F, false);

        if (Mods.TH_ARMOR.v) {
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                    EquipmentSlot.FEET, EquipmentSlot.MAINHAND};
            for (int i = 0; i < slots.length; i++) {
                ItemStack s = t.getEquippedStack(slots[i]);
                int ix = x + 44 + i * 12, iy = y + 27;
                if (s.isEmpty()) { ctx.fill(ix, iy, ix + 10, iy + 10, 0x33FFFFFF); continue; }
                ctx.getMatrices().push();
                ctx.getMatrices().translate(ix, iy, 0);
                ctx.getMatrices().scale(0.625f, 0.625f, 1f);
                ctx.drawItem(s, 0, 0);
                ctx.getMatrices().pop();
            }
        }

        int bx = x + 44, by = y + 41, bw = w - 50;
        ctx.fill(bx, by, bx + bw, by + 5, 0xFF2A2A33);
        ctx.fill(bx, by, bx + (int) (bw * frac), by + 5, Draw.lerpColor(0xFFE03C3C, 0xFF5CE85C, frac));
    }

    // ---------- Consumable (cooldowny) ----------
    static void consumables(DrawContext ctx, MinecraftClient mc, int sh, float td) {
        int rows = 0, x = 6, y0 = sh / 2 - 40, acc = Theme.accent();
        for (Item item : WATCHED) {
            ItemStack st = new ItemStack(item);
            float p = mc.player.getItemCooldownManager().getCooldownProgress(st, td);
            if (p <= 0f) continue;
            int y = y0 + rows * 24;
            Draw.border(ctx, x, y, 120, 20, 4, 1, Draw.alpha(acc, 120), BG);
            ctx.drawItem(st, x + 2, y + 2);
            ctx.drawText(mc.textRenderer, st.getName().getString(), x + 22, y + 3, 0xFFFFFFFF, false);
            ctx.fill(x + 22, y + 14, x + 114, y + 17, 0xFF2A2A33);
            ctx.fill(x + 22, y + 14, x + 22 + (int) (92 * (1f - p)), y + 17, acc);
            rows++;
        }
    }

    // ---------- Item Overlay ----------
    static void itemOverlay(DrawContext ctx, MinecraftClient mc, int sh) {
        int acc = Theme.accent();
        int x = 6, y = sh - 26 - OVERLAY.length * 24;
        for (int i = 0; i < OVERLAY.length; i++) {
            int n = count(mc.player, OVERLAY[i]);
            int yy = y + i * 24;
            Draw.border(ctx, x, yy, 46, 20, 4, 1, n > 0 ? Draw.alpha(acc, 140) : 0x44FFFFFF, BG);
            ctx.drawItem(new ItemStack(OVERLAY[i]), x + 2, yy + 2);
            ctx.drawText(mc.textRenderer, "x" + n, x + 22, yy + 6, n > 0 ? 0xFFFFFFFF : 0xFF777777, false);
        }
    }

    static int count(PlayerEntity p, Item it) {
        int n = 0;
        PlayerInventory inv = p.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s.isOf(it)) n += s.getCount();
        }
        return n;
    }

    // ---------- Player Info ----------
    static void playerInfo(DrawContext ctx, MinecraftClient mc, int sw, int sh) {
        PlayerListEntry pe = mc.getNetworkHandler() == null ? null
                : mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        int ping = pe == null ? 0 : pe.getLatency();
        String[][] rows = {
                {"Nick", mc.player.getName().getString()},
                {"FPS", String.valueOf(mc.getCurrentFps())},
                {"Ping", ping + " ms"},
                {"XYZ", String.format("%.0f %.0f %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ())},
                {"Speed", String.format("%.1f b/s", VisualsClient.speed)}
        };
        int acc = Theme.accent();
        int w = 118, h = 8 + rows.length * 11 + 2;
        int x = sw - w - 6, y = sh / 2 - h / 2;
        Draw.border(ctx, x, y, w, h, 5, 1, Draw.alpha(acc, 120), BG);
        for (int i = 0; i < rows.length; i++) {
            int ry = y + 6 + i * 11;
            ctx.drawText(mc.textRenderer, rows[i][0] + ":", x + 6, ry, acc, false);
            ctx.drawText(mc.textRenderer, rows[i][1], x + 6 + 38, ry, 0xFFFFFFFF, false);
        }
    }

    // ---------- Watermark ----------
    static void watermark(DrawContext ctx, MinecraftClient mc) {
        int acc = Theme.accent();
        String name = mc.player.getName().getString();
        String txt = " | " + name + " | " + mc.getCurrentFps() + " fps";
        int w = mc.textRenderer.getWidth("Visuals") + mc.textRenderer.getWidth(txt) + 12;
        Draw.border(ctx, 6, 6, w, 16, 4, 1, Draw.alpha(acc, 140), BG);
        ctx.drawText(mc.textRenderer, "Visuals", 12, 11, acc, true);
        ctx.drawText(mc.textRenderer, txt, 12 + mc.textRenderer.getWidth("Visuals"), 11, 0xFFFFFFFF, false);
    }

    // ---------- Modules List ----------
    static void modulesList(DrawContext ctx, MinecraftClient mc, int sw) {
        List<Mod> on = new ArrayList<>();
        for (Mod m : Mods.ALL) if (m.enabled && m != Mods.THEME) on.add(m);
        on.sort(Comparator.comparingInt((Mod m) -> -mc.textRenderer.getWidth(m.name)));
        int acc = Theme.accent();
        int y = 6;
        for (Mod m : on) {
            int w = mc.textRenderer.getWidth(m.name) + 10;
            ctx.fill(sw - w - 6, y, sw - 4, y + 12, BG);
            ctx.fill(sw - 6, y, sw - 4, y + 12, acc);
            ctx.drawText(mc.textRenderer, m.name, sw - w - 1, y + 2, 0xFFFFFFFF, false);
            y += 12;
        }
    }

    // ---------- Crosshair ----------
    static void crosshair(DrawContext ctx, int sw, int sh) {
        int cx = sw / 2, cy = sh / 2;
        int size = Mods.CX_SIZE.i(), gap = Mods.CX_GAP.i();
        int col = Theme.pick(Mods.CX_THEME.v, Mods.CX_HUE.v);
        switch (Mods.CX_STYLE.get()) {
            case "Dot" -> ctx.fill(cx - 1, cy - 1, cx + 2, cy + 2, col);
            case "Ring" -> {
                int r = gap + size;
                for (int d = 0; d < 360; d += 5) {
                    int px = cx + (int) Math.round(Math.cos(Math.toRadians(d)) * r);
                    int py = cy + (int) Math.round(Math.sin(Math.toRadians(d)) * r);
                    ctx.fill(px, py, px + 1, py + 1, col);
                }
            }
            default -> {
                ctx.fill(cx - gap - size, cy, cx - gap, cy + 1, col);
                ctx.fill(cx + 1 + gap, cy, cx + 1 + gap + size, cy + 1, col);
                ctx.fill(cx, cy - gap - size, cx + 1, cy - gap, col);
                ctx.fill(cx, cy + 1 + gap, cx + 1, cy + 1 + gap + size, col);
            }
        }
    }

    private Hud() {}
}
