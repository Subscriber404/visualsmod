package ru.visualsmod;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class Features {
    public static final int VISUALS = 0, HUD = 1;
    public static final List<Feature> ALL = new ArrayList<>();

    static {
        add("bright",    "Полная яркость",   "Видно в темноте",       VISUALS, GLFW.GLFW_KEY_G, false);
        add("aura",      "Аура",             "Частицы вокруг игрока", VISUALS, GLFW.GLFW_KEY_B, true);
        add("trail",     "След",             "Частицы за ногами",     VISUALS, GLFW.GLFW_KEY_N, true);
        add("hitfx",     "Эффекты удара",    "Вспышка при ударе",     VISUALS, GLFW.GLFW_KEY_K, true);
        add("jump",      "Круги прыжка",     "Круг при прыжке",       VISUALS, GLFW.GLFW_KEY_L, true);
        add("esp",       "Подсветка существ","Свечение сквозь стены", VISUALS, GLFW.GLFW_KEY_J, false);
        add("tint",      "Тон экрана",       "Цветная подсветка",     VISUALS, GLFW.GLFW_KEY_H, false);

        add("watermark", "Ватермарка",       "Ник, FPS и пинг",       HUD, -1, true);
        add("coords",    "Координаты",       "XYZ и направление",     HUD, -1, true);
        add("armor",     "Броня",            "Прочность брони",       HUD, -1, true);
        add("potions",   "Эффекты",          "Активные эффекты",      HUD, -1, true);
        add("target",    "Инфо о цели",      "Здоровье и дистанция",  HUD, -1, true);
        add("arraylist", "Список функций",   "Включённые функции",    HUD, -1, true);
        add("hotkeys",   "Горячие клавиши",  "Список биндов",         HUD, -1, false);
    }

    private static void add(String id, String name, String desc, int cat, int key, boolean on) {
        ALL.add(new Feature(id, name, desc, cat, key, on));
    }

    public static Feature get(String id) {
        for (Feature f : ALL) if (f.id.equals(id)) return f;
        return null;
    }

    public static boolean on(String id) {
        Feature f = get(id);
        return f != null && f.enabled;
    }
}
