package net.p455w0rd.wirelesscraftingterminal.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTermHandler;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiCraftAmount;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiCraftConfirm;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiCraftingStatus;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiMagnet;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftAmount;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftConfirm;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerMagnet;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.helpers.WirelessTerminalGuiObject;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftingStatus;
import cpw.mods.fml.common.network.IGuiHandler;

public class WCTGuiHandler implements IGuiHandler {

    public void registerRenderers() {}

    @Override
    public Object getServerGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z) {
        final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries().wireless()
                .getWirelessTerminalHandler(RandomUtils.getWirelessTerm(player.inventory));
        if (wh != null) {
            final WirelessTerminalGuiObject obj = new WirelessTerminalGuiObject(
                    wh,
                    RandomUtils.getWirelessTerm(player.inventory),
                    player,
                    world,
                    x,
                    y,
                    z);
            if (obj != null) {
                final IPortableCell terminal = obj;

                if (guiId == Reference.GUI_WCT) {
                    return updateGui(
                            new ContainerWirelessCraftingTerminal(player, player.inventory),
                            world,
                            x,
                            y,
                            z,
                            ForgeDirection.UNKNOWN,
                            obj);
                }

                if (guiId == Reference.GUI_CRAFTING_STATUS) {
                    return updateGui(
                            new ContainerCraftingStatus(player.inventory, terminal),
                            world,
                            x,
                            y,
                            z,
                            ForgeDirection.UNKNOWN,
                            obj);
                }

                if (guiId == Reference.GUI_CRAFT_AMOUNT) {
                    return updateGui(
                            new ContainerCraftAmount(player, terminal),
                            world,
                            x,
                            y,
                            z,
                            ForgeDirection.UNKNOWN,
                            obj);
                }

                if (guiId == Reference.GUI_CRAFT_CONFIRM) {
                    return updateGui(
                            new ContainerCraftConfirm(player, terminal),
                            world,
                            x,
                            y,
                            z,
                            ForgeDirection.UNKNOWN,
                            obj);
                }
            }
        }
        if (guiId == Reference.GUI_MAGNET) {
            return new ContainerMagnet(player, player.inventory);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z) {
        final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries().wireless()
                .getWirelessTerminalHandler(RandomUtils.getWirelessTerm(player.inventory));
        if (wh != null) {
            final WirelessTerminalGuiObject obj = new WirelessTerminalGuiObject(
                    wh,
                    RandomUtils.getWirelessTerm(player.inventory),
                    player,
                    world,
                    x,
                    y,
                    z);
            if (obj != null) {
                final IPortableCell terminal = obj;

                if (guiId == Reference.GUI_WCT) {
                    return new GuiWirelessCraftingTerminal(
                            new ContainerWirelessCraftingTerminal(player, player.inventory));
                }

                if (guiId == Reference.GUI_CRAFTING_STATUS) {
                    return new GuiCraftingStatus(player.inventory, terminal);
                }

                if (guiId == Reference.GUI_CRAFT_AMOUNT) {
                    return new GuiCraftAmount(player.inventory, terminal);
                }

                if (guiId == Reference.GUI_CRAFT_CONFIRM) {
                    return new GuiCraftConfirm(player.inventory, terminal);
                }
            }
        }
        if (guiId == Reference.GUI_MAGNET) {
            return new GuiMagnet(new ContainerMagnet(player, player.inventory));
        }

        return null;
    }

    public static void launchGui(final int ID, final EntityPlayer player, final World world, final int x, final int y,
            final int z) {
        player.openGui(WirelessCraftingTerminal.INSTANCE, ID, world, x, y, z);
    }

    private Object updateGui(final Object newContainer, final World w, final int x, final int y, final int z,
            final ForgeDirection side, final Object myItem) {
        if (newContainer instanceof AEBaseContainer) {
            final AEBaseContainer bc = (AEBaseContainer) newContainer;
            bc.setOpenContext(new ContainerOpenContext(myItem));
            bc.getOpenContext().setWorld(w);
            bc.getOpenContext().setX(x);
            bc.getOpenContext().setY(y);
            bc.getOpenContext().setZ(z);
            bc.getOpenContext().setSide(side);
        }

        return newContainer;
    }
}
