package carpettisaddition.logging.loggers.lifetime;

import carpet.utils.Messenger;
import carpettisaddition.commands.lifetime.LifeTimeTracker;
import carpettisaddition.commands.lifetime.LifeTimeWorldTracker;
import carpettisaddition.commands.lifetime.trackeddata.BasicTrackedData;
import carpettisaddition.commands.lifetime.utils.LifeTimeTrackerUtil;
import carpettisaddition.logging.ExtensionHUDLogger;
import carpettisaddition.logging.TISAdditionLoggerRegistry;
import carpettisaddition.logging.loggers.AbstractHUDLogger;
import carpettisaddition.utils.TextUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.BaseText;
import net.minecraft.util.Formatting;

import java.util.Optional;

/**
 * Independent of lifetime tracker
 * It only reads some data from the tracker
 */
public class LifeTimeHUDLogger extends AbstractHUDLogger
{
	public static final String NAME = "lifeTime";

	private static final LifeTimeHUDLogger INSTANCE = new LifeTimeHUDLogger();

	public LifeTimeHUDLogger()
	{
		super(NAME);
	}

	public static LifeTimeHUDLogger getInstance()
	{
		return INSTANCE;
	}

	@Override
	public BaseText[] onHudUpdate(String option, PlayerEntity playerEntity)
	{
		LifeTimeWorldTracker tracker = LifeTimeTracker.getInstance().getTracker(playerEntity.getEntityWorld());
		if (tracker != null)
		{
			Optional<EntityType<?>> entityTypeOptional = LifeTimeTrackerUtil.getEntityTypeFromName(option);
			if (entityTypeOptional.isPresent())
			{
				EntityType<?> entityType = entityTypeOptional.get();
				BasicTrackedData data = tracker.getDataMap().getOrDefault(entityType, new BasicTrackedData());
				return new BaseText[]{Messenger.c(
						TextUtil.attachFormatting(TextUtil.copyText((BaseText)entityType.getName()), Formatting.GRAY),
						"g : ",
						"e " + data.getSpawningCount(),
						"g /",
						"r " + data.getRemovalCount(),
						"w  ",
						data.lifeTimeStatistic.getCompressedResult(false)
				)};
			}
		}
		return null;
	}

	public LifeTimeStandardCarpetHUDLogger getHUDLogger()
	{
		return new LifeTimeStandardCarpetHUDLogger();
	}

	public class LifeTimeStandardCarpetHUDLogger extends ExtensionHUDLogger
	{
		public LifeTimeStandardCarpetHUDLogger()
		{
			super(TISAdditionLoggerRegistry.getLoggerField(NAME), NAME, null,null);
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
					Messenger.m(player, Messenger.s(String.format("%s: %s", LifeTimeHUDLogger.this.tr("Unknown entity type"), option)));
				}
			}
		}

		@Override
		public String[] getOptions()
		{
			return LifeTimeTracker.getInstance().getAvailableEntityType().toArray(String[]::new);
		}
	}
}
