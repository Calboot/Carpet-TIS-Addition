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

package carpettisaddition.mixins.carpet.tweaks.command.noInfoBlockChunkLoading;

import carpet.commands.InfoCommand;
import carpettisaddition.utils.CommandUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InfoCommand.class)
public abstract class InfoCommandMixin
{
	@Inject(method = "infoBlock", at = @At("HEAD"))
	private static void stopUsingInfoBlockCommandAsARemoteChunkLoader(ServerCommandSource source, BlockPos pos, String grep, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException
	{
		if (CommandUtil.canCheat(source))
		{
			return;
		}
		ChunkPos chunkPos = new ChunkPos(pos);
		if (!source.getWorld().isChunkLoaded(chunkPos.x, chunkPos.z))
		{
			throw BlockPosArgumentType.UNLOADED_EXCEPTION.create();
		}
	}
}
