package slimeknights.tconstruct.smeltery.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import slimeknights.tconstruct.smeltery.tileentity.CastingTileEntity;

import javax.annotation.Nonnull;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class CastingTableBlock extends AbstractCastingBlock {

  private static final VoxelShape SHAPE = Shapes.join(
    Shapes.block(),
    Shapes.or(
      Block.box(4.0D, 0.0D, 0.0D, 12.0D, 10.0D, 16.0D),
      Block.box(0.0D, 0.0D, 4.0D, 16.0D, 10.0D, 12.0D),
      Block.box(1.0D, 15.0D, 1.0D, 15.0D, 16.0D, 15.0D)
    ), BooleanOp.ONLY_FIRST);

  public CastingTableBlock(Properties builder) {
    super(builder);
  }

  @Deprecated
  @Override
  public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }

  @Nonnull
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new CastingTileEntity.Table(pos, state);
  }
}
