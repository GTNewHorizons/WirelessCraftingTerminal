package net.p455w0rd.wirelesscraftingterminal.api.networking.security;

import net.minecraft.entity.player.EntityPlayer;

import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.PlayerSource;

public class WCTPlayerSource extends PlayerSource {

    public final EntityPlayer player;
    public final WCTIActionHost via;

    public WCTPlayerSource(final EntityPlayer p, final WCTIActionHost v) {
        super(p, (IActionHost) v);
        this.player = p;
        this.via = v;
    }

    @Override
    public boolean isPlayer() {
        return true;
    }
}
