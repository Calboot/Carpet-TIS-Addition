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

package carpettisaddition.mixins.rule.tntIgnoreRedstoneSignal;

import carpettisaddition.CarpetTISAdditionSettings;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.block.TntBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TntBlock.class)
public abstract class TntBlockMixin
{
	/**
	 * Notes: Carpet rule tntDoNotUpdate already applied @Redirect
	 */
	@ModifyExpressionValue(
			method = {
					"onBlockAdded",
					"neighborUpdate"
			},
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/World;isReceivingRedstonePower(Lnet/minecraft/util/math/BlockPos;)Z"
			)
	)
	private boolean tntIgnoreRedstoneSignalImpl(boolean isReceivingRedstonePower)
	{
		if (CarpetTISAdditionSettings.tntIgnoreRedstoneSignal)
		{
			isReceivingRedstonePower = false;
		}
		return isReceivingRedstonePower;
	}
}
