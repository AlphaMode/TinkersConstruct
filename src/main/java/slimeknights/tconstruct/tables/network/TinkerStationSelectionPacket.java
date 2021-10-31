package slimeknights.tconstruct.tables.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.tables.inventory.table.tinkerstation.TinkerStationContainer;

@RequiredArgsConstructor
public class TinkerStationSelectionPacket implements IThreadsafePacket {

  private final int activeSlots;
  private final boolean tinkerSlotHidden;
  private final Item toolFilter;

  public TinkerStationSelectionPacket(FriendlyByteBuf buffer) {
    this.activeSlots = buffer.readInt();
    this.tinkerSlotHidden = buffer.readBoolean();
    this.toolFilter = buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS);
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeInt(this.activeSlots);
    buffer.writeBoolean(this.tinkerSlotHidden);
    buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, toolFilter);
  }

  @Override
  public void handleThreadsafe(Context context) {
    ServerPlayer sender = context.getSender();
    if (sender != null) {
      AbstractContainerMenu container = sender.containerMenu;
      if (container instanceof TinkerStationContainer) {
        ToolDefinition filter = null;
        if (toolFilter instanceof IModifiable) {
          filter = ((IModifiable) toolFilter).getToolDefinition();
        }
        ((TinkerStationContainer) container).setToolSelection(this.activeSlots, this.tinkerSlotHidden, filter);
      }
    }
  }
}
