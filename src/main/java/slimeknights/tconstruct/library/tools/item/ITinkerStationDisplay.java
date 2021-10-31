package slimeknights.tconstruct.library.tools.item;

import net.minecraft.world.level.ItemLike;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.library.utils.TooltipFlag;
import slimeknights.tconstruct.library.utils.Util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Interface to implement for tools that also display in the tinker station
 */
public interface ITinkerStationDisplay extends ItemLike {
  /**
   * The "title" displayed in the GUI
   */
  default Component getLocalizedName() {
    return new TranslatableComponent(asItem().getDescriptionId());
  }

  /**
   * Returns the tool stat information for this tool
   * @param tool         Tool to display
   * @param tooltips     List of tooltips for display
   * @param tooltipFlag  Determines the type of tooltip to display
   */
  default List<Component> getStatInformation(IModifierToolStack tool, List<Component> tooltips, TooltipFlag tooltipFlag) {
    return TooltipUtil.getDefaultStats(tool, tooltips, tooltipFlag);
  }

  /**
   * Combines the given display name with the material names to form the new given name
   *
   * @param itemName the standard display name
   * @param materials the list of materials
   * @return the combined item name
   */
  static Component getCombinedItemName(Component itemName, Collection<IMaterial> materials) {
    if (materials.isEmpty() || materials.stream().allMatch(IMaterial.UNKNOWN::equals)) {
      return itemName;
    }

    if (materials.size() == 1) {
      IMaterial material = materials.iterator().next();

      if (Util.canTranslate(material.getTranslationKey() + ".format")) {
        return new TranslatableComponent(material.getTranslationKey() + ".format", itemName);
      }

      return new TranslatableComponent(materials.iterator().next().getTranslationKey()).append(new TextComponent(" ")).append(itemName);
    }

    // multiple materials. we'll have to combine
    TextComponent name = new TextComponent("");

    Iterator<IMaterial> iter = materials.iterator();

    IMaterial material = iter.next();
    name.append(new TranslatableComponent(material.getTranslationKey()));

    while (iter.hasNext()) {
      material = iter.next();
      name.append("-").append(new TranslatableComponent(material.getTranslationKey()));
    }

    name.append(" ").append(itemName);

    return name;
  }
}
