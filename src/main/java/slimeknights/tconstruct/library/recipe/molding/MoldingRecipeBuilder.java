package slimeknights.tconstruct.library.recipe.molding;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.util.ResourceLocation;
import slimeknights.mantle.recipe.ItemOutput;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

@RequiredArgsConstructor(staticName = "molding")
public class MoldingRecipeBuilder extends AbstractRecipeBuilder<MoldingRecipeBuilder> {
  private final ItemOutput output;
  private final MoldingRecipe.Serializer<?> serializer;
  private Ingredient material = Ingredient.EMPTY;
  private Ingredient pattern = Ingredient.EMPTY;
  private boolean patternConsumed = false;

  /**
   * Creates a new builder of the given item
   * @param item  Item output
   * @return  Recipe
   */
  public static MoldingRecipeBuilder moldingTable(ItemLike item) {
    return molding(ItemOutput.fromItem(item), TinkerSmeltery.moldingTableSerializer.get());
  }

  /**
   * Creates a new builder of the given item
   * @param item  Item output
   * @return  Recipe
   */
  public static MoldingRecipeBuilder moldingBasin(ItemLike item) {
    return molding(ItemOutput.fromItem(item), TinkerSmeltery.moldingBasinSerializer.get());
  }

  /* Inputs */

  /** Sets the material item, on the table */
  public MoldingRecipeBuilder setMaterial(Ingredient ingredient) {
    this.material = ingredient;
    return this;
  }

  /** Sets the material item, on the table */
  public MoldingRecipeBuilder setMaterial(ItemLike item) {
    return setMaterial(Ingredient.fromItems(item));
  }

  /** Sets the material item, on the table */
  public MoldingRecipeBuilder setMaterial(Tag<Item> tag) {
    return setMaterial(Ingredient.fromTag(tag));
  }

  /** Sets the mold item, in the players hand */
  public MoldingRecipeBuilder setPattern(Ingredient ingredient, boolean consumed) {
    this.pattern = ingredient;
    this.patternConsumed = consumed;
    return this;
  }

  /** Sets the mold item, in the players hand */
  public MoldingRecipeBuilder setPattern(ItemLike item, boolean consumed) {
    return setPattern(Ingredient.fromItems(item), consumed);
  }

  /** Sets the mold item, in the players hand */
  public MoldingRecipeBuilder setPattern(Tag<Item> tag, boolean consumed) {
    return setPattern(Ingredient.fromTag(tag), consumed);
  }


  /* Building */

  @Override
  public void build(Consumer<IFinishedRecipe> consumer) {
    build(consumer, Objects.requireNonNull(output.get().getItem().getRegistryName()));
  }

  @Override
  public void build(Consumer<IFinishedRecipe> consumer, ResourceLocation id) {
    if (material == Ingredient.EMPTY) {
      throw new IllegalStateException("Missing material for molding recipe");
    }
    ResourceLocation advancementId = buildOptionalAdvancement(id, "molding");
    consumer.accept(new FinishedRecipe(id, advancementId));
  }

  private class FinishedRecipe extends AbstractFinishedRecipe {
    public FinishedRecipe(ResourceLocation ID, @Nullable ResourceLocation advancementID) {
      super(ID, advancementID);
    }

    @Override
    public void serialize(JsonObject json) {
      json.add("material", material.serialize());
      if (pattern != Ingredient.EMPTY) {
        json.add("pattern", pattern.serialize());
        if (patternConsumed) {
          json.addProperty("pattern_consumed", true);
        }
      }
      json.add("result", output.serialize());
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
      return serializer;
    }
  }
}
