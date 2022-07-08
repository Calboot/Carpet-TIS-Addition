package carpettisaddition.mixins.rule.optimizedFastEntityMovement;

import carpettisaddition.CarpetTISAdditionSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.util.ReusableStream;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class EntityMixin
{
	private static final ThreadLocal<Boolean> optimizedFEMEnable = ThreadLocal.withInitial(() -> false);  // optimizedFastEntityMovementEnable
	private static final ThreadLocal<World> currentCollidingWorld = new ThreadLocal<>();
	private static final ThreadLocal<Entity> currentCollidingEntity = new ThreadLocal<>();

	// minimum velocity to trigger the optimization
	// set it to 0 or enable rule ultraSecretSetting to test vanilla behavior if you want
	private static final double OPTIMIZE_THRESHOLD = 4.0D;

	private static boolean checkMovement$OFEM(Vec3d movement)
	{
		return movement.lengthSquared() >= OPTIMIZE_THRESHOLD * OPTIMIZE_THRESHOLD || CarpetTISAdditionSettings.ultraSecretSetting.equals("optimizedFastEntityMovement");
	}

	@Redirect(
			//#if MC >= 11600
			//$$ method = "adjustMovementForCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/world/World;Lnet/minecraft/block/ShapeContext;Lnet/minecraft/util/collection/ReusableStream;)Lnet/minecraft/util/math/Vec3d;",
			//#else
			method = "adjustMovementForCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityContext;Lnet/minecraft/util/ReusableStream;)Lnet/minecraft/util/math/Vec3d;",
			//#endif
			at = @At(
					value = "INVOKE",
					//#if MC >= 11500
					target = "Lnet/minecraft/world/World;getBlockCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/stream/Stream;"
					//#else
					//$$ target = "Lnet/minecraft/world/World;method_20812(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/stream/Stream;"
					//#endif
			)
	)
	private static Stream<VoxelShape> dontUseThatLargeBlockCollisions(World world, Entity entity, Box box, /* parent method parameters -> */ Entity entityParam, Vec3d movement, Box entityBoundingBox, World worldParam, EntityContext context, ReusableStream<VoxelShape> collisions)
	{
		optimizedFEMEnable.set(CarpetTISAdditionSettings.optimizedFastEntityMovement && checkMovement$OFEM(movement));
		if (optimizedFEMEnable.get())
		{
			currentCollidingEntity.set(entity);
			currentCollidingWorld.set(world);
			return Stream.empty();
		}

		// vanilla
		return world.getBlockCollisions(entity, box);
	}

	@Redirect(
			//#if MC >= 11600
			//$$ method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/util/collection/ReusableStream;)Lnet/minecraft/util/math/Vec3d;",
			//#elseif MC >= 11500
			method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/util/ReusableStream;)Lnet/minecraft/util/math/Vec3d;",
			//#else
			//$$ method = "method_20737",
			//#endif
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/shape/VoxelShapes;calculateMaxOffset(Lnet/minecraft/util/math/Direction$Axis;Lnet/minecraft/util/math/Box;Ljava/util/stream/Stream;D)D"
			),
			require = 4
	)
	private static double useAxisOnlyBlockCollisions(Direction.Axis axis, Box box, Stream<VoxelShape> shapes, double maxDist, /* parent method parameters -> */ Vec3d movement, Box entityBoundingBox, ReusableStream<VoxelShape> collisions)
	{
		if (optimizedFEMEnable.get())
		{
			Vec3d axisOnlyMovement = null;
			switch (axis)
			{
				case X:
					axisOnlyMovement = new Vec3d(movement.getX(), 0.0D, 0.0D);
					break;
				case Y:
					axisOnlyMovement = new Vec3d(0.0D, movement.getY(), 0.0D);
					break;
				case Z:
					axisOnlyMovement = new Vec3d(0.0D, 0.0D, movement.getZ());
					break;
			}
			Stream<VoxelShape> blockCollisions = currentCollidingWorld.get().getBlockCollisions(currentCollidingEntity.get(), entityBoundingBox.stretch(axisOnlyMovement));
			shapes = Stream.concat(collisions.stream(), blockCollisions);
		}
		return VoxelShapes.calculateMaxOffset(axis, box, shapes, maxDist);
	}
}
