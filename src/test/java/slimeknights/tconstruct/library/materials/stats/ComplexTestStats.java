package slimeknights.tconstruct.library.materials.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ComplexTestStats extends BaseMaterialStats {
  private MaterialStatsId identifier;
  private int num;
  private float floating;
  private String text;

  public ComplexTestStats(MaterialStatsId identifier) {
    this.identifier = identifier;
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeInt(num);
    buffer.writeFloat(floating);
    buffer.writeString(text);
  }

  @Override
  public void decode(FriendlyByteBuf buffer) {
    num = buffer.readInt();
    floating = buffer.readFloat();
    text = buffer.readString();
  }

  @Override
  public List<ITextComponent> getLocalizedInfo() {
    return new ArrayList<>();
  }

  @Override
  public List<ITextComponent> getLocalizedDescriptions() {
    return new ArrayList<>();
  }
}
