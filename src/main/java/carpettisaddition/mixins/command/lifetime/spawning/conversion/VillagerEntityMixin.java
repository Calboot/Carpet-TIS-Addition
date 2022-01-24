package carpettisaddition.mixins.command.lifetime.spawning.conversion;

import carpettisaddition.commands.lifetime.interfaces.LifetimeTrackerTarget;
import carpettisaddition.commands.lifetime.spawning.MobConversionSpawningReason;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AbstractTraderEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends AbstractTraderEntity
{
	public VillagerEntityMixin(EntityType<? extends AbstractTraderEntity> entityType, World world)
	{
		super(entityType, world);
	}

	@ModifyArg(
			method = "onStruckByLightning",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"
			)
	)
	private Entity recordWitchSpawning$LifeTimeTracker(Entity witch)
	{
		((LifetimeTrackerTarget)witch).recordSpawning(new MobConversionSpawningReason(this.getType()));
		return witch;
	}
}
