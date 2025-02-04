package slimeknights.tconstruct.tools.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import slimeknights.mantle.network.packet.IThreadsafePacket;

public class EntityMovementChangePacket implements IThreadsafePacket {

  private int entityID;
  private double x;
  private double y;
  private double z;
  private float yaw;
  private float pitch;

  public EntityMovementChangePacket(Entity entity) {
    this.entityID = entity.getId();
    this.x = entity.getDeltaMovement().x;
    this.y = entity.getDeltaMovement().y;
    this.z = entity.getDeltaMovement().z;
    this.yaw = entity.yRot;
    this.pitch = entity.xRot;
  }

  public EntityMovementChangePacket(FriendlyByteBuf buffer) {
    this.entityID = buffer.readInt();
    this.x = buffer.readDouble();
    this.y = buffer.readDouble();
    this.z = buffer.readDouble();
    this.yaw = buffer.readFloat();
    this.pitch = buffer.readFloat();
  }

  @Override
  public void encode(FriendlyByteBuf FriendlyByteBuf) {
    FriendlyByteBuf.writeInt(this.entityID);
    FriendlyByteBuf.writeDouble(this.x);
    FriendlyByteBuf.writeDouble(this.y);
    FriendlyByteBuf.writeDouble(this.z);
    FriendlyByteBuf.writeFloat(this.yaw);
    FriendlyByteBuf.writeFloat(this.pitch);
  }

  @Override
  public void handleThreadsafe(Context context) {
    if (context.getSender() != null) {
      HandleClient.handle(this);
    }
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle(EntityMovementChangePacket packet) {
      assert Minecraft.getInstance().level != null;
      Entity entity = Minecraft.getInstance().level.getEntity(packet.entityID);
      if (entity != null) {
        entity.setDeltaMovement(packet.x, packet.y, packet.z);
        entity.yRot = packet.yaw;
        entity.xRot = packet.pitch;
      }
    }
  }
}
