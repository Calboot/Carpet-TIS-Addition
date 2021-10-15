package carpettisaddition.mixins.logger.microtiming.tickstages.console;

import carpettisaddition.logging.loggers.microtiming.MicroTimingLoggerManager;
import carpettisaddition.logging.loggers.microtiming.enums.TickStage;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MinecraftDedicatedServerMixin
{
	@Inject(method = "executeQueuedCommands", at = @At("HEAD"))
	private void enterStageConsole(CallbackInfo ci)
	{
		MicroTimingLoggerManager.setTickStage(TickStage.CONSOLE);
	}

	@Inject(method = "executeQueuedCommands", at = @At("TAIL"))
	private void exitStageConsole(CallbackInfo ci)
	{
		MicroTimingLoggerManager.setTickStage(TickStage.UNKNOWN);
	}
}
