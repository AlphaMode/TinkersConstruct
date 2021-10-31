package slimeknights.tconstruct.common.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import slimeknights.mantle.recipe.RecipeHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.shared.TinkerCommons;

/**
 * Loot function to set the fluid on a dropped item
 */
public class SetFluidLootFunction extends LootItemConditionalFunction {
  public static final ResourceLocation ID = TConstruct.getResource("set_fluid");

  /** Fluid to add to the item */
  private final FluidStack fluid;
  protected SetFluidLootFunction(LootItemCondition[] conditionsIn, FluidStack fluid) {
    super(conditionsIn);
    this.fluid = fluid;
  }

  @Override
  protected ItemStack run(ItemStack stack, LootContext context) {
    return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                .map(handler -> {
                  handler.fill(fluid.copy(), FluidAction.EXECUTE);
                  return handler.getContainer();
                }).orElse(stack);
  }

  @Override
  public LootItemFunctionType getType() {
    return TinkerCommons.lootSetFluid;
  }

  /**
   * Creates a new builder with the given fluid
   * @param fluid  Fluid to set
   * @return  Builder instance
   */
  public static LootItemConditionalFunction.Builder<?> builder(FluidStack fluid) {
    return simpleBuilder(conditions -> new SetFluidLootFunction(conditions, fluid));
  }

  /** Serializer logic for the function */
  public static class Serializer extends LootItemConditionalFunction.Serializer<SetFluidLootFunction> {
    @Override
    public void serialize(JsonObject json, SetFluidLootFunction loot, JsonSerializationContext context) {
      super.serialize(json, loot, context);
      json.add("fluid", RecipeHelper.serializeFluidStack(loot.fluid));
    }

    @Override
    public SetFluidLootFunction deserialize(JsonObject object, JsonDeserializationContext context, LootItemCondition[] conditions) {
      FluidStack fluid = RecipeHelper.deserializeFluidStack(GsonHelper.getAsJsonObject(object, "fluid"));
      return new SetFluidLootFunction(conditions, fluid);
    }
  }
}
