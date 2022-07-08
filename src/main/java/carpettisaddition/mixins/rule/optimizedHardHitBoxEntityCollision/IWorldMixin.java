package carpettisaddition.mixins.rule.optimizedHardHitBoxEntityCollision;

import carpettisaddition.CarpetTISAdditionSettings;
import carpettisaddition.helpers.rule.optimizedHardHitBoxEntityCollision.OptimizedHardHitBoxEntityCollisionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EntityView;
import net.minecraft.world.IWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Set;
import java.util.stream.Stream;

@Mixin(value = IWorld.class, priority = 2000)
public interface IWorldMixin extends EntityView
{
	/**
	 * @reason Interface injection is not supported by Mixin yet
	 * So here comes the @Overwrite
	 *
	 * @author Fallen_Breath
	 */
	@Overwrite
	default Stream<VoxelShape>
	//#if MC >= 11500
	getEntityCollisions
	//#else
	//$$ method_20743
	//#endif
	(Entity entity, Box box, Set<Entity> excluded)
	{
		try
		{
			if (CarpetTISAdditionSettings.optimizedHardHitBoxEntityCollision)
			{
				if (!OptimizedHardHitBoxEntityCollisionHelper.treatsGeneralEntityAsHardHitBox(entity))
				{
					OptimizedHardHitBoxEntityCollisionHelper.checkHardHitBoxEntityOnly.set(true);
				}
			}

			// vanill copy
			return EntityView.super.
					//#if MC >= 11500
							getEntityCollisions
					//#else
					//$$ method_20743
					//#endif
							(entity, box, excluded);
		}
		finally
		{
			OptimizedHardHitBoxEntityCollisionHelper.checkHardHitBoxEntityOnly.set(false);
		}
	}
}
