package slimeknights.tconstruct.shared.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import slimeknights.mantle.inventory.BaseContainer;
import slimeknights.tconstruct.shared.TinkerCommons;

import javax.annotation.Nullable;

/** Container that triggers the criteria instance */
public class TriggeringBaseContainer<TILE extends BlockEntity> extends BaseContainer<TILE> {
  protected TriggeringBaseContainer(MenuType<?> type, int id, @Nullable Inventory inv, @Nullable TILE tile) {
    super(type, id, inv, tile);
    TinkerCommons.CONTAINER_OPENED_TRIGGER.trigger(tile, inv);
  }
}
