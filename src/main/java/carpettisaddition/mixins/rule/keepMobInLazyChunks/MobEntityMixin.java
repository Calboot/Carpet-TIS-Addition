package carpettisaddition.mixins.rule.keepMobInLazyChunks;

import carpettisaddition.CarpetTISAdditionSettings;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity
{
	protected MobEntityMixin(EntityType<? extends LivingEntity> entityType_1, World world_1)
	{
		super(entityType_1, world_1);
	}

    //#if MC >= 11500
	@Inject(
            method = "tickNewAi",
            at = @At(
                    value = "INVOKE_STRING",
                    target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V",
                    args = "ldc=sensing"
            )
    )
	private void keepMobInLazyChunks_reintroduce114CheckDespawnLogic(CallbackInfo ci)
	{
		if (CarpetTISAdditionSettings.keepMobInLazyChunks)
		{
			this.checkDespawn();
		}
	}
    //#endif
}
