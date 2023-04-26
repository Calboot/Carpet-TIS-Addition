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

package carpettisaddition.logging.loggers.microtiming;

import carpettisaddition.logging.loggers.microtiming.interfaces.ServerWorldWithMicroTimingLogger;
import carpettisaddition.logging.loggers.microtiming.tickphase.TickPhase;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class MicroTimingAccess
{
	public static TickPhase getTickPhase(@NotNull ServerWorld world)
	{
		return ((ServerWorldWithMicroTimingLogger)world).getMicroTimingLogger().getTickPhase();
	}

	public static TickPhase getTickPhase(@NotNull World world)
	{
		return world instanceof ServerWorld ? getTickPhase((ServerWorld)world) : getTickPhase();
	}

	public static TickPhase getTickPhase()
	{
		ServerWorld serverWorld = MicroTimingLoggerManager.getCurrentWorld();
		if (serverWorld != null)
		{
			return getTickPhase(serverWorld);
		}
		else
		{
			return MicroTimingLoggerManager.getOffWorldTickPhase();
		}
	}
}
