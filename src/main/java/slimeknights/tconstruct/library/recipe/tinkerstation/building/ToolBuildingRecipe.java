package slimeknights.tconstruct.library.recipe.tinkerstation.building;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationInventory;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.tables.TinkerTables;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This recipe is used for crafting a set of parts into a tool
 */
@AllArgsConstructor
public class ToolBuildingRecipe implements ITinkerStationRecipe {
  @Getter
  protected final ResourceLocation id;
  @Getter
  protected final String group;
  protected final IModifiable output;

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerTables.toolBuildingRecipeSerializer.get();
  }

  @Override
  public boolean matches(ITinkerStationInventory inv, Level worldIn) {
    if (!inv.getTinkerableStack().isEmpty()) {
      return false;
    }
    List<IToolPart> parts = output.getToolDefinition().getRequiredComponents();
    if (parts.isEmpty()) {
      return false;
    }
    // each part must match the given slot
    int i;
    for (i = 0; i < parts.size(); i++) {
      if (parts.get(i).asItem() != inv.getInput(i).getItem()) {
        return false;
      }
    }
    // remaining slots must be empty
    for (; i < inv.getInputCount(); i++) {
      if (!inv.getInput(i).isEmpty()) {
        return false;
      }
    }

    return true;
  }

  @Override
  public ItemStack assemble(ITinkerStationInventory inv) {
    // first n slots contain parts
    List<IMaterial> materials = IntStream.range(0, output.getToolDefinition().getRequiredComponents().size())
                                         .mapToObj(inv::getInput)
                                         .map(IMaterialItem::getMaterialFromStack)
                                         .collect(Collectors.toList());
    return ToolBuildHandler.buildItemFromMaterials(this.output, materials);
  }

  /** @deprecated Use {@link #getCraftingResult(ITinkerStationInventory)} */
  @Deprecated
  @Override
  public ItemStack getResultItem() {
    return new ItemStack(this.output);
  }
}
