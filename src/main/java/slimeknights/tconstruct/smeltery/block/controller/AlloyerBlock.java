package slimeknights.tconstruct.smeltery.block.controller;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.util.TileEntityHelper;
import slimeknights.tconstruct.library.utils.Util;
import slimeknights.tconstruct.smeltery.tileentity.controller.AlloyerTileEntity;

import java.util.Random;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class AlloyerBlock extends TinyMultiblockControllerBlock {
  public AlloyerBlock(Properties builder) {
    super(builder);
  }

  @Override
  public BlockEntity createTileEntity(BlockState blockState, BlockGetter iBlockReader) {
    return new AlloyerTileEntity();
  }

  @Override
  public void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
    Direction direction = Util.directionFromOffset(pos, fromPos);
    if (direction != Direction.DOWN) {
      TileEntityHelper.getTile(AlloyerTileEntity.class, world, pos).ifPresent(te -> te.neighborChanged(direction));
    }
  }

  /*
   * Display
   */

  @Deprecated
  @Override
  @OnlyIn(Dist.CLIENT)
  public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
    return 1.0F;
  }

  @Override
  public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
    return true;
  }

  @Override
  public void animateTick(BlockState state, Level world, BlockPos pos, Random rand) {
    if (state.getValue(ACTIVE)) {
      double x = pos.getX() + 0.5D;
      double y = (double) pos.getY() + (rand.nextFloat() * 4F) / 16F;
      double z = pos.getZ() + 0.5D;
      double frontOffset = 0.52D;
      double sideOffset = rand.nextDouble() * 0.6D - 0.3D;
      spawnFireParticles(world, state, x, y, z, frontOffset, sideOffset, ParticleTypes.SOUL_FIRE_FLAME);
    }
  }
}
