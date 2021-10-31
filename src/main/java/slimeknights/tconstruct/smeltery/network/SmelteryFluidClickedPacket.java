package slimeknights.tconstruct.smeltery.network;

import lombok.AllArgsConstructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import slimeknights.mantle.inventory.BaseContainer;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.smeltery.tileentity.tank.ISmelteryTankHandler;

/**
 * Packet sent when a fluid is clicked in the smeltery UI
 */
@AllArgsConstructor
public class SmelteryFluidClickedPacket implements IThreadsafePacket {
  private final int index;

  public SmelteryFluidClickedPacket(FriendlyByteBuf buffer) {
    index = buffer.readVarInt();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(index);
  }

  @Override
  public void handleThreadsafe(Context context) {
    ServerPlayer sender = context.getSender();
    if (sender != null) {
      AbstractContainerMenu container = sender.containerMenu;
      if (container instanceof BaseContainer<?>) {
        BlockEntity te = ((BaseContainer<?>)container).getTile();
        if (te instanceof ISmelteryTankHandler) {
          ((ISmelteryTankHandler) te).getTank().moveFluidToBottom(index);
        }
      }
    }
  }
}
