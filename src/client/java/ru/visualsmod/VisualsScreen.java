package ru.visualsmod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class VisualsScreen extends Screen {
    private static final int SB = 110;
    private static final String[] TABS = {"Визуалы", "HUD", "Настройки"};
    private static int tab = 0;

    private int px, py, pw, ph;

    private static final class Cell {
        final String name, desc;
        final BooleanSupplier state;
        final Runnable click;
        Cell(String n, String d, BooleanSupplier s, Runnable c) { name = n; desc = d; state = s; click = c; }
    }

    public VisualsScreen() {
        super(Text.literal("Визуалы"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private List<Cell> cells() {
        List<Cell> list = new ArrayList<>();
        if (tab == 2) {
            list.add(new Cell("Оранжевая тема", "Цвет интерфейса",
                () -> Config.accent == Config.ORANGE, () -> Config.accent = Config.ORANGE));
            list.add(new Cell("Фиолетовая тема", "Цвет интерфейса",
                () -> Config.accent == Config.PURPLE, () -> Config.accent = Config.PURPLE));
            return list;
        }
        for (Feature f : Features.ALL) {
            if (f.cat == tab) list.add(new Cell(f.name, f.desc, () -> f.enabled, () -> f.enabled = !f.enabled));
        }
        return list;
    }

    private int contentX() { return px + SB + 16; }
    private int contentW() { return pw - SB - 32; }
    private int colW() { return (contentW() - 12) / 2; }
    private int cellX(int i) { return contentX() + (i % 2) * (colW() + 12); }
    private int cellY(int i) { return py + 46 + (i / 2) * 42; }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        super.render(c, mx, my, delta);
        pw = Math.min(480, width - 20);
        ph = Math.min(280, height - 20);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        int acc = Config.accent;

        Gfx.rounded(c, px, py, px + pw, py + ph, 8, 0xFF111114);
        Gfx.rounded(c, px, py, px + SB, py + ph, 8, 0xFF0C0C0E);
        c.fill(px + SB - 8, py, px + SB, py + ph, 0xFF0C0C0E);
        c.fill(px + SB, py, px + SB + 1, py + ph, 0xFF1C1C20);

        Gfx.rounded(c, px + 12, py + 12, px + 24, py + 24, 3, acc);
        c.drawText(textRenderer, "Визуалы", px + 30, py + 14, 0xFFFFFFFF, true);

        for (int i = 0; i < TABS.length; i++) {
            int ty = py + 40 + i * 26;
            boolean sel = i == tab;
            boolean hov = mx >= px + 8 && mx < px + SB - 8 && my >= ty && my < ty + 22;
            if (sel) Gfx.rounded(c, px + 8, ty, px + SB - 8, ty + 22, 6, acc);
            else if (hov) Gfx.rounded(c, px + 8, ty, px + SB - 8, ty + 22, 6, 0xFF1A1A1E);
            c.drawText(textRenderer, TABS[i], px + 18, ty + 7, sel ? 0xFFFFFFFF : 0xFF9A9AA2, true);
        }

        c.drawText(textRenderer, TABS[tab], contentX(), py + 16, 0xFFFFFFFF, true);
        c.fill(px + SB + 1, py + 34, px + pw, py + 35, 0xFF1C1C20);

        List<Cell> list = cells();
        for (int i = 0; i < list.size(); i++) {
            Cell cell = list.get(i);
            int x = cellX(i), y = cellY(i), w = colW();
            boolean hov = mx >= x && mx < x + w && my >= y && my < y + 38;
            if (hov) Gfx.rounded(c, x, y, x + w, y + 38, 6, 0xFF18181C);

            int textMax = w - 46;
            c.drawText(textRenderer, textRenderer.trimToWidth(cell.name, textMax), x + 8, y + 8, 0xFFFFFFFF, true);
            c.drawText(textRenderer, textRenderer.trimToWidth(cell.desc, textMax), x + 8, y + 21, 0xFF7A7A83, false);

            boolean on = cell.state.getAsBoolean();
            int tx = x + w - 34, ty = y + 13;
            Gfx.rounded(c, tx, ty, tx + 26, ty + 12, 6, on ? acc : 0xFF3A3A40);
            int kx = on ? tx + 26 - 10 : tx + 2;
            Gfx.rounded(c, kx, ty + 2, kx + 8, ty + 10, 4, 0xFFFFFFFF);
        }

        c.drawText(textRenderer, "Правый Shift - меню", px + 12, py + ph - 14, 0xFF5A5A62, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            for (int i = 0; i < TABS.length; i++) {
                int ty = py + 40 + i * 26;
                if (mx >= px + 8 && mx < px + SB - 8 && my >= ty && my < ty + 22) {
                    tab = i;
                    return true;
                }
            }
            List<Cell> list = cells();
            for (int i = 0; i < list.size(); i++) {
                int x = cellX(i), y = cellY(i), w = colW();
                if (mx >= x && mx < x + w && my >= y && my < y + 38) {
                    list.get(i).click.run();
                    Config.save();
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }
}
