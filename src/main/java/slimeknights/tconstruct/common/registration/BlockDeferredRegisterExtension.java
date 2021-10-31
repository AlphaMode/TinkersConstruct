package slimeknights.tconstruct.common.registration;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.fml.RegistryObject;
import slimeknights.mantle.registration.deferred.BlockDeferredRegister;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.ItemObject;

import java.util.function.Function;
import java.util.function.Supplier;

public class BlockDeferredRegisterExtension extends BlockDeferredRegister {
  public BlockDeferredRegisterExtension(String modID) {
    super(modID);
  }

  /**
   * Creates a new metal item object
   * @param name           Metal name
   * @param tagName        Name to use for tags for this block
   * @param blockSupplier  Supplier for the block
   * @param blockItem      Block item
   * @param itemProps      Properties for the item
   * @return  Metal item object
   */
  public MetalItemObject registerMetal(String name, String tagName, Supplier<Block> blockSupplier, Function<Block,? extends BlockItem> blockItem, Item.Properties itemProps) {
    ItemObject<Block> block = register(name + "_block", blockSupplier, blockItem);
    Supplier<Item> itemSupplier = () -> new Item(itemProps);
    RegistryObject<Item> ingot = itemRegister.register(name + "_ingot", itemSupplier);
    RegistryObject<Item> nugget = itemRegister.register(name + "_nugget", itemSupplier);
    return new MetalItemObject(tagName, block, ingot, nugget);
  }

  /**
   * Creates a new metal item object
   * @param name           Metal name
   * @param blockSupplier  Supplier for the block
   * @param blockItem      Block item
   * @param itemProps      Properties for the item
   * @return  Metal item object
   */
  public MetalItemObject registerMetal(String name, Supplier<Block> blockSupplier, Function<Block,? extends BlockItem> blockItem, Item.Properties itemProps) {
    return registerMetal(name, name, blockSupplier, blockItem, itemProps);
  }

  /**
   * Creates a new metal item object
   * @param name        Metal name
   * @param tagName     Name to use for tags for this block
   * @param blockProps  Properties for the block
   * @param blockItem   Block item
   * @param itemProps   Properties for the item
   * @return  Metal item object
   */
  public MetalItemObject registerMetal(String name, String tagName, BlockBehaviour.Properties blockProps, Function<Block,? extends BlockItem> blockItem, Item.Properties itemProps) {
    return registerMetal(name, tagName, () -> new Block(blockProps), blockItem, itemProps);
  }

  /**
   * Creates a new metal item object
   * @param name        Metal name
   * @param blockProps  Properties for the block
   * @param blockItem   Block item
   * @param itemProps   Properties for the item
   * @return  Metal item object
   */
  public MetalItemObject registerMetal(String name, BlockBehaviour.Properties blockProps, Function<Block,? extends BlockItem> blockItem, Item.Properties itemProps) {
    return registerMetal(name, name, blockProps, blockItem, itemProps);
  }

  /**
   * Registers a block with enum variants, but no item form
   * @param values  Enum value list
   * @param name    Suffix after value name
   * @param mapper  Function to map types to blocks
   * @param <T>  Type of enum
   * @param <B>  Type of block
   * @return  Enum object
   */
  public <T extends Enum<T> & StringRepresentable, B extends Block> EnumObject<T, B> registerEnumNoItem(T[] values, String name, Function<T, ? extends B> mapper) {
    return registerEnum(values, name, (fullName, value) -> this.registerNoItem(fullName, () -> mapper.apply(value)));
  }
}
