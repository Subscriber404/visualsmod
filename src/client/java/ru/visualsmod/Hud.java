package ru.visualsmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Hud {
    private static final int BG = 0xCC101014;
    private static final EquipmentSlot[] ARMOR = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private static String facingRu(PlayerEntity p) {
        return switch (p.getHorizontalFacing()) {
            case NORTH -> "Север";
            case SOUTH -> "Юг";
            case EAST -> "Восток";
            case WEST -> "Запад";
            default -> "";
        };
    }

    private static String time(int ticks) {
        int s = ticks / 20;
        return (s / 60) + ":" + String.format("%02d", s % 60);
    }

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity p = mc.player;
        if (p == null || mc.world == null || mc.options.hudHidden) return;
        int w = ctx.getScaledWindowWidth();
        int h = ctx.getScaledWindowHeight();
        int acc = Config.accent;
        var tr = mc.textRenderer;

        if (Features.on("tint")) ctx.fill(0, 0, w, h, Gfx.alpha(acc, 0x22));

        int y = 6;

        if (Features.on("watermark")) {
            int ping = 0;
            if (mc.getNetworkHandler() != null) {
                PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(p.getUuid());
                if (e != null) ping = e.getLatency();
            }
            String s = "Визуалы  |  " + ping + " мс  |  " + mc.getCurrentFps() + " FPS";
            int bw = tr.getWidth(s) + 30;
            Gfx.rounded(ctx, 6, y, 6 + bw, y + 18, 6, BG);
            Gfx.rounded(ctx, 12, y + 5, 20, y + 13, 2, acc);
            ctx.drawText(tr, s, 26, y + 5, 0xFFFFFFFF, true);
            y += 22;
        }

        if (Features.on("coords")) {
            String s = String.format("XYZ  %.0f  %.0f  %.0f   %s", p.getX(), p.getY(), p.getZ(), facingRu(p));
            Gfx.rounded(ctx, 6, y, 6 + tr.getWidth(s) + 12, y + 16, 5, BG);
            ctx.drawText(tr, s, 12, y + 4, 0xFFDDDDDD, true);
            y += 20;
        }

        if (Features.on("potions")) {
            for (StatusEffectInstance e : p.getStatusEffects()) {
                if (Features.on("bright") && e.getEffectType().equals(StatusEffects.NIGHT_VISION)) continue;
                String s = Text.translatable(e.getTranslationKey()).getString()
                    + " " + (e.getAmplifier() + 1) + "  " + (e.isInfinite() ? "∞" : time(e.getDuration()));
                Gfx.rounded(ctx, 6, y, 6 + tr.getWidth(s) + 12, y + 14, 4, BG);
                ctx.drawText(tr, s, 12, y + 3, 0xFFFFFFFF, true);
                y += 17;
            }
        }

        if (Features.on("arraylist")) {
            List<String> names = new ArrayList<>();
            for (Feature f : Features.ALL) if (f.enabled && f.cat == Features.VISUALS) names.add(f.name);
            names.sort(Comparator.comparingInt((String s) -> -tr.getWidth(s)));
            int ry = 6;
            for (String s : names) {
                int tw = tr.getWidth(s);
                Gfx.rounded(ctx, w - tw - 16, ry, w - 6, ry + 14, 4, BG);
                ctx.fill(w - 8, ry + 3, w - 6, ry + 11, acc);
                ctx.drawText(tr, s, w - tw - 12, ry + 3, acc, true);
                ry += 17;
            }
        }

        if (Features.on("hotkeys")) {
            List<String> rows = new ArrayList<>();
            for (Feature f : Features.ALL) if (f.enabled && f.key > 0) rows.add(f.name);
            if (!rows.isEmpty()) {
                int bw = 110;
                int bh = 18 + rows.size() * 12;
                int bx = w - bw - 6, by = h / 2 - bh / 2;
                Gfx.rounded(ctx, bx, by, bx + bw, by + bh, 6, BG);
                ctx.drawText(tr, "Горячие клавиши", bx + 8, by + 5, acc, true);
                int ry = by + 18;
                for (String s : rows) {
                    ctx.drawText(tr, s, bx + 8, ry, 0xFFDDDDDD, false);
                    ry += 12;
                }
            }
        }

        if (Features.on("armor")) {
            int ay = h / 2 - 44;
            Gfx.rounded(ctx, 6, ay - 4, 56, ay + 80 + 4, 6, BG);
            for (EquipmentSlot slot : ARMOR) {
                ItemStack s = p.getEquippedStack(slot);
                if (!s.isEmpty()) {
                    ctx.drawItem(s, 9, ay);
                    if (s.getMaxDamage() > 0) {
                        int left = s.getMaxDamage() - s.getDamage();
                        int col = left * 100 / s.getMaxDamage() < 25 ? 0xFFFF5555 : 0xFFFFFFFF;
                        ctx.drawText(tr, String.valueOf(left), 28, ay + 4, col, true);
                    }
                }
                ay += 20;
            }
        }

        if (Features.on("target")) {
            LivingEntity target = null;
            if (mc.targetedEntity instanceof LivingEntity le) target = le;
            else if (VisualsModClient.lastTarget != null && VisualsModClient.lastTarget.isAlive()
                     && mc.world.getTime() - VisualsModClient.lastHitTime < 80) target = VisualsModClient.lastTarget;
            if (target != null) {
                int bx = w / 2 + 24, by = h / 2 + 12, bw = 130;
                Gfx.rounded(ctx, bx, by, bx + bw, by + 40, 6, BG);
                ctx.drawText(tr, tr.trimToWidth(target.getName().getString(), bw - 16), bx + 8, by + 6, 0xFFFFFFFF, true);
                float hp = Math.max(0, target.getHealth());
                float max = Math.max(1, target.getMaxHealth());
                Gfx.rounded(ctx, bx + 8, by + 18, bx + bw - 8, by + 24, 3, 0xFF2A2A30);
                int fill = Math.max(6, (int) ((bw - 16) * (hp / max)));
                Gfx.rounded(ctx, bx + 8, by + 18, bx + 8 + fill, by + 24, 3, acc);
                ctx.drawText(tr, String.format("%.1f/%.0f  %.1f м", hp, max, p.distanceTo(target)),
                    bx + 8, by + 28, 0xFFAAAAAA, false);
            }
        }
    }
}
