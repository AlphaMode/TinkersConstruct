package slimeknights.tconstruct.common.multiblock;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.extensions.IForgeBlockEntity;

// TODO: move back to Mantle after smeltery is updated
public interface IMasterLogic extends IForgeBlockEntity {

  /**
   * Called when servants change their state
   *
   * @param servant  Servant tile instance
   * @param pos      Position that changed. May not be the servant position
   * @param state    State that changed. May not be the servant state
   */
  void notifyChange(IServantLogic servant, BlockPos pos, BlockState state);

  /**
   * @return The block entity this is attached to
   */
  default BlockEntity getBlockEntity() {
    return (BlockEntity) this;
  }
}
