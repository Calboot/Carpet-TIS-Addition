package carpettisaddition.commands.manipulate.container;

import carpettisaddition.utils.Messenger;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.server.command.CommandManager.literal;

public abstract class AbstractEntityListController extends AbstractContainerController
{
	public AbstractEntityListController(String translationName)
	{
		super(translationName);
	}

	protected abstract int processWholeList(ServerWorld world, Consumer<List<?>> collectionOperator);

	public int revert(ServerCommandSource source)
	{
		return this.manipulate(source, () -> {
			int size = this.processWholeList(source.getWorld(), Collections::reverse);
			Messenger.tell(source, tr("revert", this.getName(), size));
		});
	}

	public int shuffle(ServerCommandSource source)
	{
		return this.manipulate(source, () -> {
			int size = this.processWholeList(source.getWorld(), Collections::shuffle);
			Messenger.tell(source, tr("shuffle", this.getName(), size));
		});
	}

	protected abstract boolean canManipulate(ServerWorld world);

	protected int manipulate(ServerCommandSource source, Runnable task)
	{
		if (this.canManipulate(source.getWorld()))
		{
			task.run();
			return 1;
		}
		else
		{
			Messenger.tell(source, basicTranslator.tr("cannot_manipulate", this.getName()));
		}
		return 0;
	}

	@Override
	public ArgumentBuilder<ServerCommandSource, ?> getCommandNode()
	{
		return super.getCommandNode().
				then(literal("shuffle").executes(c -> this.shuffle(c.getSource()))).
				then(literal("revert").executes(c -> this.revert(c.getSource())));
	}
}
