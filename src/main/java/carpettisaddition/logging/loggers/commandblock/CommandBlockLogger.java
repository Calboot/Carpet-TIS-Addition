package carpettisaddition.logging.loggers.commandblock;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import carpettisaddition.logging.ExtensionLoggerRegistry;
import carpettisaddition.logging.loggers.AbstractLogger;
import carpettisaddition.utils.TextUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.text.BaseText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.World;

public class CommandBlockLogger extends AbstractLogger
{
	public static final String NAME = "commandBlock";
	public static final int MINIMUM_LOG_INTERVAL = 3 * 20;  // 3s
	public static final int MAXIMUM_PREVIEW_LENGTH = 16;
	private static final CommandBlockLogger INSTANCE = new CommandBlockLogger();

	public CommandBlockLogger()
	{
		super(NAME);
	}

	public static CommandBlockLogger getInstance()
	{
		return INSTANCE;
	}

	private void logCommandBlockExecution(World world, BaseText nameText, BaseText posText, CommandBlockExecutor executor)
	{
		if (!ExtensionLoggerRegistry.__commandBlock)
		{
			return;
		}

		ICommandBlockExecutor iExecutor = (ICommandBlockExecutor)executor;
		long time = world.getTime();
		String commandPreview = executor.getCommand();
		if (commandPreview.length() > MAXIMUM_PREVIEW_LENGTH)
		{
			commandPreview = commandPreview.substring(0, MAXIMUM_PREVIEW_LENGTH - 3) + "...";
		}
		String finalCommandPreview = commandPreview;

		LoggerRegistry.getLogger(NAME).log((option) -> {
			boolean isThrottledLogging = !option.equals("all");
			if (time - iExecutor.getLastLoggedTime() < MINIMUM_LOG_INTERVAL && isThrottledLogging)
			{
				return null;
			}
			if (isThrottledLogging)
			{
				iExecutor.setLastLoggedTime(time);
			}
			return new BaseText[]{Messenger.c(
					TextUtil.attachFormatting(TextUtil.copyText(nameText), Formatting.GOLD),
					TextUtil.getSpaceText(),
					"w " + this.tr("executed"),
					TextUtil.getSpaceText(),
					TextUtil.getFancyText(
							"c",
							Messenger.s(finalCommandPreview),
							Messenger.s(executor.getCommand()),
							null
					),
					"g  @ ",
					posText
			)};
		});
	}

	public void onCommandBlockActivated(World world, BlockPos pos, BlockState state, CommandBlockExecutor executor)
	{
		this.logCommandBlockExecution(
				world,
				TextUtil.getBlockName(state.getBlock()),
				TextUtil.getCoordinateText("w", pos, world.getDimension().getType()),
				executor
		);
	}

	public void onCommandBlockMinecartActivated(CommandBlockMinecartEntity entity)
	{
		this.logCommandBlockExecution(
				entity.getEntityWorld(),
				TextUtil.getEntityText(null, entity),
				TextUtil.getCoordinateText("w", entity.getPos(), entity.getEntityWorld().getDimension().getType()),
				entity.getCommandExecutor()
		);
	}
}
