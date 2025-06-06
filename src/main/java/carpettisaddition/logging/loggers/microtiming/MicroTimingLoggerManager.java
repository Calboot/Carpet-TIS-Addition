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

package carpettisaddition.logging.loggers.microtiming;

import carpettisaddition.CarpetTISAdditionSettings;
import carpettisaddition.logging.TISAdditionLoggerRegistry;
import carpettisaddition.logging.loggers.AbstractLogger;
import carpettisaddition.logging.loggers.microtiming.enums.BlockUpdateType;
import carpettisaddition.logging.loggers.microtiming.enums.EventType;
import carpettisaddition.logging.loggers.microtiming.enums.TickStage;
import carpettisaddition.logging.loggers.microtiming.events.*;
import carpettisaddition.logging.loggers.microtiming.interfaces.ServerWorldWithMicroTimingLogger;
import carpettisaddition.logging.loggers.microtiming.marker.MicroTimingMarkerManager;
import carpettisaddition.logging.loggers.microtiming.tickphase.TickPhase;
import carpettisaddition.logging.loggers.microtiming.tickphase.substages.AbstractSubStage;
import carpettisaddition.logging.loggers.microtiming.utils.MicroTimingContext;
import carpettisaddition.logging.loggers.microtiming.utils.MicroTimingUtil;
import carpettisaddition.translations.Translator;
import carpettisaddition.utils.ItemUtils;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.BlockAction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.text.BaseText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ScheduledTick;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//#if MC >= 11600
//$$ import carpettisaddition.script.MicroTimingEvent;
//$$ import com.google.common.collect.Sets;
//$$ import java.util.Set;
//$$ import java.util.function.Supplier;
//#endif

public class MicroTimingLoggerManager
{
	private static MicroTimingLoggerManager instance;

	private final Map<ServerWorld, MicroTimingLogger> loggers = new Reference2ObjectArrayMap<>();
	public static final Translator TRANSLATOR = (new AbstractLogger(MicroTimingLogger.NAME, false){}).getTranslator();
	private TickPhase offWorldTickPhase = new TickPhase(TickStage.UNKNOWN, null);
	public ThreadLocal<ServerWorld> currentWorld = ThreadLocal.withInitial(() -> null);

	//#if MC >= 11600
	//$$ // for scarpet event
	//$$ public static final Set<BlockPos> trackedPositions = Sets.newHashSet();
	//#endif

	public static BaseText tr(String key, Object... args)
	{
		return TRANSLATOR.tr(key, args);
	}

	public MicroTimingLoggerManager(MinecraftServer minecraftServer)
	{
		for (ServerWorld world : minecraftServer.getWorlds())
		{
			this.loggers.put(world, ((ServerWorldWithMicroTimingLogger)world).getMicroTimingLogger());
		}
	}

	public Map<ServerWorld, MicroTimingLogger> getLoggers()
	{
		return this.loggers;
	}

	public static boolean isLoggerActivated()
	{
		// make sure it's available first
		if (CarpetTISAdditionSettings.microTiming && instance != null)
		{
			//#if MC >= 11600
			//$$ if (!trackedPositions.isEmpty())
			//$$ {
			//$$ 	return true;
			//$$ }
			//#endif

			// has subscriber
			return TISAdditionLoggerRegistry.__microTiming;
		}
		return false;
	}

	public static void attachServer(MinecraftServer minecraftServer)
	{
		instance = new MicroTimingLoggerManager(minecraftServer);
	}

	public static void detachServer()
	{
		instance = null;
	}

	@NotNull
	public static MicroTimingLoggerManager getInstance()
	{
		if (instance == null)
		{
			throw new RuntimeException("MicroTimingLoggerManager not attached");
		}
		return instance;
	}

	private static Optional<MicroTimingLogger> getWorldLogger(World world)
	{
		if (instance != null && world instanceof ServerWorld)
		{
			return Optional.of(((ServerWorldWithMicroTimingLogger)world).getMicroTimingLogger());
		}
		return Optional.empty();
	}

	/*
	 * -------------------------
	 *  General Event Operation
	 * -------------------------
	 */

	//#if MC >= 11600
	//$$ public static void dispatchScarpetEvent(World world, BlockPos pos, Supplier<BaseEvent> supplier)
	//$$ {
	//$$ 	if (CarpetTISAdditionSettings.microTiming)
	//$$ 	{
	//$$ 		// For scarpet, checking if it's a block tracked by the scarpet bit. Separate from the rest cos idk how to works
	//$$ 		if (trackedPositions.contains(pos))
	//$$ 		{
	//$$ 			MicroTimingEvent.determineBlockEvent(supplier.get(), world, pos);
	//$$ 		}
	//$$ 	}
	//$$ }
	//#endif

	private static void onEvent(MicroTimingContext context)
	{
		//#if MC >= 11600
		//$$ dispatchScarpetEvent(context.getWorld(), context.getBlockPos(), context.getEventSupplier());
		//#endif

		getWorldLogger(context.getWorld()).ifPresent(logger -> logger.addMessage(context));
	}

	/*
	 * ----------------------------------
	 *  Block Update and Block Operation
	 * ----------------------------------
	 */

	public static void onScheduleBlockUpdate(World world, BlockPos pos, Block sourceBlock, BlockUpdateType updateType, Direction exceptSide)
	{
		if (!isLoggerActivated())
		{
			return;
		}
		onEvent(MicroTimingContext.create().
				withWorld(world).withBlockPos(pos).
				withEventSupplier(() -> new ScheduleBlockUpdateEvent(sourceBlock, updateType, exceptSide)).
				withWoolGetter(MicroTimingUtil::blockUpdateColorGetter)
		);
	}

	public static void onBlockUpdate(World world, BlockPos pos, Block sourceBlock, BlockUpdateType updateType, Direction exceptSide, EventType eventType)
	{
		if (!isLoggerActivated())
		{
			return;
		}
		onEvent(MicroTimingContext.create().
				withWorld(world).withBlockPos(pos).
				withEventSupplier(() -> new DetectBlockUpdateEvent(eventType, sourceBlock, updateType, exceptSide)).
				withWoolGetter(MicroTimingUtil::blockUpdateColorGetter)
		);
	}
	
	public static void onSetBlockState(World world, BlockPos pos, BlockState oldState, BlockState newState, Boolean returnValue, int flags, EventType eventType)
	{
		if (!isLoggerActivated())
		{
			return;
		}
		if (oldState.getBlock() == newState.getBlock())
		{
			// lazy loading
			DyeColor color = null;
			List<BlockStateChangeEvent.PropertyChange> changes = Lists.newArrayList();

			for (Property<?> property: newState.getProperties())
			{
				if (color == null)
				{
					Optional<DyeColor> optionalDyeColor = MicroTimingUtil.defaultColorGetter(world, pos);
					if (!optionalDyeColor.isPresent())
					{
						break;
					}
					color = optionalDyeColor.get();
				}
				if (oldState.get(property) != newState.get(property))
				{
					changes.add(new BlockStateChangeEvent.PropertyChange(property, oldState.get(property), newState.get(property)));
				}
			}
			if (!changes.isEmpty())
			{
				onEvent(MicroTimingContext.create().
						withWorld(world).withBlockPos(pos).withColor(color).
						withEventSupplier(() -> {
							BlockStateChangeEvent event = new BlockStateChangeEvent(eventType, oldState, newState, returnValue, flags);
							event.setChanges(changes);
							return event;
						})
				);
			}
		}
		else
		{
			onEvent(MicroTimingContext.create().
					withWorld(world).withBlockPos(pos).
					withEventSupplier(() -> new BlockReplaceEvent(eventType, oldState, newState, returnValue, flags)).
					withWoolGetter(MicroTimingUtil::defaultColorGetter)
			);
		}
	}

	/*
	 * -----------
	 *  Tile Tick
	 * -----------
	 */

	public static void onExecuteTileTickEvent(World world, ScheduledTick<?> tileTickEvent, EventType eventType)
	{
		if (!isLoggerActivated())
		{
			return;
		}

		BlockPos pos =
				//#if MC >= 11800
				//$$ tileTickEvent.pos();
				//#else
				tileTickEvent.pos;
				//#endif

		ExecuteTileTickEvent.createFrom(eventType, tileTickEvent).ifPresent(event -> onEvent(
				MicroTimingContext.create().
						withWorld(world).withBlockPos(pos).
						withEvent(event)
		));
	}

	public static void onScheduleTileTickEvent(World world, Object object, BlockPos pos, int delay, TickPriority priority, Boolean success)
	{
		if (!isLoggerActivated())
		{
			return;
		}
		EventSource.fromObject(object).ifPresent(eventSource -> onEvent(
				MicroTimingContext.create().
						withWorld(world).withBlockPos(pos).
						withEvent(new ScheduleTileTickEvent(eventSource, delay, priority, success))
		));
	}

	/*
	 * -------------
	 *  Block Event
	 * -------------
	 */

	public static void onExecuteBlockEvent(World world, BlockAction blockAction, Boolean returnValue, ExecuteBlockEventEvent.FailInfo failInfo, EventType eventType)
	{
		if (!isLoggerActivated())
		{
			return;
		}
		onEvent(MicroTimingContext.create().
				withWorld(world).withBlockPos(blockAction.getPos()).
				withEvent(new ExecuteBlockEventEvent(eventType, blockAction, returnValue, failInfo))
		);
	}

	public static void onScheduleBlockEvent(World world, BlockAction blockAction, boolean success)
	{
		if (!isLoggerActivated())
		{
			return;
		}
		onEvent(MicroTimingContext.create().
				withWorld(world).withBlockPos(blockAction.getPos()).
				withEvent(new ScheduleBlockEventEvent(blockAction, success))
		);
	}

	/*
	 * ------------------
	 *  Component things
	 * ------------------
	 */

	public static void onEmitBlockUpdate(World world, Block block, BlockPos pos, EventType eventType, String methodName)
	{
		if (!isLoggerActivated())
		{
			return;
		}
		onEvent(MicroTimingContext.create().
				withWorld(world).withBlockPos(pos).
				withEvent(new EmitBlockUpdateEvent(eventType, block, methodName))
		);
	}

	public static void onEmitBlockUpdateRedstoneDust(World world, Block block, BlockPos pos, EventType eventType, String methodName, Collection<BlockPos> updateOrder)
	{
		if (!isLoggerActivated())
		{
			return;
		}
		onEvent(MicroTimingContext.create().
				withWorld(world).withBlockPos(pos).
				withEvent(new EmitBlockUpdateRedstoneDustEvent(eventType, block, methodName, pos, updateOrder))
		);
	}

	/*
	 * --------------------
	 *  Tick Stage / Phase
	 * --------------------
	 */

	public static void setTickStage(World world, @NotNull TickStage stage)
	{
		getWorldLogger(world).ifPresent(logger -> logger.setTickStage(stage));
	}

	public static void setTickStage(@NotNull TickStage stage)
	{
		if (instance != null)
		{
			for (MicroTimingLogger logger : instance.loggers.values())
			{
				logger.setTickStage(stage);
			}
			instance.offWorldTickPhase = instance.offWorldTickPhase.withMainStage(stage);
		}
	}

	public static void setTickStageDetail(World world, @Nullable String detail)
	{
		getWorldLogger(world).ifPresent(logger -> logger.setTickStageDetail(detail));
	}

	public static void setSubTickStage(World world, AbstractSubStage stage)
	{
		getWorldLogger(world).ifPresent(logger -> logger.setSubTickStage(stage));
	}

	public static void setSubTickStage(AbstractSubStage stage)
	{
		if (instance != null)
		{
			for (MicroTimingLogger logger : instance.loggers.values())
			{
				logger.setSubTickStage(stage);
			}
			instance.offWorldTickPhase = instance.offWorldTickPhase.withSubStage(stage);
		}
	}

	private synchronized void flush()
	{
		for (MicroTimingLogger logger : this.loggers.values())
		{
			logger.flushMessages();
		}
	}

	/*
	 * ----------------
	 *     For API
	 * ----------------
	 */

	public static void setCurrentWorld(ServerWorld world)
	{
		getInstance().currentWorld.set(world);
	}

	@Nullable
	public static ServerWorld getCurrentWorld()
	{
		return getInstance().currentWorld.get();
	}

	@Nullable
	public static TickPhase getOffWorldTickPhase()
	{
		return getInstance().offWorldTickPhase;
	}

	/**
	 * Invoke this at the end of a "gametick", or right before the first thing in the next "gametick" happens
	 */
	public static void flushMessages()
	{
		if (instance != null && isLoggerActivated())
		{
			instance.flush();
		}
	}

	/*
	 * ----------------
	 *   Marker Logic
	 * ----------------
	 */

	public static boolean onPlayerRightClick(PlayerEntity playerEntity, Hand hand, BlockPos blockPos)
	{
		if (MicroTimingUtil.isMarkerEnabled() && playerEntity instanceof ServerPlayerEntity && hand == Hand.MAIN_HAND && MicroTimingUtil.isPlayerSubscribed(playerEntity))
		{
			ItemStack itemStack = playerEntity.getMainHandStack();
			Item holdingItem = itemStack.getItem();
			if (holdingItem instanceof DyeItem)
			{
				BaseText name = null;
				if (ItemUtils.hasCustomName(itemStack))
				{
					name = (BaseText)itemStack.getName();
				}
				// server-side check will be in addMarker
				MicroTimingMarkerManager.getInstance().addMarker(playerEntity, blockPos, ((DyeItem)holdingItem).getColor(), name);
				return true;
			}
			if (holdingItem == Items.SLIME_BALL)
			{
				return MicroTimingMarkerManager.getInstance().tweakMarkerMobility(playerEntity, blockPos);
			}
		}
		return false;
	}

	public static void moveMarker(World world, BlockPos blockPos, Direction direction)
	{
		if (MicroTimingUtil.isMarkerEnabled())
		{
			MicroTimingMarkerManager.getInstance().moveMarker(world, blockPos, direction);
		}
	}
}
