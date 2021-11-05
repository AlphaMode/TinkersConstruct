package slimeknights.tconstruct.common;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;

/** Effect extension with a few helpers */
public class TinkerEffect extends MobEffect {
  /** If true, effect is visible, false for hidden */
  private final boolean show;
  public TinkerEffect(MobEffectCategory typeIn, boolean show) {
    this(typeIn, 0xffffff, show);
  }

  public TinkerEffect(MobEffectCategory typeIn, int color, boolean show) {
    super(typeIn, color);
    this.show = show;
  }

  /* Visibility */

//  @Override
//  public boolean shouldRender(MobEffectInstance effect) {
//    return this.show;
//  }
//
//  @Override
//  public boolean shouldRenderInvText(MobEffectInstance effect) {
//    return this.show;
//  }
//
//  @Override
//  public boolean shouldRenderHUD(MobEffectInstance effect) {
//    return this.show;
//  }


  /* Helpers */

  /**
   * Applies this potion to an entity
   * @param entity    Entity
   * @param duration  Duration
   * @return  Applied instance
   */
  public MobEffectInstance apply(LivingEntity entity, int duration) {
    return this.apply(entity, duration, 0);
  }

  /**
   * Applies this potion to an entity
   * @param entity    Entity
   * @param duration  Duration
   * @param level     Effect level
   * @return  Applied instance
   */
  public MobEffectInstance apply(LivingEntity entity, int duration, int level) {
    MobEffectInstance effect = new MobEffectInstance(this, duration, level, false, false);
    entity.addEffect(effect);
    return effect;
  }

  /**
   * Gets the level of the effect on the entity, or -1 if not active
   * @param entity  Entity to check
   * @return  Level, or -1 if inactive
   */
  public int getLevel(LivingEntity entity) {
    MobEffectInstance effect = entity.getEffect(this);
    if (effect != null) {
      return effect.getAmplifier();
    }
    return -1;
  }

}
