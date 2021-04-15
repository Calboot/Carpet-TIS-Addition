package carpettisaddition.logging.loggers.microtiming.events;

import carpet.utils.Messenger;
import carpettisaddition.logging.loggers.microtiming.enums.EventType;
import carpettisaddition.logging.loggers.microtiming.utils.MicroTimingUtil;
import carpettisaddition.utils.TextUtil;
import net.minecraft.server.world.BlockAction;
import net.minecraft.text.BaseText;

import java.util.Objects;

public class ScheduleBlockEventEvent extends BaseEvent
{
	private final BlockAction blockAction;
	private final boolean success;

	public ScheduleBlockEventEvent(BlockAction blockAction, boolean success)
	{
		super(EventType.EVENT, "schedule_block_event", blockAction.getBlock());
		this.blockAction = blockAction;
		this.success = success;
	}

	@Override
	public BaseText toText()
	{
		return Messenger.c(
				COLOR_ACTION + this.tr("Scheduled"),
				TextUtil.getSpaceText(),
				COLOR_TARGET + this.tr("BlockEvent"),
				ExecuteBlockEventEvent.getMessageExtraMessengerHoverText(blockAction),
				TextUtil.getSpaceText(),
				MicroTimingUtil.getSuccessText(this.success, false)
		);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof ScheduleBlockEventEvent)) return false;
		if (!super.equals(o)) return false;
		ScheduleBlockEventEvent that = (ScheduleBlockEventEvent) o;
		return success == that.success &&
				Objects.equals(blockAction, that.blockAction);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), blockAction);
	}
}
