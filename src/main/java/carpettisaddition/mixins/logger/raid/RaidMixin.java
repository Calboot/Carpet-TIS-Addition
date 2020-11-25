package carpettisaddition.mixins.logger.raid;

import carpettisaddition.commands.raid.RaidTracker;
import carpettisaddition.logging.loggers.raid.IRaid;
import carpettisaddition.logging.loggers.raid.RaidLogger;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.Raid;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Raid.class)
public abstract class RaidMixin implements IRaid
{
	@Shadow private int badOmenLevel;

	@Shadow public abstract boolean hasWon();

	private int previousBadOmenLevel;

	@Inject(
			method = "<init>(ILnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;)V",
			at = @At(value = "RETURN")
	)
	private void onConstruct(CallbackInfo ci)
	{
		RaidLogger.getInstance().onRaidCreated((Raid)(Object)this);
	}

	@Override
	public void onRaidInvalidated(RaidLogger.InvalidateReason reason)
	{
		RaidLogger.getInstance().onRaidInvalidated((Raid)(Object)this, reason);
		RaidTracker.getInstance().trackRaidInvalidated(reason);
	}

	@Inject(
			method = "start",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/player/PlayerEntity;getStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Lnet/minecraft/entity/effect/StatusEffectInstance;"
			)
	)
	private void onStartBeforeCalculated(PlayerEntity player, CallbackInfo ci)
	{
		this.previousBadOmenLevel = this.badOmenLevel;
	}

	@Inject(
			method = "start",
			at = @At(
					value = "INVOKE_ASSIGN",
					shift = At.Shift.AFTER,
					target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I"
			)
	)
	private void onStarted(PlayerEntity player, CallbackInfo ci)
	{
		if (this.badOmenLevel > 1 && this.badOmenLevel > this.previousBadOmenLevel)
		{
			RaidLogger.getInstance().onBadOmenLevelIncreased((Raid)(Object)this, this.badOmenLevel);
		}
	}

	/*
	 * -------------------------
	 *    onInvalidated start
	 * -------------------------
	 */

	@Inject(
			method = "tick",
			slice = @Slice(
					from = @At(
							value = "INVOKE",
							target = "Lnet/minecraft/world/IWorld;getDifficulty()Lnet/minecraft/world/Difficulty;"
					)
			),
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/raid/Raid;invalidate()V",
					ordinal = 0
			)
	)
	private void onInvalidatedByDifficulty(CallbackInfo ci)
	{
		onRaidInvalidated(RaidLogger.InvalidateReason.DIFFICULTY_PEACEFUL);
	}

	@Inject(
			method = "tick",
			slice = @Slice(
					from = @At(
							value = "INVOKE",
							target = "Lnet/minecraft/server/world/ServerWorld;isNearOccupiedPointOfInterest(Lnet/minecraft/util/math/BlockPos;)Z"
					)
			),
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/raid/Raid;invalidate()V",
					ordinal = 0
			)
	)
	private void onInvalidatedByPOINotFound(CallbackInfo ci)
	{
		onRaidInvalidated(RaidLogger.InvalidateReason.POI_REMOVED_BEFORE_SPAWN);
	}

	@Inject(
			method = "tick",
			slice = @Slice(
					from = @At(
							value = "CONSTANT",
							args = "longValue=48000",
							ordinal = 0
					)
			),
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/raid/Raid;invalidate()V",
					ordinal = 0
			)
	)
	private void onInvalidatedByTimeOut(CallbackInfo ci)
	{
		onRaidInvalidated(RaidLogger.InvalidateReason.TIME_OUT);
	}

	@Inject(
			method = "tick",
			slice = @Slice(
					from = @At(
							value = "INVOKE",
							target = "Lnet/minecraft/entity/raid/Raid;playRaidHorn(Lnet/minecraft/util/math/BlockPos;)V"
					)
			),
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/raid/Raid;invalidate()V",
					ordinal = 0
			)
	)
	private void onInvalidatedByRaiderCannotSpawn(CallbackInfo ci)
	{
		onRaidInvalidated(RaidLogger.InvalidateReason.RAIDER_CANNOT_SPAWN);
	}

	@Inject(
			method = "tick",
			slice = @Slice(
					from = @At(
							value = "CONSTANT",
							args = "intValue=600"
					)
			),
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/raid/Raid;invalidate()V",
					ordinal = 0
			)
	)
	private void onInvalidatedByFinished(CallbackInfo ci)
	{
		if (this.hasWon())
		{
			onRaidInvalidated(RaidLogger.InvalidateReason.RAID_VICTORY);
		}
		else
		{
			onRaidInvalidated(RaidLogger.InvalidateReason.RAID_DEFEAT);
		}
	}

	/*
	 * -----------------------
	 *    onInvalidated end
	 * -----------------------
	 */

	@Inject(
			method = "method_20509",
			at = @At(value = "HEAD")
	)
	void onCenterMoved(BlockPos blockPos, CallbackInfo ci)
	{
		RaidLogger.getInstance().onCenterMoved((Raid)(Object)this, blockPos);
	}
}
