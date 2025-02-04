package slimeknights.tconstruct.library.recipe.tinkerstation.repairing;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.tables.TinkerTables;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import slimeknights.mantle.recipe.data.AbstractRecipeBuilder.AbstractFinishedRecipe;

/** Builds a recipe to repair a tool in the tinker station */
@RequiredArgsConstructor(staticName = "repair")
public class SpecializedRepairRecipeBuilder extends AbstractRecipeBuilder<SpecializedRepairRecipeBuilder> {
  private final Ingredient tool;
  private final MaterialId repairMaterial;

  /** Creates a builder from the given item and material */
  public static SpecializedRepairRecipeBuilder repair(ItemLike item, MaterialId repairMaterial) {
    return repair(Ingredient.of(item), repairMaterial);
  }

  @Override
  public void build(Consumer<FinishedRecipe> consumer) {
    build(consumer, repairMaterial);
  }

  /** Builds the recipe for the crafting table using a repair kit */
  public SpecializedRepairRecipeBuilder buildRepairKit(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    ResourceLocation advancementId = buildOptionalAdvancement(id, "tinker_station");
    consumer.accept(new FinishedRecipe(id, advancementId, TinkerTables.specializedRepairKitSerializer.get()));
    return this;
  }

  @Override
  public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    ResourceLocation advancementId = buildOptionalAdvancement(id, "tinker_station");
    consumer.accept(new FinishedRecipe(id, advancementId, TinkerTables.specializedRepairSerializer.get()));
  }

  private class FinishedRecipe extends AbstractFinishedRecipe {
    @Getter
    private final RecipeSerializer<?> serializer;

    public FinishedRecipe(ResourceLocation ID, @Nullable ResourceLocation advancementID, RecipeSerializer<?> serializer) {
      super(ID, advancementID);
      this.serializer = serializer;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      json.add("tool", tool.toJson());
      json.addProperty("repair_material", repairMaterial.toString());
    }
  }
}
