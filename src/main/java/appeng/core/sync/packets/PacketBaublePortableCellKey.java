package appeng.core.sync.packets;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import baubles.api.BaubleType;
import baubles.api.cap.IBaublesItemHandler;

import appeng.capabilities.Capabilities;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.INetworkInfo;
import appeng.items.tools.powered.ToolPortableCell;
import appeng.util.Platform;


/**
 * Created by Thiakil on 7/10/2017.
 */
public class PacketBaublePortableCellKey extends AppEngPacket
{

	public PacketBaublePortableCellKey( final ByteBuf stream ){
		//we store nothing
	}

	public PacketBaublePortableCellKey(){
		final ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( INetworkInfo manager, AppEngPacket packet, EntityPlayer player )
	{
		if ( Capabilities.CAPABILITY_BAUBLES != null ) {
			// Someone pressed our terminalKey. We send a message
			IBaublesItemHandler handler = player.getCapability( Capabilities.CAPABILITY_BAUBLES, null );
			if (handler != null){
				ItemStack term = ItemStack.EMPTY;
				for (int slot : BaubleType.BELT.getValidSlots()){
					ItemStack stack = handler.getStackInSlot( slot );
					if (!stack.isEmpty() && stack.getItem() instanceof ToolPortableCell ){
						term = stack;
						break;
					}
				}
				if (!term.isEmpty()){
					Platform.openGUI( player, GuiBridge.GUI_PORTABLE_CELL, term );
				}
			}
		}
	}
}
