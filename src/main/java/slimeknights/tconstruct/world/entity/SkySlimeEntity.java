package slimeknights.tconstruct.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.world.TinkerWorld;

public class SkySlimeEntity extends Slime {
  public SkySlimeEntity(EntityType<? extends SkySlimeEntity> type, Level worldIn) {
    super(type, worldIn);
  }

  @Override
  protected float getJumpPower() {
    return (float)Math.sqrt(this.getSize()) * this.getBlockJumpFactor() / 2;
  }

  @Override
  protected ParticleOptions getParticleType() {
    return TinkerWorld.skySlimeParticle.get();
  }
}
