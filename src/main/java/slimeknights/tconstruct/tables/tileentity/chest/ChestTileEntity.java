package slimeknights.tconstruct.tables.tileentity.chest;

import lombok.Getter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import slimeknights.mantle.tileentity.NamableTileEntity;
import slimeknights.tconstruct.tables.inventory.TinkerChestContainer;

import javax.annotation.Nullable;

/** Shared base logic for all Tinkers' chest tile entities */
public abstract class ChestTileEntity extends NamableTileEntity {
  private static final String KEY_ITEMS = "Items";

  @Getter
  private final ItemStackHandler itemHandler;
  private final LazyOptional<IItemHandler> capability;
  protected ChestTileEntity(BlockEntityType<?> tileEntityTypeIn, String name, ItemStackHandler itemHandler) {
    super(tileEntityTypeIn, new TranslatableComponent(name));
    this.itemHandler = itemHandler;
    this.capability = LazyOptional.of(() -> itemHandler);
  }

  @Override
  public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
    if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
      return capability.cast();
    }
    return super.getCapability(cap, side);
  }

  @Override
  protected void invalidateCaps() {
    super.invalidateCaps();
    capability.invalidate();
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player playerEntity) {
    return new TinkerChestContainer(menuId, playerInventory, this);
  }

  /**
   * Checks if the given item should be inserted into the chest on interact
   * @param player    Player inserting
   * @param heldItem  Stack to insert
   * @return  Return true
   */
  public boolean canInsert(Player player, ItemStack heldItem) {
    return true;
  }

  @Override
  public CompoundTag save(CompoundTag tags) {
    tags = super.save(tags);
    // move the items from the serialized result
    // we don't care about the size and need it here for compat with old worlds
    CompoundTag handlerNBT = itemHandler.serializeNBT();
    tags.put(KEY_ITEMS, handlerNBT.getList(KEY_ITEMS, NBT.TAG_COMPOUND));
    return tags;
  }

  /** Reads the inventory from NBT */
  public void readInventory(CompoundTag tags) {
    // copy in just the items key for deserializing, don't want to change the size
    CompoundTag handlerNBT = new CompoundTag();
    handlerNBT.put(KEY_ITEMS, tags.getList(KEY_ITEMS, NBT.TAG_COMPOUND));
    itemHandler.deserializeNBT(handlerNBT);
  }

  @Override
  public void load(BlockState blockState, CompoundTag tags) {
    super.load(blockState, tags);
    readInventory(tags);
  }
}
