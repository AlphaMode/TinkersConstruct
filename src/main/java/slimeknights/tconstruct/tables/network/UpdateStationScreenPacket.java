package slimeknights.tconstruct.tables.network;

import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.tables.client.inventory.BaseStationScreen;

@NoArgsConstructor
public class UpdateStationScreenPacket implements IThreadsafePacket {
  public UpdateStationScreenPacket(FriendlyByteBuf buffer) {}

  @Override
  public void encode(FriendlyByteBuf FriendlyByteBuf) {}

  @Override
  public void handleThreadsafe(NetworkEvent.Context context) {
    HandleClient.handle(this);
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle(UpdateStationScreenPacket packet) {
      Screen screen = Minecraft.getInstance().screen;
      if (screen != null) {
        if (screen instanceof BaseStationScreen) {
          ((BaseStationScreen<?,?>) screen).updateDisplay();
        }
      }
    }
  }
}
