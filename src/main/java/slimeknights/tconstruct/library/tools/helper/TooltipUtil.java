package slimeknights.tconstruct.library.tools.helper;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.definition.PartRequirement;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.utils.TooltipFlag;
import slimeknights.tconstruct.library.utils.TooltipKey;

import java.util.List;

/** Helper functions for adding tooltips to tools */
public class TooltipUtil {
  /** Tool tag to set that makes a tool a display tool */
  public static final String KEY_DISPLAY = "tic_display";

  private TooltipUtil() {}

  /** Tooltip telling the player to hold shift for more info */
  public static final Component TOOLTIP_HOLD_SHIFT = TConstruct.makeTranslation("tooltip", "hold_shift", TConstruct.makeTranslation("key", "shift").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
  /** Tooltip telling the player to hold control for part info */
  public static final Component TOOLTIP_HOLD_CTRL = TConstruct.makeTranslation("tooltip", "hold_ctrl", TConstruct.makeTranslation("key", "ctrl").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
  /** Tooltip for when tool data is missing */
  private static final Component NO_DATA = TConstruct.makeTranslation("tooltip", "missing_data").withStyle(ChatFormatting.GRAY);

  /**
   * If true, this stack was created for display, so some of the tooltip is suppressed
   * @param stack  Stack to check
   * @return  True if marked display
   */
  public static boolean isDisplay(ItemStack stack) {
    CompoundTag nbt = stack.getTag();
    return nbt != null && nbt.getBoolean(KEY_DISPLAY);
  }

  /**
   * Full logic for adding tooltip information
   */
  public static void addInformation(IModifiableDisplay item, ItemStack stack, List<Component> tooltip, TooltipKey tooltipKey, boolean isAdvanced) {
    // if the display tag is set, just show modifiers
    if (isDisplay(stack)) {
      ToolStack tool = ToolStack.from(stack);
      for (ModifierEntry entry : tool.getModifierList()) {
        if (entry.getModifier().shouldDisplay(false)) {
          tooltip.add(entry.getModifier().getDisplayName(tool, entry.getLevel()));
        }
      }
      // if not initialized, show no data tooltip on non-standard items
    } else if (!ToolStack.isInitialized(stack)) {
      if (item.getToolDefinition().isMultipart()) {
        tooltip.add(NO_DATA);
      }
    } else {
      switch (tooltipKey) {
        case SHIFT:
          item.getStatInformation(ToolStack.from(stack), tooltip, isAdvanced ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL);
          break;
        case CONTROL:
          if (item.getToolDefinition().isMultipart()) {
            TooltipUtil.getComponents(item, stack, tooltip);
            break;
          }
          // intentional fallthrough
        default:
          getDefaultInfo(stack, tooltip);
          break;
      }
    }
  }

  /**
   * Adds information when holding neither control nor shift
   * @param stack     Stack instance
   * @param tooltips  Tooltip list
   */
  public static void getDefaultInfo(ItemStack stack, List<Component> tooltips) {
    ToolStack tool = ToolStack.from(stack);
    // shows as broken when broken, hold shift for proper durability
    if (stack.isDamageableItem()) {
      tooltips.add(TooltipBuilder.formatDurability(tool.getCurrentDurability(), tool.getStats().getInt(ToolStats.DURABILITY), true));
    }
    // modifier tooltip
    for (ModifierEntry entry : tool.getModifierList()) {
      if (entry.getModifier().shouldDisplay(false)) {
        tooltips.add(entry.getModifier().getDisplayName(tool, entry.getLevel()));
      }
    }
    tooltips.add(TextComponent.EMPTY);
    tooltips.add(TOOLTIP_HOLD_SHIFT);
    if (tool.getDefinition().isMultipart()) {
      tooltips.add(TOOLTIP_HOLD_CTRL);
    }
  }

  /**
   * Gets the  default information for the given tool stack
   *
   * @param tool      the tool stack
   * @param tooltip   Tooltip list
   * @param flag      Tooltip flag
   * @return List from the parameter after filling
   */
  public static List<Component> getDefaultStats(IModifierToolStack tool, List<Component> tooltip, TooltipFlag flag) {
    TooltipBuilder builder = new TooltipBuilder(tool, tooltip);
    Item item = tool.getItem();
    if (TinkerTags.Items.DURABILITY.contains(item)) {
      builder.addDurability();
    }
    if (TinkerTags.Items.MELEE.contains(item)) {
      builder.addWithAttribute(ToolStats.ATTACK_DAMAGE, Attributes.ATTACK_DAMAGE);
      builder.add(ToolStats.ATTACK_SPEED);
    }
    if (TinkerTags.Items.HARVEST.contains(item)) {
      if (TinkerTags.Items.HARVEST_PRIMARY.contains(tool.getItem())) {
        builder.add(ToolStats.HARVEST_LEVEL);
      }
      builder.add(ToolStats.MINING_SPEED);
    }

    builder.addAllFreeSlots();

    for (ModifierEntry entry : tool.getModifierList()) {
      entry.getModifier().addInformation(tool, entry.getLevel(), tooltip, flag);
    }

    return builder.getTooltips();
  }

  /**
   * Gets the tooltip of the components list of a tool
   * @param item      Modifiable item instance
   * @param stack     Item stack being displayed
   * @param tooltips  List of tooltips
   */
  public static void getComponents(IModifiable item, ItemStack stack, List<Component> tooltips) {
    // no components, nothing to do
    List<PartRequirement> components = item.getToolDefinition().getData().getParts();
    if (components.isEmpty()) {
      return;
    }
    // no materials is bad
    List<IMaterial> materials = ToolStack.from(stack).getMaterialsList();
    if (materials.isEmpty()) {
      tooltips.add(NO_DATA);
      return;
    }
    // wrong number is bad
    if (materials.size() < components.size()) {
      return;
    }
    // finally, display them all
    int max = components.size() - 1;
    for (int i = 0; i <= max; i++) {
      PartRequirement requirement = components.get(i);
      IMaterial material = materials.get(i);
      ItemStack partStack = requirement.getPart().withMaterial(material);
      tooltips.add(partStack.getDisplayName().copy().withStyle(ChatFormatting.UNDERLINE).withStyle(style -> style.withColor(material.getColor())));
      MaterialRegistry.getInstance().getMaterialStats(material.getIdentifier(), requirement.getStatType()).ifPresent(stat -> tooltips.addAll(stat.getLocalizedInfo()));
      if (i != max) {
        tooltips.add(TextComponent.EMPTY);
      }
    }
  }
}
