package slimeknights.tconstruct.library.recipe.casting.material;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.mantle.recipe.RecipeHelper;
import slimeknights.tconstruct.common.recipe.LoggingRecipeSerializer;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.recipe.RecipeTypes;
import slimeknights.tconstruct.library.recipe.casting.DisplayCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.recipe.ICastingInventory;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Casting recipe taking a part of a material and a fluid and outputting the part with a new material
 */
public abstract class CompositeCastingRecipe extends MaterialCastingRecipe {
  public CompositeCastingRecipe(RecipeType<?> type, ResourceLocation id, String group, IMaterialItem result, int itemCost) {
    super(type, id, group, Ingredient.of(result), itemCost, result, true, false);
  }

  @Override
  protected Optional<MaterialFluidRecipe> getMaterialFluid(ICastingInventory inv) {
    return MaterialCastingLookup.getCompositeFluid(inv);
  }

  /* JEI display */
  @Override
  public List<IDisplayableCastingRecipe> getRecipes() {
    if (multiRecipes == null) {
      RecipeType<?> type = getType();
      multiRecipes = MaterialCastingLookup
        .getAllCompositeFluids().stream()
        .filter(recipe -> {
          IMaterial output = recipe.getOutput();
          IMaterial input = recipe.getInput();
          return output != IMaterial.UNKNOWN && input != null && input != IMaterial.UNKNOWN
            && !output.isHidden() && !input.isHidden() && result.canUseMaterial(output) && result.canUseMaterial(input);
        })
        .map(recipe -> {
          List<FluidStack> fluids = resizeFluids(recipe.getFluids());
          int fluidAmount = fluids.stream().mapToInt(FluidStack::getAmount).max().orElse(0);
          return new DisplayCastingRecipe(type, Collections.singletonList(result.withMaterial(recipe.getInput())), fluids, result.withMaterial(recipe.getOutput()),
                                          ICastingRecipe.calcCoolingTime(recipe.getTemperature(), itemCost * fluidAmount), consumed);
        })
        .collect(Collectors.toList());
    }
    return multiRecipes;
  }

  /** Basin implementation */
  public static class Basin extends CompositeCastingRecipe {
    public Basin(ResourceLocation id, String group, IMaterialItem result, int itemCost) {
      super(RecipeTypes.CASTING_BASIN, id, group, result, itemCost);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
      return TinkerSmeltery.basinCompositeSerializer.get();
    }
  }

  /** Table implementation */
  public static class Table extends CompositeCastingRecipe {
    public Table(ResourceLocation id, String group, IMaterialItem result, int itemCost) {
      super(RecipeTypes.CASTING_TABLE, id, group, result, itemCost);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
      return TinkerSmeltery.tableCompositeSerializer.get();
    }
  }

  /**
   * Interface representing a composite casting recipe constructor
   * @param <T>  Recipe class type
   */
  public interface IFactory<T extends CompositeCastingRecipe> {
    T create(ResourceLocation id, String group, IMaterialItem result, int itemCost);
  }

  /** Shared serializer logic */
  @RequiredArgsConstructor
  public static class Serializer<T extends CompositeCastingRecipe> extends LoggingRecipeSerializer<T> {
    private final IFactory<T> factory;

    @Override
    public T read(ResourceLocation id, JsonObject json) {
      String group = GsonHelper.getString(json, "group", "");
      IMaterialItem result = RecipeHelper.deserializeItem(GsonHelper.getString(json, "result"), "result", IMaterialItem.class);
      int itemCost = GsonHelper.getInt(json, "item_cost");
      return factory.create(id, group, result, itemCost);
    }

    @Nullable
    @Override
    protected T readSafe(ResourceLocation id, FriendlyByteBuf buffer) {
      String group = buffer.readString(Short.MAX_VALUE);
      IMaterialItem result = RecipeHelper.readItem(buffer, IMaterialItem.class);
      int itemCost = buffer.readVarInt();
      return factory.create(id, group, result, itemCost);
    }

    @Override
    protected void writeSafe(FriendlyByteBuf buffer, T recipe) {
      buffer.writeString(recipe.group);
      RecipeHelper.writeItem(buffer, recipe.result);
      buffer.writeVarInt(recipe.itemCost);
    }
  }
}
