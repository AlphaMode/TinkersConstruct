package slimeknights.tconstruct.plugin.jei.partbuilder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.NoArgsConstructor;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.network.chat.Component;
import slimeknights.tconstruct.library.recipe.partbuilder.Pattern;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
public class PatternIngredientRenderer implements IIngredientRenderer<Pattern> {
  public static final PatternIngredientRenderer INSTANCE = new PatternIngredientRenderer();

  @Override
  public void render(PoseStack matrices, int x, int y, @Nullable Pattern pattern) {
    if (pattern != null) {
      TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(pattern.getTexture());
      RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
      Screen.blit(matrices, x, y, 100, 16, 16, sprite);
    }
  }

  @Override
  public List<Component> getTooltip(Pattern pattern, TooltipFlag iTooltipFlag) {
    return Collections.singletonList(pattern.getDisplayName());
  }
}
