package slimeknights.tconstruct.common.data.loot;

import com.google.common.collect.Maps;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.loot.RetexturedLootFunction;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.FenceBuildingBlockObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.library.utils.NBTTags;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.shared.block.ClearStainedGlassBlock;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.tileentity.chest.TinkersChestTileEntity;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.world.TinkerWorld;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BlockLootTableProvider extends BlockLoot {

  private final Map<ResourceLocation, LootTable.Builder> loot_tables = Maps.newHashMap();

  @Nonnull
  @Override
  protected Iterable<Block> getKnownBlocks() {
    return ForgeRegistries.BLOCKS.getValues().stream()
                                 .filter((block) -> TConstruct.MOD_ID.equals(Objects.requireNonNull(block.getRegistryName()).getNamespace()))
                                 .collect(Collectors.toList());
  }

  @Override
  protected void addTables() {
    this.addCommon();
    this.addDecorative();
    this.addGadgets();
    this.addWorld();
    this.addTools();
    this.addSmeltery();
    this.addFoundry();
  }

  private void addCommon() {
    this.registerBuildingLootTables(TinkerCommons.blazewood);
    this.registerBuildingLootTables(TinkerCommons.lavawood);
    this.registerFenceBuildingLootTables(TinkerMaterials.nahuatl);

    this.dropSelf(TinkerModifiers.silkyJewelBlock.get());

    // ores
    this.dropSelf(TinkerMaterials.copper.get());
    this.dropSelf(TinkerMaterials.cobalt.get());
    // tier 3
    this.dropSelf(TinkerMaterials.slimesteel.get());
    this.dropSelf(TinkerMaterials.tinkersBronze.get());
    this.dropSelf(TinkerMaterials.roseGold.get());
    this.dropSelf(TinkerMaterials.pigIron.get());
    // tier 4
    this.dropSelf(TinkerMaterials.manyullyn.get());
    this.dropSelf(TinkerMaterials.hepatizon.get());
    this.dropSelf(TinkerMaterials.queensSlime.get());
    this.dropSelf(TinkerMaterials.soulsteel.get());
    // tier 5
    this.dropSelf(TinkerMaterials.knightslime.get());
  }

  private void addDecorative() {
    this.dropSelf(TinkerCommons.obsidianPane.get());
    this.dropSelf(TinkerCommons.clearGlass.get());
    this.dropSelf(TinkerCommons.clearGlassPane.get());
    for (ClearStainedGlassBlock.GlassColor color : ClearStainedGlassBlock.GlassColor.values()) {
      this.dropSelf(TinkerCommons.clearStainedGlass.get(color));
      this.dropSelf(TinkerCommons.clearStainedGlassPane.get(color));
    }
    this.dropSelf(TinkerCommons.soulGlass.get());
    this.dropSelf(TinkerCommons.soulGlassPane.get());

    this.registerBuildingLootTables(TinkerCommons.mudBricks);
  }

  private void addTools() {
    // chests
    // tinker chest - name and color
    this.add(TinkerTables.tinkersChest.get(), block -> droppingWithFunctions(block, builder ->
      builder.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
             .apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy(TinkersChestTileEntity.TAG_CHEST_COLOR, "display.color"))));
    // part chest - just name
    this.add(TinkerTables.partChest.get(), block ->
      droppingWithFunctions(block, builder ->
        builder.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))));
    // cast chest - name and inventory
    this.add(TinkerTables.castChest.get(), block -> droppingWithFunctions(block, builder ->
      builder.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
             .apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("Items", "TinkerData.Items"))));

    // tables with legs
    Function<Block, LootTable.Builder> addTable = block -> droppingWithFunctions(block, (builder) ->
      builder.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY)).apply(RetexturedLootFunction::new));
    this.add(TinkerTables.craftingStation.get(), addTable);
    this.add(TinkerTables.partBuilder.get(), addTable);
    this.add(TinkerTables.tinkerStation.get(), addTable);
    this.add(TinkerTables.tinkersAnvil.get(), addTable);
    this.add(TinkerTables.scorchedAnvil.get(), addTable);
  }

  private void addWorld() {
    this.dropSelf(TinkerWorld.cobaltOre.get());
    this.dropSelf(TinkerWorld.copperOre.get());
    TinkerWorld.heads.forEach(this::dropSelf);

    // slime blocks
    TinkerWorld.slime.forEach((type, block) -> {
      if (type != SlimeType.EARTH) {
        this.dropSelf(block);
      }
    });
    TinkerWorld.congealedSlime.forEach((slime, block) -> this.add(block, createSingleItemTableWithSilkTouch(block, TinkerCommons.slimeball.get(slime), ConstantIntValue.exactly(4))));

    // slime dirt and grass
    TinkerWorld.slimeDirt.forEach(this::dropSelf);
    TinkerWorld.vanillaSlimeGrass.forEach(block -> this.add(block, createSingleItemTableWithSilkTouch(block, Blocks.DIRT)));
    TinkerWorld.earthSlimeGrass.forEach(block -> this.add(block, createSingleItemTableWithSilkTouch(block, TinkerWorld.slimeDirt.get(SlimeType.EARTH))));
    TinkerWorld.skySlimeGrass.forEach(block -> this.add(block, createSingleItemTableWithSilkTouch(block, TinkerWorld.slimeDirt.get(SlimeType.SKY))));
    TinkerWorld.enderSlimeGrass.forEach(block -> this.add(block, createSingleItemTableWithSilkTouch(block, TinkerWorld.slimeDirt.get(SlimeType.ENDER))));
    TinkerWorld.ichorSlimeGrass.forEach(block -> this.add(block, createSingleItemTableWithSilkTouch(block, TinkerWorld.slimeDirt.get(SlimeType.ICHOR))));

    // saplings
    TinkerWorld.slimeSapling.forEach(this::dropSelf);

    // foliage
    TinkerWorld.slimeTallGrass.forEach(block -> this.add(block, BlockLootTableProvider::onlyShearsTag));
    for (SlimeType type : SlimeType.OVERWORLD) {
      // overworld leaves, drops with leaves and slimeballs
      this.add(TinkerWorld.slimeLeaves.get(type), block -> randomDropSlimeBallOrSapling(type, block, TinkerWorld.slimeSapling.get(type), NORMAL_LEAVES_SAPLING_CHANCES));
      this.add(TinkerWorld.slimeFern.get(type), BlockLootTableProvider::onlyShearsTag);
    }
    for (SlimeType type : SlimeType.NETHER) {
      // nether leaves drop self
      this.dropSelf(TinkerWorld.slimeLeaves.get(type));
      this.dropSelf(TinkerWorld.slimeFern.get(type));
    }

    // vines
    this.add(TinkerWorld.skySlimeVine.get(), BlockLootTableProvider::onlyShearsTag);
    this.add(TinkerWorld.enderSlimeVine.get(), BlockLootTableProvider::onlyShearsTag);

    // wood
    this.registerWoodLootTables(TinkerWorld.greenheart);
    this.registerWoodLootTables(TinkerWorld.skyroot);
    this.registerWoodLootTables(TinkerWorld.bloodshroom);
  }

  private void addGadgets() {
    this.dropSelf(TinkerGadgets.punji.get());
    TinkerGadgets.cake.forEach(block -> this.add(block, noDrop()));
    this.add(TinkerGadgets.magmaCake.get(), noDrop());
  }

  private void addSmeltery() {
    this.dropSelf(TinkerSmeltery.grout.get());
    // controller
    this.dropSelf(TinkerSmeltery.searedMelter.get());
    this.dropSelf(TinkerSmeltery.searedHeater.get());
    this.dropSelf(TinkerSmeltery.smelteryController.get());

    // smeltery component
    this.registerBuildingLootTables(TinkerSmeltery.searedStone);
    this.registerWallBuildingLootTables(TinkerSmeltery.searedCobble);
    this.registerBuildingLootTables(TinkerSmeltery.searedPaver);
    this.registerWallBuildingLootTables(TinkerSmeltery.searedBricks);
    this.dropSelf(TinkerSmeltery.searedCrackedBricks.get());
    this.dropSelf(TinkerSmeltery.searedFancyBricks.get());
    this.dropSelf(TinkerSmeltery.searedTriangleBricks.get());
    this.dropSelf(TinkerSmeltery.searedLadder.get());
    this.dropSelf(TinkerSmeltery.searedGlass.get());
    this.dropSelf(TinkerSmeltery.searedGlassPane.get());
    this.dropSelf(TinkerSmeltery.searedDrain.get());
    this.dropSelf(TinkerSmeltery.searedChute.get());
    this.dropSelf(TinkerSmeltery.searedDuct.get());

    Function<Block, LootTable.Builder> dropTank = block -> droppingWithFunctions(block, builder ->
      builder.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
             .apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy(NBTTags.TANK, NBTTags.TANK)));
    TinkerSmeltery.searedTank.forEach(block -> this.add(block, dropTank));
    this.add(TinkerSmeltery.searedLantern.get(), dropTank);

    // fluid
    this.dropSelf(TinkerSmeltery.searedFaucet.get());
    this.dropSelf(TinkerSmeltery.searedChannel.get());

    // casting
    this.dropSelf(TinkerSmeltery.searedBasin.get());
    this.dropSelf(TinkerSmeltery.searedTable.get());
  }

  private void addFoundry() {
    this.dropSelf(TinkerSmeltery.netherGrout.get());
    // controller
    this.dropSelf(TinkerSmeltery.scorchedAlloyer.get());
    this.dropSelf(TinkerSmeltery.foundryController.get());

    // smeltery component
    this.dropSelf(TinkerSmeltery.scorchedStone.get());
    this.dropSelf(TinkerSmeltery.polishedScorchedStone.get());
    this.registerFenceBuildingLootTables(TinkerSmeltery.scorchedBricks);
    this.dropSelf(TinkerSmeltery.chiseledScorchedBricks.get());
    this.registerBuildingLootTables(TinkerSmeltery.scorchedRoad);
    this.dropSelf(TinkerSmeltery.scorchedLadder.get());
    this.dropSelf(TinkerSmeltery.scorchedGlass.get());
    this.dropSelf(TinkerSmeltery.scorchedGlassPane.get());
    this.dropSelf(TinkerSmeltery.scorchedDrain.get());
    this.dropSelf(TinkerSmeltery.scorchedChute.get());
    this.dropSelf(TinkerSmeltery.scorchedDuct.get());

    Function<Block, LootTable.Builder> dropTank = block -> droppingWithFunctions(block, builder ->
      builder.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
             .apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy(NBTTags.TANK, NBTTags.TANK)));
    TinkerSmeltery.scorchedTank.forEach(block -> this.add(block, dropTank));
    this.add(TinkerSmeltery.scorchedLantern.get(), dropTank);

    // fluid
    this.dropSelf(TinkerSmeltery.scorchedFaucet.get());
    this.dropSelf(TinkerSmeltery.scorchedChannel.get());

    // casting
    this.dropSelf(TinkerSmeltery.scorchedBasin.get());
    this.dropSelf(TinkerSmeltery.scorchedTable.get());
  }


  /*
   * Utils
   */

  private static final LootItemCondition.Builder SILK_TOUCH = MatchTool.toolMatches(ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))));
  private static final LootItemCondition.Builder SHEARS = MatchTool.toolMatches(ItemPredicate.Builder.item().of(Tags.Items.SHEARS));
  private static final LootItemCondition.Builder SILK_TOUCH_OR_SHEARS = SHEARS.or(SILK_TOUCH);

  protected static LootTable.Builder onlyShearsTag(ItemLike item) {
    return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).when(SHEARS).add(LootItem.lootTableItem(item)));
  }

  private static LootTable.Builder droppingSilkOrShearsTag(Block block, LootPoolEntryContainer.Builder<?> alternativeLootEntry) {
    return createSelfDropDispatchTable(block, SILK_TOUCH_OR_SHEARS, alternativeLootEntry);
  }

  private static LootTable.Builder dropSapling(Block blockIn, Block saplingIn, float... fortuneIn) {
    return droppingSilkOrShearsTag(blockIn, applyExplosionCondition(blockIn, LootItem.lootTableItem(saplingIn)).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, fortuneIn)));
  }

  private static LootTable.Builder randomDropSlimeBallOrSapling(SlimeType foliageType, Block blockIn, Block sapling, float... fortuneIn) {
    return dropSapling(blockIn, sapling, fortuneIn)
      .withPool(LootPool.lootPool()
                           .setRolls(ConstantIntValue.exactly(1))
                           .when(HAS_NO_SHEARS_OR_SILK_TOUCH)
                           .add(applyExplosionCondition(blockIn, LootItem.lootTableItem(TinkerCommons.slimeball.get(foliageType)))
                                       .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 1/50f, 1/45f, 1/40f, 1/30f, 1/20f))));

  }

  private static LootTable.Builder droppingWithFunctions(Block block, Function<LootItem.Builder<?>,LootItem.Builder<?>> mapping) {
    return LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(mapping.apply(LootItem.lootTableItem(block)))));
  }

  /**
   * Registers all loot tables for a building block object
   * @param object  Object instance
   */
  private void registerBuildingLootTables(BuildingBlockObject object) {
    this.dropSelf(object.get());
    this.add(object.getSlab(), BlockLoot::createSlabItemTable);
    this.dropSelf(object.getStairs());
  }

  /**
   * Registers all loot tables for a wall building block object
   * @param object  Object instance
   */
  private void registerWallBuildingLootTables(WallBuildingBlockObject object) {
    registerBuildingLootTables(object);
    this.dropSelf(object.getWall());
  }

  /**
   * Registers all loot tables for a fence building block object
   * @param object  Object instance
   */
  private void registerFenceBuildingLootTables(FenceBuildingBlockObject object) {
    registerBuildingLootTables(object);
    this.dropSelf(object.getFence());
  }

  /** Adds all loot tables relevant to the given wood object */
  private void registerWoodLootTables(WoodBlockObject object) {
    registerFenceBuildingLootTables(object);
    // basic
    this.dropSelf(object.getLog());
    this.dropSelf(object.getStrippedLog());
    this.dropSelf(object.getWood());
    this.dropSelf(object.getStrippedWood());
    // door
    this.dropSelf(object.getFenceGate());
    this.add(object.getDoor(), BlockLoot::createDoorTable);
    this.dropSelf(object.getTrapdoor());
    // redstone
    this.dropSelf(object.getPressurePlate());
    this.dropSelf(object.getButton());
    // sign
    this.dropSelf(object.getSign());
  }
}
