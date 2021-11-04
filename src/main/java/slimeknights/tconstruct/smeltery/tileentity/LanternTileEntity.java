package slimeknights.tconstruct.smeltery.tileentity;

import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.tileentity.component.TankTileEntity;

import slimeknights.tconstruct.smeltery.tileentity.component.TankTileEntity.ITankBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Extension of {@link TankTileEntity} that uses no TESR, forcing the model fluid render, its more efficient for decoration */
public class LanternTileEntity extends TankTileEntity {
  public LanternTileEntity(BlockPos pos, BlockState state) {
    this(TinkerSmeltery.searedLantern.get(), pos, state);
  }

  /** Main constructor */
  public LanternTileEntity(ITankBlock block, BlockPos pos, BlockState state) {
    super(TinkerSmeltery.lantern.get(), block);
  }

  @Override
  public boolean isFluidInModel() {
    return true;
  }
}
