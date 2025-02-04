package slimeknights.tconstruct.tools.modifiers.traits;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.utils.Util;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Shared logic for jagged and stonebound. Trait boosts attack damage as it lowers mining speed.
 */
public class DamageSpeedTradeModifier extends Modifier {
  private static final Component MINING_SPEED = TConstruct.makeTranslation("modifier", "fake_attribute.mining_speed");
  private final float multiplier;
  private final Lazy<UUID> uuid = Lazy.of(() -> UUID.nameUUIDFromBytes(getId().toString().getBytes()));
  private final Lazy<String> attributeName = Lazy.of(() -> {
    ResourceLocation id = getId();
    return id.getPath() + "." + id.getNamespace() + ".attack_damage";
  });

  /**
   * Creates a new instance of
   * @param color       Modifier text color
   * @param multiplier  Multiplier. Positive boosts damage, negative boosts mining speed
   */
  public DamageSpeedTradeModifier(int color, float multiplier) {
    super(color);
    this.multiplier = multiplier;
  }

  /** Gets the multiplier for this modifier at the current durability and level */
  private double getMultiplier(IModifierToolStack tool, int level) {
    return Math.sqrt(tool.getDamage() * level / tool.getModifier(ToolStats.DURABILITY)) * multiplier;
  }

  @Override
  public void addInformation(IModifierToolStack tool, int level, List<Component> tooltip, boolean isAdvanced, boolean detailed) {
    double boost = getMultiplier(tool, level);
    if (boost != 0 && tool.hasTag(TinkerTags.Items.HARVEST)) {
      tooltip.add(applyStyle(new TextComponent(Util.PERCENT_BOOST_FORMAT.format(-boost)).append(" ").append(MINING_SPEED)));
    }
  }

  @Override
  public void addAttributes(IModifierToolStack tool, int level, EquipmentSlot slot, BiConsumer<Attribute,AttributeModifier> consumer) {
    if (slot == EquipmentSlot.MAINHAND) {
      double boost = getMultiplier(tool, level);
      if (boost != 0) {
        // half boost for attack speed, its
        consumer.accept(Attributes.ATTACK_DAMAGE, new AttributeModifier(uuid.get(), attributeName.get(), boost / 2, Operation.MULTIPLY_TOTAL));
      }
    }
  }

  @Override
  public void onBreakSpeed(IModifierToolStack tool, int level, BreakSpeed event, Direction sideHit, boolean isEffective, float miningSpeedModifier) {
    if (isEffective) {
      event.setNewSpeed((float)(event.getNewSpeed() * (1 - getMultiplier(tool, level))));
    }
  }
}
