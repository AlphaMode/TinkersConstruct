package slimeknights.tconstruct.tools.modifiers.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.common.TinkerEffect;

import java.util.List;

public class MagneticEffect extends TinkerEffect {
  public MagneticEffect() {
    super(MobEffectCategory.BENEFICIAL, 0x720000, false);
  }

  @Override
  public boolean isDurationEffectTick(int duration, int amplifier) {
    return (duration & 1) == 0;
  }

  @Override
  public void applyEffectTick(LivingEntity entity, int amplifier) {
    // super magnetic - inspired by botanias code
    double x = entity.getX();
    double y = entity.getY();
    double z = entity.getZ();
    float range = 3f + 1f * amplifier;
    List<ItemEntity> items = entity.getCommandSenderWorld().getEntitiesOfClass(ItemEntity.class, new AABB(x - range, y - range, z - range, x + range, y + range, z + range));

    // only pull up to 200 items
    int pulled = 0;
    for (ItemEntity item : items) {
      if (item.getItem().isEmpty() || !item.isAlive()) {
        continue;
      }
      // calculate direction: item -> player
      Vec3 vec = entity.position()
                           .subtract(item.getX(), item.getY(), item.getZ())
                           .normalize()
                           .scale(0.05f + amplifier * 0.05f);
      if (!item.isNoGravity()) {
        vec = vec.add(0, 0.04f, 0);
      }

      // we calculated the movement vector and set it to the correct strength.. now we apply it \o/
      item.setDeltaMovement(item.getDeltaMovement().add(vec));

      // use stack size as limiting factor
      pulled += item.getItem().getCount();
      if (pulled > 200) {
        break;
      }
    }
  }
}
