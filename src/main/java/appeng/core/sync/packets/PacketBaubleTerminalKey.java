package appeng.core.sync.packets;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;

import appeng.api.AEApi;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.items.tools.powered.ToolWirelessTerminal;


/**
 * Created by Thiakil on 7/10/2017.
 */
public class PacketBaubleTerminalKey extends AppEngPacket
{
	@CapabilityInject(IBaublesItemHandler.class)
	private static Capability<IBaublesItemHandler> CAPABILITY_BAUBLES = null;
	@CapabilityInject(IBauble.class)
	private static Capability<IBauble> CAPABILITY_ITEM_BAUBLE = null;

	public PacketBaubleTerminalKey( final ByteBuf stream ){
		//we store nothing
	}

	public PacketBaubleTerminalKey(){
		final ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( INetworkInfo manager, AppEngPacket packet, EntityPlayer player )
	{
		if (CAPABILITY_BAUBLES != null && CAPABILITY_ITEM_BAUBLE != null) {
			// Someone pressed our terminalKey. We send a message
			IBaublesItemHandler handler = player.getCapability( CAPABILITY_BAUBLES, null );
			if (handler != null){
				int slots = handler.getSlots();
				ItemStack term = ItemStack.EMPTY;
				for (int slot = 0; slot < slots; slot++){
					ItemStack stack = handler.getStackInSlot( slot );
					if (!stack.isEmpty() && stack.getItem() instanceof ToolWirelessTerminal ){
						term = stack;
						break;
					}
				}
				if (!term.isEmpty()){
					AEApi.instance().registries().wireless().openWirelessTerminalGui( term, player.world, player );
				}
			}
		}
	}
}
