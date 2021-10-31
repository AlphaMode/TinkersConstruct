package slimeknights.tconstruct.gadgets.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.util.TranslationHelper;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.gadgets.entity.shuriken.ShurikenEntityBase;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;

import net.minecraft.world.item.Item.Properties;

public class ShurikenItem extends SnowballItem {

  private final BiFunction<Level, Player, ShurikenEntityBase> entity;

  public ShurikenItem(Properties properties, BiFunction<Level, Player, ShurikenEntityBase> entity) {
    super(properties);
    this.entity = entity;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    world.playSound(null, player.getX(), player.getY(), player.getZ(), Sounds.SHURIKEN_THROW.getSound(), SoundCategory.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
    player.getCooldownTracker().setCooldown(stack.getItem(), 4);
    if(!world.isRemote) {
      ShurikenEntityBase entity = this.entity.apply(world, player);
      entity.setItem(stack);
      entity.setDirectionAndMovement(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
      world.addEntity(entity);
    }
    player.addStat(Stats.ITEM_USED.get(this));
    if (!player.abilities.isCreativeMode) {
      stack.shrink(1);
    }

    return ActionResult.sidedSuccess(stack, world.isRemote());
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
    TranslationHelper.addOptionalTooltip(stack, tooltip);
    super.addInformation(stack, worldIn, tooltip, flagIn);
  }
}
