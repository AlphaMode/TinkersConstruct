package slimeknights.tconstruct.tables.block.table;

import lombok.Getter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.BlockGetter;
import slimeknights.tconstruct.tables.tileentity.table.TinkerStationTileEntity;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class TinkerStationBlock extends RetexturedTableBlock {
  @Getter
  private final int slotCount;

  public TinkerStationBlock(Properties builder, int slotCount) {
    super(builder);
    this.slotCount = slotCount;
  }

  @Override
  public BlockEntity createTileEntity(BlockState blockState, BlockGetter iBlockReader) {
    return new TinkerStationTileEntity(getSlotCount());
  }
}
