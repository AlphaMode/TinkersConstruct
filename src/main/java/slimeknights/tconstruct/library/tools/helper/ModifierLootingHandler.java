package slimeknights.tconstruct.library.tools.helper;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Logic to handle the looting event for all main tinker tools
 */
public class ModifierLootingHandler {
  /** If contained in the set, they should use the offhand for looting */
  private static final Set<UUID> LOOTING_OFFHAND = new HashSet<>();
  private static boolean init = false;

  /** Initializies this listener */
  public static void init() {
    if (init) {
      return;
    }
    init = true;
    MinecraftForge.EVENT_BUS.addListener(ModifierLootingHandler::onLooting);
    MinecraftForge.EVENT_BUS.addListener(ModifierLootingHandler::onLeaveServer);
  }

  /**
   * Sets the hand used for looting, so the tool is fetched from the proper context
   * @param entity  Player to set
   * @param hand    Hand
   */
  public static void setLootingHand(LivingEntity entity, InteractionHand hand) {
    if (hand == InteractionHand.OFF_HAND) {
      LOOTING_OFFHAND.add(entity.getUUID());
    } else {
      LOOTING_OFFHAND.remove(entity.getUUID());
    }
  }

  /** Gets the hand to use for looting */
  public static InteractionHand getLootingHand(@Nullable LivingEntity entity) {
    return entity != null && LOOTING_OFFHAND.contains(entity.getUUID()) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
  }

  /** Applies the looting bonus for modifiers */
  private static void onLooting(LootingLevelEvent event) {
    // must be an attacker with our tool
    DamageSource damageSource = event.getDamageSource();
    if (damageSource == null) {
      return;
    }
    Entity source = damageSource.getEntity();
    if (source instanceof LivingEntity) {
      // TODO: consider bow usage, as the attack time is not the same as the death time
      // TODO: extend to armor eventually
      LivingEntity holder = ((LivingEntity)source);
      ItemStack held = holder.getItemInHand(getLootingHand(holder));
      if (TinkerTags.Items.MODIFIABLE.contains(held.getItem())) {
        ToolStack tool = ToolStack.from(held);
        int newLevel = ModifierUtil.getLootingLevel(tool, holder, event.getEntityLiving(), damageSource);
        event.setLootingLevel(newLevel);
      }
    }
  }

  /** Called when a player leaves the server to clear the face */
  private static void onLeaveServer(PlayerLoggedOutEvent event) {
    LOOTING_OFFHAND.remove(event.getPlayer().getUUID());
  }
}
