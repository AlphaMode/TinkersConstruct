package slimeknights.tconstruct.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.LineStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.smeltery.block.controller.ControllerBlock;
import slimeknights.tconstruct.smeltery.block.controller.HeatingControllerBlock;
import slimeknights.tconstruct.smeltery.tileentity.controller.HeatingStructureTileEntity;
import slimeknights.tconstruct.smeltery.tileentity.module.MeltingModuleInventory;
import slimeknights.tconstruct.smeltery.tileentity.multiblock.HeatingStructureMultiblock.StructureData;

import java.util.OptionalDouble;

public class HeatingStructureTileEntityRenderer extends BlockEntityRenderer<HeatingStructureTileEntity> {
  private static final RenderType ERROR_BLOCK = RenderType.create(
    "lines", DefaultVertexFormat.POSITION_COLOR, 1, 256,
    RenderType.CompositeState.builder()
                    .setLineState(new LineStateShard(OptionalDouble.empty()))
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .createCompositeState(false));

  private static final float ITEM_SCALE = 15f/16f;
  public HeatingStructureTileEntityRenderer(BlockEntityRenderDispatcher rendererDispatcherIn) {
    super(rendererDispatcherIn);
  }

  @Override
  public void render(HeatingStructureTileEntity smeltery, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
    Level world = smeltery.getLevel();
    if (world == null) return;
    BlockState state = smeltery.getBlockState();
    StructureData structure = smeltery.getStructure();
    boolean structureValid = state.get(ControllerBlock.IN_STRUCTURE) && structure != null;

    // render erroring block, done whether in the structure or not
    BlockPos errorPos = smeltery.getErrorPos();
    if (errorPos != null && Minecraft.getInstance().player != null) {
      // either we must be holding the book, or the structure must be erroring and it be within 10 seconds of last update
      boolean highlightError = smeltery.isHighlightError();
      if ((!structureValid && highlightError) || HeatingControllerBlock.holdingBook(Minecraft.getInstance().player)) {
        // distance check, 512 is the squared length of the diagonal of a max size structure
        BlockPos pos = smeltery.getPos();
        BlockPos playerPos = Minecraft.getInstance().player.getPosition();
        int dx = playerPos.getX() - pos.getX();
        int dz = playerPos.getZ() - pos.getZ();
        if ((dx * dx + dz * dz) < 512) {
          // color will be yellow if the structure is valid (expanding), red if invalid
          IVertexBuilder vertexBuilder = buffer.getBuffer(highlightError ? ERROR_BLOCK : RenderType.LINES);
          WorldRenderer.drawShape(matrices, vertexBuilder, VoxelShapes.fullCube(), errorPos.getX() - pos.getX(), errorPos.getY() - pos.getY(), errorPos.getZ() - pos.getZ(), 1f, structureValid ? 1f : 0f, 0f, 0.5f);
        }
      }
    }

    // if no structure, nothing else to do
    if (!structureValid) {
      return;
    }

    // relevant positions
    BlockPos pos = smeltery.getPos();
    BlockPos minPos = structure.getMinInside();
    BlockPos maxPos = structure.getMaxInside();

    // offset to make rendering min pos relative
    matrices.push();
    matrices.translate(minPos.getX() - pos.getX(), minPos.getY() - pos.getY(), minPos.getZ() - pos.getZ());
    // render tank fluids, use minPos for brightness
    SmelteryTankRenderer.renderFluids(matrices, buffer, smeltery.getTank(), minPos, maxPos, WorldRenderer.getCombinedLight(world, minPos));

    // render items
    int xd = 1 + maxPos.getX() - minPos.getX();
    int zd = 1 + maxPos.getZ() - minPos.getZ();
    int layer = xd * zd;
    Direction facing = state.get(ControllerBlock.FACING);
    Quaternion itemRotation = Vector3f.YP.rotationDegrees(-90.0F * (float)facing.getHorizontalIndex());
    MeltingModuleInventory inventory = smeltery.getMeltingInventory();
    for (int i = 0; i < inventory.getSlots(); i++) {
      ItemStack stack = inventory.getStackInSlot(i);
      if (!stack.isEmpty()) {
        // calculate position inside the smeltery from slot index
        int height = i / layer;
        int layerIndex = i % layer;
        int offsetX = layerIndex % xd;
        int offsetZ = layerIndex / xd;
        BlockPos itemPos = minPos.add(offsetX, height, offsetZ);

        // offset to the slot position in the structure, scale, and rotate the item
        matrices.push();
        matrices.translate(offsetX + 0.5f, height + 0.5f, offsetZ + 0.5f);
        matrices.rotate(itemRotation);
        matrices.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        Minecraft.getInstance().getItemRenderer()
                 .renderItem(stack, TransformType.NONE, WorldRenderer.getCombinedLight(world, itemPos),
                             OverlayTexture.NO_OVERLAY, matrices, buffer);
        matrices.pop();
      }
    }

    matrices.pop();
  }

  @Override
  public boolean isGlobalRenderer(HeatingStructureTileEntity tile) {
    return tile.getBlockState().get(ControllerBlock.IN_STRUCTURE) && tile.getStructure() != null;
  }
}
