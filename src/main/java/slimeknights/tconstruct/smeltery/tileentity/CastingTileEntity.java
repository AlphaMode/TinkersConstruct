package slimeknights.tconstruct.smeltery.tileentity;

import lombok.Getter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import slimeknights.mantle.recipe.RecipeHelper;
import slimeknights.mantle.util.TickableBlockEntity;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.library.recipe.RecipeTypes;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipe;
import slimeknights.tconstruct.shared.tileentity.TableTileEntity;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.network.FluidUpdatePacket;
import slimeknights.tconstruct.smeltery.recipe.TileCastingWrapper;
import slimeknights.tconstruct.smeltery.tileentity.inventory.MoldingInventoryWrapper;
import slimeknights.tconstruct.smeltery.tileentity.tank.CastingFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public abstract class CastingTileEntity extends TableTileEntity implements TickableBlockEntity, WorldlyContainer, FluidUpdatePacket.IFluidPacketReceiver {
  // slots
  public static final int INPUT = 0;
  public static final int OUTPUT = 1;
  // NBT
  private static final String TAG_TANK = "tank";
  private static final String TAG_TIMER = "timer";
  private static final String TAG_RECIPE = "recipe";

  /** Special casting fluid tank */
  @Getter
  private final CastingFluidHandler tank = new CastingFluidHandler(this);
  private final LazyOptional<CastingFluidHandler> holder = LazyOptional.of(() -> tank);

  /* Casting recipes */
  /** Recipe type for casting recipes, may be basin or table */
  private final RecipeType<ICastingRecipe> castingType;
  /** Inventory for use in casting recipes */
  private final TileCastingWrapper castingInventory;
  /** Current recipe progress */
  @Getter
  private int timer;
  /** Current in progress recipe */
  private ICastingRecipe currentRecipe;
  /** Name of the current recipe, fetched from NBT. Used since NBT is read before recipe manager access */
  private ResourceLocation recipeName;
  /** Cache recipe to reduce time during recipe lookups. Not saved to NBT */
  private ICastingRecipe lastCastingRecipe;
  /** Last recipe output for client side display */
  private ItemStack lastOutput = null;

  /* Molding recipes */
  /** Recipe type for molding recipes, may be basin or table */
  private final RecipeType<MoldingRecipe> moldingType;
  /** Inventory to use for molding recipes */
  private final MoldingInventoryWrapper moldingInventory;
  /** Cache recipe to reduce time during recipe lookups. Not saved to NBT */
  private MoldingRecipe lastMoldingRecipe;

  protected CastingTileEntity(BlockEntityType<?> BlockEntityTypeIn, BlockPos pos, BlockState state, RecipeType<ICastingRecipe> castingType, RecipeType<MoldingRecipe> moldingType) {
    super(BlockEntityTypeIn, pos, state, "gui.tconstruct.casting", 2, 1);
    this.itemHandler = new SidedInvWrapper(this, Direction.DOWN);
    this.castingType = castingType;
    this.moldingType = moldingType;
    this.castingInventory = new TileCastingWrapper(this);
    this.moldingInventory = new MoldingInventoryWrapper(itemHandler, INPUT);
  }

  @Override
  @Nonnull
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
    if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
      return holder.cast();
    return super.getCapability(capability, facing);
  }

  /**
   * Called from {@link slimeknights.tconstruct.smeltery.block.AbstractCastingBlock#use(BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)}
   * @param player Player activating the block.
   */
  public void interact(Player player, InteractionHand hand) {
    if (level == null || level.isClientSide) {
      return;
    }
    // can't interact if liquid inside
    if (!tank.isEmpty()) {
      return;
    }

    ItemStack held = player.getItemInHand(hand);
    ItemStack input = getItem(INPUT);
    ItemStack output = getItem(OUTPUT);

    // all molding recipes require a stack in the input slot and nothing in the output slot
    if (!input.isEmpty() && output.isEmpty()) {
      // first, try the players hand item for a recipe
      moldingInventory.setPattern(held);
      MoldingRecipe recipe = findMoldingRecipe();
      if (recipe != null) {
        // if hand is empty, pick up the result (hand empty will only match recipes with no mold item)
        ItemStack result = recipe.assemble(moldingInventory);
        if (held.isEmpty()) {
          setItem(INPUT, ItemStack.EMPTY);
          player.setItemInHand(hand, result);
        } else {
          // if the recipe has a mold, hand item goes on table (if not consumed in crafting)
          setItem(INPUT, result);
          if (!recipe.isPatternConsumed()) {
            setItem(OUTPUT, ItemHandlerHelper.copyStackWithSize(held, 1));
            // send a block update for the comparator, needs to be done after the stack is removed
            level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
          }
          held.shrink(1);
          player.setItemInHand(hand, held.isEmpty() ? ItemStack.EMPTY : held);
        }
        moldingInventory.setPattern(ItemStack.EMPTY);
        return;
      } else {
        // if no recipe was found using the held item, try to find a mold-less recipe to perform
        // this ensures that if a recipe happens "on pickup" you get consistent behavior, without this it would fall though to pick up normally
        moldingInventory.setPattern(ItemStack.EMPTY);
        recipe = findMoldingRecipe();
        if (recipe != null) {
          setItem(INPUT, ItemStack.EMPTY);
          ItemHandlerHelper.giveItemToPlayer(player, recipe.assemble(moldingInventory), player.getInventory().selected);
          return;
        }
      }
      // clear mold stack, prevents storing an unneeded item
      moldingInventory.setPattern(ItemStack.EMPTY);
    }

    // recipes failed, so do normal pickup
    // completely empty -> insert current item into input
    if (input.isEmpty() && output.isEmpty()) {
      if (!held.isEmpty()) {
        ItemStack stack = held.split(stackSizeLimit);
        player.setItemInHand(hand, held.isEmpty() ? ItemStack.EMPTY : held);
        setItem(INPUT, stack);
      }
    } else {
      // stack in either slot, take one out
      // prefer output stack, as often the input is a cast that we want to use again
      int slot = output.isEmpty() ? INPUT : OUTPUT;

      // Additional info: Only 1 item can be put into the casting block usually, however recipes
      // can have ItemStacks with stacksize > 1 as output
      // we therefore spill the whole contents on extraction.
      ItemStack stack = getItem(slot);
      ItemHandlerHelper.giveItemToPlayer(player, stack, player.getInventory().selected);
      setItem(slot, ItemStack.EMPTY);

      // send a block update for the comparator, needs to be done after the stack is removed
      if (slot == OUTPUT) {
        level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
      }
    }
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    ItemStack original = getItem(slot);
    super.setItem(slot, stack);
    // if the stack changed emptiness, update
    if (original.isEmpty() != stack.isEmpty() && level != null && !level.isClientSide) {
      level.updateNeighbourForOutputSignal(worldPosition, this.getBlockState().getBlock());
    }
  }
  
  @Override
  @Nonnull
  public int[] getSlotsForFace(Direction side) {
    return new int[]{INPUT, OUTPUT};
  }

  @Override
  public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, @Nullable Direction direction) {
    return tank.isEmpty() && index == INPUT && !isStackInSlot(OUTPUT);
  }

  @Override
  public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
    return tank.isEmpty() && index == OUTPUT;
  }

  @Override
  public void tick() {
    // no recipe
    if (level == null || currentRecipe == null) {
      return;
    }
    // fully filled
    FluidStack currentFluid = tank.getFluid();
    if (currentFluid.getAmount() >= tank.getCapacity() && !currentFluid.isEmpty()) {
      timer++;
      if (!level.isClientSide) {
        castingInventory.setFluid(currentFluid.getFluid());
        if (timer >= currentRecipe.getCoolingTime(castingInventory)) {
          if (!currentRecipe.matches(castingInventory, level)) {
            // if lost our recipe or the recipe needs more fluid then we have, we are done
            // will come around later for the proper fluid amount
            currentRecipe = findCastingRecipe();
            recipeName = null;
            if (currentRecipe == null || currentRecipe.getFluidAmount(castingInventory) > currentFluid.getAmount()) {
              timer = 0;
              return;
            }
          }

          // actual recipe result
          ItemStack output = currentRecipe.assemble(castingInventory);
          if (currentRecipe.switchSlots()) {
            if (!currentRecipe.isConsumed()) {
              setItem(OUTPUT, getItem(INPUT));
            }
            setItem(INPUT, output);
          } else {
            if (currentRecipe.isConsumed()) {
              setItem(INPUT, ItemStack.EMPTY);
            }
            setItem(OUTPUT, output);
          }
          level.playSound(null, worldPosition, Sounds.CASTING_COOLS.getSound(), SoundSource.AMBIENT, 0.5f, 4f);

          reset();

          level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
        }
      }
      else if (level.random.nextFloat() > 0.9f) {
        level.addParticle(ParticleTypes.SMOKE, worldPosition.getX() + level.random.nextDouble(), worldPosition.getY() + 1.1d, worldPosition.getZ() + level.random.nextDouble(), 0.0D, 0.0D, 0.0D);
      }
    }
  }

  @Nullable
  private ICastingRecipe findCastingRecipe() {
    if (level == null) return null;
    if (this.lastCastingRecipe != null && this.lastCastingRecipe.matches(castingInventory, level)) {
      return this.lastCastingRecipe;
    }
    ICastingRecipe castingRecipe = level.getRecipeManager().getRecipeFor(this.castingType, castingInventory, level).orElse(null);
    if (castingRecipe != null) {
      this.lastCastingRecipe = castingRecipe;
    }
    return castingRecipe;
  }


  /**
   * Finds a molding recipe for the given inventory
   * @return  Recipe, or null if no recipe found
   */
  @Nullable
  private MoldingRecipe findMoldingRecipe() {
    if (level == null) return null;
    if (lastMoldingRecipe != null && lastMoldingRecipe.matches(moldingInventory, level)) {
      return lastMoldingRecipe;
    }
    Optional<MoldingRecipe> newRecipe = level.getRecipeManager().getRecipeFor(moldingType, moldingInventory, level);
    if (newRecipe.isPresent()) {
      lastMoldingRecipe = newRecipe.get();
      return lastMoldingRecipe;
    }
    return null;
  }


  /**
   * Called from CastingFluidHandler.fill()
   * @param fluid   Fluid used in casting
   * @param action  EXECUTE or SIMULATE
   * @return        Amount of fluid needed for recipe, used to resize the tank.
   */
  public int initNewCasting(Fluid fluid, IFluidHandler.FluidAction action) {
    if (this.currentRecipe != null || this.recipeName != null) {
      return 0;
    }

    boolean hasInput = !getItem(INPUT).isEmpty();
    boolean hasOutput = !getItem(OUTPUT).isEmpty();

    // no space for output, done
    if (hasInput && hasOutput) {
      return 0;
    }

    this.castingInventory.setFluid(fluid);
    // normal casting requires an empty output
    if (!hasOutput) {
      castingInventory.useInput();
      ICastingRecipe castingRecipe = findCastingRecipe();
      if (castingRecipe != null) {
        if (action == FluidAction.EXECUTE) {
          this.currentRecipe = castingRecipe;
          this.recipeName = null;
          this.lastOutput = null;
        }
        return castingRecipe.getFluidAmount(castingInventory);
      }
    } else {
      // if we have an output and no input, try using that as the input
      castingInventory.useOutput();
      ICastingRecipe castingRecipe = findCastingRecipe();
      if (castingRecipe != null) {
        if (action == FluidAction.EXECUTE) {
          this.currentRecipe = castingRecipe;
          this.recipeName = null;
          this.lastOutput = null;
          // move output to input slot, prevents removing and ensures item is reduced properly
          setItem(INPUT, getItem(OUTPUT));
          setItem(OUTPUT, ItemStack.EMPTY);
          castingInventory.useInput();
        }
        return castingRecipe.getFluidAmount(castingInventory);
      }
    }
    return 0;
  }

  /**
   * Resets the casting table recipe to the default empty state
   */
  public void reset() {
    timer = 0;
    currentRecipe = null;
    recipeName = null;
    lastOutput = null;
    castingInventory.setFluid(Fluids.EMPTY);
    tank.reset();
  }

  @Override
  public void updateFluidTo(FluidStack fluid) {
    if (fluid.isEmpty()) {
      reset();
    } else {
      int capacity = initNewCasting(fluid.getFluid(), FluidAction.EXECUTE);
      if (capacity > 0) {
        tank.setCapacity(capacity);
      }
    }
    tank.setFluid(fluid);
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
    // no GUI
    return null;
  }


  /* TER display */

  /**
   * Gets the recipe output for display in the TER
   * @return  Recipe output
   */
  public ItemStack getRecipeOutput() {
    if (lastOutput == null) {
      if (currentRecipe == null) {
        return ItemStack.EMPTY;
      }
      castingInventory.setFluid(tank.getFluid().getFluid());
      lastOutput = currentRecipe.assemble(castingInventory);
    }
    return lastOutput;
  }

  /**
   * Gets the total time for this recipe for display in the TER
   * @return  total recipe time
   */
  public int getRecipeTime() {
    if (currentRecipe == null) {
      return -1;
    }
    return currentRecipe.getCoolingTime(castingInventory);
  }


  /* NBT */

  /**
   * Loads a recipe in from its name and updates the tank capacity
   * @param world  Nonnull world instance
   * @param name   Recipe name to load
   */
  private void loadRecipe(Level world, ResourceLocation name) {
    // if the tank is empty, ignore old recipe
    FluidStack fluid = tank.getFluid();
    if(!fluid.isEmpty()) {
      // fetch recipe by name
      RecipeHelper.getRecipe(world.getRecipeManager(), name, ICastingRecipe.class).ifPresent(recipe -> {
        this.currentRecipe = recipe;
        castingInventory.setFluid(fluid.getFluid());
        tank.setCapacity(recipe.getFluidAmount(castingInventory));
      });
    }
  }

  @Override
  public void setLevel(Level world) {
    super.setLevel(world);
    // if we have a recipe name, swap recipe name for recipe instance
    if (recipeName != null) {
      loadRecipe(world, recipeName);
      recipeName = null;
    }
  }

  @Override
  public void writeSynced(CompoundTag tags) {
    super.writeSynced(tags);
    tags.put(TAG_TANK, tank.writeToNBT(new CompoundTag()));
    tags.putInt(TAG_TIMER, timer);
  }

  @Override
  @Nonnull
  public CompoundTag save(CompoundTag tags) {
    tags = super.save(tags);
    if (currentRecipe != null) {
      tags.putString(TAG_RECIPE, currentRecipe.getId().toString());
    } else if (recipeName != null) {
      tags.putString(TAG_RECIPE, recipeName.toString());
    }
    return tags;
  }

  @Override
  public void load(CompoundTag tags) {
    super.load(tags);
    tank.readFromNBT(tags.getCompound(TAG_TANK));
    timer = tags.getInt(TAG_TIMER);
    if (tags.contains(TAG_RECIPE, NBT.TAG_STRING)) {
      ResourceLocation name = new ResourceLocation(tags.getString(TAG_RECIPE));
      // if we have a world, fetch the recipe
      if (level != null) {
        loadRecipe(level, name);
      } else {
        // otherwise fetch the recipe when the world is set
        recipeName = name;
      }
    }
  }

  public static class Basin extends CastingTileEntity {
    public Basin(BlockPos pos, BlockState state) {
      super(TinkerSmeltery.basin.get(), pos, state, RecipeTypes.CASTING_BASIN, RecipeTypes.MOLDING_BASIN);
    }
  }

  public static class Table extends CastingTileEntity {
    public Table(BlockPos pos, BlockState state) {
      super(TinkerSmeltery.table.get(), pos, state, RecipeTypes.CASTING_TABLE, RecipeTypes.MOLDING_TABLE);
    }
  }
}
