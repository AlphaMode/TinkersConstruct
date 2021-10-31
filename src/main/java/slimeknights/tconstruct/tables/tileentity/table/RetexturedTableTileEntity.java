package slimeknights.tconstruct.tables.tileentity.table;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.IModelData;
import slimeknights.mantle.tileentity.IRetexturedTileEntity;
import slimeknights.mantle.util.RetexturedHelper;
import slimeknights.tconstruct.shared.tileentity.TableTileEntity;

public abstract class RetexturedTableTileEntity extends TableTileEntity implements IRetexturedTileEntity {
  private final LazyLoadedValue<IModelData> data = new LazyLoadedValue<>(this::getRetexturedModelData);
  public RetexturedTableTileEntity(BlockEntityType<?> type, String name, int size) {
    super(type, name, size);
  }

  @Override
  public IModelData getModelData() {
    return this.data.get();
  }

  @Override
  public AABB getRenderBoundingBox() {
    return new AABB(worldPosition, worldPosition.offset(1, 2, 1));
  }

  @Override
  public void load(BlockState blockState, CompoundTag tags) {
    String oldName = getTextureName();
    super.load(blockState, tags);
    String newName = getTextureName();
    // if the texture name changed, mark the position for rerender
    if (!oldName.equals(newName) && level != null && level.isClientSide) {
      data.get().setData(RetexturedHelper.BLOCK_PROPERTY, getTexture());
      requestModelDataUpdate();
      level.sendBlockUpdated(worldPosition, blockState, blockState, 0);
    }
  }
}
