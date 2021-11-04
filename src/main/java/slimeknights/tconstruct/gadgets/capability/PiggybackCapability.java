package slimeknights.tconstruct.gadgets.capability;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;

/** Capability logic */
public class PiggybackCapability {
  private static final ResourceLocation ID = TConstruct.getResource("piggyback");
  @CapabilityInject(PiggybackHandler.class)
  public static Capability<PiggybackHandler> PIGGYBACK = null;

  private static final PiggybackCapability INSTANCE = new PiggybackCapability();

  private PiggybackCapability() {
  }

  /** Registers this capability */
  public static void register(RegisterCapabilitiesEvent event) {
    event.register(PiggybackHandler.class);
    MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, PiggybackCapability::attachCapability);
  }

  /** Event listener to attach the capability */
  private static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
    if (event.getObject() instanceof Player) {
      event.addCapability(ID, new PiggybackHandler((Player) event.getObject()));
    }
  }
}
