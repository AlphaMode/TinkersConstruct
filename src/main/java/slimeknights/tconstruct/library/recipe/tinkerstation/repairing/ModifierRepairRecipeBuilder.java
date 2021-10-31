package slimeknights.tconstruct.library.recipe.tinkerstation.repairing;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.tools.TinkerModifiers;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import slimeknights.mantle.recipe.data.AbstractRecipeBuilder.AbstractFinishedRecipe;

/** Builds a recipe to repair a tool using a modifier */
@RequiredArgsConstructor(staticName = "repair")
public class ModifierRepairRecipeBuilder extends AbstractRecipeBuilder<ModifierRepairRecipeBuilder> {
  private final Modifier modifier;
  private final Ingredient ingredient;
  private final int repairAmount;

  @Override
  public void build(Consumer<FinishedRecipe> consumer) {
    build(consumer, modifier.getId());
  }

  /** Builds the recipe for the crafting table using a repair kit */
  public ModifierRepairRecipeBuilder buildCraftingTable(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    ResourceLocation advancementId = buildOptionalAdvancement(id, "tinker_station");
    consumer.accept(new FinishedRecipe(id, advancementId, TinkerModifiers.craftingModifierRepair.get()));
    return this;
  }

  @Override
  public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    ResourceLocation advancementId = buildOptionalAdvancement(id, "tinker_station");
    consumer.accept(new FinishedRecipe(id, advancementId, TinkerModifiers.modifierRepair.get()));
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
      json.addProperty("modifier", modifier.getId().toString());
      json.add("ingredient", ingredient.toJson());
      json.addProperty("repair_amount", repairAmount);
    }
  }
}
