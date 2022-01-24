package carpettisaddition.mixins.command.lifetime.removal.conversion;

import carpettisaddition.commands.lifetime.interfaces.LifetimeTrackerTarget;
import carpettisaddition.commands.lifetime.removal.MobConversionRemovalReason;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin extends HostileEntity
{
	protected ZombieEntityMixin(EntityType<? extends HostileEntity> type, World world)
	{
		super(type, world);
	}

	@ModifyArg(
			method = "convertTo",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"
			)
	)

	private Entity recordSelfRemoval$LifeTimeTracker(Entity zombieVariant)
	{
		((LifetimeTrackerTarget)this).recordRemoval(new MobConversionRemovalReason(zombieVariant.getType()));
		return zombieVariant;
	}

	@Inject(
			method = "onKilledOther",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/passive/VillagerEntity;remove()V"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void recordVillagerRemoval$LifeTimeTracker(LivingEntity other, CallbackInfo ci, VillagerEntity villagerEntity, ZombieVillagerEntity zombieVillagerEntity)
	{
		((LifetimeTrackerTarget)villagerEntity).recordRemoval(new MobConversionRemovalReason(zombieVillagerEntity.getType()));
	}
}
