package slimeknights.tconstruct.tools.modifiers.slotless;

import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.util.Constants.NBT;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.SingleUseModifier;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.nbt.IModDataReadOnly;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;

import java.util.Iterator;
import java.util.List;

/** Modifier that adds a variable number of slots to a tool. Could easily be done via NBT editing, but this makes it easier */
public class CreativeSlotModifier extends SingleUseModifier {
  /** Key representing the prefix in the lang file */
  private static final String SLOT_PREFIX = TConstruct.makeTranslationKey("modifier", "creative_slot.prefix");
  /** Key representing the slots object in the modifier */
  public static final ResourceLocation KEY_SLOTS = TConstruct.getResource("creative");

  public CreativeSlotModifier() {
    super(0xCCBA47);
  }

  @Override
  public void addVolatileData(Item item, ToolDefinition toolDefinition, StatsNBT baseStats, IModDataReadOnly persistentData, int level, ModDataNBT volatileData) {
    if (persistentData.contains(KEY_SLOTS, NBT.TAG_COMPOUND)) {
      CompoundTag slots = persistentData.getCompound(KEY_SLOTS);
      for (String key : slots.getAllKeys()) {
        SlotType slotType = SlotType.getIfPresent(key);
        if (slotType != null) {
          volatileData.addSlots(slotType, slots.getInt(key));
        }
      }
    }
  }

  @Override
  public void addInformation(IModifierToolStack tool, int level, List<Component> tooltip, boolean isAdvanced, boolean detailed) {
    if (detailed) {
      IModDataReadOnly persistentData = tool.getPersistentData();
      if (persistentData.contains(KEY_SLOTS, NBT.TAG_COMPOUND)) {
        CompoundTag slots = persistentData.getCompound(KEY_SLOTS);

        // first, find the first valid slot
        Iterator<String> keys = slots.getAllKeys().iterator();
        while (keys.hasNext()) {
          String key = keys.next();
          SlotType slotType = SlotType.getIfPresent(key);
          if (slotType != null) {
            tooltip.add(new TranslatableComponent(SLOT_PREFIX));


            tooltip.add(
              new TextComponent("* +" + slots.getInt(key) + " ")
                .append(slotType.getDisplayName())
                .withStyle(style -> style.withColor(slotType.getColor())));

            // after that, the next valid slots need comma separation
            while (keys.hasNext()) {
              key = keys.next();
              SlotType slotType2 = SlotType.getIfPresent(key);
              if (slotType2 != null) {
                tooltip.add(new StringTextComponent("* +" + slots.getInt(key) + " ")
                              .appendSibling(slotType2.getDisplayName())
                              .modifyStyle(style -> style.setColor(slotType2.getColor())));
              }
            }
            break;
          }
        }
      }
    }
  }
}
