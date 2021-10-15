package carpettisaddition.mixins.carpet.tweaks.rule.tntRandomRange;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Random;

@Mixin(World.class)
public interface WorldAccessor
{
	@Mutable
	@Accessor
	void setRandom(Random random);
}
