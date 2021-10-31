package slimeknights.tconstruct.tables.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import slimeknights.tconstruct.tables.tileentity.chest.ChestTileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/**
 * Shared block logic for all chest types
 */
public class ChestBlock extends TinkerTableBlock {
  private static final VoxelShape SHAPE = Shapes.or(
    Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D), //top
    Block.box(1.0D, 3.0D, 1.0D, 15.0D, 16.0D, 15.0D), //middle
    Block.box(0.5D, 0.0D, 0.5D, 2.5D, 15.0D, 2.5D), //leg
    Block.box(13.5D, 0.0D, 0.5D, 15.5D, 15.0D, 2.5D), //leg
    Block.box(13.5D, 0.0D, 13.5D, 15.5D, 15.0D, 15.5D), //leg
    Block.box(0.5D, 0.0D, 13.5D, 2.5D, 15.0D, 15.5D) //leg
                                                        );

  private final Supplier<? extends BlockEntity> te;
  private final boolean dropsItems;
  public ChestBlock(Properties builder, Supplier<? extends BlockEntity> te, boolean dropsItems) {
    super(builder);
    this.te = te;
    this.dropsItems = dropsItems;
  }

  @Nonnull
  @Override
  public BlockEntity createTileEntity(BlockState blockState, BlockGetter iBlockReader) {
    return te.get();
  }

  @Override
  public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(worldIn, pos, state, placer, stack);
    // check if we also have an inventory

    CompoundTag tag = stack.getTag();
    if (tag != null && tag.contains("TinkerData", NBT.TAG_COMPOUND)) {
      CompoundTag tinkerData = tag.getCompound("TinkerData");
      BlockEntity te = worldIn.getBlockEntity(pos);
      if (te instanceof ChestTileEntity) {
        ((ChestTileEntity)te).readInventory(tinkerData);
      }
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  @Deprecated
  public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }

  @SuppressWarnings("deprecation")
  @Override
  @Deprecated
  public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
    BlockEntity te = worldIn.getBlockEntity(pos);
    ItemStack heldItem = player.inventory.getSelected();

    if (!heldItem.isEmpty() && te instanceof ChestTileEntity) {
      ChestTileEntity chest = (ChestTileEntity) te;
      if (chest.canInsert(player, heldItem)) {
        IItemHandlerModifiable itemHandler = chest.getItemHandler();
        ItemStack rest = ItemHandlerHelper.insertItem(itemHandler, heldItem, false);
        if (rest.isEmpty() || rest.getCount() < heldItem.getCount()) {
          player.inventory.mainInventory.set(player.inventory.currentItem, rest);
          return ActionResultType.SUCCESS;
        }
      }
    }

    return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
  }

  @Override
  protected void dropInventoryItems(BlockState state, World worldIn, BlockPos pos, IItemHandler inventory) {
    if (dropsItems) {
      dropInventoryItems(worldIn, pos, inventory);
    }
  }
}
