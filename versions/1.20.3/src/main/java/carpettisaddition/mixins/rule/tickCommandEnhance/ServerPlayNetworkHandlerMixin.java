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

package carpettisaddition.mixins.rule.tickCommandEnhance;

import carpettisaddition.CarpetTISAdditionSettings;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin
{
	/**
	 * There's an issue with vanilla Minecraft's remote command completion, that might give inaccessible suggestions to the client
	 * <p>
	 * The issue occurs when there's a command node that has at least 2 children.
	 * For example, one child is a literal node that it's not accessible,
	 * (e.g. using {@link com.mojang.brigadier.builder.ArgumentBuilder#requires} with always false predicate),
	 * and the other child is a normal argument node, like the following command tree setup:
	 * <pre>{@code
	 * tick
	 * +-- sprint          (the node that has at least 2 children)
	 *     +-- status      (an inaccessible literal node)
	 *     +-- <duration>  (a normal argument node with suggestion "60s" and "1d")
	 * }</pre>
	 * <p>
	 * Now, if the player enters "/tick sprint " (with tailing space) in the client, the client will try to create a list of
	 * command suggestion for the children of node "sprint". Here's what will happen next:
	 * <ul>
	 * <li>
	 * The node "status" does not exist on the client-side command tree,
	 * because the server filters it out before sending it to the client, which is expected
	 * <li>
	 * The node "duration" will request a remote command completion, using {@link RequestCommandCompletionsC2SPacket}
	 * to send the current command "/tick sprint " to the server
	 * <p>
	 * Now, the server will try to generate a list of suggestion, using the serverside command tree which contains the "status" node.
	 * And, there's no extra requirement check about whether the client is able to access the nodes.
	 * As a result, the serverside generated suggestion will be ["status", "60s", "1d"].
	 * Yes, the "status" node is included, and it's an issue
	 * </ul>
	 * The expected behavior should be, the server only returns suggestion ["60s", "1d"], excluding the "status" node
	 * <p>
	 * Prefect fixing requires to mix-in into {@link com.mojang.brigadier.CommandDispatcher#getCompletionSuggestions},
	 * which is part of the brigadier library. Doable with modern mixin, but I don't like that.
	 * So here's comes a working but stupid fix XD
	 */
	@ModifyVariable(method = "method_14365", at = @At("HEAD"), argsOnly = true)
	private Suggestions removeTickSprintStatusSuggestionIfNotEnabled(Suggestions suggestions, RequestCommandCompletionsC2SPacket packet, Suggestions suggestions_)
	{
		String command = packet.getPartialCommand();
		if (command.startsWith("/"))
		{
			command = command.substring(1);
		}

		// yeet the "status" suggestion for "/tick sprint" (and "/tick warp", provided by rule tickWarpCommandAsAnAlias)
		if (command.startsWith("tick sprint ") || (CarpetTISAdditionSettings.tickWarpCommandAsAnAlias && command.startsWith("tick warp ")))
		{
			if (!CarpetTISAdditionSettings.tickCommandEnhance)
			{
				int spaces = StringUtils.countMatches(command, " ");
				if (spaces == 2)  // still entering the 3rd segment, might have the "status" suggestion
				{
					var filtered = suggestions.getList().stream().
							filter(s -> !s.getText().equals("status")).
							toList();
					suggestions = new Suggestions(suggestions.getRange(), filtered);
				}
			}
		}
		return suggestions;
	}
}
