package slimeknights.tconstruct.smeltery.tileentity.component;

import lombok.Getter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraftforge.client.model.data.IModelData;
import slimeknights.mantle.client.model.data.SinglePropertyData;
import slimeknights.mantle.util.TileEntityHelper;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.tileentity.component.SmelteryInputOutputTileEntity.SmelteryFluidIO;
import slimeknights.tconstruct.smeltery.tileentity.tank.IDisplayFluidListener;
import slimeknights.tconstruct.smeltery.tileentity.tank.ISmelteryTankHandler;

import javax.annotation.Nullable;

/**
 * Fluid IO extension to display controller fluid
 */
public class DrainTileEntity extends SmelteryFluidIO implements IDisplayFluidListener {
  @Getter
  private final IModelData modelData = new SinglePropertyData<>(IDisplayFluidListener.PROPERTY);
  @Getter
  private Fluid displayFluid = Fluids.EMPTY;

  public DrainTileEntity() {
    super(TinkerSmeltery.drain.get());
  }

  protected DrainTileEntity(BlockEntityType<?> type) {
    super(type);
  }

  @Override
  public void notifyDisplayFluidUpdated(Fluid fluid) {
    if (fluid != displayFluid) {
      displayFluid = fluid;
      modelData.setData(IDisplayFluidListener.PROPERTY, fluid);
      requestModelDataUpdate();
      assert level != null;
      BlockState state = getBlockState();
      level.sendBlockUpdated(worldPosition, state, state, 48);
    }
  }

  @Override
  public BlockPos getListenerPos() {
    return getBlockPos();
  }


  /* Updating */

  /** Attaches this TE to the master as a display fluid listener */
  private void attachFluidListener() {
    BlockPos masterPos = getMasterPos();
    if (masterPos != null && level != null && level.isClientSide) {
      TileEntityHelper.getTile(ISmelteryTankHandler.class, level, masterPos).ifPresent(te -> te.addDisplayListener(this));
    }
  }

  // override instead of writeSynced to avoid writing master to the main tag twice
  @Override
  public CompoundTag getUpdateTag() {
    CompoundTag nbt = super.getUpdateTag();
    writeMaster(nbt);
    return nbt;
  }

  @Override
  public void handleUpdateTag(BlockState state, CompoundTag tag) {
    super.handleUpdateTag(state, tag);
    attachFluidListener();
  }

  @Override
  @Nullable
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return new ClientboundBlockEntityDataPacket(worldPosition, 0, writeMaster(new CompoundTag()));
  }

  @Override
  public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
    readMaster(pkt.getTag());
    attachFluidListener();
  }
}
