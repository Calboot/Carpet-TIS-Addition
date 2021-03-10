package carpettisaddition.logging.loggers.entity;

import carpet.utils.Messenger;
import carpettisaddition.logging.TISAdditionLoggerRegistry;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.text.BaseText;


public class XPOrbLogger extends EntityLogger<ExperienceOrbEntity>
{
	private static final XPOrbLogger INSTANCE = new XPOrbLogger();

	public XPOrbLogger()
	{
		super("xporb");
	}

	public static XPOrbLogger getInstance()
	{
		return INSTANCE;
	}

	@Override
	protected BaseText getNameTextHoverText(ExperienceOrbEntity xp)
	{
		return Messenger.s(String.format("%s: %d", tr("XP amount"), xp.getExperienceAmount()));
	}

	@Override
	protected boolean getAcceleratorBoolean()
	{
		return TISAdditionLoggerRegistry.__xporb;
	}
}
