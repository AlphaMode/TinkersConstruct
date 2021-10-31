package slimeknights.tconstruct.plugin.jei.modifiers;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.RequiredArgsConstructor;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.network.chat.Component;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;

import javax.annotation.Nullable;
import java.util.List;

@RequiredArgsConstructor
public class ModifierIngredientRenderer implements IIngredientRenderer<ModifierEntry> {
  private final int width;

  @Override
  public void render(PoseStack matrices, int x, int y, @Nullable ModifierEntry entry) {
    if (entry != null) {
      Component name = entry.getModifier().getDisplayName(entry.getLevel());
      FontRenderer fontRenderer = getFontRenderer(Minecraft.getInstance(), entry);
      x += (width - fontRenderer.getStringPropertyWidth(name)) / 2;
      fontRenderer.drawTextWithShadow(matrices, name, x, y + 1, -1);
    }
  }

  @Override
  public List<ITextComponent> getTooltip(ModifierEntry entry, ITooltipFlag iTooltipFlag) {
    return entry.getModifier().getDescriptionList();
  }
}
