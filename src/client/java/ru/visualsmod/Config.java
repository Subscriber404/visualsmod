package ru.visualsmod;

import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Config {
    public static final int ORANGE = 0xFFFF8A1F;
    public static final int PURPLE = 0xFF8A63FF;
    public static int accent = ORANGE;

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("visualsmod.properties");
    }

    public static void load() {
        try {
            Path f = file();
            if (!Files.exists(f)) return;
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(f)) { p.load(in); }
            for (Feature ft : Features.ALL) {
                String v = p.getProperty("f." + ft.id);
                if (v != null) ft.enabled = Boolean.parseBoolean(v);
            }
            accent = "purple".equals(p.getProperty("theme")) ? PURPLE : ORANGE;
        } catch (Exception ignored) { }
    }

    public static void save() {
        try {
            Properties p = new Properties();
            for (Feature ft : Features.ALL) p.setProperty("f." + ft.id, String.valueOf(ft.enabled));
            p.setProperty("theme", accent == PURPLE ? "purple" : "orange");
            try (OutputStream out = Files.newOutputStream(file())) { p.store(out, "Visuals Mod"); }
        } catch (Exception ignored) { }
    }
}
