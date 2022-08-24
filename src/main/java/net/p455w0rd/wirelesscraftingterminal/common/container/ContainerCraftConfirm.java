package net.p455w0rd.wirelesscraftingterminal.common.container;

import appeng.api.implementations.guiobjects.IPortableCell;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.p455w0rd.wirelesscraftingterminal.common.WCTGuiHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketSwitchGuis;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class ContainerCraftConfirm extends appeng.container.implementations.ContainerCraftConfirm {
    private final EntityPlayer player;

    public ContainerCraftConfirm(EntityPlayer player, IPortableCell terminal) {
        super(player.inventory, terminal);
        this.player = player;
    }

    @Override
    public void switchToOriginalGUI() {
        int x = (int) player.posX;
        int y = (int) player.posY;
        int z = (int) player.posZ;
        WCTGuiHandler.launchGui(Reference.GUI_WCT, player, player.worldObj, x, y, z);
    }
}
