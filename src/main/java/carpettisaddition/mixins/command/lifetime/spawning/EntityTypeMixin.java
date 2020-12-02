package carpettisaddition.mixins.command.lifetime.spawning;

import carpettisaddition.commands.lifetime.interfaces.IEntity;
import carpettisaddition.commands.lifetime.spawning.LiteralSpawningReason;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin<T extends Entity>
{
	@Inject(method = "spawnFromItemStack", at = @At("TAIL"))
	private void onEntitySpawnFromItemLifeTimeTracker(World world, ItemStack stack, PlayerEntity player, BlockPos pos, SpawnType spawnType, boolean alignPosition, boolean invertY, CallbackInfoReturnable<Entity> cir)
	{
		Entity entity = cir.getReturnValue();
		if (entity != null)
		{
			((IEntity)entity).recordSpawning(LiteralSpawningReason.ITEM);
		}
	}
}
