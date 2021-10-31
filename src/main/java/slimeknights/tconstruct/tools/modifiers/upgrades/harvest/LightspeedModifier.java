package slimeknights.tconstruct.tools.modifiers.upgrades.harvest;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.IncrementalModifier;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;

public class LightspeedModifier extends IncrementalModifier {
  public LightspeedModifier() {
    super(0xFFBC5E);
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
    BlockPos pos = event.getPos();
    if (pos != null) {
      int light = event.getPlayer().getCommandSenderWorld().getBrightness(LightLayer.BLOCK, pos.relative(sideHit));
      // bonus is +9 mining speed at light level 15, +3 at light level 10, +1 at light level 5
      float boost = (float)(level * Math.pow(3, (light - 5) / 5f) * tool.getModifier(ToolStats.MINING_SPEED) * miningSpeedModifier);
      event.setNewSpeed(event.getNewSpeed() + boost);
    }
  }

  @Override
  public void addInformation(IModifierToolStack tool, int level, List<Component> tooltip, boolean isAdvanced, boolean detailed) {
    addStatTooltip(tool, ToolStats.MINING_SPEED, TinkerTags.Items.HARVEST, 9 * getScaledLevel(tool, level), tooltip);
  }
}
