package slimeknights.tconstruct.smeltery.inventory;

import lombok.Getter;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import slimeknights.mantle.inventory.BaseContainer;
import slimeknights.mantle.inventory.ItemHandlerSlot;
import slimeknights.mantle.util.sync.ValidZeroIntReference;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.tileentity.controller.MelterTileEntity;
import slimeknights.tconstruct.smeltery.tileentity.module.MeltingModuleInventory;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class MelterContainer extends BaseContainer<MelterTileEntity> {
  @SuppressWarnings("MismatchedReadAndWriteOfArray")
  @Getter
  private final Slot[] inputs;
  @Getter
  private boolean hasFuelSlot = false;
  public MelterContainer(int id, @Nullable Inventory inv, @Nullable MelterTileEntity melter) {
    super(TinkerSmeltery.melterContainer.get(), id, inv, melter);

    // create slots
    if (melter != null) {
      MeltingModuleInventory inventory = melter.getMeltingInventory();
      inputs = new Slot[inventory.getSlots()];
      for (int i = 0; i < inputs.length; i++) {
        inputs[i] = this.addSlot(new ItemHandlerSlot(inventory, i, 22, 16 + (i * 18)));
      }

      // add fuel slot if present, we only add for the melter though
      World world = melter.getWorld();
      BlockPos down = melter.getPos().down();
      if (world != null && world.getBlockState(down).isIn(TinkerTags.Blocks.FUEL_TANKS)) {
        TileEntity te = world.getTileEntity(down);
        if (te != null) {
          hasFuelSlot = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).filter(handler -> {
            this.addSlot(new ItemHandlerSlot(handler, 0, 151, 32));
            return true;
          }).isPresent();
        }
      }

      this.addInventorySlots();

      // syncing
      Consumer<IntReferenceHolder> referenceConsumer = this::trackInt;
      ValidZeroIntReference.trackIntArray(referenceConsumer, melter.getFuelModule());
      inventory.trackInts(array -> ValidZeroIntReference.trackIntArray(referenceConsumer, array));
    } else {
      inputs = new Slot[0];
    }
  }

  public MelterContainer(int id, PlayerInventory inv, FriendlyByteBuf buf) {
    this(id, inv, getTileEntityFromBuf(buf, MelterTileEntity.class));
  }
}
