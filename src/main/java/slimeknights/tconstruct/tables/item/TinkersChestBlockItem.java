package slimeknights.tconstruct.tables.item;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants.NBT;
import slimeknights.mantle.util.TileEntityHelper;
import slimeknights.tconstruct.tables.tileentity.chest.TinkersChestTileEntity;

import javax.annotation.Nullable;

import net.minecraft.world.item.Item.Properties;

/** Dyeable chest block */
public class TinkersChestBlockItem extends BlockItem implements DyeableLeatherItem {
  public TinkersChestBlockItem(Block blockIn, Properties builder) {
    super(blockIn, builder);
  }

  @Override
  public int getColor(ItemStack stack) {
    CompoundTag tag = stack.getTagElement("display");
    return tag != null && tag.contains("color", NBT.TAG_ANY_NUMERIC) ? tag.getInt("color") : TinkersChestTileEntity.DEFAULT_COLOR;
  }

  @Override
  protected boolean updateCustomBlockEntityTag(BlockPos pos, Level worldIn, @Nullable Player player, ItemStack stack, BlockState state) {
    boolean result = super.updateCustomBlockEntityTag(pos, worldIn, player, stack, state);
    if (hasCustomColor(stack)) {
      int color = getColor(stack);
      TileEntityHelper.getTile(TinkersChestTileEntity.class, worldIn, pos).ifPresent(te -> te.setColor(color));
    }
    return result;
  }
}
