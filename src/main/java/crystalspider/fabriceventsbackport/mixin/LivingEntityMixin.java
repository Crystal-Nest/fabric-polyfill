package main.java.crystalspider.fabriceventsbackport.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
  @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "net/minecraft/world/World.sendEntityStatus(Lnet/minecraft/entity/Entity;B)V"))
	private void notifyDeath(DamageSource source, CallbackInfo ci) {
		ServerLivingEntityEvents.AFTER_DEATH.invoker().afterDeath((LivingEntity) (Object) this, source);
	}

	@Redirect(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isDead()Z", ordinal = 1))
	boolean beforeEntityKilled(LivingEntity livingEntity, DamageSource source, float amount) {
		return isDead() && ServerLivingEntityEvents.ALLOW_DEATH.invoker().allowDeath(livingEntity, source, amount);
	}

	@Inject(method = "damage", at = @At(value = "INVOKE", target = "net/minecraft/entity/LivingEntity.isSleeping()Z"), cancellable = true)
	private void beforeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage((LivingEntity) (Object) this, source, amount)) {
			cir.setReturnValue(false);
		}
	}
}
