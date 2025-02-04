package slimeknights.tconstruct.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.inventory.container.InventoryMenu;
import net.minecraft.core.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.mantle.client.model.FaucetFluidLoader;
import slimeknights.mantle.client.model.fluid.FluidCuboid;
import slimeknights.mantle.client.model.fluid.FluidsModel;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.client.render.FluidRenderer;
import slimeknights.mantle.client.render.RenderingHelper;
import slimeknights.tconstruct.smeltery.block.FaucetBlock;
import slimeknights.tconstruct.smeltery.tileentity.FaucetTileEntity;

import java.util.function.Function;

public class FaucetTileEntityRenderer extends BlockEntityRenderer<FaucetTileEntity> {
  public FaucetTileEntityRenderer(BlockEntityRenderDispatcher rendererDispatcherIn) {
    super(rendererDispatcherIn);
  }

  @Override
  public void render(FaucetTileEntity tileEntity, float partialTicks, PoseStack matrices, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
    FluidStack renderFluid = tileEntity.getRenderFluid();
    if (!tileEntity.isPouring() || renderFluid.isEmpty()) {
      return;
    }

    // safety
    World world = tileEntity.getWorld();
    if (world == null) {
      return;
    }

    // fetch faucet model to determine where to render fluids
    BlockState state = tileEntity.getBlockState();
    FluidsModel.BakedModel model = ModelHelper.getBakedModel(state, FluidsModel.BakedModel.class);
    if (model != null) {
      // if side, rotate fluid model
      Direction direction = state.get(FaucetBlock.FACING);
      boolean isRotated = RenderingHelper.applyRotation(matrices, direction);

      // fluid props
      FluidAttributes attributes = renderFluid.getFluid().getAttributes();
      int color = attributes.getColor(renderFluid);
      Function<ResourceLocation, TextureAtlasSprite> spriteGetter = Minecraft.getInstance().getAtlasSpriteGetter(InventoryMenu.BLOCK_ATLAS);
      TextureAtlasSprite still = spriteGetter.apply(attributes.getStillTexture(renderFluid));
      TextureAtlasSprite flowing = spriteGetter.apply(attributes.getFlowingTexture(renderFluid));
      boolean isGas = attributes.isGaseous(renderFluid);
      combinedLightIn = FluidRenderer.withBlockLight(combinedLightIn, attributes.getLuminosity(renderFluid));

      // render all cubes in the model
      IVertexBuilder buffer = bufferIn.getBuffer(FluidRenderer.RENDER_TYPE);
      for (FluidCuboid cube : model.getFluids()) {
        FluidRenderer.renderCuboid(matrices, buffer, cube, 0, still, flowing, color, combinedLightIn, isGas);
      }

      // render into the block(s) below
      FaucetFluidLoader.renderFaucetFluids(world, tileEntity.getPos(), direction, matrices, buffer, still, flowing, color, combinedLightIn);

      // if rotated, pop back rotation
      if(isRotated) {
        matrices.pop();
      }
    }
  }
}
