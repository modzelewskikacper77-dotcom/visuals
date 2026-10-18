package pl.visuals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Jeden modul (karta w menu) + jego ustawienia. */
public class Mod {
    public enum Category {
        VISUALS("Visuals", "V"), CLIENT("Client", "C"), OTHER("Other", "O"), HUD("HUD", "H");
        public final String title, icon;
        Category(String t, String i) { title = t; icon = i; }
    }

    public static abstract class Setting {
        public final String name;
        Setting(String n) { name = n; }
        public String key() { return name.replace(' ', '_'); }
        abstract String save();
        abstract void load(String s);
    }

    public static class Bool extends Setting {
        public boolean v;
        public Bool(String n, boolean def) { super(n); v = def; }
        String save() { return String.valueOf(v); }
        void load(String s) { v = Boolean.parseBoolean(s); }
    }

    public static class Num extends Setting {
        public double v, min, max, step;
        public Num(String n, double def, double min, double max, double step) {
            super(n); this.min = min; this.max = max; this.step = step; v = def;
        }
        public void set(double x) {
            x = Math.max(min, Math.min(max, x));
            if (step > 0) x = Math.round((x - min) / step) * step + min;
            v = Math.round(x * 1000) / 1000.0;
        }
        public float f() { return (float) v; }
        public int i() { return (int) Math.round(v); }
        public String text() {
            return step >= 1 ? String.valueOf(i()) : String.format(Locale.ROOT, "%.2f", v);
        }
        String save() { return String.valueOf(v); }
        void load(String s) { set(Double.parseDouble(s)); }
    }

    public static class Mode extends Setting {
        public final String[] modes;
        public int idx;
        public Mode(String n, String def, String... modes) {
            super(n); this.modes = modes;
            for (int i = 0; i < modes.length; i++) if (modes[i].equals(def)) idx = i;
        }
        public String get() { return modes[idx]; }
        public boolean is(String s) { return get().equals(s); }
        public void cycle(int d) { idx = (idx + d + modes.length) % modes.length; }
        String save() { return get(); }
        void load(String s) { for (int i = 0; i < modes.length; i++) if (modes[i].equals(s)) idx = i; }
    }

    public final String name;
    public final Category cat;
    public boolean enabled;
    public boolean expanded;
    public boolean locked;
    public final List<Setting> settings = new ArrayList<>();

    public Mod(String name, Category cat, boolean def, Setting... s) {
        this.name = name; this.cat = cat; this.enabled = def;
        settings.addAll(List.of(s));
    }

    public Mod lock() { locked = true; enabled = true; return this; }
    public void toggle() { if (!locked) enabled = !enabled; }
    public String key() { return name.replace(' ', '_'); }
}
