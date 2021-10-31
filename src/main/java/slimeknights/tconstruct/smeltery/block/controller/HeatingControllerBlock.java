package slimeknights.tconstruct.smeltery.block.controller;

import net.minecraft.block.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import slimeknights.mantle.util.TileEntityHelper;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.smeltery.network.StructureErrorPositionPacket;
import slimeknights.tconstruct.smeltery.tileentity.controller.HeatingStructureTileEntity;
import slimeknights.tconstruct.smeltery.tileentity.multiblock.MultiblockResult;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/**
 * Multiblock that displays the error from the tile entity on right click
 */
public abstract class HeatingControllerBlock extends ControllerBlock {
  protected HeatingControllerBlock(Properties builder) {
    super(builder);
  }

  /** If true, the player is holding or wearing one of the debug items */
  public static boolean holdingBook(Player player) {
    // either hand or head (mod compat goggles)
    return TinkerTags.Items.STRUCTURE_DEBUG.contains(player.getMainHandItem().getItem())
           || TinkerTags.Items.STRUCTURE_DEBUG.contains(player.getOffhandItem().getItem())
           || TinkerTags.Items.STRUCTURE_DEBUG.contains(player.getItemBySlot(EquipmentSlot.HEAD).getItem());
  }

  @Override
  protected boolean openGui(Player player, Level world, BlockPos pos) {
    super.openGui(player, world, pos);
    // only need to update if holding the book
    if (!world.isClientSide && holdingBook(player)) {
      TileEntityHelper.getTile(HeatingStructureTileEntity.class, world, pos).ifPresent(te -> {
        MultiblockResult result = te.getStructureResult();
        if (!result.isSuccess()) {
          TinkerNetwork.getInstance().sendTo(new StructureErrorPositionPacket(pos, result.getPos()), player);
        }
      });
    }
    return true;
  }

  @Override
  protected boolean displayStatus(PlayerEntity player, World world, BlockPos pos, BlockState state) {
    if (!world.isRemote) {
      TileEntityHelper.getTile(HeatingStructureTileEntity.class, world, pos).ifPresent(te -> {
        MultiblockResult result = te.getStructureResult();
        if (!result.isSuccess()) {
          player.sendStatusMessage(result.getMessage(), true);
          TinkerNetwork.getInstance().sendTo(new StructureErrorPositionPacket(pos, result.getPos()), player);
        }
      });
    }
    return true;
  }
}
