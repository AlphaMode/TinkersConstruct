package slimeknights.tconstruct.world.item;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import slimeknights.mantle.item.TooltipItem;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.world.TinkerWorld;

import javax.annotation.Nullable;

import net.minecraft.world.item.Item.Properties;

public class SlimeGrassSeedItem extends TooltipItem {
  private final SlimeType foliage;
  public SlimeGrassSeedItem(Properties properties, SlimeType foliage) {
    super(properties);
    this.foliage = foliage;
  }

  /** Gets the slime type for the given block */
  @Nullable
  private SlimeType getSlimeType(Block block) {
    for (SlimeType type : SlimeType.values()) {
      if (TinkerWorld.allDirt.get(type) == block) {
        return type;
      }
    }
    return null;
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    BlockPos pos = context.getClickedPos();
    Level world = context.getLevel();
    BlockState state = world.getBlockState(pos);
    SlimeType type = getSlimeType(state.getBlock());
    if (type != null) {
      if (!world.isClientSide) {
        BlockState grassState = TinkerWorld.slimeGrass.get(type).get(foliage).defaultBlockState();
        world.setBlockAndUpdate(pos, grassState);
        world.playSound(null, pos, grassState.getSoundType(world, pos, context.getPlayer()).getPlaceSound(), SoundSource.BLOCKS, 1.0f, 1.0f);
        Player player = context.getPlayer();
        if (player == null || !player.isCreative()) {
          context.getItemInHand().shrink(1);
        }
      }
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
  }

  @Override
  public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
    if (this.foliage != SlimeType.ICHOR) {
      super.fillItemCategory(group, items);
    }
  }
}
