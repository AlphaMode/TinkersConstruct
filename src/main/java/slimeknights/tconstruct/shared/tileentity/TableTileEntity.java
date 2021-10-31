package slimeknights.tconstruct.shared.tileentity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import slimeknights.mantle.tileentity.InventoryTileEntity;
import slimeknights.tconstruct.common.SoundUtils;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.common.network.InventorySlotSyncPacket;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.tables.inventory.BaseStationContainer;
import slimeknights.tconstruct.tables.network.UpdateStationScreenPacket;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Tile entity that displays items in world. TODO: better name?
 */
public abstract class TableTileEntity extends InventoryTileEntity {

  public TableTileEntity(BlockEntityType<?> tileEntityTypeIn, String name, int inventorySize) {
    super(tileEntityTypeIn, new TranslatableComponent(name), inventorySize);
  }

  public TableTileEntity(BlockEntityType<?> tileEntityTypeIn, String name, int inventorySize, int maxStackSize) {
    super(tileEntityTypeIn, new TranslatableComponent(name), inventorySize, maxStackSize);
  }

  /* Syncing */

  @Override
  public void setItem(int slot, @Nonnull ItemStack itemstack) {
    // send a slot update to the client when items change, so we can update the TESR
    if (level != null && level instanceof ServerLevel && !level.isClientSide && !ItemStack.matches(itemstack, getItem(slot))) {
      TinkerNetwork.getInstance().sendToClientsAround(new InventorySlotSyncPacket(itemstack, slot, worldPosition), (ServerLevel) level, this.worldPosition);
    }
    super.setItem(slot, itemstack);
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  public CompoundTag getUpdateTag() {
    CompoundTag nbt = super.getUpdateTag();
    // inventory is already in main NBT, include it in update tag
    writeInventoryToNBT(nbt);
    return nbt;
  }

  /**
   * Sends a packet to all players with this container open
   */
  public void syncToRelevantPlayers(Consumer<Player> action) {
    if (this.level == null || this.level.isClientSide) {
      return;
    }

    this.level.players().stream()
      // sync if they are viewing this tile
      .filter(player -> {
        if (player.containerMenu instanceof BaseStationContainer) {
          return ((BaseStationContainer<?>) player.containerMenu).getTile() == this;
        }
        return false;
      })
      // send packets
      .forEach(action);
  }

  /**
   * Plays the crafting sound for all players around the given player
   *
   * @param player the player
   */
  protected void playCraftSound(Player player) {
    SoundUtils.playSoundForAll(player, Sounds.SAW.getSound(), 0.8f, 0.8f + 0.4f * player.getEntityWorld().rand.nextFloat());
  }

  /**
   * Update the screen to the given player
   * @param player  Player to send an update to
   */
  protected void syncScreen(PlayerEntity player) {
    if (this.world != null && !this.world.isRemote && player instanceof ServerPlayerEntity) {
      TinkerNetwork.getInstance().sendTo(new UpdateStationScreenPacket(), (ServerPlayerEntity) player);
    }
  }
}
