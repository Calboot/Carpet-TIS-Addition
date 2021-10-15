package carpettisaddition.logging.loggers.microtiming.tickphase.substages;

import carpet.utils.Messenger;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.BaseText;

public class PlayerEntitySubStage extends AbstractPlayerRelatedSubStage
{
	public PlayerEntitySubStage(ServerPlayerEntity player)
	{
		super(player);
	}

	@Override
	public BaseText toText()
	{
		return Messenger.c(
				String.format("w %s\n", this.tr("Ticking player entity")),
				super.toText()
		);
	}
}
