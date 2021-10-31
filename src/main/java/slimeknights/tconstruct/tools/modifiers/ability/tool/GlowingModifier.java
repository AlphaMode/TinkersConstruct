package slimeknights.tconstruct.tools.modifiers.ability.tool;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.SingleUseModifier;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.shared.TinkerCommons;

public class GlowingModifier extends SingleUseModifier {
  public GlowingModifier() {
    super(0xffffaa);
  }

  @Override
  public int getPriority() {
    return 70; // after bucketing
  }
  
  @Override
  public InteractionResult afterBlockUse(IModifierToolStack tool, int level, UseOnContext context) {
    Player player = context.getPlayer();
    if (tool.getCurrentDurability() >= 10) {
      if (!context.getLevel().isClientSide) {
        Level world = context.getLevel();
        Direction face = context.getClickedFace();
        BlockPos pos = context.getClickedPos().relative(face);
        if (TinkerCommons.glow.get().addGlow(world, pos, face.getOpposite())) {
          // damage the tool, showing animation if relevant
          if (ToolDamageUtil.damage(tool, 10, player, context.getItemInHand()) && player != null) {
            player.broadcastBreakEvent(context.getHand());
          }
          world.playSound(null, pos, world.getBlockState(pos).getSoundType(world, pos, player).getPlaceSound(), SoundSource.BLOCKS, 1.0f, 1.0f);
        }
      }
      return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
    return InteractionResult.PASS;
  }
}
