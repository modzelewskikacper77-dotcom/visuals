package pl.visuals;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Config {
    static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("visuals.properties"); }

    public static void load() {
        Properties p = new Properties();
        try (var in = Files.newInputStream(file())) { p.load(in); } catch (IOException e) { return; }
        for (Mod m : Mods.ALL) {
            String v = p.getProperty(m.key() + ".enabled");
            if (v != null && !m.locked) m.enabled = Boolean.parseBoolean(v);
            for (Mod.Setting s : m.settings) {
                String sv = p.getProperty(m.key() + "." + s.key());
                if (sv != null) try { s.load(sv); } catch (Exception ignored) { }
            }
        }
    }

    public static void save() {
        Properties p = new Properties();
        for (Mod m : Mods.ALL) {
            p.setProperty(m.key() + ".enabled", String.valueOf(m.enabled));
            for (Mod.Setting s : m.settings) p.setProperty(m.key() + "." + s.key(), s.save());
        }
        try (var out = Files.newOutputStream(file())) { p.store(out, "Visuals config"); } catch (IOException ignored) { }
    }

    private Config() {}
}
