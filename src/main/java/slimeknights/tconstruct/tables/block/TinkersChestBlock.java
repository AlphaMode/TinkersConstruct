package slimeknights.tconstruct.tables.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.BlockGetter;
import slimeknights.mantle.util.TileEntityHelper;
import slimeknights.tconstruct.tables.tileentity.chest.TinkersChestTileEntity;

import java.util.function.Supplier;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class TinkersChestBlock extends ChestBlock {
  public TinkersChestBlock(Properties builder, Supplier<? extends BlockEntity> te, boolean dropsItems) {
    super(builder, te, dropsItems);
  }

  @Override
  public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
    ItemStack stack = new ItemStack(this);
    TileEntityHelper.getTile(TinkersChestTileEntity.class, world, pos).ifPresent(te -> {
      if (te.hasColor()) {
        ((IDyeableArmorItem) stack.getItem()).setColor(stack, te.getColor());
      }
    });
    return stack;
  }
}
