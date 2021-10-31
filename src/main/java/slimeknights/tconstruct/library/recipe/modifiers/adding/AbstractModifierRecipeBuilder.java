package slimeknights.tconstruct.library.recipe.modifiers.adding;

import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Lazy;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierMatch;
import slimeknights.tconstruct.library.tools.SlotType;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/** Shared logic between normal and incremental modifier recipe builders */
@SuppressWarnings("unchecked")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractModifierRecipeBuilder<T extends AbstractModifierRecipeBuilder<T>> extends AbstractRecipeBuilder<T> {
  protected static final Lazy<Ingredient> DEFAULT_TOOL = Lazy.of(() -> Ingredient.of(TinkerTags.Items.MODIFIABLE));

  // shared
  protected final ModifierEntry result;
  protected Ingredient tools = Ingredient.EMPTY;
  protected SlotType slotType;
  protected int slots;
  protected int maxLevel = 0;
  // modifier recipe
  protected ModifierMatch requirements = ModifierMatch.ALWAYS;
  protected String requirementsError = null;
  // salvage recipe
  protected int salvageMinLevel = 1;
  protected int salvageMaxLevel = 0;

  /**
   * Sets the list of tools this modifier can be applied to
   * @param tools  Modifier tools list
   * @return  Builder instance
   */
  public T setTools(Ingredient tools) {
    this.tools = tools;
    return (T) this;
  }

  /**
   * Sets the tag for applicable tools
   * @param tag  Tag
   * @return  Builder instance
   */
  public T setTools(Tag<Item> tag) {
    return this.setTools(Ingredient.of(tag));
  }

  /**
   * Sets the modifier requirements for this recipe
   * @param requirements  Modifier requirements
   * @return  Builder instance
   */
  public T setRequirements(ModifierMatch requirements) {
    this.requirements = requirements;
    return (T) this;
  }

  /**
   * Sets the modifier requirements error for when it does not matcH
   * @param requirementsError  Requirements error lang key
   * @return  Builder instance
   */
  public T setRequirementsError(String requirementsError) {
    this.requirementsError = requirementsError;
    return (T) this;
  }

  /**
   * Sets the min level for the salvage recipe
   * @param level  Min level
   * @return  Builder instance
   */
  public T setMinSalvageLevel(int level) {
    if (level < 1) {
      throw new IllegalArgumentException("Min level must be greater than 0");
    }
    this.salvageMinLevel = level;
    return (T) this;
  }

  /**
   * Sets the min level for the salvage recipe
   * @param minLevel  Min level for salvage
   * @param maxLevel  Max level for salvage
   * @return  Builder instance
   */
  public T setSalvageLevelRange(int minLevel, int maxLevel) {
    setMinSalvageLevel(minLevel);
    if (maxLevel < minLevel) {
      throw new IllegalArgumentException("Max level must be grater than or equal to min level");
    }
    this.salvageMaxLevel = maxLevel;
    return (T) this;
  }

  /**
   * Sets the max level for this modifier, affects both the recipe and the salvage
   * @param level  Max level
   * @return  Builder instance
   */
  public T setMaxLevel(int level) {
    if (level < 1) {
      throw new IllegalArgumentException("Max level must be greater than 0");
    }
    this.maxLevel = level;
    return (T) this;
  }


  /* Slots */

  /**
   * Sets the number of slots required by this recipe
   * @param slotType  Slot type
   * @param slots     Slot count
   * @return  Builder instance
   */
  public T setSlots(SlotType slotType, int slots) {
    if (slots < 0) {
      throw new IllegalArgumentException("Slots must be positive");
    }
    this.slotType = slotType;
    this.slots = slots;
    return (T) this;
  }

  /** @deprecated use {@link #setSlots(SlotType, int)} */
  @Deprecated
  public T setUpgradeSlots(int slots) {
    return setSlots(SlotType.UPGRADE, slots);
  }

  /** @deprecated use {@link #setSlots(SlotType, int)} */
  public T setAbilitySlots(int slots) {
    return setSlots(SlotType.ABILITY, slots);
  }

  @Override
  public void build(Consumer<FinishedRecipe> consumer) {
    build(consumer, result.getModifier().getId());
  }

  /**
   * Builds a salvage recipe from this recipe builder
   * @param consumer  Consumer instance
   * @param id        Recipe ID
   */
  public abstract T buildSalvage(Consumer<IFinishedRecipe> consumer, ResourceLocation id);

  /** Writes common JSON components between the two types */
  private void writeCommon(JsonObject json) {
    if (tools == Ingredient.EMPTY) {
      json.add("tools", DEFAULT_TOOL.get().serialize());
    } else {
      json.add("tools", tools.serialize());
    }
    if (slotType != null && slots > 0) {
      JsonObject slotJson = new JsonObject();
      slotJson.addProperty(slotType.getName(), slots);
      json.add("slots", slotJson);
    }
  }

  /** Base logic to write all relevant builder fields to JSON */
  protected abstract class ModifierFinishedRecipe extends AbstractFinishedRecipe {
    public ModifierFinishedRecipe(ResourceLocation ID, @Nullable ResourceLocation advancementID) {
      super(ID, advancementID);
    }

    @Override
    public void serialize(JsonObject json) {
      writeCommon(json);
      if (requirements != ModifierMatch.ALWAYS) {
        JsonObject reqJson = requirements.serialize();
        reqJson.addProperty("error", requirementsError);
        json.add("requirements", reqJson);
      }
      json.add("result", result.toJson());
      if (maxLevel != 0) {
        json.addProperty("max_level", maxLevel);
      }
    }
  }

  /** Base logic to write all relevant builder fields to JSON */
  protected abstract class SalvageFinishedRecipe extends AbstractFinishedRecipe {
    public SalvageFinishedRecipe(ResourceLocation ID, @Nullable ResourceLocation advancementID) {
      super(ID, advancementID);
    }

    @Override
    public void serialize(JsonObject json) {
      writeCommon(json);
      json.addProperty("modifier", result.getModifier().getId().toString());
      json.addProperty("min_level", salvageMinLevel);
      if (salvageMaxLevel != 0) {
        json.addProperty("max_level", salvageMaxLevel);
      }
    }
  }
}
