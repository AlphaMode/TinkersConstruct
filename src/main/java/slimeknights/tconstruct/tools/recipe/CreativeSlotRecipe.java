package slimeknights.tconstruct.tools.recipe;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants.NBT;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationInventory;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.item.CreativeSlotItem;
import slimeknights.tconstruct.tools.modifiers.slotless.CreativeSlotModifier;

import javax.annotation.Nullable;

/**
 * Recipe to add additional slots with the creative modifier
 */
@RequiredArgsConstructor
public class CreativeSlotRecipe implements ITinkerStationRecipe, IModifierRecipe {
  @Getter
  private final ResourceLocation id;

  /**
   * Finds the slot type from the inventory
   * @param inv             Inventory
   * @param stopAfterFirst  If true, stops after the first item is found
   * @return  Slot type found, or null if invalid
   */
  @Nullable
  private SlotType findSlotType(ITinkerStationInventory inv, boolean stopAfterFirst) {
    // goal is to find exactly 1 stack of creative modifiers
    SlotType type = null;
    for (int i = 0; i < inv.getInputCount(); i++) {
      ItemStack stack = inv.getInput(i);
      if (!stack.isEmpty()) {
        // must be the first creative slot modifer, anymore than one is invalid
        if (type != null || stack.getItem() != TinkerModifiers.creativeSlotItem.get()) {
          return null;
        }
        // must have a valid slot
        type = CreativeSlotItem.getSlot(stack);
        if (type == null) {
          return null;
        }
        if (stopAfterFirst) {
          return type;
        }
      }
    }
    return type;
  }

  @Override
  public boolean matches(ITinkerStationInventory inv, Level world) {
    // must be modifiable
    if (!TinkerTags.Items.MODIFIABLE.contains(inv.getTinkerableStack().getItem())) {
      return false;
    }
    return findSlotType(inv, false) != null;
  }

  @Override
  public ItemStack assemble(ITinkerStationInventory inv) {
    ToolStack toolStack = ToolStack.copyFrom(inv.getTinkerableStack());

    // first, fetch the slots compound
    CompoundTag slots;
    ModDataNBT persistentData = toolStack.getPersistentData();
    if (persistentData.contains(CreativeSlotModifier.KEY_SLOTS, NBT.TAG_COMPOUND)) {
      slots = persistentData.getCompound(CreativeSlotModifier.KEY_SLOTS);
    } else {
      slots = new CompoundTag();
      persistentData.put(CreativeSlotModifier.KEY_SLOTS, slots);
    }

    // find the input
    SlotType slotType = findSlotType(inv, true);
    if (slotType != null) {
      String name = slotType.getName();
      slots.putInt(name, slots.getInt(name) + 1);
    }

    // add the modifier if needed
    if (toolStack.getModifierLevel(TinkerModifiers.creativeSlot.get()) == 0) {
      toolStack.addModifier(TinkerModifiers.creativeSlot.get(), 1);
    } else {
      toolStack.rebuildStats();
    }
    return toolStack.createStack();
  }

  @Override
  public Modifier getModifier() {
    return TinkerModifiers.creativeSlot.get();
  }

  /** @deprecated Use {@link #getCraftingResult(ITinkerStationInventory)} */
  @Deprecated
  @Override
  public ItemStack getRecipeOutput() {
    return ItemStack.EMPTY;
  }

  @Override
  public IRecipeSerializer<?> getSerializer() {
    return TinkerModifiers.creativeSlotSerializer.get();
  }
}
