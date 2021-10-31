package slimeknights.tconstruct.tables.block.table;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.BlockGetter;
import slimeknights.tconstruct.tables.tileentity.table.PartBuilderTileEntity;

import javax.annotation.Nonnull;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class PartBuilderBlock extends RetexturedTableBlock {

  public PartBuilderBlock(Properties builder) {
    super(builder);
  }

  @Nonnull
  @Override
  public BlockEntity createTileEntity(BlockState blockState, BlockGetter iBlockReader) {
    return new PartBuilderTileEntity();
  }
}
