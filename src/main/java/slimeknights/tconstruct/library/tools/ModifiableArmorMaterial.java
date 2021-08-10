package slimeknights.tconstruct.library.tools;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.EquipmentSlotType.Group;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/** Armor material that doubles as a container for tool definitions for each armor slot */
public class ModifiableArmorMaterial implements IArmorMaterial {
  // copy of the vanilla array for the builder
  private static final int[] MAX_DAMAGE_ARRAY = {13, 15, 16, 11};
  /** Array of all four armor slot types */
  private static final EquipmentSlotType[] ARMOR_SLOTS = {EquipmentSlotType.FEET, EquipmentSlotType.LEGS, EquipmentSlotType.CHEST, EquipmentSlotType.HEAD};

  /** Namespaced name of the armor */
  private final ResourceLocation name;
  /** Array of slot index to tool definition for the slot */
  private final ToolDefinition[] armorDefinitions;
  /** Sound to play when equipping the armor */
  @Getter
  private final SoundEvent soundEvent;

  public ModifiableArmorMaterial(ResourceLocation name, SoundEvent soundEvent, ToolDefinition... armorDefinitions) {
    this.name = name;
    this.soundEvent = soundEvent;
    if (armorDefinitions.length != 4) {
      throw new IllegalArgumentException("Must have an armor definition for each slot");
    }
    this.armorDefinitions = armorDefinitions;
  }

  /**
   * Gets the armor definition for the given armor slot, used in item construction
   * @param slotType  Slot type
   * @return  Armor definition
   */
  @Nullable
  public ToolDefinition getArmorDefinition(EquipmentSlotType slotType) {
    return armorDefinitions[slotType.getIndex()];
  }

  /** Gets the value of a stat for the given slot */
  private float getStat(FloatToolStat toolStat, EquipmentSlotType slotType) {
    ToolDefinition toolDefinition = getArmorDefinition(slotType);
    float defaultValue = toolStat.getDefaultValue();
    if (toolDefinition == null) {
      return defaultValue;
    }
    ToolBaseStatDefinition baseStatDefinition = toolDefinition.getBaseStatDefinition();
    return (baseStatDefinition.getBonus(toolStat) + defaultValue) * baseStatDefinition.getModifier(toolStat);
  }

  @Override
  public String getName() {
    return name.toString();
  }

  @Override
  public int getDurability(EquipmentSlotType slotIn) {
    return (int)getStat(ToolStats.DURABILITY, slotIn);
  }

  @Override
  public int getDamageReductionAmount(EquipmentSlotType slotIn) {
    return (int)getStat(ToolStats.ARMOR, slotIn);
  }

  @Override
  public float getToughness() {
    return getStat(ToolStats.ARMOR_TOUGHNESS, EquipmentSlotType.CHEST);
  }

  @Override
  public float getKnockbackResistance() {
    return getStat(ToolStats.KNOCKBACK_RESISTANCE, EquipmentSlotType.CHEST);
  }

  @Override
  public int getEnchantability() {
    return 0;
  }

  @Override
  public Ingredient getRepairMaterial() {
    return Ingredient.EMPTY;
  }

  /** Gets a builder for a modifiable armor material, creates tool definition for all four armor slots */
  public static StatsBuilder builder(ResourceLocation name) {
    return new StatsBuilder(name);
  }

  /**
   * Builds tool definitions that behave similar to vanilla armor
   */
  public static class StatsBuilder {
    private final ResourceLocation name;
    private final ToolBaseStatDefinition.Builder[] statBuilders;
    protected StatsBuilder(ResourceLocation name) {
      this.name = name;
      statBuilders = new ToolBaseStatDefinition.Builder[4];
      for (int i = 0; i < 4; i++) {
        statBuilders[i] = ToolBaseStatDefinition.builder();
      }
    }

    /** Gets the builder for the given slot */
    private ToolBaseStatDefinition.Builder getBuilder(EquipmentSlotType slotType) {
      if (slotType.getSlotType() != Group.ARMOR) {
        throw new IllegalArgumentException("Invalid armor slot " + slotType);
      }
      return statBuilders[slotType.getIndex()];
    }


    /* Slots */

    /**
     * Sets the starting modifier slots for the given equiptment slot
     * @param equipmentSlot  Equipment slot
     * @param modifierSlot   Modifier slot type
     * @param value          Starting slots
     * @return  Builder instance
     */
    public StatsBuilder startingSlots(EquipmentSlotType equipmentSlot, SlotType modifierSlot, int value) {
      getBuilder(equipmentSlot).startingSlots(modifierSlot, value);
      return this;
    }

    /**
     * Sets the starting modifier slots for all slots
     * @param modifierSlot   Modifier slot type
     * @param value          Starting slots
     * @return  Builder instance
     */
    public StatsBuilder startingSlots(SlotType modifierSlot, int value) {
      for (EquipmentSlotType slotType : ARMOR_SLOTS) {
        startingSlots(slotType, modifierSlot, value);
      }
      return this;
    }

    /**
     * Sets the starting modifier slots for all slots
     * @param modifierSlot   Modifier slot type
     * @param feet   Value for the feet
     * @param legs   Value for the legs
     * @param chest  Value for the chest
     * @param head   Value for the head
     * @return  Builder instance
     */
    public StatsBuilder startingSlots(SlotType modifierSlot, int feet, int legs, int head, int chest) {
      startingSlots(EquipmentSlotType.FEET,  modifierSlot, feet);
      startingSlots(EquipmentSlotType.LEGS,  modifierSlot, legs);
      startingSlots(EquipmentSlotType.CHEST, modifierSlot, chest);
      startingSlots(EquipmentSlotType.HEAD,  modifierSlot, head);
      return this;
    }


    /* Stats */

    /**
     * Adds a bonus to the builder for the given slot, applied during tool stat creation
     * @param equipmentSlot  Equipment slot
     * @param stat   Stat to apply
     * @param bonus  Bonus amount
     * @return  Builder
     */
    public StatsBuilder bonus(EquipmentSlotType equipmentSlot, FloatToolStat stat, float bonus) {
      getBuilder(equipmentSlot).bonus(stat, bonus);
      return this;
    }

    /**
     * Adds a bonus to the builder for all slots, applied during tool stat creation
     * @param stat   Stat to apply
     * @param bonus  Bonus amount
     * @return  Builder
     */
    public StatsBuilder bonus(FloatToolStat stat, float bonus) {
      for (EquipmentSlotType slotType : ARMOR_SLOTS) {
        bonus(slotType, stat, bonus);
      }
      return this;
    }

    /**
     * Adds a bonus to the builder for all slots, applied during tool stat creation
     * Internally, sets the bonus to the passed value minus the default value, as the default will be added down the line
     * @param stat   Stat to apply
     * @param feet   Value for the feet
     * @param legs   Value for the legs
     * @param chest  Value for the chest
     * @param head   Value for the head
     * @return  Builder
     */
    public StatsBuilder bonus(FloatToolStat stat, float feet, float legs, float chest, float head) {
      bonus(EquipmentSlotType.FEET,  stat, feet);
      bonus(EquipmentSlotType.LEGS,  stat, legs);
      bonus(EquipmentSlotType.CHEST, stat, chest);
      bonus(EquipmentSlotType.HEAD,  stat, head);
      return this;
    }

    /**
     * Sets the stat to a particular value for the given slot, replacing the default value.
     * Internally, sets the bonus to the passed value minus the default value, as the default will be added down the line
     * @param equipmentSlot  Equipment slot
     * @param stat   Stat to apply
     * @param value  Value to set
     * @return  Builder
     */
    public StatsBuilder set(EquipmentSlotType equipmentSlot, FloatToolStat stat, float value) {
      getBuilder(equipmentSlot).set(stat, value);
      return this;
    }

    /**
     * Sets the stat to a particular value for all slots, replacing the default value.
     * Internally, sets the bonus to the passed value minus the default value, as the default will be added down the line
     * @param stat   Stat to apply
     * @param value  Value to set
     * @return  Builder
     */
    public StatsBuilder set(FloatToolStat stat, float value) {
      for (EquipmentSlotType slotType : ARMOR_SLOTS) {
        set(slotType, stat, value);
      }
      return this;
    }

    /**
     * Sets the stat to a particular value for all slots, replacing the default value.
     * Internally, sets the bonus to the passed value minus the default value, as the default will be added down the line
     * @param stat   Stat to apply
     * @param feet   Value for the feet
     * @param legs   Value for the legs
     * @param chest  Value for the chest
     * @param head   Value for the head
     * @return  Builder
     */
    public StatsBuilder set(FloatToolStat stat, float feet, float legs, float chest, float head) {
      set(EquipmentSlotType.FEET,  stat, feet);
      set(EquipmentSlotType.LEGS,  stat, legs);
      set(EquipmentSlotType.CHEST, stat, chest);
      set(EquipmentSlotType.HEAD,  stat, head);
      return this;
    }

    /**
     * Adds a multiplier to the armor for the given slot, applied during modifier stats
     * @param equipmentSlot  Equipment slot
     * @param stat           Stat to apply
     * @param bonus          Multiplier
     * @return  Builder
     */
    public StatsBuilder modifier(EquipmentSlotType equipmentSlot, FloatToolStat stat, float bonus) {
      getBuilder(equipmentSlot).modifier(stat, bonus);
      return this;
    }

    /**
     * Adds a multiplier to the armor for all slots, applied during modifier stats
     * @param stat           Stat to apply
     * @param bonus          Multiplier
     * @return  Builder
     */
    public StatsBuilder modifier(FloatToolStat stat, float bonus) {
      for (EquipmentSlotType slotType : ARMOR_SLOTS) {
        modifier(slotType, stat, bonus);
      }
      return this;
    }

    /**
     * Sets the durability for all parts like vanilla armor materials
     * @param maxDamageFactor  Durability modifier applied to the base value for each slot
     * @return  Builder
     */
    public StatsBuilder setDurabilityFactor(float maxDamageFactor) {
      for (EquipmentSlotType slotType : ARMOR_SLOTS) {
        set(ToolStats.DURABILITY, MAX_DAMAGE_ARRAY[slotType.getIndex()] * maxDamageFactor);
      }
      return this;
    }

    /**
     * Finishes building the stats for all armor types
     * @return  Material builder to finish building
     */
    public MaterialBuilder buildStats() {
      ToolBaseStatDefinition[] toolBaseStatDefinitions = new ToolBaseStatDefinition[4];
      for (int i = 0; i < 4; i++) {
        toolBaseStatDefinitions[i] = statBuilders[i].build();
      }
      return new MaterialBuilder(name, toolBaseStatDefinitions);
    }
  }

  /**
   * Builds tool definitions that behave similar to vanilla armor
   */
  public static class MaterialBuilder {
    private final ResourceLocation name;
    private final ToolDefinition.Builder[] builders;
    @Setter @Accessors(chain = true)
    private SoundEvent soundEvent = SoundEvents.ITEM_ARMOR_EQUIP_LEATHER;
    protected MaterialBuilder(ResourceLocation name, ToolBaseStatDefinition[] toolBaseStatDefinitions) {
      this.name = name;
      if (toolBaseStatDefinitions.length != 4) {
        throw new IllegalArgumentException("Must have exactly 4 builders");
      }
      builders = new ToolDefinition.Builder[4];
      for (int i = 0; i < 4; i++) {
        builders[i] = ToolDefinition.builder(toolBaseStatDefinitions[i]);
      }
    }

    /** Gets the builder for the given slot */
    private ToolDefinition.Builder getBuilder(EquipmentSlotType slotType) {
      if (slotType.getSlotType() != Group.ARMOR) {
        throw new IllegalArgumentException("Invalid armor slot " + slotType);
      }
      return builders[slotType.getIndex()];
    }

    /**
     * Adds a tool part to the list of requirements for the given slot
     * @param slotType  Equipment slot type
     * @param part      Part supplier
     * @return  Builder instance
     */
    public MaterialBuilder addPart(EquipmentSlotType slotType, Supplier<? extends IToolPart> part) {
      getBuilder(slotType).addPart(part);
      return this;
    }


    /* Modifiers */

    /**
     * Adds a modifier to the builder for the given slot
     * @param slotType  Equipment slot type
     * @param modifier  Modifier supplier
     * @param level     Modifier level
     * @return Builder instance
     */
    public MaterialBuilder addModifier(EquipmentSlotType slotType, Supplier<? extends Modifier> modifier, int level) {
      getBuilder(slotType).addModifier(modifier, level);
      return this;
    }

    /**
     * Adds a modifier to the builder for the given slot at level 1
     * @param slotType  Equipment slot type
     * @param modifier  Modifier supplier
     * @return Builder instance
     */
    public MaterialBuilder addModifier(EquipmentSlotType slotType, Supplier<? extends Modifier> modifier) {
      return addModifier(slotType, modifier, 1);
    }

    /**
     * Adds a modifier to the builder for all slots
     * @param modifier  Modifier supplier
     * @param level     Modifier level
     * @return Builder instance
     */
    public MaterialBuilder addModifier(Supplier<? extends Modifier> modifier, int level) {
      for (EquipmentSlotType slotType : ARMOR_SLOTS) {
        addModifier(slotType, modifier, level);
      }
      return this;
    }

    /**
     * Adds a modifier to the builder for all slots at level 1
     * @param modifier  Modifier supplier
     * @return Builder instance
     */
    public MaterialBuilder addModifier(Supplier<? extends Modifier> modifier) {
      return addModifier(modifier, 1);
    }

    /** Builds the final material */
    public ModifiableArmorMaterial build() {
      ToolDefinition[] toolDefinitions = new ToolDefinition[4];
      for (int i = 0; i < 4; i++) {
        toolDefinitions[i] = builders[i].build();
      }
      return new ModifiableArmorMaterial(name, soundEvent, toolDefinitions);
    }
  }
}
