package slimeknights.tconstruct.tools.modifiers.upgrades.harvest;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import slimeknights.tconstruct.library.modifiers.SingleLevelModifier;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;

import java.util.function.BiConsumer;

public class FortuneModifier extends SingleLevelModifier {
  public FortuneModifier() {
    super(0xA982BC);
  }

  @Override
  public void applyHarvestEnchantments(IModifierToolStack tool, int level, ToolHarvestContext context, BiConsumer<Enchantment,Integer> consumer) {
    consumer.accept(Enchantments.BLOCK_FORTUNE, level);
  }
}
