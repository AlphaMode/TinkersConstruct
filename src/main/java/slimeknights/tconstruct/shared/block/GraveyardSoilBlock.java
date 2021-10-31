package slimeknights.tconstruct.shared.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class GraveyardSoilBlock extends Block {

  public GraveyardSoilBlock(Properties properties) {
    super(properties);
  }

  @Override
  public void stepOn(Level worldIn, BlockPos pos, Entity entityIn) {
    this.processGraveyardSoil(entityIn);
  }

  // heal undead entities
  private void processGraveyardSoil(Entity entity) {
    if (entity instanceof Mob) {
      LivingEntity entityLiving = (LivingEntity) entity;
      if (entityLiving.getMobType() == MobType.UNDEAD) {
        entityLiving.heal(1);
      }
    }
  }
}
