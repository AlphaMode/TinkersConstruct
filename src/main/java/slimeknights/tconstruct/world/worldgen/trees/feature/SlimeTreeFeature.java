package slimeknights.tconstruct.world.worldgen.trees.feature;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.common.Tags;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.world.worldgen.trees.config.SlimeTreeConfig;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class SlimeTreeFeature extends Feature<SlimeTreeConfig> {

  public SlimeTreeFeature(Codec<SlimeTreeConfig> codec) {
    super(codec);
  }

  @Override
  public final boolean place(WorldGenLevel seedReader, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, SlimeTreeConfig config) {
    Set<BlockPos> trunkPos = Sets.newHashSet();
    Set<BlockPos> foliagePos = Sets.newHashSet();
    Set<BlockPos> leavesPos = Sets.newHashSet();
    BoundingBox boundingBox = BoundingBox.getUnknownBox();
    boolean placed = this.place(seedReader, random, blockPos, trunkPos, foliagePos, boundingBox, config);
    if (boundingBox.x0 <= boundingBox.x1 && placed && !trunkPos.isEmpty()) {
      DiscreteVoxelShape voxelshapepart = this.updateLeaves(seedReader, boundingBox, trunkPos, leavesPos);
      StructureTemplate.updateShapeAtEdge(seedReader, 3, voxelshapepart, boundingBox.x0, boundingBox.y0, boundingBox.z0);
      return true;
    }
    else {
      return false;
    }
  }

  private boolean place(LevelSimulatedRW generationReader, Random rand, BlockPos positionIn, Set<BlockPos> trunkBlockPosSet, Set<BlockPos> foliagePositions, BoundingBox boundingBoxIn, SlimeTreeConfig configIn) {
    if (!(generationReader instanceof LevelAccessor)) {
      return false;
    }
    // determine tree height
    int height = rand.nextInt(configIn.randomHeight) + configIn.baseHeight;
    if (configIn.canDoubleHeight && rand.nextInt(10) == 0) {
      height *= 2;
    }

//    BlockPos blockpos;
//    if (!configIn.forcePlacement) {
//      int oceanFloorHeight = generationReader.getHeight(Heightmap.Type.OCEAN_FLOOR, positionIn).getY();
//
//      blockpos = new BlockPos(positionIn.getX(), oceanFloorHeight, positionIn.getZ());
//    }
//    else {
//      blockpos = positionIn;
//    }

    if (positionIn.getY() >= 1 && positionIn.getY() + height + 1 <= 256 && isSlimySoilAt(generationReader, positionIn.below())) {
      this.setDirtAt(generationReader, positionIn.below(), positionIn);
      this.placeTrunk(generationReader, rand, height, positionIn, trunkBlockPosSet, boundingBoxIn, configIn);
      this.placeCanopy(generationReader, rand, height, positionIn, trunkBlockPosSet, boundingBoxIn, configIn);
      return true;
    }
    return false;
  }

  protected void setDirtAt(LevelSimulatedRW reader, BlockPos pos, BlockPos origin) {
    if (!(reader instanceof LevelAccessor)) {
      return;
    }
    BlockState state = ((LevelAccessor)reader).getBlockState(pos);
    if (state.is(Tags.Blocks.DIRT)) {
      reader.setBlock(pos, Blocks.DIRT.defaultBlockState(), 2);
    }
  }

  protected void placeTrunk(LevelSimulatedRW worldIn, Random randomIn, int treeHeight, BlockPos blockPos, Set<BlockPos> blockPosSet, BoundingBox mutableBoundingBoxIn, SlimeTreeConfig treeFeatureConfigIn) {
    while (treeHeight > 0) {
      this.setLog(worldIn, randomIn, blockPos, blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);

      blockPos = blockPos.above();
      treeHeight--;
    }
  }

  protected void placeCanopy(LevelSimulatedRW worldIn, Random randomIn, int treeHeight, BlockPos blockPos, Set<BlockPos> blockPosSet, BoundingBox mutableBoundingBoxIn, SlimeTreeConfig treeFeatureConfigIn) {
    blockPos = blockPos.above(treeHeight);
    for (int i = 0; i < 4; i++) {
      this.placeDiamondLayer(worldIn, randomIn, i + 1, blockPos.below(i), blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);
    }

    blockPos = blockPos.below(3);

    this.placeAir(worldIn, randomIn, blockPos.offset(+4, 0, 0), blockPosSet, mutableBoundingBoxIn);
    this.placeAir(worldIn, randomIn, blockPos.offset(-4, 0, 0), blockPosSet, mutableBoundingBoxIn);
    this.placeAir(worldIn, randomIn, blockPos.offset(0, 0, +4), blockPosSet, mutableBoundingBoxIn);
    this.placeAir(worldIn, randomIn, blockPos.offset(0, 0, -4), blockPosSet, mutableBoundingBoxIn);

    if (treeFeatureConfigIn.hasVines) {
      this.placeAir(worldIn, randomIn, blockPos.offset(+1, 0, +1), blockPosSet, mutableBoundingBoxIn);
      this.placeAir(worldIn, randomIn, blockPos.offset(+1, 0, -1), blockPosSet, mutableBoundingBoxIn);
      this.placeAir(worldIn, randomIn, blockPos.offset(-1, 0, +1), blockPosSet, mutableBoundingBoxIn);
      this.placeAir(worldIn, randomIn, blockPos.offset(-1, 0, -1), blockPosSet, mutableBoundingBoxIn);
    }

    //Drippers
    // stuck with only one block down because of leaf decay distance
    blockPos = blockPos.below();
    this.setLeaf(worldIn, randomIn, blockPos.offset(+3, 0, 0), blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);
    this.setLeaf(worldIn, randomIn, blockPos.offset(-3, 0, 0), blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);
    this.setLeaf(worldIn, randomIn, blockPos.offset(0, 0, -3), blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);
    this.setLeaf(worldIn, randomIn, blockPos.offset(0, 0, +3), blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);

    if (!treeFeatureConfigIn.hasVines) {
      this.setLeaf(worldIn, randomIn, blockPos.offset(+1, 0, +1), blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);
      this.setLeaf(worldIn, randomIn, blockPos.offset(-3, 0, 0), blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);
      this.setLeaf(worldIn, randomIn, blockPos.offset(-1, 0, +1), blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);
      this.setLeaf(worldIn, randomIn, blockPos.offset(-1, 0, -1), blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);
    }

    if (treeFeatureConfigIn.hasVines) {
      blockPos = blockPos.below();
      this.placeVine(worldIn, randomIn, blockPos.offset(+3, 0, 0), blockPosSet, mutableBoundingBoxIn,
        this.getRandomizedVine(randomIn, blockPos, treeFeatureConfigIn).setValue(VineBlock.UP, true));

      this.placeVine(worldIn, randomIn, blockPos.offset(-3, 0, 0), blockPosSet, mutableBoundingBoxIn,
        this.getRandomizedVine(randomIn, blockPos, treeFeatureConfigIn).setValue(VineBlock.UP, true));

      this.placeVine(worldIn, randomIn, blockPos.offset(0, 0, -3), blockPosSet, mutableBoundingBoxIn,
        this.getRandomizedVine(randomIn, blockPos, treeFeatureConfigIn).setValue(VineBlock.UP, true));

      this.placeVine(worldIn, randomIn, blockPos.offset(0, 0, +3), blockPosSet, mutableBoundingBoxIn,
        this.getRandomizedVine(randomIn, blockPos, treeFeatureConfigIn).setValue(VineBlock.UP, true));

      BlockState randomVine = this.getRandomizedVine(randomIn, blockPos, treeFeatureConfigIn);
      this.placeVine(worldIn, randomIn, blockPos.offset(+2, 1, +2), blockPosSet, mutableBoundingBoxIn,
        randomVine.setValue(VineBlock.UP, true));
      this.placeVine(worldIn, randomIn, blockPos.offset(+2, 0, +2), blockPosSet, mutableBoundingBoxIn,
        randomVine);

      randomVine = this.getRandomizedVine(randomIn, blockPos, treeFeatureConfigIn);
      this.placeVine(worldIn, randomIn, blockPos.offset(+2, 1, -2), blockPosSet, mutableBoundingBoxIn,
        randomVine.setValue(VineBlock.UP, true));
      this.placeVine(worldIn, randomIn, blockPos.offset(+2, 0, -2), blockPosSet, mutableBoundingBoxIn,
        randomVine);

      randomVine = this.getRandomizedVine(randomIn, blockPos, treeFeatureConfigIn);
      this.placeVine(worldIn, randomIn, blockPos.offset(-2, 1, +2), blockPosSet, mutableBoundingBoxIn,
        randomVine.setValue(VineBlock.UP, true));
      this.placeVine(worldIn, randomIn, blockPos.offset(-2, 0, +2), blockPosSet, mutableBoundingBoxIn,
        randomVine);

      randomVine = this.getRandomizedVine(randomIn, blockPos, treeFeatureConfigIn);
      this.placeVine(worldIn, randomIn, blockPos.offset(-2, 1, -2), blockPosSet, mutableBoundingBoxIn,
        randomVine.setValue(VineBlock.UP, true));
      this.placeVine(worldIn, randomIn, blockPos.offset(-2, 0, -2), blockPosSet, mutableBoundingBoxIn,
        randomVine);
    }
  }

  private void placeDiamondLayer(LevelSimulatedRW worldIn, Random randomIn, int range, BlockPos blockPos, Set<BlockPos> blockPosSet, BoundingBox mutableBoundingBoxIn, SlimeTreeConfig treeFeatureConfigIn) {
    for (int x = -range; x <= range; x++) {
      for (int z = -range; z <= range; z++) {
        if (Math.abs(x) + Math.abs(z) <= range) {
          BlockPos blockpos = blockPos.offset(x, 0, z);
          this.setLeaf(worldIn, randomIn, blockpos, blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);
        }
      }
    }
  }

  protected boolean setLog(LevelSimulatedRW worldIn, Random randomIn, BlockPos blockPos, Set<BlockPos> blockPosSet, BoundingBox mutableBoundingBoxIn, SlimeTreeConfig treeFeatureConfigIn) {
    if (!isAirOrLeavesAt(worldIn, blockPos)) {
      return false;
    }
    else {
      this.setBlock(worldIn, blockPos, treeFeatureConfigIn.trunkProvider.getState(randomIn, blockPos));
      mutableBoundingBoxIn.expand(new BoundingBox(blockPos, blockPos));
      blockPosSet.add(blockPos.immutable());
      return true;
    }
  }

  protected boolean placeAir(LevelSimulatedRW worldIn, Random random, BlockPos blockPos, Set<BlockPos> blockPosSet, BoundingBox mutableBoundingBoxIn) {
    if (!isAirOrLeavesAt(worldIn, blockPos)) {
      return false;
    }
    else {
      this.setBlock(worldIn, blockPos, Blocks.AIR.defaultBlockState());
      mutableBoundingBoxIn.expand(new BoundingBox(blockPos, blockPos));
      blockPosSet.add(blockPos.immutable());
      return true;
    }
  }

  protected boolean setLeaf(LevelSimulatedRW worldIn, Random random, BlockPos blockPos, Set<BlockPos> blockPosSet, BoundingBox mutableBoundingBoxIn, SlimeTreeConfig treeFeatureConfigIn) {
    if (!isAirOrLeavesAt(worldIn, blockPos)) {
      return false;
    }
    else {
      this.setBlock(worldIn, blockPos, treeFeatureConfigIn.leavesProvider.getState(random, blockPos));
      mutableBoundingBoxIn.expand(new BoundingBox(blockPos, blockPos));
      blockPosSet.add(blockPos.immutable());
      return true;
    }
  }

  protected boolean placeVine(LevelSimulatedRW worldIn, Random random, BlockPos blockPos, Set<BlockPos> blockPosSet, BoundingBox mutableBoundingBoxIn, BlockState vineState) {
    if (!isAirOrLeavesAt(worldIn, blockPos)) {
      return false;
    }
    else {
      this.setBlock(worldIn, blockPos, vineState);
      mutableBoundingBoxIn.expand(new BoundingBox(blockPos, blockPos));
      blockPosSet.add(blockPos.immutable());
      return true;
    }
  }

  private BlockState getRandomizedVine(Random random, BlockPos blockPos, SlimeTreeConfig config) {
    BlockState state = config.vinesProvider.getState(random, blockPos);

    BooleanProperty[] sides = new BooleanProperty[] { VineBlock.NORTH, VineBlock.EAST, VineBlock.SOUTH, VineBlock.WEST };

    for (BooleanProperty side : sides) {
      state = state.setValue(side, false);
    }

    for (int i = random.nextInt(3) + 1; i > 0; i--) {
      state = state.setValue(sides[random.nextInt(sides.length)], true);
    }

    return state;
  }

  public static boolean isEmptyOrLogAt(LevelSimulatedReader reader, BlockPos blockPos) {
    return isReplaceableAt(reader, blockPos) || reader.isStateAtPosition(blockPos, state -> state.is(BlockTags.LOGS));
  }

  private static boolean isVineAt(LevelSimulatedReader reader, BlockPos blockPos) {
    return reader.isStateAtPosition(blockPos, (state) -> state.is(Blocks.VINE));
  }

  private static boolean isWaterAt(LevelSimulatedReader reader, BlockPos blockPos) {
    return reader.isStateAtPosition(blockPos, state -> state.is(Blocks.WATER));
  }

  public static boolean isAirOrLeavesAt(LevelSimulatedReader reader, BlockPos blockPos) {
    return reader.isStateAtPosition(blockPos, state -> state.isAir() || state.is(BlockTags.LEAVES));
  }

  private static boolean isSlimySoilAt(LevelSimulatedReader reader, BlockPos blockPos) {
    return reader.isStateAtPosition(blockPos, state -> TinkerTags.Blocks.SLIMY_SOIL.contains(state.getBlock()));
  }

  private static boolean isTallPlantAt(LevelSimulatedReader reader, BlockPos blockPos) {
    return reader.isStateAtPosition(blockPos, state -> state.getMaterial() == Material.REPLACEABLE_PLANT);
  }

  public static boolean isReplaceableAt(LevelSimulatedReader reader, BlockPos blockPos) {
    return isAirOrLeavesAt(reader, blockPos) || isTallPlantAt(reader, blockPos) || isWaterAt(reader, blockPos);
  }

  public static void setBlockStateAt(LevelWriter writer, BlockPos blockPos, BlockState state) {
    writer.setBlock(blockPos, state, 19);
  }

  private DiscreteVoxelShape updateLeaves(LevelAccessor world, BoundingBox boundingBox, Set<BlockPos> logs, Set<BlockPos> leaves) {
    List<Set<BlockPos>> distanceList = Lists.newArrayList();
    DiscreteVoxelShape shapePart = new BitSetDiscreteVoxelShape(boundingBox.getXSpan(), boundingBox.getYSpan(), boundingBox.getZSpan());
    for (int j = 0; j < 6; ++j) {
      distanceList.add(Sets.newHashSet());
    }

    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

    for (BlockPos leavePos : Lists.newArrayList(leaves)) {
      if (boundingBox.isInside(leavePos)) {
        shapePart.setFull(leavePos.getX() - boundingBox.x0, leavePos.getY() - boundingBox.y0, leavePos.getZ() - boundingBox.z0, true, true);
      }
    }

    for (BlockPos logPos : Lists.newArrayList(logs)) {
      if (boundingBox.isInside(logPos)) {
        shapePart.setFull(logPos.getX() - boundingBox.x0, logPos.getY() - boundingBox.y0, logPos.getZ() - boundingBox.z0, true, true);
      }
      for (Direction direction : Direction.values()) {
        mutable.setWithOffset(logPos, direction);
        if (!logs.contains(mutable)) {
          BlockState blockstate = world.getBlockState(mutable);
          if (blockstate.hasProperty(BlockStateProperties.DISTANCE)) {
            distanceList.get(0).add(mutable.immutable());
            setBlockStateAt(world, mutable, blockstate.setValue(BlockStateProperties.DISTANCE, 1));
            if (boundingBox.isInside(mutable)) {
              shapePart.setFull(mutable.getX() - boundingBox.x0, mutable.getY() - boundingBox.y0, mutable.getZ() - boundingBox.z0, true, true);
            }
          }
        }
      }
    }

    for (int distance = 1; distance < 6; ++distance) {
      Set<BlockPos> current = distanceList.get(distance - 1);
      Set<BlockPos> next = distanceList.get(distance);

      for (BlockPos pos : current) {
        if (boundingBox.isInside(pos)) {
          shapePart.setFull(pos.getX() - boundingBox.x0, pos.getY() - boundingBox.y0, pos.getZ() - boundingBox.z0, true, true);
        }

        for (Direction direction : Direction.values()) {
          mutable.setWithOffset(pos, direction);
          if (!current.contains(mutable) && !next.contains(mutable)) {
            BlockState state = world.getBlockState(mutable);
            if (state.hasProperty(BlockStateProperties.DISTANCE)) {
              int stateDistance = state.getValue(BlockStateProperties.DISTANCE);
              if (stateDistance > distance + 1) {
                BlockState furtherState = state.setValue(BlockStateProperties.DISTANCE, distance + 1);
                setBlockStateAt(world, mutable, furtherState);
                if (boundingBox.isInside(mutable)) {
                  shapePart.setFull(mutable.getX() - boundingBox.x0, mutable.getY() - boundingBox.y0, mutable.getZ() - boundingBox.z0, true, true);
                }
                next.add(mutable.immutable());
              }
            }
          }
        }
      }
    }

    return shapePart;
  }
}
