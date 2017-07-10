/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.tile.storage;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.core.AELog;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.storage.DriveWatcher;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;


public class TileDrive extends AENetworkInvTile implements IChestOrDrive, IPriorityHost
{

	private static final int BIT_POWER_MASK = 0x80000000;
	private static final int BIT_BLINK_MASK = 0x24924924;
	private static final int BIT_STATE_MASK = 0xDB6DB6DB;

	private final int[] sides = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	private final AppEngInternalInventory inv = new AppEngInternalInventory( this, 10 );
	private final ICellHandler[] handlersBySlot = new ICellHandler[10];
	private final DriveWatcher<IAEItemStack>[] invBySlot = new DriveWatcher[10];
	private final BaseActionSource mySrc;
	private boolean isCached = false;
	private List<MEInventoryHandler> items = new LinkedList<MEInventoryHandler>();
	private List<MEInventoryHandler> fluids = new LinkedList<MEInventoryHandler>();
	private int priority = 0;
	private boolean wasActive = false;

	private static byte NUMBER_CELLS = 10;
	private static byte NUMBER_COLS = 2;
	private static byte CELL_WIDTH = 5;
	private static byte CELL_HEIGHT = 2;
	private static byte CELL_GAP_X = 2;
	private static byte CELL_GAP_Y = 1;

	//x1, y1
	private static byte[][] DRIVE_BOXES = new byte[NUMBER_CELLS][2];
	static {
		for ( byte col = 0; col < NUMBER_COLS; col++)
		{
			for ( byte row = 0; row < NUMBER_CELLS/NUMBER_COLS; row++ )
			{
				byte[] cell = DRIVE_BOXES[row*2+col];
				cell[0] = (byte)(( CELL_GAP_X + CELL_WIDTH ) * col + CELL_GAP_X);
				cell[1] = (byte)(( CELL_GAP_Y + CELL_HEIGHT) * row + CELL_GAP_Y);
			}
		}
	}

	/**
	 * The state of all cells inside a drive as bitset, using the following format.
	 *
	 * Bit 31: power state. 0 = off, 1 = on.
	 * Bit 30: undefined
	 * Bit 29-0: 3 bits as state of each cell with the cell in slot 0 located in the 3 least significant bits.
	 *
	 * Cell states:
	 * Bit 2: blink. 0 = off, 1 = on.
	 * Bit 1-0: cell status
	 *
	 *
	 */
	private int state = 0;

	public TileDrive()
	{
		this.mySrc = new MachineSource( this );
		this.getProxy().setFlags( GridFlags.REQUIRE_CHANNEL );
	}

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TileDrive( final ByteBuf data )
	{
		int newState = 0;

		if( this.getProxy().isActive() )
		{
			newState |= BIT_POWER_MASK;
		}

		for( int x = 0; x < this.getCellCount(); x++ )
		{
			newState |= ( this.getCellStatus( x ) << ( 3 * x ) );
		}

		data.writeInt( newState );
	}

	@Override
	public int getCellCount()
	{
		return 10;
	}

	@Override
	public int getCellStatus( final int slot )
	{
		if( Platform.isClient() )
		{
			return ( this.state >> ( slot * 3 ) ) & 3;
		}

		final ItemStack cell = this.inv.getStackInSlot( 2 );
		final ICellHandler ch = this.handlersBySlot[slot];

		final MEInventoryHandler handler = this.invBySlot[slot];
		if( handler == null )
		{
			return 0;
		}

		if( handler.getChannel() == StorageChannel.ITEMS )
		{
			if( ch != null )
			{
				return ch.getStatusForCell( cell, handler.getInternal() );
			}
		}

		if( handler.getChannel() == StorageChannel.FLUIDS )
		{
			if( ch != null )
			{
				return ch.getStatusForCell( cell, handler.getInternal() );
			}
		}

		return 0;
	}

	@Override
	public boolean isPowered()
	{
		if( Platform.isClient() )
		{
			return ( this.state & BIT_POWER_MASK ) == BIT_POWER_MASK;
		}

		return this.getProxy().isActive();
	}

	@Override
	public boolean isCellBlinking( final int slot )
	{
		return ( ( this.state >> ( slot * 3 + 2 ) ) & 0x01 ) == 0x01;
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TileDrive( final ByteBuf data )
	{
		final int oldState = this.state;
		this.state = data.readInt();
		return ( this.state & BIT_STATE_MASK ) != ( oldState & BIT_STATE_MASK );
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileDrive( final NBTTagCompound data )
	{
		this.isCached = false;
		this.priority = data.getInteger( "priority" );
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileDrive( final NBTTagCompound data )
	{
		data.setInteger( "priority", this.priority );
	}

	@MENetworkEventSubscribe
	public void powerRender( final MENetworkPowerStatusChange c )
	{
		this.recalculateDisplay();
	}

	private void recalculateDisplay()
	{
		final boolean currentActive = this.getProxy().isActive();
		int newState = 0;

		if( currentActive )
		{
			newState |= BIT_POWER_MASK;
		}

		if( this.wasActive != currentActive )
		{
			this.wasActive = currentActive;
			try
			{
				this.getProxy().getGrid().postEvent( new MENetworkCellArrayUpdate() );
			}
			catch( final GridAccessException e )
			{
				// :P
			}
		}

		for( int x = 0; x < this.getCellCount(); x++ )
		{
			newState |= ( this.getCellStatus( x ) << ( 3 * x ) );
		}

		if( newState != this.state )
		{
			this.state = newState;
			this.markForUpdate();
		}
	}

	@MENetworkEventSubscribe
	public void channelRender( final MENetworkChannelsChanged c )
	{
		this.recalculateDisplay();
	}

	@Override
	public AECableType getCableConnectionType( final AEPartLocation dir )
	{
		return AECableType.SMART;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.inv;
	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{
		return !itemstack.isEmpty() && AEApi.instance().registries().cell().isCellHandled(itemstack);
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{
		if( this.isCached )
		{
			this.isCached = false; // recalculate the storage cell.
			this.updateState();
		}

		try
		{
			this.getProxy().getGrid().postEvent( new MENetworkCellArrayUpdate() );

			final IStorageGrid gs = this.getProxy().getStorage();
			Platform.postChanges( gs, removed, added, this.mySrc );
		}
		catch( final GridAccessException ignored )
		{
		}

		this.markForUpdate();
	}

	@Override
	public int[] getAccessibleSlotsBySide( final EnumFacing side )
	{
		return this.sides;
	}

	private void updateState()
	{
		if( !this.isCached )
		{
			this.items = new LinkedList();
			this.fluids = new LinkedList();

			double power = 2.0;

			for( int x = 0; x < this.inv.getSizeInventory(); x++ )
			{
				final ItemStack is = this.inv.getStackInSlot( x );
				this.invBySlot[x] = null;
				this.handlersBySlot[x] = null;

				if (!is.isEmpty()) {
					this.handlersBySlot[x] = AEApi.instance().registries().cell().getHandler(is);

					if (this.handlersBySlot[x] != null) {
						IMEInventoryHandler cell = this.handlersBySlot[x].getCellInventory(is, this, StorageChannel.ITEMS);

						if (cell != null) {
							power += this.handlersBySlot[x].cellIdleDrain(is, cell);

							final DriveWatcher<IAEItemStack> ih = new DriveWatcher(cell, is, this.handlersBySlot[x], this);
							ih.setPriority(this.priority);
							this.invBySlot[x] = ih;
							this.items.add(ih);
						} else {
							cell = this.handlersBySlot[x].getCellInventory(is, this, StorageChannel.FLUIDS);

							if (cell != null) {
								power += this.handlersBySlot[x].cellIdleDrain(is, cell);

								final DriveWatcher<IAEItemStack> ih = new DriveWatcher(cell, is, this.handlersBySlot[x], this);
								ih.setPriority(this.priority);
								this.invBySlot[x] = ih;
								this.fluids.add(ih);
							}
						}
					}
				}
			}

			this.getProxy().setIdlePowerUsage( power );

			this.isCached = true;
		}
	}

	@Override
	public void onReady()
	{
		super.onReady();
		this.updateState();
	}

	@Override
	public List<IMEInventoryHandler> getCellArray( final StorageChannel channel )
	{
		if( this.getProxy().isActive() )
		{
			this.updateState();
			return (List) ( channel == StorageChannel.ITEMS ? this.items : this.fluids );
		}
		return new ArrayList();
	}

	@Override
	public int getPriority()
	{
		return this.priority;
	}

	@Override
	public void setPriority( final int newValue )
	{
		this.priority = newValue;
		this.markDirty();

		this.isCached = false; // recalculate the storage cell.
		this.updateState();

		try
		{
			this.getProxy().getGrid().postEvent( new MENetworkCellArrayUpdate() );
		}
		catch( final GridAccessException e )
		{
			// :P
		}
	}

	@Override
	public void blinkCell( final int slot )
	{
		this.state |= 1 << ( slot * 3 + 2 );

		this.recalculateDisplay();
	}

	@Override
	public void saveChanges( final IMEInventory cellInventory )
	{
		this.world.markChunkDirty( this.pos, this );
	}

	/**
	 * Calculates a slot position based on the XY position on the from face.
	 * @param x position on the X axis. 0.0 <= x <= 1.0
	 * @param y position on the Y axis. 0.0 <= y <= 1.0
	 * @return slot number if found, -1 otherwise
	 */
	public int findSlotByXY( float x, float y )
	{
		int xPos = (int)(( 1.0 - x )  * 16);
		int yPos = (int)(( 1.0 - y ) * 16); // coords are from bottom. Maths used above requires from top.
		//AELog.info( "x="+xPos+", y="+yPos );
		int slot;
		for ( slot = 0; slot < DRIVE_BOXES.length; slot++ )
		{
			byte[] box = DRIVE_BOXES[slot];
			if (box[1] > yPos ){//not gonna find more matches
				slot = DRIVE_BOXES.length;
				break;
			} else if ( xPos >= box[0] && xPos < box[0]+CELL_WIDTH && yPos >= box[1] && yPos < box[1]+CELL_HEIGHT){
				//found it!
				break;
			}
		}

		return slot < DRIVE_BOXES.length ? slot : -1;
	}

	public boolean tryAutoInsertDrive( float x, float y, ItemStack cell, EntityPlayer p, EnumHand hand )
	{
		int slot = findSlotByXY( x, y );
		if ( slot > -1 )
		{
			//AELog.info( "found slot "+slot );
			if ( !getStackInSlot( slot ).isEmpty() )
				return false;
			setInventorySlotContents( slot, cell );
			p.setHeldItem( hand, ItemStack.EMPTY );
			return true;
		}

		return false;
	}

	public boolean tryAutoExtractDrive( float x, float y, EntityPlayer p, EnumHand hand )
	{
		int slot = findSlotByXY( x, y );
		if ( slot > -1 )
		{
			//AELog.info( "found slot "+slot );
			ItemStack cell = getStackInSlot( slot );
			if ( cell.isEmpty() )
				return false;
			setInventorySlotContents( slot, ItemStack.EMPTY );
			p.setHeldItem( hand, cell );
			return true;
		}

		return false;
	}
}
