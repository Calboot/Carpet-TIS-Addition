package carpettisaddition.mixins.command.lifetime.removal;

import carpettisaddition.CarpetTISAdditionSettings;
import carpettisaddition.commands.lifetime.interfaces.LifetimeTrackerTarget;
import carpettisaddition.commands.lifetime.removal.LiteralRemovalReason;
import carpettisaddition.commands.lifetime.removal.MobPickupRemovalReason;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity
{
	protected MobEntityMixin(EntityType<? extends LivingEntity> type, World world)
	{
		super(type, world);
	}

	//#if MC >= 11500
	@Inject(
			method = "checkDespawn",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/mob/MobEntity;remove()V",
					ordinal = 0
			)
	)
	private void onDifficultyDespawnLifeTimeTracker(CallbackInfo ci)
	{
		((LifetimeTrackerTarget)this).recordRemoval(LiteralRemovalReason.DESPAWN_DIFFICULTY);
	}
	//#endif

	@Inject(
			method = "checkDespawn",
			// slice for optifine reeee
			// optifine will inserts shits after getClosestPlayer
			slice = @Slice(
					from = @At(
							value = "INVOKE",
							target = "Lnet/minecraft/entity/Entity;squaredDistanceTo(Lnet/minecraft/entity/Entity;)D"
					)
			),
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/mob/MobEntity;remove()V",
					ordinal = 0
			)
	)
	private void onImmediatelyDespawnLifeTimeTracker(CallbackInfo ci)
	{
		((LifetimeTrackerTarget)this).recordRemoval(LiteralRemovalReason.DESPAWN_IMMEDIATELY);
	}

	@Inject(
			method = "checkDespawn",
			slice = @Slice(
					from = @At(
							value = "INVOKE",
							target = "Lnet/minecraft/entity/Entity;squaredDistanceTo(Lnet/minecraft/entity/Entity;)D"
					)
			),
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/mob/MobEntity;remove()V",
					ordinal = 1
			)
	)
	private void onRandomlyDespawnLifeTimeTracker(CallbackInfo ci)
	{
		((LifetimeTrackerTarget)this).recordRemoval(LiteralRemovalReason.DESPAWN_RANDOMLY);
	}

	@Inject(method = "setPersistent", at = @At("HEAD"))
	private void onEntityPersistentLifeTimeTracker(CallbackInfo ci)
	{
		if (CarpetTISAdditionSettings.lifeTimeTrackerConsidersMobcap)
		{
			((LifetimeTrackerTarget)this).recordRemoval(LiteralRemovalReason.PERSISTENT);
		}
	}

	@Inject(
			//#if MC >= 11600
			//$$ method = "equipLootStack",
			//#else
			method = "loot",
			//#endif
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/entity/mob/MobEntity;persistent:Z"
			)
	)
	private void onEntityPersistent2LifeTimeTracker(CallbackInfo ci)
	{
		if (CarpetTISAdditionSettings.lifeTimeTrackerConsidersMobcap)
		{
			((LifetimeTrackerTarget)this).recordRemoval(LiteralRemovalReason.PERSISTENT);
		}
	}

	@Inject(
			method = "loot",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/ItemEntity;remove()V"
			)
	)
	private void onItemPickUpLifeTimeTracker(ItemEntity item, CallbackInfo ci)
	{
		((LifetimeTrackerTarget)item).recordRemoval(new MobPickupRemovalReason(this.getType()));
	}
}
