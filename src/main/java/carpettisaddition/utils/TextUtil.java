package carpettisaddition.utils;

import carpet.utils.Messenger;
import carpettisaddition.CarpetTISAdditionServer;
import carpettisaddition.mixins.carpet.MessengerInvoker;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.BaseText;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.dimension.DimensionType;

import java.util.Map;

public class TextUtil
{
	// mojang compatibility thing <3
	// these get changed in 1.16 so for easier compatible coding just wrap these methods
	public static BaseText attachHoverEvent(BaseText text, HoverEvent hoverEvent)
	{
		text.getStyle().setHoverEvent(hoverEvent);
		return text;
	}

	public static BaseText attachHoverText(BaseText text, BaseText hoverText)
	{
		return attachHoverEvent(text, new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
	}

	public static BaseText attachClickEvent(BaseText text, ClickEvent clickEvent)
	{
		text.getStyle().setClickEvent(clickEvent);
		return text;
	}

	public static BaseText attachFormatting(BaseText text, Formatting... formattings)
	{
		text.formatted(formattings);
		return text;
	}

	public static BaseText copyText(BaseText text)
	{
		return (BaseText)text.deepCopy();
	}
	// mojang compatibility thing ends

	private static final Map<DimensionType, BaseText> DIMENSION_NAME = Maps.newHashMap();

	static
	{
		DIMENSION_NAME.put(DimensionType.OVERWORLD, new TranslatableText("createWorld.customize.preset.overworld"));
		DIMENSION_NAME.put(DimensionType.THE_NETHER, new TranslatableText("advancements.nether.root.title"));
		DIMENSION_NAME.put(DimensionType.THE_END, new TranslatableText("advancements.end.root.title"));
	}

	private static String getTeleportHint()
	{
		return "Click to teleport to";
	}

	public static String getTeleportCommand(Vec3d pos, DimensionType dimensionType)
	{
		return String.format("/execute in %s run tp %s %s %s", dimensionType, pos.getX(), pos.getY(), pos.getZ());
	}

	public static String getTeleportCommand(Vec3d pos)
	{
		return String.format("/tp %s %s %s", pos.getX(), pos.getY(), pos.getZ());
	}

	public static String getTeleportCommand(Vec3i pos)
	{
		return String.format("/tp %d %d %d", pos.getX(), pos.getY(), pos.getZ());
	}

	public static String getTeleportCommand(Vec3i pos, DimensionType dimensionType)
	{
		return String.format("/execute in %s run tp %d %d %d", dimensionType, pos.getX(), pos.getY(), pos.getZ());
	}

	public static String getTeleportCommandPlayer(PlayerEntity player)
	{
		String name = player.getGameProfile().getName();
		return String.format("/tp %s", name);
	}

	public static String getTeleportCommand(Entity entity)
	{
		if (entity instanceof PlayerEntity)
		{
			return getTeleportCommandPlayer((PlayerEntity)entity);
		}
		String uuid = entity.getUuid().toString();
		return String.format("/tp %s", uuid);
	}

	public static BaseText getFancyText(String style, BaseText displayText, BaseText hoverText, ClickEvent clickEvent)
	{
		BaseText text = copyText(displayText);
		if (style != null)
		{
			//noinspection ResultOfMethodCallIgnored
			MessengerInvoker.call_applyStyleToTextComponent(text, style);
		}
		if (hoverText != null)
		{
			attachHoverText(text, hoverText);
		}
		if (clickEvent != null)
		{
			attachClickEvent(text, clickEvent);
		}
		return text;
	}

	private static BaseText __getCoordinateText(String style, DimensionType dim, String posText, String command)
	{
		BaseText hoverText = Messenger.s("");
		hoverText.append(String.format("%s %s\n", getTeleportHint(), posText));
		hoverText.append("Dimension");
		hoverText.append(": ");
		hoverText.append(getDimensionNameText(dim));
		return getFancyText(style, Messenger.s(posText), hoverText, new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
	}

	public static String getCoordinateString(Vec3d pos)
	{
		return String.format("[%.1f, %.1f, %.1f]", pos.getX(), pos.getY(), pos.getZ());
	}

	public static String getCoordinateString(Vec3i pos)
	{
		return String.format("[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ());
	}

	public static BaseText getCoordinateText(String style, Vec3d pos, DimensionType dim)
	{
		return __getCoordinateText(style, dim, getCoordinateString(pos), getTeleportCommand(pos, dim));
	}

	public static BaseText getCoordinateText(String style, Vec3i pos, DimensionType dim)
	{
		return __getCoordinateText(style, dim, getCoordinateString(pos), getTeleportCommand(pos, dim));
	}

	public static BaseText getEntityText(String style, Entity entity)
	{
		BaseText entityName = copyText((BaseText)entity.getType().getName());
		BaseText hoverText = Messenger.c("w " + getTeleportHint(), getSpaceText(), entityName);
		return getFancyText(style, entityName, hoverText, new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, getTeleportCommand(entity)));
	}

	public static BaseText getAttributeText(EntityAttribute attribute)
	{
		return new TranslatableText("attribute.name." + attribute.getId());
	}

	public static BaseText getDimensionNameText(DimensionType dim)
	{
		return copyText(DIMENSION_NAME.getOrDefault(dim, Messenger.s(dim.toString())));
	}

	public static TranslatableText getTranslatedName(String key, Formatting color, Object... args)
	{
		TranslatableText text = new TranslatableText(key, args);
		if (color != null)
		{
			attachFormatting(text, color);
		}
		return text;
	}

	public static TranslatableText getTranslatedName(String key, Object... args)
	{
		return getTranslatedName(key, null, args);
	}

	public static BaseText getBlockName(Block block)
	{
		return TextUtil.attachFormatting(new TranslatableText(block.getTranslationKey()), Formatting.WHITE);
	}

	// some language doesn't use space char to divide word
	// so here comes the compatibility
	public static String getSpace()
	{
		return " ";
	}

	public static BaseText getSpaceText()
	{
		return Messenger.s(getSpace());
	}
}
