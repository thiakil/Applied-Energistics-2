package appeng.core.sync.packets;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;

import appeng.api.AEApi;
import appeng.capabilities.Capabilities;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.INetworkInfo;
import appeng.items.tools.powered.ToolPortableCell;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.util.Platform;


/**
 * Created by Thiakil on 7/10/2017.
 */
public class PacketBaubleKey extends AppEngPacket
{
	private final KeyType type;

	public PacketBaubleKey( final ByteBuf stream ){
		byte ordinal = stream.readByte();
		if (ordinal > -1 && ordinal < KeyType.values().length){
			this.type = KeyType.values()[ordinal];
		} else {
			this.type = null;
		}
	}

	public PacketBaubleKey(KeyType key){
		final ByteBuf data = Unpooled.buffer();
		this.type = key;

		data.writeInt( this.getPacketID() );
		data.writeByte( key.ordinal() );
		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( INetworkInfo manager, AppEngPacket packet, EntityPlayer player )
	{
		if ( Capabilities.CAPABILITY_BAUBLES != null && this.type != null ) {
			IBaublesItemHandler handler = player.getCapability( Capabilities.CAPABILITY_BAUBLES, null );
			if (handler != null){
				switch( this.type ){
					case TERMINAL: {
						ItemStack term = ItemStack.EMPTY;
						for (int slot : BaubleType.HEAD.getValidSlots()){
							ItemStack stack = handler.getStackInSlot( slot );
							if (!stack.isEmpty() && stack.getItem() instanceof ToolWirelessTerminal ){
								term = stack;
								break;
							}
						}
						if (!term.isEmpty()){
							AEApi.instance().registries().wireless().openWirelessTerminalGui( term, player.world, player );
						}
						break;
					}
					case PORTABLE_CELL: {
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
						break;
					}
				}

			}
		}
	}

	public enum KeyType {
		TERMINAL,
		PORTABLE_CELL,
		TOOLBELT,
		;
	}
}
