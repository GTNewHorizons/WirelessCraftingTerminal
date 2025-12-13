package net.p455w0rd.wirelesscraftingterminal.common.container;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTermHandler;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTerminalItem;
import net.p455w0rd.wirelesscraftingterminal.client.gui.WCTPrimaryGui;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotArmor;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotBooster;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotMagnet;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotTrash;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryBooster;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryCrafting;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryMagnet;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryTrash;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryViewCell;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.helpers.WTCGuiObject;
import net.p455w0rd.wirelesscraftingterminal.items.ItemInfinityBooster;
import net.p455w0rd.wirelesscraftingterminal.items.ItemMagnet;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.parts.ICraftingTerminal;
import appeng.container.ContainerNull;
import appeng.container.ContainerOpenContext;
import appeng.container.PrimaryGui;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotCraftingTerm;
import appeng.container.slot.SlotDisabled;
import appeng.container.slot.SlotInaccessible;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.IPrimaryGuiIconProvider;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;

public class ContainerWirelessCraftingTerminal extends ContainerMEMonitorable
        implements IAEAppEngInventory, IContainerCraftingPacket, IViewCellStorage {

    private final ItemStack containerstack;
    public final WCTInventoryCrafting craftingGrid;
    public final WCTInventoryViewCell viewCellInventory;
    public final WCTInventoryBooster boosterInventory;
    public final WCTInventoryMagnet magnetInventory;
    public final WCTInventoryTrash trashInventory;
    public final InventoryPlayer inventoryPlayer;
    public ItemStack[] craftMatrixInventory;
    private final EntityPlayer player;
    public static final int HOTBAR_START = 2, HOTBAR_END = HOTBAR_START + 8, INV_START = HOTBAR_END + 1,
            INV_END = INV_START + 26, ARMOR_START = INV_END + 1, ARMOR_END = ARMOR_START + 3,
            CRAFT_GRID_START = ARMOR_END + 1, CRAFT_GRID_END = CRAFT_GRID_START + 8, CRAFT_RESULT = CRAFT_GRID_END + 1,
            VIEW_CELL_START = CRAFT_RESULT + 1, VIEW_CELL_END = VIEW_CELL_START + 4, BOOSTER_INDEX = 1,
            MAGNET_INDEX = VIEW_CELL_END + 1;
    public static int CRAFTING_SLOT_X_POS = 80, CRAFTING_SLOT_Y_POS = 83;
    private SlotBooster boosterSlot;
    private final SlotMagnet magnetSlot;
    private final Slot[] hotbarSlot;
    private final Slot[] inventorySlot;
    private final SlotArmor[] armorSlot;
    private final SlotCraftingMatrix[] craftMatrixSlot;
    private final SlotCraftingTerm craftingSlot;
    public SlotTrash trashSlot;

    private final ICraftingTerminal ct;

    private final AppEngInternalInventory output = new AppEngInternalInventory(this, 1);
    private final IWirelessCraftingTerminalItem thisItem;
    private IRecipe currentRecipe;

    /**
     * Constructor for our custom container
     *
     * @author p455w0rd
     */
    public ContainerWirelessCraftingTerminal(InventoryPlayer inventoryPlayer, ICraftingTerminal host) {
        super(inventoryPlayer, host, false);
        final EntityPlayer player = inventoryPlayer.player;
        this.ct = host;

        this.setCustomName("WCTContainer");

        this.boosterInventory = new WCTInventoryBooster(RandomUtils.getWirelessTerm(inventoryPlayer));
        this.magnetInventory = new WCTInventoryMagnet(RandomUtils.getWirelessTerm(inventoryPlayer));
        this.trashInventory = new WCTInventoryTrash(RandomUtils.getWirelessTerm(inventoryPlayer));
        this.containerstack = RandomUtils.getWirelessTerm(inventoryPlayer);
        this.thisItem = (IWirelessCraftingTerminalItem) this.containerstack.getItem();
        this.craftingGrid = new WCTInventoryCrafting(this, 3, 3, containerstack);
        this.viewCellInventory = new WCTInventoryViewCell(RandomUtils.getWirelessTerm(inventoryPlayer));
        this.inventoryPlayer = inventoryPlayer;
        this.player = player;
        craftMatrixInventory = new ItemStack[9];
        hotbarSlot = new Slot[9];
        inventorySlot = new Slot[27];
        armorSlot = new SlotArmor[4];
        craftMatrixSlot = new SlotCraftingMatrix[9];

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
            armorSlot[i] = new SlotArmor(
                    this.player,
                    this.inventoryPlayer,
                    this.inventoryPlayer.getSizeInventory() - 1 - i,
                    (int) 8.5,
                    (i * 18) - 76,
                    i);
            this.addSlotToContainer(armorSlot[i]);
        }
        k = 0;
        // Add crafting grid slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                craftMatrixSlot[k] = new SlotCraftingMatrix(
                        this,
                        this.craftingGrid,
                        j + i * 3,
                        80 + j * 18,
                        (i * 18) - 76);
                this.addSlotToContainer(craftMatrixSlot[k]);
                k++;
            }
        }

        craftingSlot = new SlotCraftingTerm(
                this.getPlayerInv().player,
                this.getActionSource(),
                this.getPowerSource(),
                this.ct,
                this.craftingGrid,
                this.craftingGrid,
                this.output,
                174,
                -58,
                this);
        // Add crafting result slot
        this.addSlotToContainer(craftingSlot);

        magnetSlot = new SlotMagnet(this.magnetInventory, 152, -20);
        this.addSlotToContainer(magnetSlot);

        trashSlot = new SlotTrash(this.trashInventory, 80, -20, player);
        trashSlot.setContainer(this);
        this.addSlotToContainer(trashSlot);

        updateCraftingMatrix();

        this.onCraftMatrixChanged(this.craftingGrid);

        for (var slot : this.getCellViewSlots()) {
            slot.xDisplayPosition += 1;
        }

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
    public void onChangeInventory(final IInventory inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {
        // <3
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

    public static WTCGuiObject getGuiObject(final ItemStack it, final EntityPlayer player, final World w, final int x,
            final int y, final int z, final int slotIndex) {
        if (it != null) {
            final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries()
                    .wireless().getWirelessTerminalHandler(it);
            if (wh != null) {
                return new WTCGuiObject(wh, it, player, w, x, y, z, slotIndex);
            }
        }

        return null;
    }

    public boolean isPowered() {
        double pwr = this.thisItem.getAECurrentPower(this.containerstack);
        return (pwr > 0.0);
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

        return super.transferStackInSlot(p, idx);
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
            if (slot >= 0 && getSlot(slot) != null
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

                if (itemstack1 != null && itemstack1.getItem() == stack.getItem()
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
                                k,
                                new ItemStack(stack.getItem(), slot.getSlotStackLimit(), stack.getItemDamage()));
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

    @Override
    public PrimaryGui getPrimaryGui() {
        ContainerOpenContext context = getOpenContext();
        return new WCTPrimaryGui(
                Reference.GUI_WCT,
                ((IPrimaryGuiIconProvider) ct).getPrimaryGuiIcon(),
                context.getTile(),
                context.getSide());
    }
}
