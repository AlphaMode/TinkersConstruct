package slimeknights.tconstruct.library.book.sectiontransformer;

import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.tconstruct.library.book.content.ContentListing;
import slimeknights.tconstruct.library.book.content.ContentTool;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;

@OnlyIn(Dist.CLIENT)
public class ToolSectionTransformer extends ContentListingSectionTransformer {
  public static final ToolSectionTransformer INSTANCE = new ToolSectionTransformer("tools");

  public ToolSectionTransformer(String name) {
    super(name);
  }

  @Override
  protected void processPage(BookData book, ContentListing listing, PageData page) {
    // only add tool pages if the tool exists
    if (page.content instanceof ContentTool) {
      ResourceLocation toolId = new ResourceLocation(((ContentTool) page.content).toolName);
      if (ForgeRegistries.ITEMS.containsKey(toolId)) {
        Item toolItem = ForgeRegistries.ITEMS.getValue(toolId);
        if (toolItem instanceof IModifiableDisplay) {
          listing.addEntry(((IModifiableDisplay)toolItem).getLocalizedName().getString(), page);
        }
      }
    } else {
      super.processPage(book, listing, page);
    }
  }
}
