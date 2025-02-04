package slimeknights.tconstruct.library.data.recipe;

import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.SingleItemRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject;
import slimeknights.tconstruct.common.registration.MetalItemObject;

import java.util.Objects;
import java.util.function.Consumer;

public interface ICommonRecipeHelper extends IRecipeHelper {
  /* Metals */

  /**
   * Registers a recipe packing a small item into a large one
   * @param consumer   Recipe consumer
   * @param large      Large item
   * @param small      Small item
   * @param largeName  Large name
   * @param smallName  Small name
   * @param folder     Recipe folder
   */
  default void packingRecipe(Consumer<FinishedRecipe> consumer, String largeName, ItemLike large, String smallName, ItemLike small, String folder) {
    // ingot to block
    ShapedRecipeBuilder.shaped(large)
                       .define('#', small)
                       .pattern("###")
                       .pattern("###")
                       .pattern("###")
                       .unlockedBy("has_item", RecipeProvider.has(small))
                       .group(Objects.requireNonNull(large.asItem().getRegistryName()).toString())
                       .save(consumer, wrap(large.asItem(), folder, String.format("_from_%ss", smallName)));
    // block to ingot
    ShapelessRecipeBuilder.shapeless(small, 9)
                          .requires(large)
                          .unlockedBy("has_item", RecipeProvider.has(large))
                          .group(Objects.requireNonNull(small.asItem().getRegistryName()).toString())
                          .save(consumer, wrap(small.asItem(), folder, String.format("_from_%s", largeName)));
  }

  /**
   * Registers a recipe packing a small item into a large one
   * @param consumer   Recipe consumer
   * @param largeItem  Large item
   * @param smallItem  Small item
   * @param smallTag   Tag for small item
   * @param largeName  Large name
   * @param smallName  Small name
   * @param folder     Recipe folder
   */
  default void packingRecipe(Consumer<FinishedRecipe> consumer, String largeName, ItemLike largeItem, String smallName, ItemLike smallItem, Tag<Item> smallTag, String folder) {
    // ingot to block
    // note our item is in the center, any mod allowed around the edges
    ShapedRecipeBuilder.shaped(largeItem)
                       .define('#', smallTag)
                       .define('*', smallItem)
                       .pattern("###")
                       .pattern("#*#")
                       .pattern("###")
                       .unlockedBy("has_item", RecipeProvider.has(smallItem))
                       .group(Objects.requireNonNull(largeItem.asItem().getRegistryName()).toString())
                       .save(consumer, wrap(largeItem.asItem(), folder, String.format("_from_%ss", smallName)));
    // block to ingot
    ShapelessRecipeBuilder.shapeless(smallItem, 9)
                          .requires(largeItem)
                          .unlockedBy("has_item", RecipeProvider.has(largeItem))
                          .group(Objects.requireNonNull(smallItem.asItem().getRegistryName()).toString())
                          .save(consumer, wrap(smallItem.asItem(), folder, String.format("_from_%s", largeName)));
  }

  /**
   * Adds recipes to convert a block to ingot, ingot to block, and for nuggets
   * @param consumer  Recipe consumer
   * @param metal     Metal object
   * @param folder    Folder for recipes
   */
  default void metalCrafting(Consumer<FinishedRecipe> consumer, MetalItemObject metal, String folder) {
    ItemLike ingot = metal.getIngot();
    packingRecipe(consumer, "block", metal.get(), "ingot", ingot, metal.getIngotTag(), folder);
    packingRecipe(consumer, "ingot", ingot, "nugget", metal.getNugget(), metal.getNuggetTag(), folder);
  }


  /* Building blocks */

  /**
   * Registers generic building block recipes for slabs and stairs
   * @param consumer  Recipe consumer
   * @param building  Building object instance
   */
  default void slabStairsCrafting(Consumer<IFinishedRecipe> consumer, BuildingBlockObject building, String folder, boolean addStonecutter) {
    Item item = building.asItem();
    ICriterionInstance hasBlock = RecipeProvider.hasItem(item);
    // slab
    ItemLike slab = building.getSlab();
    ShapedRecipeBuilder.shapedRecipe(slab, 6)
                       .key('B', item)
                       .patternLine("BBB")
                       .addCriterion("has_item", hasBlock)
                       .setGroup(Objects.requireNonNull(slab.asItem().getRegistryName()).toString())
                       .build(consumer, wrap(item, folder, "_slab"));
    // stairs
    ItemLike stairs = building.getStairs();
    ShapedRecipeBuilder.shapedRecipe(stairs, 4)
                       .key('B', item)
                       .patternLine("B  ")
                       .patternLine("BB ")
                       .patternLine("BBB")
                       .addCriterion("has_item", hasBlock)
                       .setGroup(Objects.requireNonNull(stairs.asItem().getRegistryName()).toString())
                       .build(consumer, wrap(item, folder, "_stairs"));

    // only add stonecutter if relevant
    if (addStonecutter) {
      Ingredient ingredient = Ingredient.fromItems(item);
      SingleItemRecipeBuilder.stonecuttingRecipe(ingredient, slab, 2)
                             .addCriterion("has_item", hasBlock)
                             .build(consumer, wrap(item, folder, "_slab_stonecutter"));
      SingleItemRecipeBuilder.stonecuttingRecipe(ingredient, stairs)
                             .addCriterion("has_item", hasBlock)
                             .build(consumer, wrap(item, folder, "_stairs_stonecutter"));
    }
  }

  /**
   * Registers generic building block recipes for slabs, stairs, and walls
   * @param consumer  Recipe consumer
   * @param building  Building object instance
   */
  default void stairSlabWallCrafting(Consumer<IFinishedRecipe> consumer, WallBuildingBlockObject building, String folder, boolean addStonecutter) {
    slabStairsCrafting(consumer, building, folder, addStonecutter);
    // wall
    Item item = building.asItem();
    ICriterionInstance hasBlock = RecipeProvider.hasItem(item);
    ItemLike wall = building.getWall();
    ShapedRecipeBuilder.shapedRecipe(wall, 6)
                       .key('B', item)
                       .patternLine("BBB")
                       .patternLine("BBB")
                       .addCriterion("has_item", hasBlock)
                       .setGroup(Objects.requireNonNull(wall.asItem().getRegistryName()).toString())
                       .build(consumer, wrap(item, folder, "_wall"));
    // only add stonecutter if relevant
    if (addStonecutter) {
      Ingredient ingredient = Ingredient.fromItems(item);
      SingleItemRecipeBuilder.stonecuttingRecipe(ingredient, wall)
                             .addCriterion("has_item", hasBlock)
                             .build(consumer, wrap(item, folder, "_wall_stonecutter"));
    }
  }

  /**
   * Registers recipes relevant to wood
   * @param consumer  Recipe consumer
   * @param wood      Wood types
   * @param folder    Wood folder
   */
  default void woodCrafting(Consumer<IFinishedRecipe> consumer, WoodBlockObject wood, String folder) {
    ICriterionInstance hasPlanks = RecipeProvider.hasItem(wood);

    // planks
    ShapelessRecipeBuilder.shapelessRecipe(wood, 4).addIngredient(wood.getLogItemTag())
                          .setGroup("planks")
                          .addCriterion("has_log", RecipeProvider.hasItem(ItemPredicate.Builder.create().tag(wood.getLogItemTag()).build()))
                          .build(consumer, modResource(folder + "planks"));
    // slab
    ItemLike slab = wood.getSlab();
    ShapedRecipeBuilder.shapedRecipe(slab, 6)
                       .key('#', wood)
                       .patternLine("###")
                       .addCriterion("has_planks", hasPlanks)
                       .setGroup("wooden_slab")
                       .build(consumer, modResource(folder + "slab"));
    // stairs
    ItemLike stairs = wood.getStairs();
    ShapedRecipeBuilder.shapedRecipe(stairs, 4)
                       .key('#', wood)
                       .patternLine("#  ")
                       .patternLine("## ")
                       .patternLine("###")
                       .addCriterion("has_planks", hasPlanks)
                       .setGroup("wooden_stairs")
                       .build(consumer, modResource(folder + "stairs"));

    // log to stripped
    ShapedRecipeBuilder.shapedRecipe(wood.getWood(), 3)
                       .key('#', wood.getLog())
                       .patternLine("##").patternLine("##")
                       .setGroup("bark")
                       .addCriterion("has_log", RecipeProvider.hasItem(wood.getLog()))
                       .build(consumer, modResource(folder + "log_to_wood"));
    ShapedRecipeBuilder.shapedRecipe(wood.getStrippedWood(), 3)
                       .key('#', wood.getStrippedLog())
                       .patternLine("##").patternLine("##")
                       .setGroup("bark")
                       .addCriterion("has_log", RecipeProvider.hasItem(wood.getStrippedLog()))
                       .build(consumer, modResource(folder + "stripped_log_to_wood"));
    // doors
    ShapedRecipeBuilder.shapedRecipe(wood.getFence(), 3)
                       .key('#', Tags.Items.RODS_WOODEN).key('W', wood)
                       .patternLine("W#W").patternLine("W#W")
                       .setGroup("wooden_fence")
                       .addCriterion("has_planks", hasPlanks)
                       .build(consumer, modResource(folder + "fence"));
    ShapedRecipeBuilder.shapedRecipe(wood.getFenceGate())
                       .key('#', Items.STICK).key('W', wood)
                       .patternLine("#W#").patternLine("#W#")
                       .setGroup("wooden_fence_gate")
                       .addCriterion("has_planks", hasPlanks)
                       .build(consumer, modResource(folder + "fence_gate"));
    ShapedRecipeBuilder.shapedRecipe(wood.getDoor(), 3)
                       .key('#', wood)
                       .patternLine("##").patternLine("##").patternLine("##")
                       .setGroup("wooden_door")
                       .addCriterion("has_planks", hasPlanks)
                       .build(consumer, modResource(folder + "door"));
    ShapedRecipeBuilder.shapedRecipe(wood.getTrapdoor(), 2)
                       .key('#', wood)
                       .patternLine("###").patternLine("###")
                       .setGroup("wooden_trapdoor")
                       .addCriterion("has_planks", hasPlanks)
                       .build(consumer, modResource(folder + "trapdoor"));
    // buttons
    ShapelessRecipeBuilder.shapelessRecipe(wood.getButton())
                          .addIngredient(wood)
                          .setGroup("wooden_button")
                          .addCriterion("has_planks", hasPlanks)
                          .build(consumer, modResource(folder + "button"));
    ShapedRecipeBuilder.shapedRecipe(wood.getPressurePlate())
                       .key('#', wood)
                       .patternLine("##")
                       .setGroup("wooden_pressure_plate")
                       .addCriterion("has_planks", hasPlanks)
                       .build(consumer, modResource(folder + "pressure_plate"));
    // signs
    ShapedRecipeBuilder.shapedRecipe(wood.getSign(), 3)
                       .setGroup("sign")
                       .key('#', wood).key('X', Tags.Items.RODS_WOODEN)
                       .patternLine("###").patternLine("###").patternLine(" X ")
                       .addCriterion("has_planks", RecipeProvider.hasItem(wood))
                       .build(consumer, modResource(folder + "sign"));

  }
}
