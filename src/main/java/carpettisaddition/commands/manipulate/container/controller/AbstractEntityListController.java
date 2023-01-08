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

package carpettisaddition.commands.manipulate.container.controller;

import carpettisaddition.commands.CommandTreeContext;
import carpettisaddition.utils.Messenger;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.server.command.CommandManager.literal;

public abstract class AbstractEntityListController extends AbstractContainerController
{
	public AbstractEntityListController(String translationName)
	{
		super(translationName);
	}

	protected abstract int processWholeList(ServerWorld world, Consumer<List<?>> collectionOperator);

	public int revert(ServerCommandSource source)
	{
		return this.manipulate(source, () -> {
			int size = this.processWholeList(source.getWorld(), Collections::reverse);
			Messenger.tell(source, tr("revert", this.getName(), size), true);
		});
	}

	public int shuffle(ServerCommandSource source)
	{
		return this.manipulate(source, () -> {
			int size = this.processWholeList(source.getWorld(), Collections::shuffle);
			Messenger.tell(source, tr("shuffle", this.getName(), size), true);
		});
	}

	protected abstract boolean canManipulate(ServerWorld world);

	protected int manipulate(ServerCommandSource source, Runnable task)
	{
		if (this.canManipulate(source.getWorld()))
		{
			task.run();
			return 1;
		}
		else
		{
			Messenger.tell(source, basicTranslator.tr("cannot_manipulate", this.getName()));
		}
		return 0;
	}

	@Override
	public ArgumentBuilder<ServerCommandSource, ?> getCommandNode(CommandTreeContext context)
	{
		return super.getCommandNode(context).
				then(literal("shuffle").executes(c -> this.shuffle(c.getSource()))).
				then(literal("revert").executes(c -> this.revert(c.getSource())));
	}
}
