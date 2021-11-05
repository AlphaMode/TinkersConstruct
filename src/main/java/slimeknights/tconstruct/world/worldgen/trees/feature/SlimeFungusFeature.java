package slimeknights.tconstruct.world.worldgen.trees.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.HugeFungusFeature;
import slimeknights.tconstruct.world.worldgen.trees.config.SlimeFungusConfig;

import java.util.Random;

public class SlimeFungusFeature extends HugeFungusFeature {
  public SlimeFungusFeature(Codec<HugeFungusConfiguration> codec) {
    super(codec);
  }

  @Override
  public boolean place(WorldGenLevel reader, ChunkGenerator generator, Random rand, BlockPos pos, HugeFungusConfiguration config) {
    if (!(config instanceof SlimeFungusConfig)) {
      return super.place(reader, generator, rand, pos, config);
    }
    // must be on the right ground
    if (!reader.getBlockState(pos.below()).is(((SlimeFungusConfig) config).getGroundTag())) {
      return false;
    }
    // ensure not too tall
    int height = Mth.nextInt(rand, 4, 13);
    if (rand.nextInt(12) == 0) {
      height *= 2;
    }
    if (!config.planted && pos.getY() + height + 1 >= generator.getMaxBuildHeight()) {
      return false;
    }
    // actual generation
    boolean flag = !config.planted && rand.nextFloat() < 0.06F;
    reader.setBlockState(pos, Blocks.AIR.getDefaultState(), 4);
    this.generateStems(reader, rand, config, pos, height, flag);
    this.generateFungusHat(reader, rand, config, pos, height, flag);
    return true;
  }
}
