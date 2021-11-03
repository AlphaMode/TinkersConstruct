package slimeknights.tconstruct.library.tools.part;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.utils.DomainDisplayName;
import slimeknights.tconstruct.library.utils.NBTTags;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import net.minecraft.world.item.Item.Properties;

/**
 * Represents an item that has a Material associated with it. The NBT of the itemstack identifies which material the
 * itemstack of this item has.
 */
public class MaterialItem extends Item implements IMaterialItem {
  private static final String ADDED_BY = TConstruct.makeTranslationKey("tooltip", "part.added_by");

  public MaterialItem(Properties properties) {
    super(properties);
  }

  @Override
  public Optional<MaterialId> getMaterialId(ItemStack stack) {
    return Optional.ofNullable(stack.getTag())
                   .map(CompoundTag -> CompoundTag.getString(NBTTags.PART_MATERIAL))
                   .filter(string -> !string.isEmpty())
                   .map(MaterialId::tryCreate);
  }

  @Override
  public ItemStack withMaterialForDisplay(MaterialId materialId) {
    ItemStack stack = new ItemStack(this);
    stack.getOrCreateTag().putString(NBTTags.PART_MATERIAL, materialId.toString());
    return stack;
  }

  @Override
  public ItemStack withMaterial(IMaterial material) {
    if (canUseMaterial(material)) {
      return withMaterialForDisplay(material.getIdentifier());
    }
    return new ItemStack(this);
  }

  @Override
  public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
    if (this.allowdedIn(group) && MaterialRegistry.isFullyLoaded()) {
      // if a specific material is set in the config, try adding that
      String showOnlyId = Config.COMMON.showOnlyPartMaterial.get();
      boolean added = false;
      if (!showOnlyId.isEmpty()) {
        MaterialId materialId = MaterialId.tryCreate(showOnlyId);
        if (materialId != null) {
          IMaterial material = MaterialRegistry.getMaterial(materialId);
          if (material != IMaterial.UNKNOWN && canUseMaterial(material)) {
            items.add(this.withMaterial(MaterialRegistry.getMaterial(materialId)));
            added = true;
          }
        }
      }
      // if no material is set or we failed to find it, iterate all materials
      if (!added) {
        for (IMaterial material : MaterialRegistry.getInstance().getVisibleMaterials()) {
          if (this.canUseMaterial(material)) {
            items.add(this.withMaterial(material));
            // if a specific material was requested and not found, stop after first
            if (!showOnlyId.isEmpty()) {
              break;
            }
          }
        }
      }
    }
  }

  @Override
  public Component getName(ItemStack stack) {
    // if no material, return part name directly
    IMaterial material = getMaterial(stack);
    if (material == IMaterial.UNKNOWN) {
      return super.getName(stack);
    }
    String key = this.getDescriptionId(stack);
    ResourceLocation loc = material.getIdentifier();
    // if there is a specific name, use that
    String fullKey = String.format("%s.%s.%s", key, loc.getNamespace(), loc.getPath());
    if (Util.canTranslate(fullKey)) {
      return new TranslatableComponent(fullKey);
    }
    // try material name prefix next
    String materialKey = material.getTranslationKey();
    String materialPrefix = materialKey + ".format";
    if (Util.canTranslate(materialPrefix)) {
      return new TranslatableComponent(materialPrefix, new TranslatableComponent(key));
    }
    // format as "<material> <item name>"
    return new TranslatableComponent(materialKey).append(" ").append(new TranslatableComponent(key));
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
    addModTooltip(getMaterial(stack), tooltip);
  }

    /**
     * Adds the mod that added the material to the tooltip
     * @param tooltip   Tooltip list
     * @param material  Material to add
     */
  protected static void addModTooltip(IMaterial material, List<Component> tooltip) {
    if (material != IMaterial.UNKNOWN) {
      tooltip.add(TextComponent.EMPTY);
      tooltip.add(new TranslatableComponent(ADDED_BY, DomainDisplayName.nameFor(material.getIdentifier().getNamespace())));
    }
  }
}
