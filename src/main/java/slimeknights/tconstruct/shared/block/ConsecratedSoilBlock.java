package slimeknights.tconstruct.shared.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class ConsecratedSoilBlock extends Block {

  public ConsecratedSoilBlock(Properties properties) {
    super(properties);
  }

  @Override
  public void stepOn(Level worldIn, BlockPos pos, Entity entityIn) {
    this.processConsecratedSoil(entityIn);
  }

  // damage and set undead entities on fire
  private void processConsecratedSoil(Entity entity) {
    if (entity instanceof Mob) {
      LivingEntity entityLiving = (LivingEntity) entity;
      if (entityLiving.getMobType() == MobType.UNDEAD) {
        entityLiving.hurt(DamageSource.MAGIC, 1);
        entityLiving.setSecondsOnFire(1);
      }
    }
  }

  @Nullable
  @Override
  //TODO: Replace when forge Re-Evaluates
  public net.minecraftforge.common.ToolType getHarvestTool(BlockState state) {
    return ToolType.SHOVEL;
  }

  @Override
  //TODO: Replace when forge Re-Evaluates
  public int getHarvestLevel(BlockState state) {
    return -1;
  }
}
