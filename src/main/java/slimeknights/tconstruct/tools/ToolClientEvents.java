package slimeknights.tconstruct.tools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.commons.lang3.mutable.MutableInt;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.ClientEventBase;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.client.model.tools.MaterialModel;
import slimeknights.tconstruct.library.client.model.tools.ToolModel;
import slimeknights.tconstruct.library.client.modifiers.BreakableModifierModel;
import slimeknights.tconstruct.library.client.modifiers.FluidModifierModel;
import slimeknights.tconstruct.library.client.modifiers.ModifierModelManager;
import slimeknights.tconstruct.library.client.modifiers.ModifierModelManager.ModifierModelRegistrationEvent;
import slimeknights.tconstruct.library.client.modifiers.NormalModifierModel;
import slimeknights.tconstruct.library.client.modifiers.TankModifierModel;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.MaterialItem;
import slimeknights.tconstruct.tools.client.OverslimeModifierModel;
import slimeknights.tconstruct.tools.client.particles.AxeAttackParticle;
import slimeknights.tconstruct.tools.client.particles.HammerAttackParticle;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = TConstruct.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
public class ToolClientEvents extends ClientEventBase {
  /**
   * Called by TinkerClient to add the resource listeners, runs during constructor
   */
  public static void addResourceListener(ReloadableResourceManager manager) {
    ModifierModelManager.init(manager);
  }

  @SubscribeEvent
  static void registerModelLoaders(ModelRegistryEvent event) {
    ModelLoaderRegistry.registerLoader(TConstruct.getResource("material"), MaterialModel.LOADER);
    ModelLoaderRegistry.registerLoader(TConstruct.getResource("tool"), ToolModel.LOADER);
  }

  @SubscribeEvent
  static void registerModifierModels(ModifierModelRegistrationEvent event) {
    event.registerModel(TConstruct.getResource("normal"), NormalModifierModel.UNBAKED_INSTANCE);
    event.registerModel(TConstruct.getResource("breakable"), BreakableModifierModel.UNBAKED_INSTANCE);
    event.registerModel(TConstruct.getResource("overslime"), OverslimeModifierModel.UNBAKED_INSTANCE);
    event.registerModel(TConstruct.getResource("fluid"), FluidModifierModel.UNBAKED_INSTANCE);
    event.registerModel(TConstruct.getResource("tank"), TankModifierModel.UNBAKED_INSTANCE);
  }

  @SubscribeEvent
  static void clientSetupEvent(FMLClientSetupEvent event) {
    RenderingRegistry.registerEntityRenderingHandler(TinkerTools.indestructibleItem.get(), manager -> new ItemEntityRenderer(manager, Minecraft.getInstance().getItemRenderer()));
    MinecraftForge.EVENT_BUS.addListener(ToolClientEvents::onTooltipEvent);
    MinecraftForge.EVENT_BUS.addListener(ToolClientEvents::renderHand);
  }

  @SubscribeEvent
  static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
    Minecraft.getInstance().particleEngine.register(TinkerTools.hammerAttackParticle.get(), HammerAttackParticle.Factory::new);
    Minecraft.getInstance().particleEngine.register(TinkerTools.axeAttackParticle.get(), AxeAttackParticle.Factory::new);
  }

  @SubscribeEvent
  static void itemColors(ColorHandlerEvent.Item event) {
    final ItemColors colors = event.getItemColors();

    // tint tool textures for fallback
    // rock
    registerToolItemColors(colors, TinkerTools.pickaxe);
    registerToolItemColors(colors, TinkerTools.sledgeHammer);
    registerToolItemColors(colors, TinkerTools.veinHammer);
    // dirt
    registerToolItemColors(colors, TinkerTools.mattock);
    registerToolItemColors(colors, TinkerTools.excavator);
    // wood
    registerToolItemColors(colors, TinkerTools.handAxe);
    registerToolItemColors(colors, TinkerTools.broadAxe);
    // scythe
    registerToolItemColors(colors, TinkerTools.kama);
    registerToolItemColors(colors, TinkerTools.scythe);
    // weapon
    registerToolItemColors(colors, TinkerTools.dagger);
    registerToolItemColors(colors, TinkerTools.sword);
    registerToolItemColors(colors, TinkerTools.cleaver);
  }

  // registered with FORGE bus
  private static void onTooltipEvent(ItemTooltipEvent event) {
    if (event.getItemStack().getItem() instanceof IModifiableDisplay) {
      boolean isShift = Screen.hasShiftDown();
      boolean isCtrl = !isShift && ((IModifiableDisplay) event.getItemStack().getItem()).getToolDefinition().isMultipart() && Screen.hasControlDown();
      MutableInt removedWhenIn = new MutableInt(0);
      event.getToolTip().removeIf(text -> {
        // its hard to find the blank line before attributes, so shift just removes all of them
        if ((isShift || (isCtrl && removedWhenIn.intValue() > 0)) && text == TextComponent.EMPTY) {
          return true;
        }
        // the attack damage and attack speed ones are formatted weirdly, suppress on both tooltips
        if ((isShift || isCtrl) && " ".equals(text.getContents())) {
          List<Component> siblings = text.getSiblings();
          if (!siblings.isEmpty() && siblings.get(0) instanceof TranslatableComponent) {
            return ((TranslatableComponent) siblings.get(0)).getKey().startsWith("attribute.modifier.equals.");
          }
        }
        if (text instanceof TranslatableComponent) {
          String key = ((TranslatableComponent)text).getKey();

          // we want to ignore all modifiers after "when in off hand" as its typically redundant to the main hand, you will see without shift
          if ((isCtrl || isShift) && key.startsWith("item.modifiers.")) {
            removedWhenIn.add(1);
            return true;
          }

          // suppress durability from advanced, we display our own
          return key.equals("item.durability")
                 // the "when in main hand" text, don't need on either tooltip
            || ((isCtrl || (isShift && removedWhenIn.intValue() > 1)) && key.startsWith("attribute.modifier."));
        }
        return false;
      });
    }
  }

  // registered with FORGE bus
  private static void renderHand(RenderHandEvent event) {
    InteractionHand hand = event.getHand();
    Player player = Minecraft.getInstance().player;
    if (hand != InteractionHand.OFF_HAND || player == null) {
      return;
    }
    ItemStack stack = player.getMainHandItem();
    if (stack.getItem().is(TinkerTags.Items.TWO_HANDED)) {
      ToolStack tool = ToolStack.from(stack);
      // special support for replacing modifier
      if (!tool.getVolatileData().getBoolean(IModifiable.DEFER_OFFHAND)) {
        if (!(event.getItemStack().getItem() instanceof BlockItem) || tool.getModifierLevel(TinkerModifiers.exchanging.get()) == 0) {
          event.setCanceled(true);
        }
      }
    }
  }

  /** @deprecated No longer required, colors are baked into the model */
  @Deprecated
  public static void registerMaterialItemColors(ItemColors colors, Supplier<? extends MaterialItem> item) {}

  /**
   * Registers an item color handler for a part item, TODO: move to API class
   * @param colors  Item colors instance
   * @param item    Material item
   */
  public static void registerToolItemColors(ItemColors colors, Supplier<? extends IModifiable> item) {
    colors.register(ToolModel.COLOR_HANDLER, item.get());
  }
}
