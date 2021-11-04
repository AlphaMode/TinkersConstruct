package slimeknights.tconstruct.library.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;

import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;

/** Helpers related to NBT */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TagUtil {
  /* Helper functions */

  /**
   * Reads a block position from NBT
   * @param parent  Parent tag
   * @param key     Position key
   * @return  Block position, or null if invalid or missing
   */
  @Nullable
  public static BlockPos readPos(CompoundTag parent, String key) {
    if (parent.contains(key, NBT.TAG_COMPOUND)) {
      return NbtUtils.readBlockPos(parent.getCompound(key));
    }
    return null;
  }
}
