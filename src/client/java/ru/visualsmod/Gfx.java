package ru.visualsmod;

import net.minecraft.client.gui.DrawContext;

public final class Gfx {
    public static void rounded(DrawContext c, int x1, int y1, int x2, int y2, int r, int col) {
        r = Math.max(0, Math.min(r, Math.min((x2 - x1) / 2, (y2 - y1) / 2)));
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = (int) Math.round(r - Math.sqrt(Math.max(0, r * r - dy * dy)));
            c.fill(x1 + inset, y1 + i, x2 - inset, y1 + i + 1, col);
            c.fill(x1 + inset, y2 - i - 1, x2 - inset, y2 - i, col);
        }
        c.fill(x1, y1 + r, x2, y2 - r, col);
    }

    public static int alpha(int argb, int a) {
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
