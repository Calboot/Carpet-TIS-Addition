package carpettisaddition.mixins.command.lifetime.spawning.raid;

import carpettisaddition.commands.lifetime.interfaces.LifetimeTrackerTarget;
import carpettisaddition.commands.lifetime.spawning.LiteralSpawningReason;
import net.minecraft.entity.Entity;
import net.minecraft.village.raid.Raid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Raid.class)
public abstract class RaidMixin
{
	@ModifyArg(
			method = "addRaider",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/world/ServerWorld;spawnEntityAndPassengers(Lnet/minecraft/entity/Entity;)V"
			)
	)
	private Entity onSpawnRaiderLifeTimeTracker(Entity raiderEntity)
	{
		((LifetimeTrackerTarget)raiderEntity).recordSpawning(LiteralSpawningReason.RAID);
		return raiderEntity;
	}
}
