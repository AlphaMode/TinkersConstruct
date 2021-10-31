package slimeknights.tconstruct.tables.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.mantle.recipe.RecipeHelper;
import slimeknights.mantle.util.TileEntityHelper;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.tables.client.inventory.table.TinkerStationScreen;
import slimeknights.tconstruct.tables.tileentity.table.TinkerStationTileEntity;

import java.util.Optional;

/**
 * Packet to send the current crafting recipe to a player who opens the tinker station
 */
public class UpdateTinkerStationRecipePacket implements IThreadsafePacket {
  private final BlockPos pos;
  private final ResourceLocation recipe;
  public UpdateTinkerStationRecipePacket(BlockPos pos, ITinkerStationRecipe recipe) {
    this.pos = pos;
    this.recipe = recipe.getId();
  }

  public UpdateTinkerStationRecipePacket(FriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    this.recipe = buffer.readResourceLocation();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    buffer.writeResourceLocation(recipe);
  }

  @Override
  public void handleThreadsafe(Context context) {
    HandleClient.handle(this);
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle(UpdateTinkerStationRecipePacket packet) {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
        Optional<ITinkerStationRecipe> recipe = RecipeHelper.getRecipe(world.getRecipeManager(), packet.recipe, ITinkerStationRecipe.class);

        // if the screen is open, use that to get the TE and update the screen
        boolean handled = false;
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof TinkerStationScreen) {
          TinkerStationScreen stationScreen = (TinkerStationScreen) screen;
          TinkerStationTileEntity te = stationScreen.getTileEntity();
          if (te.getBlockPos().equals(packet.pos)) {
            recipe.ifPresent(te::updateRecipe);
            stationScreen.updateDisplay();
            handled = true;
          }
        }
        // if the wrong screen is open or no screen, use the tile directly
        if (!handled) {
          TileEntityHelper.getTile(TinkerStationTileEntity.class, world, packet.pos).ifPresent(te -> recipe.ifPresent(te::updateRecipe));
        }
      }
    }
  }
}
