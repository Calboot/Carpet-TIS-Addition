/*
 * This file is part of the Carpet TIS Addition project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2024  Fallen_Breath and contributors
 *
 * Carpet TIS Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Carpet TIS Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Carpet TIS Addition.  If not, see <https://www.gnu.org/licenses/>.
 */

package carpettisaddition.mixins.command.lifetime.spawning.dispensed;

import carpettisaddition.commands.lifetime.interfaces.LifetimeTrackerTarget;
import carpettisaddition.commands.lifetime.spawning.LiteralSpawningReason;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

//#if MC >= 12102
//$$ import net.minecraft.item.MinecartItem;
//#endif

@Mixin(
		//#if MC >= 12102
		//$$ MinecartItem.class
		//#else
		targets = "net/minecraft/item/MinecartItem$1"
		//#endif
)
public abstract class MinecartItemDispenseBehaviorMixin
{
	@ModifyArg(
			//#if MC >= 12102
			//$$ method = "useOnBlock",
			//#else
			method = "dispenseSilently",
			//#endif
			at = @At(
					value = "INVOKE",
					//#if MC >= 12004
					//$$ target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z"
					//#else
					target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"
					//#endif
			)
	)
	private Entity lifetimeTracker_recordSpawning_dispensed_minecart(Entity minecartEntity)
	{
		((LifetimeTrackerTarget)minecartEntity).recordSpawning(LiteralSpawningReason.DISPENSED);
		return minecartEntity;
	}
}
