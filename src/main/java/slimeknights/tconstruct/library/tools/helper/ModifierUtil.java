package slimeknights.tconstruct.library.tools.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStack.TooltipDisplayFlags;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.TriPredicate;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.utils.InventoryType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;

/** Generic modifier hooks that don't quite fit elsewhere */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModifierUtil {
  /** Vanilla enchantments tag */
  public static final String TAG_ENCHANTMENTS = "Enchantments";
  /** Vanilla tag to hide certain tooltips */
  public static final String TAG_HIDE_FLAGS = "HideFlags";

  /**
   * Adds all enchantments from tools. Separate method as tools don't have enchants all the time.
   * Typically called before actions which involve loot, such as breaking blocks or attacking mobs.
   * @param tool     Tool instance
   * @param stack    Base stack instance
   * @param context  Tool harvest context
   * @return  True if enchants were applied
   */
  public static boolean applyHarvestEnchants(ToolStack tool, ItemStack stack, ToolHarvestContext context) {
    boolean addedEnchants = false;
    PlayerEntity player = context.getPlayer();
    if (player == null || !player.isCreative()) {
      Map<Enchantment, Integer> enchantments = new HashMap<>();
      BiConsumer<Enchantment,Integer> enchantmentConsumer = (ench, add) -> {
        if (ench != null && add != null) {
          Integer level = enchantments.get(ench);
          if (level != null) {
            add += level;
          }
          enchantments.put(ench, add);
        }
      };
      for (ModifierEntry entry : tool.getModifierList()) {
        entry.getModifier().applyHarvestEnchantments(tool, entry.getLevel(), context, enchantmentConsumer);
      }
      if (!enchantments.isEmpty()) {
        addedEnchants = true;
        EnchantmentHelper.setEnchantments(enchantments, stack);
        stack.getOrCreateTag().putInt(TAG_HIDE_FLAGS, TooltipDisplayFlags.ENCHANTMENTS.func_242397_a());
      }
    }
    return addedEnchants;
  }

  /**
   * Clears enchants from the given stack
   * @param stack  Stack to clear enchants
   */
  public static void clearEnchantments(ItemStack stack) {
    CompoundNBT nbt = stack.getTag();
    if (nbt != null) {
      nbt.remove(TAG_ENCHANTMENTS);
      nbt.remove(TAG_HIDE_FLAGS);
    }
  }

  /**
   * Gets the looting value for the given tool
   * @param tool           Tool used
   * @param holder         Entity holding the tool
   * @param target         Target being looted
   * @param damageSource   Damage source for looting, may ben null if no attack
   * @return  Looting value for the tool
   */
  public static int getLootingLevel(IModifierToolStack tool, LivingEntity holder, Entity target, @Nullable DamageSource damageSource) {
    if (tool.isBroken()) {
      return 0;
    }
    int looting = 0;
    for (ModifierEntry entry : tool.getModifierList()) {
      looting = entry.getModifier().getLootingValue(tool, entry.getLevel(), holder, target, damageSource, looting);
    }
    return looting;
  }

  /** Drops an item at the entity position */
  public static void dropItem(Entity target, ItemStack stack) {
    World world = target.getEntityWorld();
    if (!stack.isEmpty() && !target.getEntityWorld().isRemote()) {
      ItemEntity ent = new ItemEntity(world, target.getPosX(), target.getPosY() + 1, target.getPosZ(), stack);
      ent.setDefaultPickupDelay();
      Random rand = target.world.rand;
      ent.setMotion(ent.getMotion().add((rand.nextFloat() - rand.nextFloat()) * 0.1F,
                                        rand.nextFloat() * 0.05F,
                                        (rand.nextFloat() - rand.nextFloat()) * 0.1F));
      world.addEntity(ent);
    }
  }

  /**
   * Ticks the item in the inventory
   * @param stack         Stack to tick
   * @param world         World with the item
   * @param entity        Entity holding the item
   * @param itemSlot      Slot containing the item
   * @param isSelected    Potentially wrong isSelected description of the item
   * @param correctSlot   Predicate to check if this slot is correct for the item
   */
  public static void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected, TriPredicate<InventoryType, Integer, Boolean> correctSlot) {
    // don't care about non-living, they skip most tool context
    if (entity instanceof LivingEntity) {
      // if no modifiers, can skip a big chunk of logic
      ToolStack tool = ToolStack.from(stack);
      List<ModifierEntry> modifiers = tool.getModifierList();
      if (!modifiers.isEmpty()) {
        // determine which inventory contains the stack
        InventoryType inventoryType;
        LivingEntity living = (LivingEntity) entity;
        boolean isMainHand = false;
        if (entity instanceof PlayerEntity) {
          PlayerInventory inventory = ((PlayerEntity) entity).inventory;
          // if the index is large, must be main
          if (itemSlot >= 4) {
            inventoryType = InventoryType.MAIN;
          } else {
            // vanilla isSelected is stupid, only checks the index and not the inventory so armor could be "selected"
            // only 0 can be the offhand
            if (itemSlot == 0 && living.getHeldItemOffhand() == stack) {
              inventoryType = InventoryType.OFFHAND;
            } else if (inventory.armorItemInSlot(itemSlot) == stack) {
              inventoryType = InventoryType.ARMOR;
            } else {
              inventoryType = InventoryType.MAIN;
              isMainHand = isSelected;
            }
          }
        } else {
          // treating hands as first inventory, armor as second like most mobs
          // if your mob ticks differently, let me know and we can figure out a solution
          if ((itemSlot == 0 && living.getHeldItemMainhand() == stack)) {
            inventoryType = InventoryType.HANDS;
            isMainHand = true;
          } else if (itemSlot == 1 && living.getHeldItemOffhand() == stack) {
            inventoryType = InventoryType.HANDS;
          } else {
            inventoryType = InventoryType.ARMOR;
          }
        }
        // determine if the slot is correct
        boolean isCorrectSlot = correctSlot.test(inventoryType, itemSlot, isMainHand);

        // we pass in the stack for most custom context, but for the sake of armor its easier to tell them that this is the correct slot for effects
        for (ModifierEntry entry : modifiers) {
          entry.getModifier().onInventoryTick(tool, entry.getLevel(), world, living, inventoryType, itemSlot, isMainHand, isCorrectSlot, stack);
        }
      }
    }
  }
}
