/*
 * This file is part of the Carpet TIS Addition project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2023  Fallen_Breath and contributors
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

package carpettisaddition.mixins.rule.farmlandTrampledDisabled;

import carpettisaddition.CarpetTISAdditionSettings;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//#if MC >= 11900
//$$ import net.minecraft.entity.Entity;
//#endif

@Mixin(FarmlandBlock.class)
public abstract class FarmLandBlockMixin
{
	@WrapWithCondition(
			method = "onLandedUpon",
			at = @At(
					value = "INVOKE",
					//#if MC >= 11900
					//$$ target = "Lnet/minecraft/block/FarmlandBlock;setToDirt(Lnet/minecraft/entity/Entity;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"
					//#else
					target = "Lnet/minecraft/block/FarmlandBlock;setToDirt(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"
					//#endif
			)
	)
	private boolean farmlandTrampledDisabled_cancel(
			//#if MC >= 11900
			//$$ Entity entity,
			//#endif
			BlockState state, World world, BlockPos pos
	)
	{
		return !CarpetTISAdditionSettings.farmlandTrampledDisabled;
	}
}
