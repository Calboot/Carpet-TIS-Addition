package carpettisaddition.mixins.command.info.entity;

import net.minecraft.entity.mob.SlimeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SlimeEntity.class)
public interface SlimeEntityAccessor
{
	@Invoker
	//#if MC >= 11500
	float invokeGetDamageAmount();
	//#else
	//$$ int invokeGetDamageAmount();
	//#endif
}
