package slimeknights.tconstruct.library.utils;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;
import slimeknights.tconstruct.common.Sounds;

public class TeleportHelper {

  /** Randomly teleports an entity, mostly copied from chorus fruit */
  public static void randomNearbyTeleport(LivingEntity living, ITeleportEventFactory factory) {
    double posX = living.getX();
    double posY = living.getY();
    double posZ = living.getZ();

    for(int i = 0; i < 16; ++i) {
      double x = posX + (living.getRandom().nextDouble() - 0.5D) * 16.0D;
      double y = Mth.clamp(posY + (double)(living.getRandom().nextInt(16) - 8), 0.0D, living.getCommandSenderWorld().getHeight() - 1);
      double z = posZ + (living.getRandom().nextDouble() - 0.5D) * 16.0D;
      if (living.isPassenger()) {
        living.stopRiding();
      }

      EntityTeleportEvent event = factory.create(living, x, y, z);
      if (!event.isCanceled() && living.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
        SoundEvent soundevent = Sounds.SLIME_TELEPORT.getSound();
        living.getEntityWorld().playSound(null, posX, posY, posZ, soundevent, SoundCategory.PLAYERS, 1.0F, 1.0F);
        living.playSound(soundevent, 1.0F, 1.0F);
        break;
      }
    }
  }

  /** Predicate to test if the entity can teleport, typically just fires a cancelable event */
  @FunctionalInterface
  public interface ITeleportEventFactory {
    EntityTeleportEvent create(LivingEntity entity, double x, double y, double z);
  }
}
