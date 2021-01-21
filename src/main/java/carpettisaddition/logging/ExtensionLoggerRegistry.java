package carpettisaddition.logging;

import carpet.logging.Logger;
import carpettisaddition.CarpetTISAdditionServer;
import carpettisaddition.logging.loggers.commandblock.CommandBlockLogger;
import carpettisaddition.logging.loggers.entity.ItemLogger;
import carpettisaddition.logging.loggers.entity.XPOrbLogger;
import carpettisaddition.logging.loggers.microtiming.utils.MicroTimingStandardCarpetLogger;


public class ExtensionLoggerRegistry
{
    public static boolean __ticket;
    public static boolean __memory;
    public static boolean __item;
    public static boolean __xporb;
    public static boolean __raid;
    public static boolean __microTiming;
    public static boolean __damage;
    public static boolean __commandBlock;

    public static void registerLoggers()
    {
        LoggerRegistry.registerLogger(
                "ticket", standardLogger("ticket", "portal", new String[]{
                        "portal,player", "portal,dragon", "start", "dragon", "player", "forced", "light", "portal", "post_teleport", "unknown"
                }
        ));
        LoggerRegistry.registerLogger(ItemLogger.getInstance().getLoggerName(), ItemLogger.getInstance().getStandardLogger());
        LoggerRegistry.registerLogger(XPOrbLogger.getInstance().getLoggerName(), XPOrbLogger.getInstance().getStandardLogger());
        LoggerRegistry.registerLogger("raid", standardLogger("raid", null, null));
        LoggerRegistry.registerLogger("memory", standardHUDLogger("memory", null, null));
        LoggerRegistry.registerLogger(MicroTimingStandardCarpetLogger.NAME, MicroTimingStandardCarpetLogger.create());
        LoggerRegistry.registerLogger("damage", standardLogger("damage", "all", new String[]{"all", "players", "me"}));
        LoggerRegistry.registerLogger(CommandBlockLogger.NAME, standardLogger(CommandBlockLogger.NAME, "throttled", new String[]{"throttled", "all"}));
    }

    public static Logger standardLogger(String logName, String def, String[] options)
	{
        try
        {
            return new ExtensionLogger(ExtensionLoggerRegistry.class.getField("__" + logName), logName, def, options);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(String.format("Failed to create standard logger \"%s\" @ %s", logName, CarpetTISAdditionServer.fancyName));
        }
    }

    private static Logger standardHUDLogger(String logName, String def, String [] options)
    {
        try
        {
            return new ExtensionHUDLogger(ExtensionLoggerRegistry.class.getField("__" + logName), logName, def, options);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(String.format("Failed to create standard HUD logger \"%s\" @ %s", logName, CarpetTISAdditionServer.fancyName));
        }
    }
}
