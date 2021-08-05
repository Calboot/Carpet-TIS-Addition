package carpettisaddition.commands.info;

import carpet.utils.Messenger;
import carpettisaddition.CarpetTISAdditionServer;
import carpettisaddition.commands.AbstractCommand;
import carpettisaddition.commands.CommandExtender;
import carpettisaddition.mixins.command.info.ServerWorldAccessor;
import carpettisaddition.utils.TextUtil;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.BlockAction;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.BaseText;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ScheduledTick;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static net.minecraft.server.command.CommandManager.literal;

public class InfoCommand extends AbstractCommand implements CommandExtender
{
	private static final InfoCommand INSTANCE = new InfoCommand();

	public static InfoCommand getInstance()
	{
		return INSTANCE;
	}

	public InfoCommand()
	{
		super("info");
	}

	@Override
	public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher)
	{
	}

	public void extendCommand(LiteralArgumentBuilder<ServerCommandSource> builder)
	{
		builder.then(
				literal("world").
				then(
						literal("ticking_order").
						executes((c) -> showWorldTickOrder(c.getSource()))
				)
		);
	}

	private int showWorldTickOrder(ServerCommandSource source)
	{
		List<World> worlds = Lists.newArrayList(CarpetTISAdditionServer.minecraft_server.getWorlds());
		Messenger.m(source, Messenger.c(
				String.format("w %s", String.format(tr("ticking_order", "Ticking order of %d dimensions in the game:"), worlds.size()))
		));
		int order = 0;
		for (World world : worlds)
		{
			order++;
			Messenger.m(source, Messenger.c(
					"g " + order + ". ",
					TextUtil.getDimensionNameText(world.getDimension().getType())
			));
		}
		return 1;
	}

	private <T> void appendTileTickInfo(List<BaseText> result, List<ScheduledTick<T>> tileTickList, String title, long currentTime, Function<T, BaseText> nameGetter)
	{
		if (!tileTickList.isEmpty())
		{
			result.add(Messenger.s(String.format(" - %s * %d", title, tileTickList.size())));
			for (ScheduledTick<T> tt : tileTickList)
			{
				result.add(Messenger.c(
						"w     ",
						nameGetter.apply(tt.getObject()),
						String.format("w : time = %d (+%dgt), priority = %d", tt.time, tt.time - currentTime, tt.priority.getIndex())
				));
			}
		}
	}

	private void appendBlockEventInfo(List<BaseText> result, List<BlockAction> blockEvents)
	{
		if (!blockEvents.isEmpty())
		{
			result.add(Messenger.s(" - Queued Block Events * " + blockEvents.size()));
			for (BlockAction be : blockEvents)
			{
				result.add(Messenger.c(
						"w     ",
						TextUtil.getBlockName(be.getBlock()),
						String.format("w : id = %d, param = %d", be.getType(), be.getData()))
				);
			}
		}
	}

	public Collection<BaseText> showMoreBlockInfo(BlockPos pos, World world)
	{
		if (!(world instanceof ServerWorld))
		{
			return Collections.emptyList();
		}
		List<BaseText> result = Lists.newArrayList();
		BlockBox bound = new BlockBox(pos, pos.add(1, 1, 1));
		List<ScheduledTick<Block>> blockTileTicks = ((ServerTickScheduler<Block>)world.getBlockTickScheduler()).getScheduledTicks(bound, false, false);
		List<ScheduledTick<Fluid>> liquidTileTicks = ((ServerTickScheduler<Fluid>)world.getFluidTickScheduler()).getScheduledTicks(bound, false, false);
		this.appendTileTickInfo(result, blockTileTicks, "Block Tile ticks", world.getTime(), TextUtil::getBlockName);
		this.appendTileTickInfo(result, liquidTileTicks, "Fluid Tile ticks", world.getTime(), TextUtil::getFluidName);
		List<BlockAction> blockEvents = ((ServerWorldAccessor)world).getPendingBlockActions().stream().filter(be -> be.getPos().equals(pos)).toList();
		this.appendBlockEventInfo(result, blockEvents);
		return result;
	}
}
