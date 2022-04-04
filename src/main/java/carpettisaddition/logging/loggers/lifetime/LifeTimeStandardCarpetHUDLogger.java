package carpettisaddition.logging.loggers.lifetime;

import carpet.logging.HUDLogger;
import carpettisaddition.commands.lifetime.LifeTimeTracker;
import carpettisaddition.commands.lifetime.utils.LifeTimeTrackerUtil;
import carpettisaddition.logging.TISAdditionLoggerRegistry;
import net.minecraft.entity.player.PlayerEntity;

public class LifeTimeStandardCarpetHUDLogger extends HUDLogger
{
	public LifeTimeStandardCarpetHUDLogger()
	{
		super(TISAdditionLoggerRegistry.getLoggerField(LifeTimeHUDLogger.NAME), LifeTimeHUDLogger.NAME, null, null, false);
	}

	@Override
	public void addPlayer(String playerName, String option)
	{
		super.addPlayer(playerName, option);
		PlayerEntity player = this.playerFromName(playerName);
		if (player != null)
		{
			if (!LifeTimeTrackerUtil.getEntityTypeFromName(option).isPresent())
			{
				LifeTimeTracker.getInstance().sendUnknownEntity(player.getCommandSource(), option);
			}
		}
	}

	@Override
	public String[] getOptions()
	{
		return LifeTimeTracker.getInstance().getAvailableEntityType().toArray(String[]::new);
	}
}
