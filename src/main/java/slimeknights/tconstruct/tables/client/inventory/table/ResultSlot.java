package slimeknights.tconstruct.tables.client.inventory.table;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.hooks.BasicEventHooks;
import slimeknights.mantle.inventory.CraftingCustomSlot;
import slimeknights.mantle.inventory.IContainerCraftingCustom;

import javax.annotation.Nonnull;

/**
 * Same as {@link CraftingCustomSlot}, but does not require an crafting inventory
 */
public class ResultSlot extends ResultSlot {
  private final IContainerCraftingCustom callback;
  @SuppressWarnings("ConstantConditions")
  public ResultSlot(IContainerCraftingCustom callback, Player player, Container inv, int index, int x, int y) {
    // pass in null for CraftingInventory
    super(player, null, inv, index, x, y);
    this.callback = callback;
  }

  /* Methods that reference CraftingInventory */

  @Override
  protected void checkTakeAchievements(ItemStack stack) {
    if (this.removeCount > 0) {
      stack.onCraftedBy(this.player.level, this.player, this.removeCount);
      net.minecraftforge.fml.hooks.BasicEventHooks.firePlayerCraftingEvent(this.player, stack, this.container);
    }

    this.removeCount = 0;
  }

  @Override
  @Nonnull
  public ItemStack onTake(Player playerIn, @Nonnull ItemStack stack) {
    BasicEventHooks.firePlayerCraftingEvent(playerIn, stack, this.container);
    this.checkTakeAchievements(stack);
    this.callback.onCrafting(playerIn, stack, this.container);
    return stack;
  }
}
