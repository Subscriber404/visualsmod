package ru.visualsmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class VisualsModClient implements ClientModInitializer {
    private static final String CAT = "key.categories.visualsmod";

    static LivingEntity lastTarget;
    static long lastHitTime = -1000;

    private static KeyBinding kMenu;
    private static final Map<Feature, KeyBinding> keys = new IdentityHashMap<>();
    private static final List<double[]> rings = new ArrayList<>();
    private static boolean wasOnGround = true;
    private static boolean wasBright = false;

    @Override
    public void onInitializeClient() {
        Config.load();

        kMenu = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.visualsmod.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CAT));
        for (Feature f : Features.ALL) {
            if (f.key > 0) {
                keys.put(f, KeyBindingHelper.registerKeyBinding(
                    new KeyBinding("key.visualsmod." + f.id, InputUtil.Type.KEYSYM, f.key, CAT)));
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        HudRenderCallback.EVENT.register((ctx, tick) -> Hud.render(ctx));

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient && entity instanceof LivingEntity le) {
                lastTarget = le;
                lastHitTime = world.getTime();
                if (Features.on("hitfx")) hitBurst(world, entity);
            }
            return ActionResult.PASS;
        });
    }

    private static void hitBurst(net.minecraft.world.World world, Entity e) {
        double cx = e.getX(), cy = e.getY() + e.getHeight() * 0.5, cz = e.getZ();
        for (int i = 0; i < 24; i++) {
            double a = Math.random() * Math.PI * 2;
            double b = (Math.random() - 0.5) * Math.PI;
            world.addParticle(i % 3 == 0 ? ParticleTypes.END_ROD : ParticleTypes.CRIT, cx, cy, cz,
                Math.cos(a) * Math.cos(b) * 0.25, Math.sin(b) * 0.25 + 0.05, Math.sin(a) * Math.cos(b) * 0.25);
        }
        for (int i = 0; i < 16; i++) {
            double a = i * Math.PI * 2 / 16;
            world.addParticle(ParticleTypes.ELECTRIC_SPARK,
                cx + Math.cos(a) * 0.4, cy, cz + Math.sin(a) * 0.4,
                Math.cos(a) * 0.2, 0, Math.sin(a) * 0.2);
        }
    }

    private void onTick(MinecraftClient client) {
        if (client.currentScreen == null) {
            while (kMenu.wasPressed()) client.setScreen(new VisualsScreen());
        }
        boolean changed = false;
        for (Map.Entry<Feature, KeyBinding> en : keys.entrySet()) {
            while (en.getValue().wasPressed()) {
                en.getKey().enabled = !en.getKey().enabled;
                changed = true;
            }
        }
        if (changed) Config.save();

        PlayerEntity p = client.player;
        if (p == null || client.world == null) return;

        boolean bright = Features.on("bright");
        if (bright) {
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, false, false, false));
        } else if (wasBright) {
            p.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
        wasBright = bright;

        long t = client.world.getTime();
        boolean crystal = p.getMainHandStack().isOf(VisualsMod.GLOW_CRYSTAL)
                       || p.getOffHandStack().isOf(VisualsMod.GLOW_CRYSTAL);

        if (Features.on("aura") && !client.isPaused()) {
            int points = crystal ? 3 : 1;
            for (int i = 0; i < points; i++) {
                double ang = t * 0.25 + i * (Math.PI * 2 / points);
                double y = p.getY() + 0.1 + (Math.sin(t * 0.1 + i) + 1) * 0.9;
                client.world.addParticle(crystal ? ParticleTypes.END_ROD : ParticleTypes.GLOW,
                    p.getX() + Math.cos(ang) * 0.9, y, p.getZ() + Math.sin(ang) * 0.9, 0, 0.01, 0);
            }
        }

        if (Features.on("trail") && p.getVelocity().horizontalLengthSquared() > 0.003 && p.isOnGround()) {
            client.world.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                p.getX(), p.getY() + 0.05, p.getZ(), 0, 0.02, 0);
        }

        boolean onGround = p.isOnGround();
        if (Features.on("jump") && wasOnGround && !onGround && p.getVelocity().y > 0.1) {
            rings.add(new double[]{p.getX(), p.getY() + 0.05, p.getZ(), 0});
        }
        wasOnGround = onGround;

        Iterator<double[]> it = rings.iterator();
        while (it.hasNext()) {
            double[] r = it.next();
            double radius = 0.3 + r[3] * 0.18;
            for (int i = 0; i < 20; i++) {
                double a = i * Math.PI * 2 / 20;
                client.world.addParticle(ParticleTypes.END_ROD,
                    r[0] + Math.cos(a) * radius, r[1], r[2] + Math.sin(a) * radius, 0, 0, 0);
            }
            r[3] += 1;
            if (r[3] > 12) it.remove();
        }
    }
}
