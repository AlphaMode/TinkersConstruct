package slimeknights.tconstruct.smeltery.item;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import net.minecraft.world.item.Item.Properties;

/**
 * Fluid container holding 1 ingot of fluid
 */
public class CopperCanItem extends Item {
  private static final String TAG_FLUID = "fluid";

  public CopperCanItem(Properties properties) {
    super(properties);
  }

  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
    return new CopperCanFluidHandler(stack);
  }

  @Override
  public boolean hasContainerItem(ItemStack stack) {
    return getFluid(stack) != Fluids.EMPTY;
  }

  @Override
  public ItemStack getContainerItem(ItemStack stack) {
    Fluid fluid = getFluid(stack);
    if (fluid != Fluids.EMPTY) {
      return new ItemStack(this);
    }
    return ItemStack.EMPTY;
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    Fluid fluid = getFluid(stack);
    if (fluid != Fluids.EMPTY) {
      tooltip.add(new TranslatableComponent(this.getDescriptionId() + ".contents", new TranslatableComponent(fluid.getAttributes().getTranslationKey())).withStyle(ChatFormatting.GRAY));
    } else {
      tooltip.add(new TranslatableComponent(this.getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    }
  }

  /** Sets the fluid on the given stack */
  public static ItemStack setFluid(ItemStack stack, Fluid fluid) {
    // if empty, try to remove the NBT, helps with recipes
    if (fluid == Fluids.EMPTY) {
      CompoundTag nbt = stack.getTag();
      if (nbt != null) {
        nbt.remove(TAG_FLUID);
        if (nbt.isEmpty()) {
          stack.setTag(null);
        }
      }
    } else {
      CompoundTag nbt = stack.getOrCreateTag();
      nbt.putString(TAG_FLUID, Objects.requireNonNull(fluid.getRegistryName()).toString());
    }
    return stack;
  }

  /** Gets the fluid from the given stack */
  public static Fluid getFluid(ItemStack stack) {
    CompoundTag nbt = stack.getTag();
    if (nbt != null) {
      ResourceLocation location = ResourceLocation.tryParse(nbt.getString(TAG_FLUID));
      if (location != null && ForgeRegistries.FLUIDS.containsKey(location)) {
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(location);
        if (fluid != null) {
          return fluid;
        }
      }
    }
    return Fluids.EMPTY;
  }

  /**
   * Gets a string variant name for the given stack
   * @param stack  Stack instance to check
   * @return  String variant name
   */
  public static String getSubtype(ItemStack stack) {
    CompoundTag nbt = stack.getTag();
    if (nbt != null) {
      return nbt.getString(TAG_FLUID);
    }
    return "";
  }
}
