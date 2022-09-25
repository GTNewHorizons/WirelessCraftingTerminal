package net.p455w0rd.wirelesscraftingterminal.common.container;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.me.InternalSlotME;
import appeng.client.me.SlotME;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerNull;
import appeng.container.slot.AppEngCraftingSlot;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotCraftingTerm;
import appeng.container.slot.SlotDisabled;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotInaccessible;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorPlayerHand;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTermHandler;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTerminalItem;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotArmor;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotBooster;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotMagnet;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotTrash;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotViewCell;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryBooster;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryCrafting;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryMagnet;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryTrash;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryViewCell;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.common.utils.WCTLog;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketMEInventoryUpdate;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketValueConfig;
import net.p455w0rd.wirelesscraftingterminal.handlers.LocaleHandler;
import net.p455w0rd.wirelesscraftingterminal.helpers.WirelessTerminalGuiObject;
import net.p455w0rd.wirelesscraftingterminal.items.ItemInfinityBooster;
import net.p455w0rd.wirelesscraftingterminal.items.ItemMagnet;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class ContainerWirelessCraftingTerminal extends AEBaseContainer
        implements IConfigManagerHost,
                IConfigurableObject,
                IMEMonitorHandlerReceiver<IAEItemStack>,
                IAEAppEngInventory,
                IContainerCraftingPacket,
                IViewCellStorage {

    private final ItemStack containerstack;
    public final WCTInventoryCrafting craftingGrid;
    public final WCTInventoryViewCell viewCellInventory;
    public final WCTInventoryBooster boosterInventory;
    public final WCTInventoryMagnet magnetInventory;
    public final WCTInventoryTrash trashInventory;
    public final InventoryPlayer inventoryPlayer;
    public ItemStack[] craftMatrixInventory;
    private final World worldObj;
    private final EntityPlayer player;
    public static final int HOTBAR_START = 2,
            HOTBAR_END = HOTBAR_START + 8,
            INV_START = HOTBAR_END + 1,
            INV_END = INV_START + 26,
            ARMOR_START = INV_END + 1,
            ARMOR_END = ARMOR_START + 3,
            CRAFT_GRID_START = ARMOR_END + 1,
            CRAFT_GRID_END = CRAFT_GRID_START + 8,
            CRAFT_RESULT = CRAFT_GRID_END + 1,
            VIEW_CELL_START = CRAFT_RESULT + 1,
            VIEW_CELL_END = VIEW_CELL_START + 4,
            BOOSTER_INDEX = 1,
            MAGNET_INDEX = VIEW_CELL_END + 1;
    public static int CRAFTING_SLOT_X_POS = 80, CRAFTING_SLOT_Y_POS = 83;
    private SlotBooster boosterSlot;
    private final SlotMagnet magnetSlot;
    private final Slot[] hotbarSlot;
    private final Slot[] inventorySlot;
    private final SlotArmor[] armorSlot;
    private final SlotCraftingMatrix[] craftMatrixSlot;
    private final SlotCraftingTerm craftingSlot;
    private final SlotViewCell[] viewCellSlot;
    public SlotTrash trashSlot;
    private int firstCraftingSlotNumber = -1, lastCraftingSlotNumber = -1;

    private final WirelessTerminalGuiObject obj;
    private double powerMultiplier = 0.5;
    private final IPortableCell civ;
    private int ticks = 0;
    private final IMEMonitor<IAEItemStack> monitor;
    private final IItemList<IAEItemStack> items = AEApi.instance().storage().createItemList();
    private final IConfigManager clientCM;
    private IConfigManager serverCM;

    private IConfigManagerHost gui;
    private final AppEngInternalInventory output = new AppEngInternalInventory(this, 1);
    private final IWirelessCraftingTerminalItem thisItem;
    private final IGridNode networkNode;
    private IRecipe currentRecipe;

    /**
     * Constructor for our custom container
     *
     * @author p455w0rd
     */
    public ContainerWirelessCraftingTerminal(EntityPlayer player, InventoryPlayer inventoryPlayer) {
        super(
                inventoryPlayer,
                getGuiObject(
                        RandomUtils.getWirelessTerm(inventoryPlayer),
                        player,
                        player.worldObj,
                        (int) player.posX,
                        (int) player.posY,
                        (int) player.posZ));
        this.clientCM = new ConfigManager(this);
        this.clientCM.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        this.clientCM.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        this.clientCM.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
        this.setCustomName("WCTContainer");

        this.boosterInventory = new WCTInventoryBooster(RandomUtils.getWirelessTerm(inventoryPlayer));
        this.magnetInventory = new WCTInventoryMagnet(RandomUtils.getWirelessTerm(inventoryPlayer));
        this.trashInventory = new WCTInventoryTrash(RandomUtils.getWirelessTerm(inventoryPlayer));
        this.containerstack = RandomUtils.getWirelessTerm(inventoryPlayer);
        this.thisItem = (IWirelessCraftingTerminalItem) this.containerstack.getItem();
        this.worldObj = player.worldObj;
        this.craftingGrid = new WCTInventoryCrafting(this, 3, 3, containerstack);
        this.viewCellInventory = new WCTInventoryViewCell(RandomUtils.getWirelessTerm(inventoryPlayer));
        this.inventoryPlayer = inventoryPlayer;
        this.player = player;
        craftMatrixInventory = new ItemStack[9];
        hotbarSlot = new Slot[9];
        inventorySlot = new Slot[27];
        armorSlot = new SlotArmor[4];
        craftMatrixSlot = new SlotCraftingMatrix[9];
        viewCellSlot = new SlotViewCell[5];

        this.obj =
                getGuiObject(containerstack, player, worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
        this.civ = this.obj;
        final IGridNode node = ((IGridHost) this.obj).getGridNode(ForgeDirection.UNKNOWN);
        this.networkNode = node;

        if (Platform.isServer()) {
            this.serverCM = civ.getConfigManager();
            this.monitor = civ.getItemInventory();
            if (this.monitor != null) {
                this.monitor.addListener(this, null);
                setCellInventory(this.monitor);
                if (civ instanceof IPortableCell) {
                    setPowerSource(this.civ);
                }
            } else {
                this.setValidContainer(false);
            }
        } else {
            this.monitor = null;
        }

        // Dummy slot at index 0 to work around ME slots all having their slot number set to 0.
        this.addSlotToContainer(new SlotInaccessible(new AppEngInternalInventory(null, 1), 0, -1000, -1000));

        if (Reference.WCT_BOOSTER_ENABLED) {
            boosterSlot = new SlotBooster(this.boosterInventory, 0, 134, -20);
            this.addSlotToContainer(boosterSlot);
        } else {
            this.addSlotToContainer(new SlotInaccessible(new AppEngInternalInventory(null, 1), 0, -1000, -1000));
        }

        // Add hotbar slots
        for (int i = 0; i < 9; ++i) {
            if (player.inventory.getStackInSlot(i) != null
                    && player.inventory.getStackInSlot(i).getItem() == this.thisItem) {
                hotbarSlot[i] = new SlotDisabled(this.inventoryPlayer, i, i * 18 + 8, 58);
            } else {
                hotbarSlot[i] = new SlotPlayerHotBar(this.inventoryPlayer, i, i * 18 + 8, 58);
            }
            this.addSlotToContainer(hotbarSlot[i]);
        }

        int k = 0;

        // Add player inventory slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                if (player.inventory.getStackInSlot(j + i * 9 + 9) != null
                        && player.inventory.getStackInSlot(j + i * 9 + 9).getItem() == this.thisItem) {
                    inventorySlot[k] = new SlotDisabled(this.inventoryPlayer, j + i * 9 + 9, j * 18 + 8, i * 18);
                } else {
                    inventorySlot[k] = new SlotPlayerInv(this.inventoryPlayer, j + i * 9 + 9, j * 18 + 8, i * 18);
                }
                this.addSlotToContainer(inventorySlot[k]);
                k++;
            }
        }

        // Add armor slots
        for (int i = 0; i < 4; ++i) {
            armorSlot[i] = new SlotArmor(this.player, this.inventoryPlayer, 39 - i, (int) 8.5, (i * 18) - 76, i);
            this.addSlotToContainer(armorSlot[i]);
        }
        k = 0;
        // Add crafting grid slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                craftMatrixSlot[k] =
                        new SlotCraftingMatrix(this, this.craftingGrid, j + i * 3, 80 + j * 18, (i * 18) - 76);
                this.addSlotToContainer(craftMatrixSlot[k]);
                if (k == 0) {
                    this.firstCraftingSlotNumber = craftMatrixSlot[k].slotNumber;
                }
                k++;
            }
        }
        this.lastCraftingSlotNumber = craftMatrixSlot[8].slotNumber;

        craftingSlot = new SlotCraftingTerm(
                this.getPlayerInv().player,
                this.getActionSource(),
                this.getPowerSource(),
                this.obj,
                this.craftingGrid,
                this.craftingGrid,
                this.output,
                174,
                -58,
                this);
        // Add crafting result slot
        this.addSlotToContainer(craftingSlot);

        // Add view cell slots
        for (int i = 0; i < 5; i++) {
            viewCellSlot[i] = new SlotViewCell(getViewCellStorage(), i, 207, (i * 18) + 8, inventoryPlayer);
            addSlotToContainer(viewCellSlot[i]);
        }

        magnetSlot = new SlotMagnet(this.magnetInventory, 152, -20);
        this.addSlotToContainer(magnetSlot);

        trashSlot = new SlotTrash(this.trashInventory, 80, -20, player);
        trashSlot.setContainer(this);
        this.addSlotToContainer(trashSlot);

        updateCraftingMatrix();

        this.onCraftMatrixChanged(this.craftingGrid);
        thisItem.checkForBooster(containerstack);
    }

    @Override
    protected Slot addSlotToContainer(final Slot newSlot) {
        if (newSlot instanceof AppEngSlot) {
            final AppEngSlot s = (AppEngSlot) newSlot;
            s.setContainer(this);
            return super.addSlotToContainer(newSlot);
        } else {
            throw new IllegalArgumentException(
                    "Invalid Slot [" + newSlot + "] for WCT Container instead of AppEngSlot.");
        }
    }

    @Override
    public boolean canDragIntoSlot(final Slot s) {
        return ((AppEngSlot) s).isDraggable();
    }

    private IRecipe findMatchingRecipe(InventoryCrafting craftMatrix, World worldIn) {
        for (Object recipe : CraftingManager.getInstance().getRecipeList()) {
            if (((IRecipe) recipe).matches(craftMatrix, worldIn)) {
                return (IRecipe) recipe;
            }
        }
        return null;
    }

    @Override
    public void onCraftMatrixChanged(final IInventory iinv) {

        final ContainerNull cn = new ContainerNull();
        final InventoryCrafting craftingInv = new InventoryCrafting(cn, 3, 3);

        for (int x = 0; x < 9; x++) {
            craftingInv.setInventorySlotContents(x, craftMatrixSlot[x].getStack());
        }
        if (currentRecipe == null || !currentRecipe.matches(craftingInv, getPlayerInv().player.worldObj)) {
            currentRecipe = findMatchingRecipe(craftingInv, getPlayerInv().player.worldObj);
        }
        if (currentRecipe == null) {
            craftingSlot.putStack(null);
        } else {
            final ItemStack craftingResult = currentRecipe.getCraftingResult(craftingInv);
            craftingSlot.putStack(craftingResult);
        }
        writeToNBT("crafting");
    }

    @Override
    public void doAction(final EntityPlayerMP player, final InventoryAction action, final int slot, final long id) {
        if (slot >= 0 && slot < this.inventorySlots.size()) {
            final Slot s = this.getSlot(slot);

            if (s instanceof SlotCraftingTerm) {
                switch (action) {
                    case CRAFT_SHIFT:
                    case CRAFT_ITEM:
                    case CRAFT_STACK:
                        ((SlotCraftingTerm) s).doClick(action, player);
                        this.updateHeld(player);
                        break;
                    default:
                        break;
                }
            }

            if (s instanceof SlotFake) {
                final ItemStack hand = player.inventory.getItemStack();

                switch (action) {
                    case PICKUP_OR_SET_DOWN:
                        if (hand == null) {
                            s.putStack(null);
                        } else {
                            s.putStack(hand.copy());
                        }

                        break;
                    case PLACE_SINGLE:
                        if (hand != null) {
                            final ItemStack is = hand.copy();
                            is.stackSize = 1;
                            s.putStack(is);
                        }

                        break;
                    case SPLIT_OR_PLACE_SINGLE:
                        ItemStack is = s.getStack();
                        if (is != null) {
                            if (hand == null) {
                                is.stackSize--;
                            } else if (hand.isItemEqual(is)) {
                                is.stackSize = Math.min(is.getMaxStackSize(), is.stackSize + 1);
                            } else {
                                is = hand.copy();
                                is.stackSize = 1;
                            }

                            s.putStack(is);
                        } else if (hand != null) {
                            is = hand.copy();
                            is.stackSize = 1;
                            s.putStack(is);
                        }

                        break;
                    case CREATIVE_DUPLICATE:
                    case MOVE_REGION:
                    case SHIFT_CLICK:
                    default:
                        break;
                }
            }

            if (action == InventoryAction.MOVE_REGION) {
                final List<Slot> from = new LinkedList<Slot>();

                for (final Object j : this.inventorySlots) {
                    if (j instanceof Slot && j.getClass() == s.getClass()) {
                        from.add((Slot) j);
                    }
                }

                for (final Slot fr : from) {
                    this.transferStackInSlot(player, fr.slotNumber);
                }
            }

            return;
        }

        // get target item.
        final IAEItemStack slotItem = getTargetStack();

        switch (action) {
            case SHIFT_CLICK:
                if (this.getPowerSource() == null || this.getCellInventory() == null) {
                    return;
                }

                if (slotItem != null) {
                    IAEItemStack ais = slotItem.copy();
                    ItemStack myItem = ais.getItemStack();

                    ais.setStackSize(myItem.getMaxStackSize());

                    final InventoryAdaptor adp = InventoryAdaptor.getAdaptor(player, ForgeDirection.UNKNOWN);
                    myItem.stackSize = (int) ais.getStackSize();
                    myItem = adp.simulateAdd(myItem);

                    if (myItem != null) {
                        ais.setStackSize(ais.getStackSize() - myItem.stackSize);
                    }

                    ais = Platform.poweredExtraction(
                            this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource());
                    if (ais != null) {
                        adp.addItems(ais.getItemStack());
                    }
                }
                break;
            case ROLL_DOWN:
                if (this.getPowerSource() == null || this.getCellInventory() == null) {
                    return;
                }

                final int releaseQty = 1;
                final ItemStack isg = player.inventory.getItemStack();

                if (isg != null && releaseQty > 0) {
                    IAEItemStack ais = AEApi.instance().storage().createItemStack(isg);
                    ais.setStackSize(1);
                    final IAEItemStack extracted = ais.copy();

                    ais = Platform.poweredInsert(
                            this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource());
                    if (ais == null) {
                        final InventoryAdaptor ia = new AdaptorPlayerHand(player);

                        final ItemStack fail = ia.removeItems(1, extracted.getItemStack(), null);
                        if (fail == null) {
                            this.getCellInventory()
                                    .extractItems(extracted, Actionable.MODULATE, this.getActionSource());
                        }

                        this.updateHeld(player);
                    }
                }

                break;
            case ROLL_UP:
            case PICKUP_SINGLE:
                if (this.getPowerSource() == null || this.getCellInventory() == null) {
                    return;
                }

                if (slotItem != null) {
                    int liftQty = 1;
                    final ItemStack item = player.inventory.getItemStack();

                    if (item != null) {
                        if (item.stackSize >= item.getMaxStackSize()) {
                            liftQty = 0;
                        }
                        if (!Platform.isSameItemPrecise(slotItem.getItemStack(), item)) {
                            liftQty = 0;
                        }
                    }

                    if (liftQty > 0) {
                        IAEItemStack ais = slotItem.copy();
                        ais.setStackSize(1);
                        ais = Platform.poweredExtraction(
                                this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource());
                        if (ais != null) {
                            final InventoryAdaptor ia = new AdaptorPlayerHand(player);

                            final ItemStack fail = ia.addItems(ais.getItemStack());
                            if (fail != null) {
                                this.getCellInventory().injectItems(ais, Actionable.MODULATE, this.getActionSource());
                            }

                            this.updateHeld(player);
                        }
                    }
                }
                break;
            case PICKUP_OR_SET_DOWN:
                if (getPowerSource() == null || this.getCellInventory() == null) {
                    return;
                }

                if (player.inventory.getItemStack() == null) {
                    if (slotItem != null) {
                        IAEItemStack ais = slotItem.copy();
                        ais.setStackSize(ais.getItemStack().getMaxStackSize());
                        ais = Platform.poweredExtraction(
                                this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource());
                        if (ais != null) {
                            player.inventory.setItemStack(ais.getItemStack());
                        } else {
                            player.inventory.setItemStack(null);
                        }
                        this.updateHeld(player);
                    }
                } else {
                    IAEItemStack ais = AEApi.instance().storage().createItemStack(player.inventory.getItemStack());
                    ais = Platform.poweredInsert(
                            this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource());
                    if (ais != null) {
                        player.inventory.setItemStack(ais.getItemStack());
                    } else {
                        player.inventory.setItemStack(null);
                    }
                    this.updateHeld(player);
                }

                break;
            case SPLIT_OR_PLACE_SINGLE:
                if (this.getPowerSource() == null || this.getCellInventory() == null) {
                    return;
                }

                if (player.inventory.getItemStack() == null) {
                    if (slotItem != null) {
                        IAEItemStack ais = slotItem.copy();
                        final long maxSize = ais.getItemStack().getMaxStackSize();
                        ais.setStackSize(maxSize);
                        ais = this.getCellInventory().extractItems(ais, Actionable.SIMULATE, this.getActionSource());

                        if (ais != null) {
                            final long stackSize = Math.min(maxSize, ais.getStackSize());
                            ais.setStackSize((stackSize + 1) >> 1);
                            ais = Platform.poweredExtraction(
                                    this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource());
                        }

                        if (ais != null) {
                            player.inventory.setItemStack(ais.getItemStack());
                        } else {
                            player.inventory.setItemStack(null);
                        }
                        this.updateHeld(player);
                    }
                } else {
                    IAEItemStack ais = AEApi.instance().storage().createItemStack(player.inventory.getItemStack());
                    ais.setStackSize(1);
                    ais = Platform.poweredInsert(
                            this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource());
                    if (ais == null) {
                        final ItemStack is = player.inventory.getItemStack();
                        is.stackSize--;
                        if (is.stackSize <= 0) {
                            player.inventory.setItemStack(null);
                        }
                        this.updateHeld(player);
                    }
                }

                break;
            case CREATIVE_DUPLICATE:
                if (player.capabilities.isCreativeMode && slotItem != null) {
                    final ItemStack is = slotItem.getItemStack();
                    is.stackSize = is.getMaxStackSize();
                    player.inventory.setItemStack(is);
                    this.updateHeld(player);
                }
                break;
            case MOVE_REGION:
                if (this.getPowerSource() == null || this.getCellInventory() == null) {
                    return;
                }

                if (slotItem != null) {
                    final int playerInv = 9 * 4;
                    for (int slotNum = 0; slotNum < playerInv; slotNum++) {
                        IAEItemStack ais = slotItem.copy();
                        ItemStack myItem = ais.getItemStack();

                        ais.setStackSize(myItem.getMaxStackSize());

                        final InventoryAdaptor adp = InventoryAdaptor.getAdaptor(player, ForgeDirection.UNKNOWN);
                        myItem.stackSize = (int) ais.getStackSize();
                        myItem = adp.simulateAdd(myItem);

                        if (myItem != null) {
                            ais.setStackSize(ais.getStackSize() - myItem.stackSize);
                        }

                        ais = Platform.poweredExtraction(
                                this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource());
                        if (ais != null) {
                            adp.addItems(ais.getItemStack());
                        } else {
                            return;
                        }
                    }
                }

                break;
            default:
                break;
        }
    }

    private void updateCraftingMatrix() {
        if (!this.containerstack.hasTagCompound()) {
            this.containerstack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound stack = this.containerstack.getTagCompound();
        readMatrixNBT(stack);
        for (int i = 0; i < 9; i++) {
            this.craftingGrid.setInventorySlotContents(i, craftMatrixInventory[i]);
        }
    }

    @Override
    public void saveChanges() {
        // :P
    }

    @Override
    public void onChangeInventory(
            final IInventory inv,
            final int slot,
            final InvOperation mc,
            final ItemStack removedStack,
            final ItemStack newStack) {
        // <3
    }

    @Override
    public IGridNode getNetworkNode() {
        return this.networkNode;
    }

    @Override
    public boolean useRealItems() {
        return true;
    }

    @Override
    public IInventory getInventoryByName(final String name) {
        if (name.equals("player") || name.equals("container.inventory")) {
            return this.getInventoryPlayer();
        }
        if (name.equals("crafting")) {
            return this.craftingGrid;
        }
        return null;
    }

    @Override
    public IInventory getViewCellStorage() {
        return viewCellInventory;
    }

    @Override
    public ItemStack[] getViewCells() {
        final ItemStack[] list = new ItemStack[viewCellSlot.length];

        for (int x = 0; x < viewCellSlot.length; x++) {
            list[x] = viewCellSlot[x].getStack();
        }

        return list;
    }

    public SlotViewCell getCellViewSlot(final int index) {
        return viewCellSlot[index];
    }

    public static WirelessTerminalGuiObject getGuiObject(
            final ItemStack it, final EntityPlayer player, final World w, final int x, final int y, final int z) {
        if (it != null) {
            final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler)
                    AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
            if (wh != null) {
                return new WirelessTerminalGuiObject(wh, it, player, w, x, y, z);
            }
        }

        return null;
    }

    @Override
    public void detectAndSendChanges() {
        // drain 1 ae t
        this.ticks++;
        if (this.ticks > 10) {
            if (!isBoosterInstalled() || !Reference.WCT_BOOSTER_ENABLED) {
                this.civ.extractAEPower(
                        this.getPowerMultiplier() * this.ticks, Actionable.MODULATE, PowerMultiplier.CONFIG);
            } else {
                this.civ.extractAEPower((int) (0.5 * this.ticks), Actionable.MODULATE, PowerMultiplier.CONFIG);
            }
            this.ticks = 0;
        }

        if (Platform.isServer()) {
            if (this.monitor != this.civ.getItemInventory()) {
                this.setValidContainer(false);
            }

            for (final Settings set : this.serverCM.getSettings()) {
                final Enum<?> sideLocal = this.serverCM.getSetting(set);
                final Enum<?> sideRemote = this.clientCM.getSetting(set);

                if (sideLocal != sideRemote) {
                    this.clientCM.putSetting(set, sideLocal);
                    for (final Object crafter : this.crafters) {
                        try {
                            NetworkHandler.instance.sendTo(
                                    new PacketValueConfig(set.name(), sideLocal.name()), (EntityPlayerMP) crafter);
                        } catch (final IOException e) {
                            WCTLog.debug(e.getMessage());
                        }
                    }
                }
            }

            if (!this.items.isEmpty()) {
                try {
                    final IItemList<IAEItemStack> monitorCache = this.monitor.getStorageList();

                    final PacketMEInventoryUpdate piu = new PacketMEInventoryUpdate();

                    for (final IAEItemStack is : this.items) {
                        final IAEItemStack send = monitorCache.findPrecise(is);
                        if (send == null) {
                            is.setStackSize(0);
                            piu.appendItem(is);
                        } else {
                            piu.appendItem(send);
                        }
                    }

                    if (!piu.isEmpty()) {
                        this.items.resetStatus();

                        for (final Object c : this.crafters) {
                            if (c instanceof EntityPlayer) {
                                NetworkHandler.instance.sendTo(piu, (EntityPlayerMP) c);
                            }
                        }
                    }
                } catch (final IOException e) {
                    WCTLog.debug(e.getMessage());
                }
            }
        }
        if (!isInRange()) {
            if (!isBoosterInstalled() || !Reference.WCT_BOOSTER_ENABLED) {
                if (this.isValidContainer()) {
                    this.getPlayerInv().player.addChatMessage(PlayerMessages.OutOfRange.get());
                }
                this.setValidContainer(false);
            }
            if (!networkIsPowered()) {
                if (this.isValidContainer()) {
                    this.getPlayerInv()
                            .player
                            .addChatMessage(new ChatComponentText(LocaleHandler.NoNetworkPower.getLocal()));
                }
                this.setValidContainer(false);
            }
        } else if (!hasAccess(SecurityPermissions.CRAFT, true)
                || !hasAccess(SecurityPermissions.EXTRACT, true)
                || !hasAccess(SecurityPermissions.INJECT, true)) {
            if (this.isValidContainer()) {
                this.getPlayerInv().player.addChatMessage(PlayerMessages.CommunicationError.get());
            }
            this.setValidContainer(false);
        } else {
            this.setPowerMultiplier(AEConfig.instance.wireless_getDrainRate(this.obj.getRange()));
        }

        super.detectAndSendChanges();
    }

    public boolean isBoosterInstalled() {
        Slot slot = getSlotFromInventory(this.boosterInventory, 0);
        if (slot == null) {
            return false;
        }
        boolean hasStack = getSlotFromInventory(this.boosterInventory, 0).getHasStack();
        if (hasStack) {
            Item boosterSlotContents =
                    getSlotFromInventory(this.boosterInventory, 0).getStack().getItem();
            return boosterSlotContents instanceof ItemInfinityBooster;
        }
        return false;
    }

    public boolean isMagnetInstalled() {
        Slot slot = getSlotFromInventory(this.magnetInventory, MAGNET_INDEX);
        if (slot == null) {
            return false;
        }
        boolean hasStack =
                getSlotFromInventory(this.magnetInventory, MAGNET_INDEX).getHasStack();
        if (hasStack) {
            Item magnetSlotContents = getSlotFromInventory(this.magnetInventory, MAGNET_INDEX)
                    .getStack()
                    .getItem();
            return magnetSlotContents instanceof ItemMagnet;
        }
        return false;
    }

    protected boolean isInRange() {
        return this.obj.rangeCheck(Reference.WCT_BOOSTER_ENABLED && this.isBoosterInstalled());
    }

    protected boolean networkIsPowered() {
        final IActionHost host = this.getActionHost();
        if (host != null) {
            final IGrid grid = this.obj.getTargetGrid();
            if (grid != null) {
                final IEnergyGrid eg = grid.getCache(IEnergyGrid.class);
                return eg.isNetworkPowered();
            }
        }
        return false;
    }

    @Override
    public IConfigManager getConfigManager() {
        if (Platform.isServer()) {
            return this.serverCM;
        }
        return this.clientCM;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void updateSetting(final IConfigManager manager, final Enum settingName, final Enum newValue) {
        if (this.getGui() != null) {
            this.getGui().updateSetting(manager, settingName, newValue);
        }
    }

    private IConfigManagerHost getGui() {
        return this.gui;
    }

    public void setGui(@Nonnull final IConfigManagerHost gui) {
        this.gui = gui;
    }

    @Override
    public void onListUpdate() {
        for (final Object c : this.crafters) {
            if (c instanceof ICrafting) {
                final ICrafting cr = (ICrafting) c;
                this.queueInventory(cr);
            }
        }
    }

    @Override
    public void postChange(
            final IBaseMonitor<IAEItemStack> monitor,
            final Iterable<IAEItemStack> change,
            final BaseActionSource source) {
        for (final IAEItemStack is : change) {
            this.items.add(is);
        }
    }

    @Override
    public boolean isValid(final Object verificationToken) {
        return true;
    }

    @Override
    public void onContainerClosed(final EntityPlayer player) {
        super.onContainerClosed(player);
        if (this.monitor != null) {
            this.monitor.removeListener(this);
        }
    }

    @Override
    public void addCraftingToCrafters(final ICrafting c) {
        super.addCraftingToCrafters(c);
        this.queueInventory(c);
    }

    @Override
    public void removeCraftingFromCrafters(final ICrafting c) {
        super.removeCraftingFromCrafters(c);

        if (this.crafters.isEmpty() && this.monitor != null) {
            this.monitor.removeListener(this);
        }
    }

    private void queueInventory(final ICrafting c) {
        if (Platform.isServer() && c instanceof EntityPlayer && this.monitor != null) {
            try {
                PacketMEInventoryUpdate piu = new PacketMEInventoryUpdate();
                final IItemList<IAEItemStack> monitorCache = this.monitor.getStorageList();

                for (final IAEItemStack send : monitorCache) {
                    try {
                        piu.appendItem(send);
                    } catch (final BufferOverflowException boe) {
                        NetworkHandler.instance.sendTo(piu, (EntityPlayerMP) c);

                        piu = new PacketMEInventoryUpdate();
                        piu.appendItem(send);
                    }
                }

                NetworkHandler.instance.sendTo(piu, (EntityPlayerMP) c);
            } catch (final IOException e) {
                WCTLog.debug(e.getMessage());
            }
        }
    }

    public boolean isPowered() {
        double pwr = this.thisItem.getAECurrentPower(this.containerstack);
        return (pwr > 0.0);
    }

    private double getPowerMultiplier() {
        return this.powerMultiplier;
    }

    void setPowerMultiplier(final double powerMultiplier) {
        this.powerMultiplier = powerMultiplier;
    }

    /**
     * Fetches crafting matrix ItemStacks
     *
     * @param nbtTagCompound
     * @author p455w0rd
     */
    private void readMatrixNBT(NBTTagCompound nbtTagCompound) {
        // Read in the ItemStacks in the inventory from NBT
        NBTTagList tagList = nbtTagCompound.getTagList("CraftingMatrix", 10);
        craftMatrixInventory = new ItemStack[9];
        for (int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            int slot = tagCompound.getByte("Slot");
            if (slot >= 0 && slot < craftMatrixInventory.length) {
                craftMatrixInventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
    }

    /**
     * Initiates external NBT saving methods that store inventory contents
     */
    public void writeToNBT(String which) {
        if (!this.containerstack.hasTagCompound()) {
            this.containerstack.setTagCompound(new NBTTagCompound());
        }
        switch (which) {
            case "booster":
                boosterInventory.writeNBT(this.containerstack.getTagCompound());
                break;
            case "crafting":
                craftingGrid.writeNBT(this.containerstack.getTagCompound());
                break;
            case "viewCell":
                viewCellInventory.writeNBT(this.containerstack.getTagCompound());
                break;
            case "magnet":
                magnetInventory.writeNBT(this.containerstack.getTagCompound());
                break;
            case "trash":
                trashInventory.writeNBT(this.containerstack.getTagCompound());
                break;
            case "all":
            default:
                boosterInventory.writeNBT(this.containerstack.getTagCompound());
                craftingGrid.writeNBT(this.containerstack.getTagCompound());
                viewCellInventory.writeNBT(this.containerstack.getTagCompound());
                magnetInventory.writeNBT(this.containerstack.getTagCompound());
                trashInventory.writeNBT(this.containerstack.getTagCompound());
                break;
        }
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer p, final int idx) {
        final AppEngSlot clickSlot = (AppEngSlot) this.inventorySlots.get(idx); // require AE SLots!
        ItemStack tis = clickSlot.getStack();
        if (tis == null || tis == containerstack) {
            return null;
        }
        // Try to place armor in armor slot/booster in booster slot first
        if (isInInventory(idx) || isInHotbar(idx)) {
            if (tis.getItem() instanceof ItemArmor) {
                int type = ((ItemArmor) tis.getItem()).armorType;
                if (this.mergeItemStack(tis, ARMOR_START + type, ARMOR_START + type + 1, false)) {
                    clickSlot.clearStack();
                    return null;
                }
            } else if (tis.getItem() instanceof ItemInfinityBooster) {
                if (this.mergeItemStack(tis, BOOSTER_INDEX, BOOSTER_INDEX + 1, false)) {
                    clickSlot.clearStack();
                    return null;
                }
            } else if (tis.getItem() instanceof ItemMagnet) {
                if (this.mergeItemStack(tis, MAGNET_INDEX, MAGNET_INDEX + 1, false)) {
                    clickSlot.clearStack();
                    return null;
                }
            } else if (AEApi.instance().definitions().items().viewCell().isSameAs(tis)) {
                if (mergeItemStack(tis.copy(), VIEW_CELL_START, VIEW_CELL_END + 1, false)) {
                    if (tis.stackSize > 1) {
                        tis.stackSize--;
                    } else {
                        clickSlot.clearStack();
                    }
                    return null;
                }
            }
        }

        if (Platform.isClient()) {
            return null;
        }

        boolean hasMETiles = false;
        for (final Object is : this.inventorySlots) {
            if (is instanceof InternalSlotME) {
                hasMETiles = true;
                break;
            }
        }

        if (hasMETiles && Platform.isClient()) {
            return null;
        }

        if (clickSlot instanceof SlotDisabled || clickSlot instanceof SlotInaccessible) {
            return null;
        }
        if (clickSlot != null && clickSlot.getHasStack()) {

            final List<Slot> selectedSlots = new ArrayList<Slot>();

            /**
             * Gather a list of valid destinations.
             */
            if (clickSlot.isPlayerSide()) {
                tis = this.shiftStoreItem(tis);

                // target slots in the container...
                for (final Object inventorySlot : this.inventorySlots) {
                    final AppEngSlot cs = (AppEngSlot) inventorySlot;

                    if (!(cs.isPlayerSide()) && !(cs instanceof SlotFake) && !(cs instanceof AppEngCraftingSlot)) {
                        if (cs.isItemValid(tis)) {
                            selectedSlots.add(cs);
                        }
                    }
                }
            } else {
                // target slots in the container...
                for (final Object inventorySlot : this.inventorySlots) {
                    final AppEngSlot cs = (AppEngSlot) inventorySlot;

                    if ((cs.isPlayerSide()) && !(cs instanceof SlotFake) && !(cs instanceof AppEngCraftingSlot)) {
                        if (cs.isItemValid(tis)) {
                            selectedSlots.add(cs);
                        }
                    }
                }
            }

            /**
             * Handle Fake Slot Shift clicking.
             */
            if (selectedSlots.isEmpty() && clickSlot.isPlayerSide()) {
                if (tis != null) {
                    // target slots in the container...
                    for (final Object inventorySlot : this.inventorySlots) {
                        final AppEngSlot cs = (AppEngSlot) inventorySlot;
                        final ItemStack destination = cs.getStack();

                        if (!(cs.isPlayerSide()) && cs instanceof SlotFake) {
                            if (Platform.isSameItemPrecise(destination, tis)) {
                                return null;
                            } else if (destination == null) {
                                cs.putStack(tis.copy());
                                cs.onSlotChanged();
                                this.updateSlot(cs);
                                return null;
                            }
                        }
                    }
                }
            }

            if (tis != null) {
                // find partials..
                for (final Slot d : selectedSlots) {
                    if (d instanceof SlotDisabled || d instanceof SlotME) {
                        continue;
                    }

                    if (d.isItemValid(tis)) {
                        if (d.getHasStack()) {
                            final ItemStack t = d.getStack();

                            if (Platform.isSameItemPrecise(tis, t)) // t.isItemEqual(tis))
                            {
                                int maxSize = t.getMaxStackSize();
                                if (maxSize > d.getSlotStackLimit()) {
                                    maxSize = d.getSlotStackLimit();
                                }

                                int placeAble = maxSize - t.stackSize;

                                if (tis.stackSize < placeAble) {
                                    placeAble = tis.stackSize;
                                }

                                t.stackSize += placeAble;
                                tis.stackSize -= placeAble;

                                if (tis.stackSize <= 0) {
                                    clickSlot.putStack(null);
                                    d.onSlotChanged();

                                    // if ( hasMETiles ) updateClient();

                                    this.updateSlot(clickSlot);
                                    this.updateSlot(d);
                                    return null;
                                } else {
                                    this.updateSlot(d);
                                }
                            }
                        }
                    }
                }

                // any match..
                for (final Slot d : selectedSlots) {
                    if (d instanceof SlotDisabled || d instanceof SlotME) {
                        continue;
                    }

                    if (d.isItemValid(tis)) {
                        if (d.getHasStack()) {
                            final ItemStack t = d.getStack();

                            if (Platform.isSameItemPrecise(t, tis)) {
                                int maxSize = t.getMaxStackSize();
                                if (d.getSlotStackLimit() < maxSize) {
                                    maxSize = d.getSlotStackLimit();
                                }

                                int placeAble = maxSize - t.stackSize;

                                if (tis.stackSize < placeAble) {
                                    placeAble = tis.stackSize;
                                }

                                t.stackSize += placeAble;
                                tis.stackSize -= placeAble;

                                if (tis.stackSize <= 0) {
                                    clickSlot.putStack(null);
                                    d.onSlotChanged();

                                    // if ( worldEntity != null )
                                    // worldEntity.markDirty();
                                    // if ( hasMETiles ) updateClient();

                                    this.updateSlot(clickSlot);
                                    this.updateSlot(d);
                                    return null;
                                } else {
                                    this.updateSlot(d);
                                }
                            }
                        } else {
                            int maxSize = tis.getMaxStackSize();
                            if (maxSize > d.getSlotStackLimit()) {
                                maxSize = d.getSlotStackLimit();
                            }

                            final ItemStack tmp = tis.copy();
                            if (tmp.stackSize > maxSize) {
                                tmp.stackSize = maxSize;
                            }

                            tis.stackSize -= tmp.stackSize;
                            d.putStack(tmp);

                            if (tis.stackSize <= 0) {
                                clickSlot.putStack(null);
                                d.onSlotChanged();

                                // if ( worldEntity != null )
                                // worldEntity.markDirty();
                                // if ( hasMETiles ) updateClient();

                                this.updateSlot(clickSlot);
                                this.updateSlot(d);
                                return null;
                            } else {
                                this.updateSlot(d);
                            }
                        }
                    }
                }
            }

            clickSlot.putStack(tis != null ? tis.copy() : null);
        }

        this.updateSlot(clickSlot);
        return null;
    }

    private void updateSlot(final Slot clickSlot) {
        this.detectAndSendChanges();
    }

    private ItemStack shiftStoreItem(final ItemStack input) {
        if (this.getPowerSource() == null || this.civ == null) {
            return input;
        }
        final IAEItemStack ais = Platform.poweredInsert(
                this.getPowerSource(),
                this.civ,
                AEApi.instance().storage().createItemStack(input),
                this.getActionSource());
        if (ais == null) {
            return null;
        }
        return ais.getItemStack();
    }

    private boolean isInHotbar(@Nonnull int index) {
        return (index >= HOTBAR_START && index <= HOTBAR_END);
    }

    private boolean isInInventory(@Nonnull int index) {
        return (index >= INV_START && index <= INV_END);
    }

    @SuppressWarnings("unused")
    private boolean isInArmorSlot(@Nonnull int index) {
        return (index >= ARMOR_START && index <= ARMOR_END);
    }

    @SuppressWarnings("unused")
    private boolean isInBoosterSlot(@Nonnull int index) {
        return (index == BOOSTER_INDEX);
    }

    @SuppressWarnings("unused")
    private boolean isCraftResult(@Nonnull int index) {
        return (index == CRAFT_RESULT);
    }

    @SuppressWarnings("unused")
    private boolean isInCraftMatrix(@Nonnull int index) {
        return (index >= CRAFT_GRID_START && index <= CRAFT_GRID_END);
    }

    @SuppressWarnings("unused")
    private boolean notArmorOrBooster(ItemStack is) {
        return (!(is.getItem() instanceof ItemInfinityBooster)) && (!(is.getItem() instanceof ItemArmor));
    }

    @Override
    public ItemStack slotClick(int slot, int button, int flag, EntityPlayer player) {
        try {
            if (slot >= 0
                    && getSlot(slot) != null
                    && getSlot(slot).getStack() == RandomUtils.getWirelessTerm(player.inventory)) {
                return null;
            }
            return super.slotClick(slot, button, flag, player);
        } catch (IndexOutOfBoundsException e) {
            // When clicking super fast, for some reason, MC tried to access this inv size (max index + 1)
        }
        return null;
    }

    /**
     * Handles shift-clicking of items whose maxStackSize is 1
     */
    @Override
    protected boolean mergeItemStack(ItemStack stack, int start, int end, boolean backwards) {
        boolean flag1 = false;
        int k = (backwards ? end - 1 : start);
        Slot slot;
        ItemStack itemstack1;

        if (stack.isStackable()) {
            while (stack.stackSize > 0 && (!backwards && k < end || backwards && k >= start)) {
                slot = (Slot) inventorySlots.get(k);
                itemstack1 = slot.getStack();

                if (!slot.isItemValid(stack)) {
                    k += (backwards ? -1 : 1);
                    continue;
                }

                if (itemstack1 != null
                        && itemstack1.getItem() == stack.getItem()
                        && (!stack.getHasSubtypes() || stack.getItemDamage() == itemstack1.getItemDamage())
                        && ItemStack.areItemStackTagsEqual(stack, itemstack1)) {
                    int l = itemstack1.stackSize + stack.stackSize;

                    if (l <= stack.getMaxStackSize() && l <= slot.getSlotStackLimit()) {
                        stack.stackSize = 0;
                        itemstack1.stackSize = l;
                        boosterInventory.markDirty();
                        flag1 = true;
                    } else if (itemstack1.stackSize < stack.getMaxStackSize() && l < slot.getSlotStackLimit()) {
                        stack.stackSize -= stack.getMaxStackSize() - itemstack1.stackSize;
                        itemstack1.stackSize = stack.getMaxStackSize();
                        boosterInventory.markDirty();
                        flag1 = true;
                    }
                }

                k += (backwards ? -1 : 1);
            }
        }
        if (stack.stackSize > 0) {
            k = (backwards ? end - 1 : start);
            while (!backwards && k < end || backwards && k >= start) {
                slot = (Slot) inventorySlots.get(k);
                itemstack1 = slot.getStack();

                if (!slot.isItemValid(stack)) {
                    k += (backwards ? -1 : 1);
                    continue;
                }

                if (itemstack1 == null) {
                    int l = stack.stackSize;
                    if (l <= slot.getSlotStackLimit()) {
                        slot.putStack(stack.copy());
                        stack.stackSize = 0;
                        boosterInventory.markDirty();
                        flag1 = true;
                        break;
                    } else {
                        putStackInSlot(
                                k, new ItemStack(stack.getItem(), slot.getSlotStackLimit(), stack.getItemDamage()));
                        stack.stackSize -= slot.getSlotStackLimit();
                        boosterInventory.markDirty();
                        flag1 = true;
                    }
                }

                k += (backwards ? -1 : 1);
            }
        }

        return flag1;
    }
}
