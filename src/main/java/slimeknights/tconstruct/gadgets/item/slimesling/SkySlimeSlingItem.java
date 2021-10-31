package slimeknights.tconstruct.gadgets.item.slimesling;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.utils.SlimeBounceHandler;
import slimeknights.tconstruct.shared.block.SlimeType;

import net.minecraft.world.item.Item.Properties;

public class SkySlimeSlingItem extends BaseSlimeSlingItem {

  public SkySlimeSlingItem(Properties props) {
    super(props, SlimeType.SKY);
  }

  @Override
  public float getForce(ItemStack stack, int timeLeft) {
    int i = this.getUseDuration(stack) - timeLeft;
    float f = i / 20.0F;
    f = (f * f + f * 2.0F) / 3.0F;
    f *= 4f;

    if (f > 3f) {
      f = 3f;
    }
    return f;
  }

  /** Called when the player stops using an Item (stops holding the right mouse button). */
  @Override
  public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
    if (!(entityLiving instanceof Player)) {
      return;
    }

    Player player = (Player) entityLiving;

    // don't allow free flight when using an elytra, should use fireworks
    if (player.isFallFlying()) {
      return;
    }

    float f = getForce(stack, timeLeft);

    player.causeFoodExhaustion(0.2F);
    player.getCooldowns().addCooldown(stack.getItem(), 3);
    player.setSprinting(true);

    float speed = f / 3F;
    player.push(
      (-Mth.sin(player.yRot / 180.0F * (float) Math.PI) * Mth.cos(player.xRot / 180.0F * (float) Math.PI) * speed),
      speed,
      (Mth.cos(player.yRot / 180.0F * (float) Math.PI) * Mth.cos(player.xRot / 180.0F * (float) Math.PI) * speed));

    playerServerMovement(player);
    onSuccess(player, stack);
    SlimeBounceHandler.addBounceHandler(player);
  }
}
