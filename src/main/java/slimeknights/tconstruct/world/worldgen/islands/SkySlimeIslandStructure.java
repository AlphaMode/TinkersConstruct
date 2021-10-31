package slimeknights.tconstruct.world.worldgen.islands;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.level.biome.MobSpawnSettings;
import slimeknights.tconstruct.world.TinkerWorld;
import slimeknights.tconstruct.world.worldgen.islands.variants.IIslandVariant;
import slimeknights.tconstruct.world.worldgen.islands.variants.IslandVariants;

import java.util.List;
import java.util.Random;

/**
 * Overworld structure containing sky slimes, spawns in the sky
 */
public class SkySlimeIslandStructure extends AbstractIslandStructure {
  private final List<MobSpawnSettings.SpawnerData> monsters = ImmutableList.of(new MobSpawnSettings.SpawnerData(TinkerWorld.skySlimeEntity.get(), 30, 4, 4));

  @Override
  public List<MobSpawnSettings.SpawnerData> getDefaultSpawnList() {
    return monsters;
  }

  @Override
  public IIslandVariant getVariant(Random random) {
    return random.nextBoolean() ? IslandVariants.SKY_BLUE : IslandVariants.SKY_GREEN;
  }
}
