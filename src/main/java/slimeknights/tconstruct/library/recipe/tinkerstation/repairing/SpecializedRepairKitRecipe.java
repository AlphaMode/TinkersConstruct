package slimeknights.tconstruct.library.recipe.tinkerstation.repairing;

import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.repairing.SpecializedRepairRecipeSerializer.ISpecializedRepairRecipe;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.recipe.CraftingTableRepairKitRecipe;

/** Recipe for using a repair kit in a crafting station for a specialized tool */
public class SpecializedRepairKitRecipe extends CraftingTableRepairKitRecipe implements ISpecializedRepairRecipe {
  /** Tool that can be repaired with this recipe */
  @Getter
  private final Ingredient tool;
  /** ID of material used in repairing */
  @Getter
  private final MaterialId repairMaterialID;
  /** Cache of the material used to repair */
  private IMaterial repairMaterial;
  public SpecializedRepairKitRecipe(ResourceLocation id, Ingredient tool, MaterialId repairMaterialID) {
    super(id);
    this.tool = tool;
    this.repairMaterialID = repairMaterialID;
  }

  /** Gets the material used to repair */
  private IMaterial getRepairMaterial() {
    if (repairMaterial == null) {
      repairMaterial = MaterialRegistry.getMaterial(repairMaterialID);
    }
    return repairMaterial;
  }

  @Override
  protected boolean toolMatches(ItemStack stack) {
    return tool.test(stack);
  }

  @Override
  protected float getRepairAmount(IModifierToolStack tool, IMaterial repairMaterial) {
    return tool.getDefinition().getData().getBaseStat(ToolStats.DURABILITY) + 1 * 2f / MaterialRecipe.INGOTS_PER_REPAIR;
  }

  @Override
  public boolean matches(CraftingContainer inv, Level worldIn) {
    Pair<ToolStack, IMaterial> inputs = getRelevantInputs(inv);
    return inputs != null && inputs.getSecond() == getRepairMaterial();
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerTables.specializedRepairKitSerializer.get();
  }
}
