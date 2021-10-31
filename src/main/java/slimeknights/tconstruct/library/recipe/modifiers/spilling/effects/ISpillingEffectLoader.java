package slimeknights.tconstruct.library.recipe.modifiers.spilling.effects;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Logic to parse spilling effects
 */
public interface ISpillingEffectLoader<T extends ISpillingEffect> {
  /** Deserializes the recipe from json */
  T deserialize(JsonObject json);

  /** Reads the recipe from the packet buffer */
  T read(FriendlyByteBuf buffer);

  /** Writes this effect to json */
  default void serialize(T effect, JsonObject json) {}

  /** Writes this effect to the packet buffer */
  default void write(T effect, FriendlyByteBuf buffer) {}
}
