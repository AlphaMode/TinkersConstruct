package slimeknights.tconstruct.gadgets.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.shared.block.TableBlock;

import javax.annotation.Nonnull;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class RackBlock extends TableBlock {

  protected RackBlock(Properties builder) {
    super(builder);
  }

  @Nonnull
  @Override
  public BlockEntity createTileEntity(BlockState blockState, BlockGetter iBlockReader) {
    return null;
  }

  @Override
  protected boolean openGui(Player playerEntity, Level world, BlockPos blockPos) {
    return false;
  }
}
