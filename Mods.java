package pl.visuals;

import pl.visuals.Mod.Bool;
import pl.visuals.Mod.Category;
import pl.visuals.Mod.Mode;
import pl.visuals.Mod.Num;

import java.util.ArrayList;
import java.util.List;

/** Rejestr wszystkich modulow i ich ustawien. */
public final class Mods {
    public static final List<Mod> ALL = new ArrayList<>();
    private static Mod reg(Mod m) { ALL.add(m); return m; }

    // ===== CLIENT =====
    public static final Num THEME_HUE = new Num("Accent Hue", 160, 0, 360, 1);
    public static final Bool THEME_RAINBOW = new Bool("Rainbow", false);
    public static final Mod THEME = reg(new Mod("Theme", Category.CLIENT, true, THEME_HUE, THEME_RAINBOW).lock());

    // ===== VISUALS =====
    public static final Mode TR_TYPE = new Mode("Render Type", "Ghosts", "Ghosts", "Ring", "Halo");
    public static final Num TR_HUE = new Num("Hue", 190, 0, 360, 1);
    public static final Bool TR_THEME = new Bool("Theme Color", false);
    public static final Num TR_COUNT = new Num("Ghosts", 3, 1, 6, 1);
    public static final Num TR_SPEED = new Num("Speed", 2.5, 0.5, 6, 0.1);
    public static final Mode TR_BEHAVIOR = new Mode("Behavior", "Look", "Look", "Attack");
    public static final Mod TARGET_RENDER = reg(new Mod("Target Render", Category.VISUALS, true,
            TR_TYPE, TR_HUE, TR_THEME, TR_COUNT, TR_SPEED, TR_BEHAVIOR));

    public static final Num JC_RADIUS = new Num("Radius", 1.4, 0.5, 3, 0.1);
    public static final Num JC_HUE = new Num("Hue", 190, 0, 360, 1);
    public static final Mod JUMP_CIRCLES = reg(new Mod("Jump Circles", Category.VISUALS, false, JC_RADIUS, JC_HUE));

    public static final Num HAT_SIZE = new Num("Size", 0.55, 0.3, 1.0, 0.05);
    public static final Num HAT_SIDES = new Num("Sides", 16, 6, 32, 1);
    public static final Bool HAT_OTHERS = new Bool("Show On Others", true);
    public static final Mod CHINA_HAT = reg(new Mod("China Hat", Category.VISUALS, false, HAT_SIZE, HAT_SIDES, HAT_OTHERS));

    // ===== OTHER =====
    public static final Mode KE_TYPE = new Mode("Effect", "Totem", "Totem", "Flame", "Soul", "Heart");
    public static final Num KE_COUNT = new Num("Amount", 40, 10, 150, 5);
    public static final Mod KILL_EFFECT = reg(new Mod("Kill Effect", Category.OTHER, true, KE_TYPE, KE_COUNT));

    public static final Mode HS_TYPE = new Mode("Sound", "Orb", "Orb", "Levelup", "Arrow");
    public static final Num HS_PITCH = new Num("Pitch", 1.2, 0.5, 2, 0.1);
    public static final Num HS_VOL = new Num("Volume", 1, 0.1, 2, 0.1);
    public static final Mod HIT_SOUNDS = reg(new Mod("Hit Sounds", Category.OTHER, true, HS_TYPE, HS_PITCH, HS_VOL));

    // ===== HUD =====
    public static final Num TH_RANGE = new Num("Range", 30, 5, 60, 1);
    public static final Bool TH_ARMOR = new Bool("Armor", true);
    public static final Mod TARGET_HUD = reg(new Mod("Target HUD", Category.HUD, true, TH_RANGE, TH_ARMOR));

    public static final Mode CX_STYLE = new Mode("Style", "Cross", "Dot", "Cross", "Ring");
    public static final Num CX_SIZE = new Num("Size", 4, 1, 12, 1);
    public static final Num CX_GAP = new Num("Gap", 2, 0, 10, 1);
    public static final Bool CX_THEME = new Bool("Theme Color", true);
    public static final Num CX_HUE = new Num("Hue", 190, 0, 360, 1);
    public static final Mod CROSSHAIR = reg(new Mod("CrossHair", Category.HUD, false, CX_STYLE, CX_SIZE, CX_GAP, CX_THEME, CX_HUE));

    public static final Mod MODULES_LIST = reg(new Mod("Modules List", Category.HUD, true));
    public static final Mod ITEM_OVERLAY = reg(new Mod("Item Overlay", Category.HUD, false));
    public static final Mod PLAYER_INFO = reg(new Mod("Player Info", Category.HUD, true));
    public static final Mod WATERMARK = reg(new Mod("Watermark", Category.HUD, true));
    public static final Mod CONSUMABLE = reg(new Mod("Consumable", Category.HUD, true));

    private Mods() {}
}
