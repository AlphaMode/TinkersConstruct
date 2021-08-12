package slimeknights.tconstruct.library.utils;

/** Simple enum to distinguish between the three inventory types as the tick hook only gives you a slot index */
public enum InventoryType {
  /** Player main 36 slot inventory */
  MAIN,
  /** Armor inventory on a player or living entity */
  ARMOR,
  /** Hand inventory on a mob entity */
  HANDS,
  /** Offhand inventory on the player */
  OFFHAND
}
