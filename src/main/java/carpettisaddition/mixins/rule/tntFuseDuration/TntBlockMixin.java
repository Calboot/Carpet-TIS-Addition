package carpettisaddition.mixins.rule.tntFuseDuration;

import net.minecraft.block.TntBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TntBlock.class)
public abstract class TntBlockMixin
{
	@ModifyArg(
			method = "onDestroyedByExplosion",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/Random;nextInt(I)I",
					remap = false
			),
			index = 0
	)
	private int makeSureItIsPositive(int bound)
	{
		return Math.max(bound, 1);
	}
}
