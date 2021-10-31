package slimeknights.tconstruct.tools.modifiers.ability.tool;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.TankModifier;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.IModDataReadOnly;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;

public class BucketingModifier extends TankModifier {
  public BucketingModifier() {
    super(0xD8D8D8, FluidAttributes.BUCKET_VOLUME);
  }

  @Override
  public int getPriority() {
    return 80; // little bit less so we get to add volatile data late
  }

  @Override
  public void addVolatileData(ToolDefinition toolDefinition, StatsNBT baseStats, IModDataReadOnly persistentData, int level, ModDataNBT volatileData) {
    super.addVolatileData(toolDefinition, baseStats, persistentData, level, volatileData);

    // boost to the nearest bucket amount
    int capacity = getCapacity(volatileData);
    int remainder = capacity % FluidAttributes.BUCKET_VOLUME;
    if (remainder != 0) {
      addCapacity(volatileData, FluidAttributes.BUCKET_VOLUME - remainder);
    }
  }

  /**
   * Checks if the block is unable to contain fluid
   * @param world  World
   * @param pos    Position to try
   * @param state  State
   * @param fluid  Fluid to place
   * @return  True if the block is unable to contain fluid, false if it can contain fluid
   */
  private static boolean cannotContainFluid(Level world, BlockPos pos, BlockState state, Fluid fluid) {
    Block block = state.getBlock();
    return !state.canBeReplaced(fluid) && (!(block instanceof LiquidBlockContainer) || !((LiquidBlockContainer)block).canPlaceLiquid(world, pos, state, fluid));
  }

  @Override
  public InteractionResult afterBlockUse(IModifierToolStack tool, int level, UseOnContext context) {
    // only place fluid if sneaking, we contain at least a bucket, and its a block
    Player player = context.getPlayer();
    if (player == null || !player.isShiftKeyDown()) {
      return InteractionResult.PASS;
    }
    FluidStack fluidStack = getFluid(tool);
    if (fluidStack.getAmount() < FluidAttributes.BUCKET_VOLUME) {
      return InteractionResult.PASS;
    }
    Fluid fluid = fluidStack.getFluid();
    if (!(fluid instanceof FlowingFluid)) {
      return InteractionResult.PASS;
    }

    // can we interact with the position
    Direction face = context.getClickedFace();
    Level world = context.getLevel();
    BlockPos target = context.getClickedPos();
    BlockPos offset = target.relative(face);
    if (!world.mayInteract(player, target) || !player.mayUseItemAt(offset, face, context.getItemInHand())) {
      return InteractionResult.PASS;
    }

    // if the block cannot be placed at the current location, try placing at the neighbor
    BlockState existing = world.getBlockState(target);
    if (cannotContainFluid(world, target, existing, fluidStack.getFluid())) {
      target = offset;
      existing = world.getBlockState(target);
      if (cannotContainFluid(world, target, existing, fluidStack.getFluid())) {
        return InteractionResult.PASS;
      }
    }

    // if water, evaporate
    boolean placed = false;
    if (world.dimensionType().ultraWarm() && fluid.is(FluidTags.WATER)) {
      world.playSound(player, target, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
      for(int l = 0; l < 8; ++l) {
        world.addParticle(ParticleTypes.LARGE_SMOKE, target.getX() + Math.random(), target.getY() + Math.random(), target.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
      }
      placed = true;
    } else if (existing.canBeReplaced(fluid)) {
      // if its a liquid container, we should have validated it already
      if (!world.isClientSide && !existing.getMaterial().isLiquid()) {
        world.destroyBlock(target, true);
      }
      if (world.setBlockAndUpdate(target, fluid.defaultFluidState().createLegacyBlock()) || existing.getFluidState().isSource()) {
        world.playSound(null, target, fluid.getAttributes().getEmptySound(fluidStack), SoundSource.BLOCKS, 1.0F, 1.0F);
        placed = true;
      }
    } else if (existing.getBlock() instanceof LiquidBlockContainer) {
      // if not replaceable, it must be a liquid container
      ((LiquidBlockContainer) existing.getBlock()).placeLiquid(world, target, existing, ((FlowingFluid)fluid).getSource(false));
      world.playSound(null, target, fluid.getAttributes().getEmptySound(fluidStack), SoundSource.BLOCKS, 1.0F, 1.0F);
      placed = true;
    }

    // if we placed something, consume fluid
    if (placed) {
      drain(tool, fluidStack, FluidAttributes.BUCKET_VOLUME);
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
  }

  @Override
  public InteractionResult onToolUse(IModifierToolStack tool, int level, Level world, Player player, InteractionHand hand) {
    if (player.isCrouching()) {
      return InteractionResult.PASS;
    }
    // need at least a bucket worth of empty space
    FluidStack fluidStack = getFluid(tool);
    if (getCapacity(tool) - fluidStack.getAmount() < FluidAttributes.BUCKET_VOLUME) {
      return InteractionResult.PASS;
    }
    // have to trace again to find the fluid, ensure we can edit the position
    BlockHitResult trace = ModifiableItem.blockRayTrace(world, player, ClipContext.Fluid.SOURCE_ONLY);
    if (trace.getType() != Type.BLOCK) {
      return InteractionResult.PASS;
    }
    Direction face = trace.getDirection();
    BlockPos target = trace.getBlockPos();
    BlockPos offset = target.relative(face);
    if (!world.mayInteract(player, target) || !player.mayUseItemAt(offset, face, player.getItemInHand(hand))) {
      return InteractionResult.PASS;
    }
    // try to find a fluid here
    FluidState fluidState = world.getFluidState(target);
    Fluid currentFluid = fluidStack.getFluid();
    if (fluidState.isEmpty() || (!fluidStack.isEmpty() && !currentFluid.isSame(fluidState.getType()))) {
      return InteractionResult.PASS;
    }
    // finally, pickup the fluid
    BlockState state = world.getBlockState(target);
    if (state.getBlock() instanceof BucketPickup) {
      Fluid pickedUpFluid = ((BucketPickup)state.getBlock()).takeLiquid(world, target, state);
      if (pickedUpFluid != Fluids.EMPTY) {
        player.playSound(pickedUpFluid.getAttributes().getFillSound(fluidStack), 1.0F, 1.0F);
        // set the fluid if empty, increase the fluid if filled
        if (!world.isClientSide) {
          if (fluidStack.isEmpty()) {
            setFluid(tool, new FluidStack(pickedUpFluid, FluidAttributes.BUCKET_VOLUME));
          } else if (pickedUpFluid == currentFluid) {
            fluidStack.grow(FluidAttributes.BUCKET_VOLUME);
            setFluid(tool, fluidStack);
          } else {
            TConstruct.LOG.error("Picked up a fluid {} that does not match the current fluid state {}, this should not happen", pickedUpFluid, fluidState.getType());
          }
        }
        return InteractionResult.SUCCESS;
      }
    }
    return InteractionResult.PASS;
  }
}
