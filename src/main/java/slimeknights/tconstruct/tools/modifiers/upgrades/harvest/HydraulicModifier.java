package slimeknights.tconstruct.tools.modifiers.upgrades.harvest;

import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.IncrementalModifier;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;

public class HydraulicModifier extends IncrementalModifier {
  public HydraulicModifier() {
    super(0x7CB3A4);
  }

  @Override
  public int getPriority() {
    return 125; // run before trait boosts such as dwarven
  }

  @Override
  public void onBreakSpeed(IModifierToolStack tool, int level, BreakSpeed event, Direction sideHit, boolean isEffective, float miningSpeedModifier) {
    if (!isEffective) {
      return;
    }
    Player player = event.getPlayer();
    float bonus = 0;
    // highest bonus in water
    if (player.isEyeInFluid(FluidTags.WATER)) {
      bonus = 8;
      // if not enchanted with aqua affinity, multiply by 5 to cancel out the effects of water
      if (!EnchantmentHelper.hasAquaAffinity(player)) {
        bonus *= 5;
      }
    } else if (player.getCommandSenderWorld().isRainingAt(player.blockPosition())) {
      // partial bonus in the rain
      bonus = 4;
    }
    if (bonus > 0) {
      bonus *= level * tool.getModifier(ToolStats.DURABILITY) * miningSpeedModifier;
      event.setNewSpeed(event.getNewSpeed() + bonus);
    }
  }

  @Override
  public void addInformation(IModifierToolStack tool, int level, List<Component> tooltip, boolean isAdvanced, boolean detailed) {
    addStatTooltip(tool, ToolStats.MINING_SPEED, TinkerTags.Items.HARVEST, 8 * getScaledLevel(tool, level), tooltip);
  }
}
