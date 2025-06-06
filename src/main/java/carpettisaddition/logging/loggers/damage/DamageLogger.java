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

package carpettisaddition.logging.loggers.damage;

import carpettisaddition.logging.TISAdditionLoggerRegistry;
import carpettisaddition.logging.loggers.AbstractLogger;
import carpettisaddition.logging.loggers.damage.interfaces.DamageLoggerTarget;
import carpettisaddition.logging.loggers.damage.modifyreasons.Modification;
import carpettisaddition.logging.loggers.damage.modifyreasons.ModifyReason;
import carpettisaddition.utils.Messenger;
import carpettisaddition.utils.TextUtils;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.BaseText;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

//#if MC >= 11904
//$$ import net.minecraft.registry.tag.DamageTypeTags;
//#endif

public class DamageLogger extends AbstractLogger
{
	public static final String NAME = "damage";
	private static final DamageLogger INSTANCE = new DamageLogger();

	private DamageLogger()
	{
		super(NAME, false);
	}

	public static DamageLogger getInstance()
	{
		return INSTANCE;
	}

	@Override
	public @Nullable String[] getSuggestedLoggingOption()
	{
		return new String[]{"all", "players", "me", "->creeper", "vex->", "me->zombie", "hotFloor->zombie", "Steve", "@e[distance=..10]"};
	}

	public static boolean isLoggerActivated()
	{
		return TISAdditionLoggerRegistry.__damage;
	}

	public static void create(LivingEntity entity, DamageSource source, float amount)
	{
		if (isLoggerActivated())
		{
			DamageLoggerTarget damageLoggerTarget = (DamageLoggerTarget)entity;
			if (!damageLoggerTarget.getDamageTracker().isPresent())
			{
				damageLoggerTarget.setDamageTracker(getInstance().new Tracker(entity, source, amount));
			}
		}
	}

	public class Tracker
	{
		private final LivingEntity entity;
		private final float initialHealth;
		private final DamageSource damageSource;
		private final float initialAmount;
		private float currentAmount;
		private final List<Modification> modificationList = Lists.newArrayList();
		private boolean valid;

		public Tracker(LivingEntity entity, DamageSource damageSource, float initialAmount)
		{
			this.entity = entity;
			this.initialHealth = entity.getHealth();
			this.damageSource = damageSource;
			this.initialAmount = initialAmount;
			this.currentAmount = initialAmount;
			this.valid = true;
		}

		public boolean isValid()
		{
			return this.valid;
		}

		public void modifyDamage(float newAmount, ModifyReason reason)
		{
			// to avoid spamming
			boolean isFire =
					//#if MC >= 11904
					//$$ this.damageSource.isIn(DamageTypeTags.IS_FIRE);
					//#else
					this.damageSource.isFire();
					//#endif
			if (isFire && (this.entity.isFireImmune() || this.entity.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)))
			{
				this.valid = false;
				return;
			}

			if (newAmount != this.currentAmount)
			{
				this.modificationList.add(new Modification(this.currentAmount, newAmount, reason));
				this.currentAmount = newAmount;
			}
			if (reason == ModifyReason.INVULNERABLE)  // no spam for creative player
			{
				this.valid = false;
			}
		}

		private BaseText[] verifyAndProduceMessage(String option, PlayerEntity player, DamageContext ctx, Supplier<BaseText[]> messageFuture)
		{
			OptionParser parser = new OptionParser(option);
			if (parser.accepts(player, ctx))
			{
				return messageFuture.get();
			}
			return null;
		}

		private BaseText getAmountText(@Nullable String style, float amount)
		{
			String display = String.format("%.2f", amount);
			String detail = String.format("%.6f", amount);
			return Messenger.fancy(
					style,
					Messenger.s(display),
					Messenger.s(detail),
					Messenger.ClickEvents.suggestCommand(detail)
			);
		}

		public void flush(float finalAmount, float remainingHealth)
		{
			if (!isLoggerActivated() || !this.isValid())
			{
				return;
			}
			this.valid = false;
			Entity attacker = this.damageSource.getAttacker();
			Entity source = this.damageSource.getSource();
			LivingEntity target = this.entity;
			DamageContext ctx = new DamageContext(this.damageSource, attacker, target);
			DamageLogger.this.log((option, player) ->
					this.verifyAndProduceMessage(option, player, ctx, () -> {
						List<Object> lines = Lists.newArrayList();
						lines.add(Messenger.s(" "));
						BaseText targetName = Messenger.entity("b", target);
						List<BaseText> sourceHoverTextList = Lists.newArrayList();
						//#if MC >= 11904
						//$$ sourceHoverTextList.add(Messenger.c(
						//$$ 		tr("type"),
						//$$ 		"w : ",
						//$$ 		Messenger.s(this.damageSource.getTypeRegistryEntry().getKey().map(key -> key.getValue().toString()).orElse("[unregistered]"))
						//$$ ));
						//#endif
						if (source != null)
						{
							sourceHoverTextList.add(Messenger.c(tr("source"), "w : ", Messenger.entity(source)));
						}
						if (attacker != null)
						{
							sourceHoverTextList.add(Messenger.c(tr("attacker"), "w : ", Messenger.entity(attacker)));
						}
						lines.add(tr(
								"header_message",
								targetName,
								getAmountText("r", this.initialAmount),
								Messenger.fancy(
										"w",
										Messenger.s(this.damageSource.getName()),
										sourceHoverTextList.isEmpty() ? null : Messenger.joinLines(sourceHoverTextList),
										attacker != null ? Messenger.ClickEvents.suggestCommand(TextUtils.tp(attacker)) : null
								),
								getAmountText(null, this.initialHealth)
						));
						for (Modification modification : this.modificationList)
						{
							float oldAmount = modification.getOldAmount();
							float newAmount = modification.getNewAmount();
							float delta = Math.abs(newAmount - oldAmount);
							String sig = newAmount > oldAmount ? "+" : "-";
							String radio = oldAmount != 0.0F ? String.format("%.1f%%", 100.0F * delta / oldAmount) : "N/A%";
							lines.add(Messenger.c(
									"g  - ",
									getAmountText("r", oldAmount), "g  -> ", getAmountText(newAmount > oldAmount ? "r" : "d", newAmount),
									String.format("g  (%s", sig),
									Messenger.hover(getAmountText("g", delta), Messenger.s(String.format("%s%.6f", sig, delta))),
									String.format("g , %s%s)", sig, radio),
									tr("due_to", modification.getReason().toText())
							));
						}
						lines.add(tr(
								"footer_message",
								targetName,
								getAmountText(finalAmount > 0.0F ? "r" : "w", finalAmount),
								getAmountText(remainingHealth > 0 ? "l" : "r", remainingHealth)
						));
						return lines.stream().map(Messenger::c).toArray(BaseText[]::new);
					})
			);
		}
	}
}
