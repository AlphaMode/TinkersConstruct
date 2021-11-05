package slimeknights.tconstruct.tools.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import net.minecraft.world.item.Item.Properties;

public class CreativeSlotItem extends Item {
  private static final String NBT_KEY = "slot";
  private static final String TOOLTIP = TConstruct.makeTranslationKey("item", "creative_slot.tooltip");
  private static final Component TOOLTIP_MISSING = TConstruct.makeTranslation("item", "creative_slot.missing").withStyle(ChatFormatting.RED);

  public CreativeSlotItem(Properties properties) {
    super(properties);
  }

  /** Gets the value of the slot tag from the given stack */
  @Nullable
  public static SlotType getSlot(ItemStack stack) {
    CompoundTag nbt = stack.getTag();
    if (nbt != null && nbt.contains(NBT_KEY, NBT.TAG_STRING)) {
      return SlotType.getIfPresent(nbt.getString(NBT_KEY));
    }
    return null;
  }

  /** Makes an item stack with the given slot type */
  public static ItemStack withSlot(ItemStack stack, SlotType type) {
    stack.getOrCreateTag().putString(NBT_KEY, type.getName());
    return stack;
  }

  @Override
  public String getTranslationKey(ItemStack stack) {
    SlotType slot = getSlot(stack);
    String originalKey = getTranslationKey();
    if (slot != null) {
      String betterKey = originalKey + "." + slot.getName();
      if (Util.canTranslate(betterKey)) {
        return betterKey;
      }
    }
    return originalKey;
  }

  @Override
  public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
    SlotType slot = getSlot(stack);
    if (slot != null) {
      tooltip.add(new TranslationTextComponent(TOOLTIP, slot.getDisplayName()).mergeStyle(ChatFormatting.GRAY));
    } else {
      tooltip.add(TOOLTIP_MISSING);
    }
  }

  @Override
  public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
    if (isInGroup(group)) {
      Collection<SlotType> allTypes = SlotType.getAllSlotTypes();
      if (allTypes.isEmpty()) {
        items.add(new ItemStack(this));
      } else {
        for (SlotType type : allTypes) {
          items.add(withSlot(new ItemStack(this), type));
        }
      }
    }
  }
}
