package slimeknights.tconstruct.tools;

import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.library.tools.ModifiableArmorMaterial;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class ArmorDefinitions {
  /** Balanced armor set */
  public static final ModifiableArmorMaterial TRAVELERS = ModifiableArmorMaterial
    .builder(TConstruct.getResource("travelers"))
    .setDurabilityFactor(10)
    .set(ToolStats.ARMOR, 1, 4, 5, 2)
    .startingSlots(SlotType.UPGRADE, 3)
    .startingSlots(SlotType.ARMOR, 3)
    .startingSlots(SlotType.ABILITY, 1)
    .buildStats()
    .setSoundEvent(Sounds.EQUIP_TRAVELERS.getSound())
    .build();

  /** High defense armor set */
  public static final ModifiableArmorMaterial PLATE = ModifiableArmorMaterial
    .builder(TConstruct.getResource("plate"))
    .setDurabilityFactor(30)
    .set(ToolStats.ARMOR, 2, 5, 7, 2)
    .set(ToolStats.ARMOR_TOUGHNESS, 2f)
    .set(ToolStats.KNOCKBACK_RESISTANCE, 0.1f)
    .startingSlots(SlotType.UPGRADE, 1)
    .startingSlots(SlotType.ARMOR, 5)
    .startingSlots(SlotType.ABILITY, 1)
    .buildStats()
    .setSoundEvent(Sounds.EQUIP_PLATE.getSound())
    .build();

  /** High modifiers armor set */
  public static final ModifiableArmorMaterial SLIMESUIT = ModifiableArmorMaterial
    .builder(TConstruct.getResource("slimesuit"))
    .setDurabilityFactor(20)
    .set(ToolStats.ARMOR, 0, 1, 0, 0)
    .set(ToolStats.KNOCKBACK_RESISTANCE, 0.0f) // TODO: negative knockback resistance
    .startingSlots(SlotType.UPGRADE, 4)
    .startingSlots(SlotType.ARMOR, 1)
    .startingSlots(SlotType.ABILITY, 2, 1, 1, 2)
    .buildStats()
    .setSoundEvent(Sounds.EQUIP_SLIME.getSound())
    .build();

}
