package slimeknights.tconstruct.world;

import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingVisibilityEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper.UnableToFindFieldException;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.common.json.SetFluidLootFunction;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = TConstruct.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldEvents {
  @SubscribeEvent
  static void onBiomeLoad(BiomeLoadingEvent event) {
    BiomeGenerationSettingsBuilder generation = event.getGeneration();

    BiomeCategory category = event.getCategory();
    if (category == BiomeCategory.NETHER) {
      if (Config.COMMON.generateBloodIslands.get()) {
        generation.addStructureStart(TinkerStructures.BLOOD_SLIME_ISLAND);
      }

      if (Config.COMMON.generateCobalt.get()) {
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, TinkerWorld.COBALT_ORE_FEATURE_SMALL);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, TinkerWorld.COBALT_ORE_FEATURE_LARGE);
      }
    }
    else if (category != BiomeCategory.THEEND) {
      // slime spawns anywhere, uses the grass and liquid
      event.getSpawns().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(TinkerWorld.earthSlimeEntity.get(), 100, 2, 4));
      event.getSpawns().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(TinkerWorld.skySlimeEntity.get(), 100, 2, 4));
      // normal sky islands - anywhere
      if (Config.COMMON.generateSkySlimeIslands.get()) {
        generation.addStructureStart(TinkerStructures.SKY_SLIME_ISLAND);
      }
      // clay islands - no forest like biomes
      if (Config.COMMON.generateClayIslands.get() && category != BiomeCategory.TAIGA && category != BiomeCategory.JUNGLE && category != BiomeCategory.FOREST && category != BiomeCategory.OCEAN && category != BiomeCategory.SWAMP) {
        generation.addStructureStart(TinkerStructures.CLAY_ISLAND);
      }
      // ocean islands - ocean
      if (category == BiomeCategory.OCEAN && Config.COMMON.generateEarthSlimeIslands.get()) {
        generation.addStructureStart(TinkerStructures.EARTH_SLIME_ISLAND);
      }
      if (Config.COMMON.generateCopper.get()) {
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, TinkerWorld.COPPER_ORE_FEATURE);
      }
    }
    else if (!doesNameMatchBiomes(event.getName(), Biomes.THE_END, Biomes.THE_VOID)) {
      // slime spawns anywhere, uses the grass and liquid
      event.getSpawns().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(TinkerWorld.enderSlimeEntity.get(), 10, 2, 4));
      if (Config.COMMON.generateEndSlimeIslands.get()) {
        generation.addStructureStart(TinkerStructures.END_SLIME_ISLAND);
      }
    }
  }

  /**
   * Helper method to determine the the given Name matches that of any of the given Biomes
   * @param name - The Name that will be compared to the given Biomes names
   * @param biomes - The Biome that will be used for the check
   */
  private static boolean doesNameMatchBiomes(@Nullable ResourceLocation name, ResourceKey<?>... biomes) {
    for (ResourceKey<?> biome : biomes) {
      if (biome.location().equals(name)) {
        return true;
      }
    }
    return false;
  }


  /* Loot injection */
  private static boolean foundField = false;
  private static Field lootEntries = null;

  /**
   * Adds a loot entry to the given loot pool
   * @param pool   Pool
   * @param entry  Entry
   */
  @SuppressWarnings("unchecked")
  private static void addEntry(LootPool pool, LootPoolEntryContainer entry) {
    // fetch field
    if (!foundField) {
      try {
        lootEntries = ObfuscationReflectionHelper.findField(LootPool.class, "entries");
        lootEntries.setAccessible(true);
        foundField = true;
      } catch (UnableToFindFieldException ex) {
        TConstruct.LOG.error("Failed to find field", ex);
        foundField = true;
        return;
      }
    }
    // access field
    try {
      Object field = lootEntries.get(pool);
      if (field instanceof List) {
        List<LootPoolEntryContainer> entries = (List<LootPoolEntryContainer>) field;
        entries.add(entry);
      }
    } catch (IllegalAccessException|ClassCastException ex) {
      TConstruct.LOG.error("Failed to access field", ex);
      lootEntries = null;
    }
  }

  /**
   * Injects an entry into a loot pool
   * @param event      Loot table evnet
   * @param tableName  Loot table name
   * @param poolName   Pool name
   * @param entry      Entry to inject
   */
  private static void injectInto(LootTableLoadEvent event, String tableName, String poolName, Supplier<LootPoolEntryContainer> entry) {
    ResourceLocation name = event.getName();
    if ("minecraft".equals(name.getNamespace()) && tableName.equals(name.getPath())) {
      LootPool pool = event.getTable().getPool(poolName);
      //noinspection ConstantConditions method is annotated wrongly
      if (pool != null) {
        addEntry(pool, entry.get());
      }
    }
  }

  @SubscribeEvent
  static void onLootTableLoad(LootTableLoadEvent event) {
    BiFunction<SlimeType, Integer, LootPoolEntryContainer> makeSeed = (type, weight) ->
      LootItem.lootTableItem(TinkerWorld.slimeGrassSeeds.get(type)).setWeight(weight)
                   .apply(SetItemCountFunction.setCount(new RandomValueBounds(2, 4))).build();
    BiFunction<SlimeType, Integer, LootPoolEntryContainer> makeSapling = (type, weight) -> LootItem.lootTableItem(TinkerWorld.slimeSapling.get(type)).setWeight(weight).build();
    // sky
    injectInto(event, "chests/simple_dungeon", "pool1", () -> makeSeed.apply(SlimeType.EARTH, 3));
    injectInto(event, "chests/simple_dungeon", "pool1", () -> makeSeed.apply(SlimeType.SKY, 7));
    injectInto(event, "chests/simple_dungeon", "main", () -> makeSapling.apply(SlimeType.EARTH, 3));
    injectInto(event, "chests/simple_dungeon", "main", () -> makeSapling.apply(SlimeType.SKY, 7));
    // ichor
    injectInto(event, "chests/nether_bridge", "main", () -> makeSeed.apply(SlimeType.BLOOD, 5));
    injectInto(event, "chests/bastion_bridge", "pool2", () -> makeSapling.apply(SlimeType.BLOOD, 1));
    // ender
    injectInto(event, "chests/end_city_treasure", "main", () -> makeSeed.apply(SlimeType.ENDER, 5));
    injectInto(event, "chests/end_city_treasure", "main", () -> makeSapling.apply(SlimeType.ENDER, 3));
    // barter for molten blaze lanterns
    injectInto(event, "gameplay/piglin_bartering", "main",
               () -> LootItem.lootTableItem(TinkerSmeltery.scorchedLantern).setWeight(20)
                                  .apply(SetFluidLootFunction.builder(new FluidStack(TinkerFluids.blazingBlood.get(), FluidAttributes.BUCKET_VOLUME / 10)))
                                  .build());
  }


  /* Heads */

  @SubscribeEvent
  public void livingVisibility(LivingVisibilityEvent event) {
    Entity lookingEntity = event.getLookingEntity();
    if (lookingEntity == null) {
      return;
    }
    LivingEntity entity = event.getEntityLiving();
    Item helmet = entity.getItemBySlot(EquipmentSlot.HEAD).getItem();
    Item item = helmet.getItem();
    if (item != Items.AIR && TinkerWorld.headItems.contains(item)) {
      if (lookingEntity.getType() == ((TinkerHeadType)((SkullBlock)((BlockItem)item).getBlock()).type).getType()) {
        event.modifyVisibility(0.5f);
      }
      EntityType<?> lookingType = lookingEntity.getType();
    }
  }

  @SubscribeEvent
  public void creeperKill(LivingDropsEvent event) {
    DamageSource source = event.getSource();
    if (source != null) {
      Entity entity = source.getEntity();
      if (entity instanceof Creeper) {
        Creeper creeper = (Creeper)entity;
        if (creeper.canDropMobsSkull()) {
          LivingEntity dying = event.getEntityLiving();
          TinkerHeadType headType = TinkerHeadType.fromEntityType(dying.getType());
          if (headType != null && Config.COMMON.headDrops.get(headType).get()) {
            creeper.increaseDroppedSkulls();
            event.getDrops().add(dying.spawnAtLocation(TinkerWorld.heads.get(headType)));
          }
        }
      }
    }
  }
}
