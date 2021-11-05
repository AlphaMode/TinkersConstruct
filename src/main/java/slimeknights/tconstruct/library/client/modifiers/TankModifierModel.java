package slimeknights.tconstruct.library.client.modifiers;

import lombok.Data;
import net.minecraft.client.resources.model.Material;
import net.minecraft.fluid.Fluid;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.TankModifier;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;

import javax.annotation.Nullable;

/**
 * Model for tank modifiers, also displays the fluid
 */
public class TankModifierModel extends FluidModifierModel {
  /** Constant unbaked model instance, as they are all the same */
  public static final IUnbakedModifierModel UNBAKED_INSTANCE = (smallGetter, largeGetter) -> {
    Material smallTexture = smallGetter.apply("");
    Material largeTexture = largeGetter.apply("");
    Material smallPartial = smallGetter.apply("_partial");
    Material largePartial = largeGetter.apply("_partial");
    Material smallFull = smallGetter.apply("_full");
    Material largeFull = largeGetter.apply("_full");
    if (smallTexture != null || largeTexture != null) {
      return new TankModifierModel(smallTexture, largeTexture, smallPartial, largePartial, smallFull, largeFull);
    }
    return null;
  };

  public TankModifierModel(@Nullable Material smallTexture, @Nullable Material largeTexture,
                           @Nullable Material smallPartial, @Nullable Material largePartial,
                           @Nullable Material smallFull, @Nullable Material largeFull) {
    super(smallTexture, largeTexture, new Material[] { smallPartial, largePartial, smallFull, largeFull });
  }

  @Nullable
  @Override
  public Object getCacheKey(IModifierToolStack tool, ModifierEntry entry) {
    if (entry.getModifier() instanceof TankModifier) {
      TankModifier tank = (TankModifier) entry.getModifier();
      FluidStack fluid = tank.getFluid(tool);
      if (!fluid.isEmpty()) {
        // cache by modifier, fluid, and not being full
        return new TankModifierCacheKey(tank, fluid.getFluid(), fluid.getAmount() < tank.getCapacity(tool));
      }
    }
    return entry.getModifier();
  }

  @Override
  @Nullable
  protected Material getTemplate(TankModifier tank, IModifierToolStack tool, FluidStack fluid, boolean isLarge) {
    boolean isFull = fluid.getAmount() == tank.getCapacity(tool);
    return fluidTextures[(isFull ? 2 : 0) | (isLarge ? 1 : 0)];
  }

  /** Cache key for the model */
  @Data
  private static class TankModifierCacheKey {
    private final Modifier modifier;
    private final Fluid fluid;
    private final boolean isPartial;
  }
}
