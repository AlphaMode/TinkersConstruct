package slimeknights.tconstruct.smeltery.block.controller;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.tconstruct.smeltery.tileentity.controller.MelterTileEntity;

import javax.annotation.Nonnull;
import java.util.Random;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class MelterBlock extends TinyMultiblockControllerBlock {
  public MelterBlock(Properties props) {
    super(props);
  }

  @Nonnull
  @Override
  public BlockEntity createTileEntity(BlockState blockState, BlockGetter iBlockReader) {
    return new MelterTileEntity();
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
      double y = (double) pos.getY() + (rand.nextFloat() * 6F) / 16F;
      double z = pos.getZ() + 0.5D;
      double frontOffset = 0.52D;
      double sideOffset = rand.nextDouble() * 0.6D - 0.3D;
      spawnFireParticles(world, state, x, y, z, frontOffset, sideOffset);
    }
  }
}
