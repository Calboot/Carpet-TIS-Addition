/*
 * This file is part of the Carpet TIS Addition project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  Fallen_Breath and contributors
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

package carpettisaddition.mixins.rule.netherPortalEntityInteractionCheckUseFullBlockShape;

import carpettisaddition.CarpetTISAdditionSettings;
import net.minecraft.block.Block;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// impl in mc [1.21.5, 1.21.6)
@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin extends Block
{
	public NetherPortalBlockMixin(Settings settings)
	{
		super(settings);
	}

	@Inject(
			method = "getInsideCollisionShape",
			at = @At("HEAD"),
			cancellable = true
	)
	private void netherPortalEntityInteractionCheckUseFullBlockShape_useFullBlock(CallbackInfoReturnable<VoxelShape> cir)
	{
		if (CarpetTISAdditionSettings.netherPortalEntityInteractionCheckUseFullBlockShape)
		{
			cir.setReturnValue(VoxelShapes.fullCube());
		}
	}
}
