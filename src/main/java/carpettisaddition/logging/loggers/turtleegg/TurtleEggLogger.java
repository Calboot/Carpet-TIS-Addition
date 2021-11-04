package carpettisaddition.logging.loggers.turtleegg;

import carpet.logging.LoggerRegistry;
import carpettisaddition.logging.TISAdditionLoggerRegistry;
import carpettisaddition.logging.loggers.AbstractLogger;
import carpettisaddition.utils.DimensionWrapper;
import carpettisaddition.utils.Messenger;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.text.BaseText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TurtleEggLogger extends AbstractLogger
{
	public static final String NAME = "turtleEgg";

	private static final TurtleEggLogger INSTANCE = new TurtleEggLogger();

	private TurtleEggLogger()
	{
		super(NAME);
	}

	public static TurtleEggLogger getInstance()
	{
		return INSTANCE;
	}

	public boolean isActivated()
	{
		return TISAdditionLoggerRegistry.__turtleEgg;
	}

	public void onBreakingEgg(World world, BlockPos pos, BlockState state, Entity entity)
	{
		if (world.isClient)
		{
			return;
		}
		LoggerRegistry.getLogger(NAME).log(() -> {
			// [O] xxx breaks egg @ {}
			return new BaseText[]{Messenger.c(
					entity != null ? Messenger.entity(null, entity) : Messenger.s("?"),
					"r  x ",
					Messenger.block(state.getBlock()),
					"g  @ ",
					Messenger.coord(null, pos, DimensionWrapper.of(world))
			)};
		});
	}
}
