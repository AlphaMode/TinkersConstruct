package slimeknights.tconstruct.world.client;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.LavaSlimeModel;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import slimeknights.tconstruct.TConstruct;

public class TerracubeRenderer extends MobRenderer<Slime,LavaSlimeModel<Slime>> {
  public static final Factory TERRACUBE_RENDERER = new Factory(TConstruct.getResource("textures/entity/terracube.png"));

  private final ResourceLocation texture;
  public TerracubeRenderer(EntityRenderDispatcher manager, ResourceLocation texture) {
    super(manager, new LavaSlimeModel<>(), 0.25F);
    this.texture = texture;
  }

  @Override
  public ResourceLocation getTextureLocation(Slime entity) {
    return texture;
  }

  @Override
  protected void scale(Slime slime, PoseStack matrices, float partialTickTime) {
    int size = slime.getSize();
    float squishFactor = Mth.lerp(partialTickTime, slime.oSquish, slime.squish) / ((float)size * 0.5F + 1.0F);
    float invertedSquish = 1.0F / (squishFactor + 1.0F);
    matrices.scale(invertedSquish * (float)size, 1.0F / invertedSquish * (float)size, invertedSquish * (float)size);
  }

  @RequiredArgsConstructor
  public static class Factory implements IRenderFactory<Slime> {
    private final ResourceLocation texture;

    @Override
    public EntityRenderer<? super Slime> createRenderFor(EntityRenderDispatcher manager) {
      return new TerracubeRenderer(manager, texture);
    }
  }
}
