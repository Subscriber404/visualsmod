package ru.visualsmod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.visualsmod.Features;

@Mixin(Entity.class)
public class EntityGlowMixin {
    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void visualsmod$glow(CallbackInfoReturnable<Boolean> cir) {
        if (!Features.on("esp")) return;
        Entity self = (Entity) (Object) this;
        if (self instanceof LivingEntity && self != MinecraftClient.getInstance().player) {
            cir.setReturnValue(true);
        }
    }
}
