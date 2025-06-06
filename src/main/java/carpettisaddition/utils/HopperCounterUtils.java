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

package carpettisaddition.utils;

import carpet.utils.WoolTool;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HopperCounterUtils
{
	@Nullable
	public static DyeColor getWoolColorForHopper(World world, HopperBlockEntity hopper)
	{
		Direction hopperFacing = hopper.getCachedState().get(HopperBlock.FACING);
		return WoolTool.getWoolColorAtPosition(world, hopper.getPos().offset(hopperFacing));
	}

	@Nullable
	public static DyeColor getWoolColorForHopper(HopperBlockEntity hopper)
	{
		World world = hopper.getWorld();
		if (world == null)
		{
			return null;
		}
		return getWoolColorForHopper(world, hopper);
	}
}
