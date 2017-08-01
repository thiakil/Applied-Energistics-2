package appeng.core.sync.packets;

import appeng.core.AEConfig;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

/**
 * Packet to sync settings from server to client that the client should not be able to override.
 */
public class PacketConfigSync extends AppEngPacket {

	private boolean patternTermRequiresItems;

	public PacketConfigSync( final ByteBuf stream ) throws IOException{
		this.patternTermRequiresItems = stream.readBoolean();
	}

	public PacketConfigSync(boolean patternTermRequiresItems){
		final ByteBuf data = Unpooled.buffer();
		data.writeBoolean(patternTermRequiresItems);
		this.configureWrite(data);
	}

	@Override
	public void clientPacketData(INetworkInfo network, AppEngPacket packet, EntityPlayer player) {
		AEConfig.instance().syncPatternTermRequiresItems(this.patternTermRequiresItems);
	}
}
