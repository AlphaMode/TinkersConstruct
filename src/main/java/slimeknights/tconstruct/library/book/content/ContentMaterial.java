package slimeknights.tconstruct.library.book.content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fmllegacy.ForgeI18n;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.TextComponentData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.ItemElement;
import slimeknights.mantle.client.screen.book.element.TextComponentElement;
import slimeknights.mantle.client.screen.book.element.TextElement;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.book.TinkerPage;
import slimeknights.tconstruct.library.book.elements.TinkerItemElement;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingLookup;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialFluidRecipe;
import slimeknights.tconstruct.library.tools.definition.PartRequirement;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tools.stats.ExtraMaterialStats;
import slimeknights.tconstruct.tools.stats.HandleMaterialStats;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ContentMaterial extends TinkerPage {
  private static final Component PART_BUILDER = TConstruct.makeTranslation("book", "material.part_builder");
  private static final String CAST_FROM = TConstruct.makeTranslationKey("book", "material.cast_from");
  private static final String COMPOSITE_FROM = TConstruct.makeTranslationKey("book", "material.composite_from");

  public static final String ID = "toolmaterial";

  private transient Lazy<IMaterial> material;
  private transient List<ItemStack> displayStacks;
  @SerializedName("material")
  public String materialName;
  public boolean detailed;

  public ContentMaterial(IMaterial material, List<ItemStack> displayStacks, boolean detailed) {
    this.material = Lazy.of(() -> material);
    this.materialName = material.getIdentifier().toString();
    this.displayStacks = displayStacks;
    this.detailed = detailed;
  }

  @Override
  public void load() {
    if (this.material == null) {
      this.material = Lazy.of(() -> MaterialRegistry.getMaterial(new MaterialId(this.materialName)));
    }
  }

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    IMaterial material = this.material.get();

    this.addTitle(list, new TranslatableComponent(material.getTranslationKey()).getString(), true, material.getColor().getValue());

    // the cool tools to the left/right
    this.addDisplayItems(list, rightSide ? BookScreen.PAGE_WIDTH - 18 : 0, material.getIdentifier());

    int col_margin = 22;
    int top = 15;
    int left = rightSide ? 0 : col_margin;

    int y = top + 10;
    int x = left + 5;
    int w = BookScreen.PAGE_WIDTH / 2 - 5;

    // head stats
    int headTraits = this.addStatsDisplay(x, y, w, list, material, HeadMaterialStats.ID);
    // handle
    int handleTraits = this.addStatsDisplay(x + w, y, w - 10, list, material, HandleMaterialStats.ID);

    // extra
    y+= 65;
    this.addStatsDisplay(x, y + 10 * headTraits, w, list, material, ExtraMaterialStats.ID);

    // inspirational quote, or boring description text
    MaterialId id = material.getIdentifier();
    String textKey = String.format(detailed ? "material.%s.%s.encyclopedia" : "material.%s.%s.flavor", id.getNamespace(), id.getPath());
    if (I18n.exists(textKey)) {
      // using forge instead of I18n.format as that prevents % from being interpreted as a format key
      String translated = ForgeI18n.getPattern(textKey);
      if (!detailed) {
        translated = '"' + translated + '"';
      }
      TextData flavourData = new TextData(translated);
      flavourData.italic = !detailed;
      list.add(new TextElement(x + w - 16, y + 10 * handleTraits, w, 60, flavourData));
    }
  }

  private int addStatsDisplay(int x, int y, int w, ArrayList<BookElement> list, IMaterial material, MaterialStatsId statsId) {
    Optional<IMaterialStats> stats = MaterialRegistry.getInstance().getMaterialStats(material.getIdentifier(), statsId);

    if (!stats.isPresent()) {
      return 0;
    }

    List<ModifierEntry> traits = MaterialRegistry.getInstance().getTraits(material.getIdentifier(), statsId);

    // create a list of all valid toolparts with the stats
    List<ItemStack> parts = Lists.newLinkedList();

    for (IToolPart part : getToolParts()) {
      if (part.getStatType() == statsId) {
        parts.add(part.withMaterial(material));
      }
    }

    // said parts next to the name
    if (parts.size() > 0) {
      ItemElement display = new TinkerItemElement(x, y + 1, 0.5f, parts);
      list.add(display);
    }

    // and the name itself
    TextElement name = new TextElement(x + 10, y, w - 10, 10, stats.get().getLocalizedName().getString());
    name.text[0].underlined = true;
    list.add(name);
    y += 12;

    List<TextComponentData> lineData = Lists.newArrayList();
    // add lines of tool information
    lineData.addAll(getStatLines(stats.get()));
    lineData.addAll(getTraitLines(traits, material));

    list.add(new TextComponentElement(x, y, w, BookScreen.PAGE_HEIGHT, lineData));

    return traits.size();
  }

  public static List<TextComponentData> getStatLines(IMaterialStats stats) {
    List<TextComponentData> lineData = new ArrayList<>();

    for (int i = 0; i < stats.getLocalizedInfo().size(); i++) {
      TextComponentData text = new TextComponentData(stats.getLocalizedInfo().get(i));
      if (stats.getLocalizedDescriptions().get(i).getString().isEmpty()) {
        text.tooltips = null;
      } else {
        text.tooltips = new Component[]{stats.getLocalizedDescriptions().get(i)};
      }

      lineData.add(text);
      lineData.add(new TextComponentData("\n"));
    }

    return lineData;
  }

  public static List<TextComponentData> getTraitLines(List<ModifierEntry> traits, IMaterial material) {
    List<TextComponentData> lineData = new ArrayList<>();

    for (ModifierEntry trait : traits) {
      Modifier mod = trait.getModifier();
      TextComponentData textComponentData = new TextComponentData(mod.getDisplayName());

      List<Component> textComponents = mod.getDescriptionList();
      List<Component> formatted = new ArrayList<>();


      for (int index = 0; index < textComponents.size(); index++) {
        Component textComponent = textComponents.get(index);

        if (index == 0) {
          formatted.add(textComponent.copy().withStyle(style -> style.withColor(material.getColor())));
        } else {
          formatted.add(textComponent);
        }
      }

      textComponentData.tooltips = formatted.toArray(new Component[0]);
      textComponentData.text = textComponentData.text.copy().withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.UNDERLINE);

      lineData.add(textComponentData);
      lineData.add(new TextComponentData("\n"));
    }

    return lineData;
  }

  /** Checks if the given material has the given stat type */
  private static boolean hasStatType(MaterialId materialId, MaterialStatsId statsId) {
    return MaterialRegistry.getInstance().getMaterialStats(materialId, statsId).isPresent();
  }

  /** Gets the first material from the registry for the given stat type */
  private static IMaterial getFirstMaterialWithType(MaterialStatsId statsId) {
    for (IMaterial material : MaterialRegistry.getMaterials()) {
      if (hasStatType(material.getIdentifier(), statsId)) {
        return material;
      }
    }
    return IMaterial.UNKNOWN;
  }

  private void addDisplayItems(ArrayList<BookElement> list, int x, MaterialId materialId) {
    List<ItemElement> displayTools = Lists.newArrayList();

    // representative item first
    if (!this.displayStacks.isEmpty())
      displayTools.add(new TinkerItemElement(0, 0, 1f, displayStacks));
    else {
      System.out.println("Material with id " + materialId + " has no representation items associated with it");
    }

    if (material.get().isCraftable()) {
      ItemStack partBuilder = new ItemStack(TinkerTables.partBuilder.asItem());
      ItemElement elementItem = new TinkerItemElement(partBuilder);
      elementItem.tooltip = ImmutableList.of(PART_BUILDER);
      displayTools.add(elementItem);
    }
    // regular casting recipes
    List<MaterialFluidRecipe> fluids = MaterialCastingLookup.getCastingFluids(materialId);
    if (!fluids.isEmpty()) {
      ItemElement elementItem = new TinkerItemElement(0, 0, 1, fluids.stream().flatMap(recipe -> recipe.getFluids().stream())
                                                                     .map(fluid -> new ItemStack(fluid.getFluid().getBucket()))
                                                                     .collect(Collectors.toList()));
      FluidStack firstFluid = fluids.stream()
                                    .flatMap(recipe -> recipe.getFluids().stream())
                                    .findFirst().orElse(FluidStack.EMPTY);
      elementItem.tooltip = ImmutableList.of(new TranslatableComponent(CAST_FROM, firstFluid.getFluid().getAttributes().getDisplayName(firstFluid)));
      displayTools.add(elementItem);
    }
    // composite casting
    List<MaterialFluidRecipe> composites = MaterialCastingLookup.getCompositeFluids(materialId);
    for (MaterialFluidRecipe composite : composites) {
      IMaterial input = composite.getInput();
      if (input != null) {
        ItemElement elementItem = new TinkerItemElement(0, 0, 1, MaterialCastingLookup.getAllItemCosts().stream()
                                                                                      .map(Entry::getKey)
                                                                                      .filter(part -> part.canUseMaterial(input) && part.canUseMaterial(material.get()))
                                                                                      .map(part -> part.withMaterial(input))
                                                                                      .collect(Collectors.toList()));
        FluidStack firstFluid = composite.getFluids().stream().findFirst().orElse(FluidStack.EMPTY);
        elementItem.tooltip = ImmutableList.of(new TranslatableComponent(COMPOSITE_FROM,
                                                                            firstFluid.getFluid().getAttributes().getDisplayName(firstFluid),
                                                                            new TranslatableComponent(input.getTranslationKey())));
        displayTools.add(elementItem);
      }
    }

    int y = 10;
    for (Item item : TinkerTags.Items.MULTIPART_TOOL.getValues()) {
      if (item instanceof IModifiable) {
        IModifiable tool = ((IModifiable) item);
        List<PartRequirement> requirements = tool.getToolDefinition().getData().getParts();
        // start building the tool with the given material
        List<IMaterial> materials = new ArrayList<>(requirements.size());
        IMaterial material = MaterialRegistry.getMaterial(materialId);
        boolean usedMaterial = false;
        for (PartRequirement part : requirements) {
          if (hasStatType(materialId, part.getStatType())) {
            materials.add(material);
            usedMaterial = true;
          } else {
            materials.add(getFirstMaterialWithType(part.getStatType()));
          }
        }

        // only add a stack if our material showed up
        if (usedMaterial) {
          ItemStack display = ToolBuildHandler.buildItemFromMaterials(tool, materials);
          displayTools.add(new TinkerItemElement(display));
          if (displayTools.size() == 9) {
            break;
          }
        }
      }
    }

    // built tools
    if (!displayTools.isEmpty()) {
      for (ItemElement element : displayTools) {
        element.x = x;
        element.y = y;
        element.scale = 1f;
        y += ItemElement.ITEM_SIZE_HARDCODED;

        list.add(element);
      }
    }
  }

  public List<IToolPart> getToolParts() {
    return TinkerTags.Items.TOOL_PARTS.getValues().stream().filter(item -> item instanceof IToolPart).map(item -> (IToolPart) item).collect(Collectors.toList());
  }

}
