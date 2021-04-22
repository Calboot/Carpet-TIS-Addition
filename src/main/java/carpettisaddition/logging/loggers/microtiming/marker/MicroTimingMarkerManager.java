package carpettisaddition.logging.loggers.microtiming.marker;

import carpet.utils.Messenger;
import carpettisaddition.logging.loggers.microtiming.MicroTimingLoggerManager;
import carpettisaddition.translations.TranslatableBase;
import carpettisaddition.utils.compact.scarpet.ShapeDispatcher;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.BaseText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MicroTimingMarkerManager extends TranslatableBase
{
	private static final MicroTimingMarkerManager INSTANCE = new MicroTimingMarkerManager();

	private final Map<StorageKey, MicroTimingMarker> markers = Maps.newHashMap();

	public MicroTimingMarkerManager()
	{
		super(MicroTimingLoggerManager.TRANSLATOR.getDerivedTranslator("marker"));
	}

	public static MicroTimingMarkerManager getInstance()
	{
		return INSTANCE;
	}

	public int clear()
	{
		this.markers.values().forEach(MicroTimingMarker::cleanShapeToAll);
		int size = this.markers.size();
		this.markers.clear();
		return size;
	}

	private static boolean checkServerSide(PlayerEntity playerEntity)
	{
		return playerEntity instanceof ServerPlayerEntity && !playerEntity.world.isClient() && playerEntity.world instanceof ServerWorld;
	}

	private void removeMarker(MicroTimingMarker marker)
	{
		marker.cleanShapeToAll();
		this.markers.remove(marker.getStorageKey());
	}

	private void addMarker(MicroTimingMarker marker)
	{
		marker.sendShapeToAll();
		StorageKey key = marker.getStorageKey();
		MicroTimingMarker existedMarker = this.markers.get(key);
		if (existedMarker != null)
		{
			this.removeMarker(existedMarker);
		}
		this.markers.put(marker.getStorageKey(), marker);
	}

	public void addMarker(PlayerEntity playerEntity, BlockPos blockPos, DyeColor color, @Nullable BaseText name)
	{
		if (checkServerSide(playerEntity))
		{
			StorageKey key = new StorageKey(playerEntity.world, blockPos);
			MicroTimingMarker existedMarker = this.markers.get(key);
			boolean removeExistedMarker = false;
			boolean createNewMarker = false;
			if (existedMarker != null)
			{
				// roll the marker type to the next one
				if (existedMarker.color == color)
				{
					// has next marker type
					if (existedMarker.rollMarkerType())
					{
						playerEntity.addChatMessage(Messenger.s(String.format(
								this.tr("on_type_switch", "Switch marker to %1$s mode"),
								existedMarker.getMarkerType().getFancyString()
						)), true);
					}
					// no more marker type, remove it
					else
					{
						removeExistedMarker = true;
					}
				}
				// color is different, just remove it and create a new one
				else
				{
					removeExistedMarker = true;
					createNewMarker = true;
				}
			}
			// no existed marker, create a new one
			else
			{
				createNewMarker = true;
			}

			if (removeExistedMarker)
			{
				this.removeMarker(existedMarker);
				playerEntity.addChatMessage(Messenger.c(
						Messenger.s(this.tr("on_unmark", "§cRemoved§r MicroTiming marker") + ": "),
						existedMarker.toFullText()
				), true);
			}
			if (createNewMarker)
			{
				MicroTimingMarker newMarker = new MicroTimingMarker((ServerWorld)playerEntity.world, blockPos, color, name);
				this.addMarker(newMarker);
				playerEntity.addChatMessage(Messenger.c(
						Messenger.s(this.tr("on_mark", "§aAdded§r MicroTiming marker: ") + ": "),
						newMarker.toFullText()
				), true);
			}
		}
	}

	public Optional<DyeColor> getColor(World world, BlockPos blockPos, MicroTimingMarkerType requiredMarkerType)
	{
		MicroTimingMarker marker = this.markers.get(new StorageKey(world, blockPos));
		if (marker == null)
		{
			return Optional.empty();
		}
		return Optional.ofNullable(marker.getMarkerType().ordinal() >= requiredMarkerType.ordinal() ? marker.color : null);
	}

	public Optional<String> getMarkerName(World world, BlockPos blockPos)
	{
		return Optional.ofNullable(this.markers.get(new StorageKey(world, blockPos))).map(MicroTimingMarker::getMarkerNameString);
	}

	private void sendMarkersForPlayerInner(ServerPlayerEntity player, boolean display)
	{
		ShapeDispatcher.sendShape(
				Collections.singletonList(player),
				this.markers.values().stream().
						flatMap(marker -> marker.getShapeDataList(display).stream()).
						collect(Collectors.toList())
		);
	}

	public void sendMarkersForPlayer(ServerPlayerEntity player)
	{
		this.sendMarkersForPlayerInner(player, true);
	}

	public void cleanMarkersForPlayer(ServerPlayerEntity player)
	{
		this.sendMarkersForPlayerInner(player, false);
	}

	/**
	 * return false if there is not a marker there, true otherwise
	 */
	public boolean tweakMarkerMobility(PlayerEntity playerEntity, BlockPos blockPos)
	{
		if (checkServerSide(playerEntity))
		{
			StorageKey key = new StorageKey(playerEntity.world, blockPos);
			MicroTimingMarker marker = this.markers.get(key);
			if (marker != null)
			{
				boolean nextState = !marker.isMovable();
				marker.setMovable(nextState);
				playerEntity.addChatMessage(nextState ?
						Messenger.c(
								Messenger.s(this.tr("on_mobility_true.pre", "Marker ")),
								marker.toShortText(),
								Messenger.s(this.tr("on_mobility_true.post", " is set to be §amovable§r"))
						) :
						Messenger.c(
								Messenger.s(this.tr("on_mobility_false.pre", "Marker ")),
								marker.toShortText(),
								Messenger.s(this.tr("on_mobility_false.post", " is set to be §cimmovable§r"))
						)
				, true);
				return true;
			}
		}
		return false;
	}

	public void moveMarker(World world, BlockPos blockPos, Direction direction)
	{
		StorageKey prevPosKey = new StorageKey(world, blockPos);
		MicroTimingMarker marker = this.markers.get(prevPosKey);
		if (marker != null && marker.isMovable())
		{
			this.removeMarker(marker);
			this.addMarker(marker.offset(direction));
		}
	}
}
