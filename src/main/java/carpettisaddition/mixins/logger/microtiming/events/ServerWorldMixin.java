package carpettisaddition.mixins.logger.microtiming.events;

import carpettisaddition.logging.loggers.microtiming.MicroTimingLoggerManager;
import carpettisaddition.logging.loggers.microtiming.enums.EventType;
import carpettisaddition.logging.loggers.microtiming.events.ExecuteBlockEventEvent;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.world.BlockAction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ScheduledTick;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin
{
	/*
	 * -----------
	 *  Tile Tick
	 * -----------
	 */

	private final ThreadLocal<Boolean> thisTileTickEventExecuted = ThreadLocal.withInitial(() -> false);

	@Inject(method = {"tickBlock", "tickFluid"}, at = @At("HEAD"))
	private void beforeExecuteTileTickEvent(CallbackInfo ci)
	{
		this.thisTileTickEventExecuted.set(false);
	}

	@Inject(
			method = "tickBlock",
			at = @At(
					value = "INVOKE",
					//#if MC >= 11500
					target = "Lnet/minecraft/block/BlockState;scheduledTick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V"
					//#else
					//$$ target = "Lnet/minecraft/block/BlockState;scheduledTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V"
					//#endif
			)
	)
	private void preExecuteBlockTileTickEvent(ScheduledTick<Block> event, CallbackInfo ci)
	{
		MicroTimingLoggerManager.onExecuteTileTickEvent((ServerWorld)(Object)this, event, EventType.ACTION_START);
		this.thisTileTickEventExecuted.set(true);
	}

	@Inject(
			method = "tickFluid",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/fluid/FluidState;onScheduledTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"
			)
	)
	private void preExecuteFluidTileTickEvent(ScheduledTick<Fluid> event, CallbackInfo ci)
	{
		MicroTimingLoggerManager.onExecuteTileTickEvent((ServerWorld)(Object)this, event, EventType.ACTION_START);
		this.thisTileTickEventExecuted.set(true);
	}

	@Inject(method = {"tickBlock", "tickFluid"}, at = @At("RETURN"))
	private void afterExecuteTileTickEvent(ScheduledTick<?> event, CallbackInfo ci)
	{
		if (this.thisTileTickEventExecuted.get())
		{
			MicroTimingLoggerManager.onExecuteTileTickEvent((ServerWorld) (Object) this, event, EventType.ACTION_END);
		}
	}

	/*
	 * -------------
	 *  Block Event
	 * -------------
	 */

	@Shadow @Final private ObjectLinkedOpenHashSet<BlockAction> pendingBlockActions;

	private int oldBlockActionQueueSize;

	@Inject(method = "addBlockAction", at = @At("HEAD"))
	private void startScheduleBlockEvent(BlockPos pos, Block block, int type, int data, CallbackInfo ci)
	{
		this.oldBlockActionQueueSize = this.pendingBlockActions.size();
	}

	@Inject(method = "addBlockAction", at = @At("RETURN"))
	private void endScheduleBlockEvent(BlockPos pos, Block block, int type, int data, CallbackInfo ci)
	{
		MicroTimingLoggerManager.onScheduleBlockEvent((ServerWorld)(Object)this, new BlockAction(pos, block, type, data), this.pendingBlockActions.size() > this.oldBlockActionQueueSize);
	}

	@Inject(method = "method_14174", at = @At(value = "HEAD", shift = At.Shift.AFTER))
	private void beforeBlockEventExecuted(BlockAction blockAction, CallbackInfoReturnable<Boolean> cir)
	{
		MicroTimingLoggerManager.onExecuteBlockEvent((ServerWorld)(Object)this, blockAction, null, null, EventType.ACTION_START);
	}

	@Inject(method = "method_14174", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void afterBlockEventExecuted(BlockAction blockAction, CallbackInfoReturnable<Boolean> cir, BlockState blockState)
	{
		ExecuteBlockEventEvent.FailInfo failInfo = new ExecuteBlockEventEvent.FailInfo(blockState.getBlock() != blockAction.getBlock() ? ExecuteBlockEventEvent.FailReason.BLOCK_CHANGED : ExecuteBlockEventEvent.FailReason.EVENT_FAIL, blockState.getBlock());
		MicroTimingLoggerManager.onExecuteBlockEvent((ServerWorld)(Object)this, blockAction, cir.getReturnValue(), failInfo, EventType.ACTION_END);
	}
}
