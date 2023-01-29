package net.p455w0rd.wirelesscraftingterminal.client.gui;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.p455w0rd.wirelesscraftingterminal.client.gui.widgets.GuiTrashButton;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotTrash;
import net.p455w0rd.wirelesscraftingterminal.common.utils.WCTLog;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketEmptyTrash;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketInventoryAction;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketSwitchGuis;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketValueConfig;
import net.p455w0rd.wirelesscraftingterminal.handlers.LocaleHandler;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import yalter.mousetweaks.api.IMTModGuiContainer;
import appeng.api.config.ActionItems;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.config.YesNo;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.ActionKey;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.client.me.InternalSlotME;
import appeng.client.me.ItemRepo;
import appeng.client.me.SlotME;
import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.helpers.InventoryAction;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import codechicken.nei.TextField;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.ReflectionHelper;

@Optional.Interface(modid = "MouseTweaks", iface = "yalter.mousetweaks.api.IMTModGuiContainer")
public class GuiWirelessCraftingTerminal extends AEBaseGui
        implements ISortSource, IConfigManagerHost, IMTModGuiContainer {

    private float xSize_lo;
    private float ySize_lo;
    public static int tick = 0, GUI_HEIGHT = 240, GUI_WIDTH = 230, AE_ROW_HEIGHT = 18, AE_NUM_ROWS = 0,
            GUI_UPPER_HEIGHT = 35, GUI_SEARCH_ROW = 35, SEARCH_X = 80, SEARCH_Y = 4, SEARCH_WIDTH = 88,
            SEARCH_HEIGHT = 12, SEARCH_MAXCHARS = 15, GUI_LOWER_HEIGHT, AE_TOTAL_ROWS_HEIGHT,
            BUTTON_SEARCH_MODE_POS_X = -18, BUTTON_SEARCH_MODE_POS_Y = 68, BUTTON_SIZE = 16, NEI_EXTRA_SPACE = 30,
            slotYOffset;
    protected static final int BUTTON_SEARCH_MODE_ID = 5;
    protected static final long TOOLTIP_UPDATE_INTERVAL = 3000L;
    private int currScreenWidth, currScreenHeight;
    private static final String bgTexturePath = "gui/crafting_viewcell.png";
    private final ContainerWirelessCraftingTerminal containerWCT;
    private boolean isFullScreen, init = true, reInit, wasResized = false;
    public static GuiWirelessCraftingTerminal INSTANCE;
    private static boolean switchingGuis;
    public static int craftingGridOffsetX = 80;
    public static int craftingGridOffsetY;
    private static String memoryText = "";
    private final ItemRepo repo;
    private final int offsetX = 8;
    private final IConfigManager configSrc;
    private GuiTabButton craftingStatusBtn;
    private MEGuiTextField searchField;
    private int perRow = 9;
    private boolean customSortOrder = true;
    private GuiImgButton ViewBox;
    private GuiImgButton SortByBox;
    private GuiImgButton SortDirBox;
    private GuiImgButton searchBoxSettings;
    private GuiImgButton clearBtn;
    private GuiTrashButton trashBtn;
    private GuiImgButton terminalStyleBox;
    private GuiImgButton searchStringSave;
    private boolean isAutoFocus = false;
    private int currentMouseX = 0;
    private int currentMouseY = 0;
    public boolean devicePowered = false;
    private boolean isNEIEnabled;
    private boolean wasTextboxFocused = false;
    private int screenResTicks = 0;
    private int reservedSpace = 0;
    private int maxRows = Integer.MAX_VALUE;
    private int standardSize;
    private final int lowerTextureOffset = 0;
    private int rows = 0;
    private final ItemStack[] myCurrentViewCells = new ItemStack[5];

    public GuiWirelessCraftingTerminal(Container container) {
        super(container);
        GuiWirelessCraftingTerminal.INSTANCE = this;
        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
        this.standardSize = this.xSize;
        this.setReservedSpace(73);
        this.containerWCT = (ContainerWirelessCraftingTerminal) container;
        this.setScrollBar(new GuiScrollbar());
        this.repo = new ItemRepo(getScrollBar(), this);
        this.configSrc = ((IConfigurableObject) this.inventorySlots).getConfigManager();
        this.devicePowered = containerWCT.isPowered();
        ((ContainerWirelessCraftingTerminal) this.inventorySlots).setGui(this);
    }

    public void postUpdate(final List<IAEItemStack> list) {
        for (final IAEItemStack is : list) {
            this.repo.postUpdate(is);
        }
        this.repo.updateView();
        this.setScrollBar();
    }

    private void setScrollBar() {
        this.getScrollBar().setTop(18).setLeft(174).setHeight(this.rows * 18 - 2);
        this.getScrollBar().setRange(
                0,
                (this.repo.size() + this.perRow - 1) / this.perRow - this.rows,
                Math.max(1, this.rows / 6));
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn == this.craftingStatusBtn) {
            NetworkHandler.instance.sendToServer(new PacketSwitchGuis(Reference.GUI_CRAFTING_STATUS));
        }

        if (btn instanceof GuiImgButton) {
            final boolean backwards = Mouse.isButtonDown(1);

            final GuiImgButton iBtn = (GuiImgButton) btn;
            if (iBtn.getSetting() != Settings.ACTIONS) {
                final Enum cv = iBtn.getCurrentValue();
                final Enum next = Platform.rotateEnum(cv, backwards, iBtn.getSetting().getPossibleValues());

                if (btn == this.terminalStyleBox) {
                    AEConfig.instance.settings.putSetting(iBtn.getSetting(), next);
                }
                if (btn == this.searchBoxSettings) {
                    AEConfig.instance.settings.putSetting(iBtn.getSetting(), next);
                } else if (btn == this.searchStringSave) {
                    AEConfig.instance.preserveSearchBar = next == YesNo.YES;
                } else {
                    try {
                        NetworkHandler.instance
                                .sendToServer(new PacketValueConfig(iBtn.getSetting().name(), next.name()));
                    } catch (final IOException e) {
                        WCTLog.debug(e.getMessage());
                    }
                }

                iBtn.set(next);

                if (next.getClass() == SearchBoxMode.class || next.getClass() == TerminalStyle.class) {
                    this.reinitalize();
                }
            }
            if (this.clearBtn == btn) {
                Slot s = null;
                final Container c = this.inventorySlots;
                for (final Object j : c.inventorySlots) {
                    if (j instanceof SlotCraftingMatrix) {
                        s = (Slot) j;
                    }
                }

                if (s != null) {
                    final PacketInventoryAction p = new PacketInventoryAction(
                            InventoryAction.MOVE_REGION,
                            s.slotNumber,
                            0);
                    NetworkHandler.instance.sendToServer(p);
                }
            }
        }
        if (btn instanceof GuiTrashButton) {
            if (this.trashBtn == btn) {
                Slot s = null;
                final Container c = this.inventorySlots;
                for (final Object j : c.inventorySlots) {
                    if (j instanceof SlotTrash) {
                        s = (Slot) j;
                    }
                }

                if (s != null) {
                    if (s.getHasStack()) {
                        containerWCT.trashSlot.clearStack();
                        NetworkHandler.instance.sendToServer(new PacketEmptyTrash());
                    }
                }
            }
        }
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotIdx, int ctrlDown, int mouseButton) {
        // Handle autocraft requests ourselves to spawn the right GUI classes
        final EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        if (slot instanceof SlotME) {
            InventoryAction action = null;
            IAEItemStack stack = null;

            switch (mouseButton) {
                case 0: // pickup / set-down.
                    action = ctrlDown == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
                    stack = ((SlotME) slot).getAEStack();

                    if (stack != null && action == InventoryAction.PICKUP_OR_SET_DOWN
                            && stack.getStackSize() == 0
                            && player.inventory.getItemStack() == null) {
                        action = InventoryAction.AUTO_CRAFT;
                    }

                    break;
                case 3: // creative dupe:
                    stack = ((SlotME) slot).getAEStack();
                    if (stack != null && stack.isCraftable()) {
                        action = InventoryAction.AUTO_CRAFT;
                    }
                    break;
                default:
            }
            if (action == InventoryAction.AUTO_CRAFT) {
                ((AEBaseContainer) this.inventorySlots).setTargetStack(stack);
                final PacketInventoryAction p = new PacketInventoryAction(
                        action,
                        this.inventorySlots.inventorySlots.size(),
                        0);
                NetworkHandler.instance.sendToServer(p);
                return;
            }
        }
        super.handleMouseClick(slot, slotIdx, ctrlDown, mouseButton);
    }

    private void reinitalize() {
        this.buttonList.clear();
        this.initGui();
    }

    int getReservedSpace() {
        return this.reservedSpace;
    }

    void setReservedSpace(final int reservedSpace) {
        this.reservedSpace = reservedSpace;
    }

    public int getStandardSize() {
        return this.standardSize;
    }

    void setStandardSize(final int standardSize) {
        this.standardSize = standardSize;
    }

    /**
     * Initializes the GUI
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void initGui() {
        super.initGui();

        Keyboard.enableRepeatEvents(true);
        this.maxRows = this.getMaxRows();
        this.perRow = AEConfig.instance.getConfigManager().getSetting(Settings.TERMINAL_STYLE) != TerminalStyle.FULL ? 9
                : 9 + ((this.width - this.standardSize) / 18);
        this.isNEIEnabled = Loader.isModLoaded("NotEnoughItems");
        int top = isNEIEnabled ? 22 : 0;
        final int magicNumber = 114 + 1;
        final int extraSpace = this.height - magicNumber - top - this.reservedSpace;
        this.rows = (int) Math.floor(extraSpace / 18);
        if (this.rows > this.maxRows) {
            top += (this.rows - this.maxRows) * 18 / 2;
            this.rows = this.maxRows;
        }

        if (isNEIEnabled) {
            this.rows--;
        }

        if (this.rows < 3) {
            this.rows = 3;
        }

        this.getMeSlots().clear();
        for (int y = 0; y < this.rows; y++) {
            for (int x = 0; x < this.perRow; x++) {
                this.getMeSlots()
                        .add(new InternalSlotME(this.repo, x + y * this.perRow, this.offsetX + x * 18, 18 + y * 18));
            }
        }
        if (AEConfig.instance.getConfigManager().getSetting(Settings.TERMINAL_STYLE) != TerminalStyle.FULL) {
            this.xSize = this.standardSize + ((this.perRow - 9) * 18);
        } else {
            this.xSize = this.standardSize;
        }
        super.initGui();

        this.ySize = magicNumber + this.rows * 18 + this.reservedSpace;
        final int unusedSpace = this.height - this.ySize;
        this.guiTop = (int) Math.floor(unusedSpace / (unusedSpace < 0 ? 3.8f : 2.0f));
        int offset = this.guiTop + 8;

        this.buttonList.clear();
        this.buttonList.add(
                this.clearBtn = new GuiImgButton(
                        this.guiLeft + 134,
                        this.guiTop + this.ySize - 160,
                        Settings.ACTIONS,
                        ActionItems.STASH));
        this.buttonList.add(this.trashBtn = new GuiTrashButton(this.guiLeft + 98, this.guiTop + this.ySize - 104));
        this.clearBtn.setHalfSize(true);
        if (this.customSortOrder) {
            this.buttonList.add(
                    this.SortByBox = new GuiImgButton(
                            this.guiLeft - 18,
                            offset,
                            Settings.SORT_BY,
                            this.configSrc.getSetting(Settings.SORT_BY)));
            offset += 20;
        }

        this.buttonList.add(
                this.ViewBox = new GuiImgButton(
                        this.guiLeft - 18,
                        offset,
                        Settings.VIEW_MODE,
                        this.configSrc.getSetting(Settings.VIEW_MODE)));
        offset += 20;

        this.buttonList.add(
                this.SortDirBox = new GuiImgButton(
                        this.guiLeft - 18,
                        offset,
                        Settings.SORT_DIRECTION,
                        this.configSrc.getSetting(Settings.SORT_DIRECTION)));
        offset += 20;

        this.buttonList.add(
                this.searchBoxSettings = new GuiImgButton(
                        this.guiLeft - 18,
                        offset,
                        Settings.SEARCH_MODE,
                        AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE)));
        offset += 20;

        this.buttonList.add(
                this.searchStringSave = new GuiImgButton(
                        this.guiLeft - 18,
                        offset,
                        Settings.SAVE_SEARCH,
                        AEConfig.instance.preserveSearchBar ? YesNo.YES : YesNo.NO));
        offset += 20;

        this.buttonList.add(
                this.terminalStyleBox = new GuiImgButton(
                        this.guiLeft - 18,
                        offset,
                        Settings.TERMINAL_STYLE,
                        AEConfig.instance.settings.getSetting(Settings.TERMINAL_STYLE)));

        this.searchField = new MEGuiTextField(SEARCH_WIDTH, SEARCH_HEIGHT);
        this.searchField.x = SEARCH_X;
        this.searchField.y = SEARCH_Y;

        this.buttonList.add(
                this.craftingStatusBtn = new GuiTabButton(
                        this.guiLeft + 169,
                        this.guiTop - 4,
                        2 + 11 * 16,
                        GuiText.CraftingStatus.getLocal(),
                        itemRender));
        this.craftingStatusBtn.setHideEdge(13);

        final Enum searchMode = AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE);
        this.isAutoFocus = SearchBoxMode.AUTOSEARCH == searchMode || SearchBoxMode.NEI_AUTOSEARCH == searchMode;

        this.searchField.setFocused(this.isAutoFocus);

        if (AEConfig.instance.preserveSearchBar || this.isSubGui()) {
            this.searchField.setText(memoryText);
            this.repo.setSearchString(memoryText);
        }
        if (this.isSubGui()) {
            this.repo.updateView();
            this.setScrollBar();
        }

        craftingGridOffsetX = Integer.MAX_VALUE;
        craftingGridOffsetY = Integer.MAX_VALUE;

        for (final Object s : this.inventorySlots.inventorySlots) {
            if (s instanceof AppEngSlot) {
                if (((Slot) s).xDisplayPosition < 197) {
                    this.repositionSlot((AppEngSlot) s);
                }
            }

            if (s instanceof SlotCraftingMatrix || s instanceof SlotFakeCraftingMatrix) {
                final Slot g = (Slot) s;
                if (g.xDisplayPosition > 0 && g.yDisplayPosition > 0) {
                    craftingGridOffsetX = Math.min(craftingGridOffsetX, g.xDisplayPosition);
                    craftingGridOffsetY = Math.min(craftingGridOffsetY, g.yDisplayPosition);
                }
            }
        }
    }

    int getMaxRows() {
        return AEConfig.instance.getConfigManager().getSetting(Settings.TERMINAL_STYLE) == TerminalStyle.SMALL ? 6
                : Integer.MAX_VALUE;
    }

    protected void repositionSlot(final AppEngSlot s) {
        s.yDisplayPosition = s.getY() + this.ySize - 78 - 5;
    }

    /**
     * Called every tick. It is used in this instance to detect screen size changes to automatically update the gui
     * height.
     * <p>
     * reInit variable is used to call initGui() on the next tick. It has to be called this way to update correctly.
     *
     * @author p455w0rd
     */
    @Override
    public void updateScreen() {
        this.devicePowered = containerWCT.isPowered();
        this.repo.setPower(devicePowered);

        super.updateScreen();
        if (this.init) {
            this.currScreenWidth = this.mc.displayWidth;
            this.currScreenHeight = this.mc.displayHeight;
            this.isFullScreen = this.mc.isFullScreen();
            this.reInit = true;
            this.init = false;
        }
        ++screenResTicks;
        if (screenResTicks == 20) {
            this.wasTextboxFocused = this.searchField.isFocused();
            screenResTicks = 0;
        }
        if (this.reInit) {
            if (tick != 1) {
                tick++;
            } else {
                this.initGui();
                this.setScrollBar();
                if (this.wasResized) {
                    this.searchField.setFocused(this.wasTextboxFocused);
                    this.searchField.setText(this.repo.getSearchString());
                    this.wasTextboxFocused = false;
                    this.wasResized = false;
                }
                this.reInit = false;
                tick = 0;
            }
        }
        if (hasScreenResChanged()) {
            this.reInit = true;
            this.wasResized = true;
        }
        if (!this.mc.thePlayer.isEntityAlive() || this.mc.thePlayer.isDead) {
            this.mc.thePlayer.closeScreen();
        }
    }

    /**
     * Detects if the window has been resized
     *
     * @return True if window has been resized
     * @author p455w0rd
     */
    private boolean hasScreenResChanged() {
        if ((this.currScreenWidth != this.mc.displayWidth) || (this.currScreenHeight != this.mc.displayHeight)
                || (this.isFullScreen != this.mc.isFullScreen())) {
            this.currScreenWidth = this.mc.displayWidth;
            this.currScreenHeight = this.mc.displayHeight;
            this.isFullScreen = this.mc.isFullScreen();
            return true;
        }
        return false;
    }

    /**
     * Draws the screen and tooltips.
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float btn) {
        super.drawScreen(mouseX, mouseY, btn);
        this.xSize_lo = (float) mouseX;
        this.ySize_lo = (float) mouseY;
    }

    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        String s = LocaleHandler.WirelessTermLabel.getLocal();
        this.mc.fontRenderer.drawString(s, 7, 5, 4210752);
        this.mc.fontRenderer.drawString(I18n.format("container.inventory"), 7, this.ySize - 172 + 3, 4210752);
        if (this.searchField != null) {
            this.searchField.drawTextBox();
        }

        this.currentMouseX = mouseX;
        this.currentMouseY = mouseY;
    }

    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.bindMyTexture(bgTexturePath);
        final int x_width = 199;

        // draw "over inventory area"
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, x_width, 18);

        for (int x = 0; x < this.rows; x++) {
            this.drawTexturedModalRect(offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18);
        }

        // draw player inv
        this.drawTexturedModalRect(
                offsetX,
                offsetY + 16 + this.rows * 18 + this.lowerTextureOffset,
                0,
                106 - 18 - 18,
                x_width,
                99 + this.reservedSpace - this.lowerTextureOffset);

        // draw view cells
        this.drawTexturedModalRect(offsetX + x_width, offsetY, x_width, 0, 32, 104);

        if (Reference.WCT_BOOSTER_ENABLED) {
            this.drawTexturedModalRect(this.guiLeft + 132, (this.guiTop + this.rows * 18) + 83, 237, 237, 19, 19);
        }
        GuiInventory.func_147046_a(
                this.guiLeft + 51,
                (this.guiTop + this.rows * 18) + 94,
                32,
                (float) (this.guiLeft + 51) - this.xSize_lo,
                (float) ((this.guiTop + this.rows * 18) + 50) - this.ySize_lo,
                this.mc.thePlayer);

        boolean update = false;
        for (int i = 0; i < 5; i++) {
            if (myCurrentViewCells[i] != containerWCT.getCellViewSlot(i).getStack()) {
                update = true;
                myCurrentViewCells[i] = containerWCT.getCellViewSlot(i).getStack();
            }
        }

        if (update) {
            repo.setViewCell(myCurrentViewCells);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) {

        if (!this.isAutoFocus) {
            this.searchField.mouseClicked(xCoord - this.guiLeft, yCoord - this.guiTop, btn);
        }

        if (btn == 1 && this.searchField.isMouseIn(xCoord - this.guiLeft, yCoord - this.guiTop)) {
            this.searchField.setText("");
            this.repo.setSearchString("");
            this.repo.updateView();
            this.setScrollBar();
        }

        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        if (this.searchField.getText() != null) {
            memoryText = this.searchField.getText();
        }
    }

    protected boolean enableSpaceClicking() {
        return true;
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        if (!this.checkHotbarKeys(key)) {
            if (isNEIFocused()) {
                return;
            }

            if (CommonHelper.proxy.isActionKey(ActionKey.TOGGLE_FOCUS, key)) {
                this.searchField.setFocused(!this.searchField.isFocused());
                return;
            }

            if (this.searchField.isFocused() && key == Keyboard.KEY_RETURN) {
                this.searchField.setFocused(false);
                return;
            }

            if (character == ' ' && this.searchField.getText().isEmpty()) {
                return;
            }

            final boolean mouseInGui = this
                    .isPointInRegion(0, 0, this.xSize, this.ySize, this.currentMouseX, this.currentMouseY);
            if (this.isAutoFocus && !this.searchField.isFocused() && mouseInGui) {
                this.searchField.setFocused(true);
            }

            if (this.searchField.textboxKeyTyped(character, key)) {
                this.repo.setSearchString(this.searchField.getText());
                this.repo.updateView();
                this.setScrollBar();
            } else {
                super.keyTyped(character, key);
            }
        }
    }

    protected boolean isPowered() {
        return this.repo.hasPower();
    }

    public void bindMyTexture(final String file) {
        final ResourceLocation loc = new ResourceLocation(Reference.MODID, "textures/" + file);
        this.mc.getTextureManager().bindTexture(loc);
    }

    public List<String> handleItemTooltip(final ItemStack stack, final int mouseX, final int mouseY,
            final List<String> currentToolTip) {
        if (stack != null) {
            final Slot s = this.getSlot(mouseX, mouseY);
            if (s instanceof SlotME) {
                final int BigNumber = AEConfig.instance.useTerminalUseLargeFont() ? 999 : 9999;

                IAEItemStack myStack = null;

                try {
                    final SlotME theSlotField = (SlotME) s;
                    myStack = theSlotField.getAEStack();
                } catch (final Throwable ignore) {}

                if (myStack != null) {
                    if (myStack.getStackSize() > BigNumber || (myStack.getStackSize() > 1 && stack.isItemDamaged())) {
                        final String local = ButtonToolTips.ItemsStored.getLocal();
                        final String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                                .format(myStack.getStackSize());
                        final String format = String.format(local, formattedAmount);

                        currentToolTip.add("\u00a77" + format);
                    }

                    if (myStack.getCountRequestable() > 0) {
                        final String local = ButtonToolTips.ItemsRequestable.getLocal();
                        final String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                                .format(myStack.getCountRequestable());
                        final String format = String.format(local, formattedAmount);

                        currentToolTip.add("\u00a77" + format);
                    }
                } else if (stack.stackSize > BigNumber || (stack.stackSize > 1 && stack.isItemDamaged())) {
                    final String local = ButtonToolTips.ItemsStored.getLocal();
                    final String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(stack.stackSize);
                    final String format = String.format(local, formattedAmount);

                    currentToolTip.add("\u00a77" + format);
                }
            }
        }
        return currentToolTip;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void renderToolTip(final ItemStack stack, final int x, final int y) {
        final Slot s = this.getSlot(x, y);
        if (s instanceof SlotME && stack != null) {
            final int BigNumber = AEConfig.instance.useTerminalUseLargeFont() ? 999 : 9999;

            IAEItemStack myStack = null;

            try {
                final SlotME theSlotField = (SlotME) s;
                myStack = theSlotField.getAEStack();
            } catch (final Throwable ignore) {}

            if (myStack != null) {
                final List<String> currentToolTip = stack
                        .getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);

                if (myStack.getStackSize() > BigNumber || (myStack.getStackSize() > 1 && stack.isItemDamaged())) {
                    currentToolTip.add(
                            "Items Stored: "
                                    + NumberFormat.getNumberInstance(Locale.US).format(myStack.getStackSize()));
                }

                if (myStack.getCountRequestable() > 0) {
                    currentToolTip.add(
                            "Items Requestable: "
                                    + NumberFormat.getNumberInstance(Locale.US).format(myStack.getCountRequestable()));
                }

                this.drawTooltip(x, y, 0, join(currentToolTip, "\n"));
            } else if (stack.stackSize > BigNumber) {
                final List var4 = stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);
                var4.add("Items Stored: " + NumberFormat.getNumberInstance(Locale.US).format(stack.stackSize));
                this.drawTooltip(x, y, 0, join(var4, "\n"));
                return;
            }
        }
        super.renderToolTip(stack, x, y);
        // super.drawItemStackTooltip( stack, x, y );
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void updateSetting(final IConfigManager manager, final Enum settingName, final Enum newValue) {
        if (this.SortByBox != null) {
            this.SortByBox.set(this.configSrc.getSetting(Settings.SORT_BY));
        }

        if (this.SortDirBox != null) {
            this.SortDirBox.set(this.configSrc.getSetting(Settings.SORT_DIRECTION));
        }

        if (this.ViewBox != null) {
            this.ViewBox.set(this.configSrc.getSetting(Settings.VIEW_MODE));
        }

        this.repo.updateView();
        this.setScrollBar();
    }

    public boolean isCustomSortOrder() {
        return this.customSortOrder;
    }

    void setCustomSortOrder(final boolean customSortOrder) {
        this.customSortOrder = customSortOrder;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enum getSortBy() {
        return this.configSrc.getSetting(Settings.SORT_BY);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enum getSortDir() {
        return this.configSrc.getSetting(Settings.SORT_DIRECTION);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enum getSortDisplay() {
        return this.configSrc.getSetting(Settings.VIEW_MODE);
    }

    public boolean isOverSearchField(int x, int y) {
        return searchField.isMouseIn(x - guiLeft, y - guiTop);
    }

    public void setSearchString(String memoryText) {
        this.searchField.setText(memoryText);
        this.repo.setSearchString(memoryText);
        this.repo.updateView();
        this.setScrollBar();
    }

    @SuppressWarnings("SameParameterValue")
    protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {
        int i = this.guiLeft;
        int j = this.guiTop;
        pointX -= i;
        pointY -= j;
        return pointX >= rectX - 1 && pointX < rectX + rectWidth + 1
                && pointY >= rectY - 1
                && pointY < rectY + rectHeight + 1;
    }

    private boolean isNEIFocused() {
        if (!Loader.isModLoaded("NotEnoughItems")) {
            return false;
        }
        try {
            final Class<? super Object> c = ReflectionHelper
                    .getClass(this.getClass().getClassLoader(), "codechicken.nei.LayoutManager");
            final Field fldSearchField = c.getField("searchField");
            final TextField searchField = (TextField) fldSearchField.get(c);
            return searchField.focused();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    // MouseTweaks compat

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public int getAPIVersion() {
        return 1;
    }

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public String getModName() {
        return Reference.NAME;
    }

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public boolean isMouseTweaksDisabled() {
        return true;
    }

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public boolean isWheelTweakDisabled() {
        return true;
    }

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public boolean isCraftingOutputSlot(Object modContainer, Object slot) {
        return slot == containerWCT.getSlot(ContainerWirelessCraftingTerminal.CRAFT_RESULT);
    }

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public Object getModContainer() {
        return containerWCT;
    }

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public int getModSlotCount(Object modContainer) {
        return containerWCT.inventorySlots.size();
    }

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public Object getModSlot(Object modContainer, int slotNumber) {
        return slotNumber < containerWCT.inventorySlots.size() ? containerWCT.inventorySlots.get(slotNumber) : null;
    }

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public Object getModSelectedSlot(Object modContainer, int slotCount) {
        Slot s = getSlot(currentMouseX, currentMouseY);
        return s;
    }

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public void clickModSlot(Object modContainer, Object slotO, int mouseButton, boolean shiftPressed) {}

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public void disableRMBDragIfRequired(Object modContainer, Object firstSlot, boolean shouldClick) {}
}
