/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.parts.p2p;


import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.util.inv.NullItemHandler;


// TODO: BC Integration
//@Interface( iface = "buildcraft.api.transport.IPipeConnection", iname = IntegrationType.BuildCraftTransport )
public class PartP2PItems extends PartP2PTunnel<PartP2PItems>
{

	private static final P2PModels MODELS = new P2PModels( "part/p2p/p2p_tunnel_items" );

	@PartModels
	public static List<IPartModel> getModels()
	{
		return MODELS.getModels();
	}

	private final LinkedList<PartP2PItems> which = new LinkedList<>();
	private int oldSize = 0;
	//private boolean requested;

	private static IItemHandler NULL_ITEM_HANDLER = new NullItemHandler();

	private IItemHandler outputHandler = new OutputHandler();
	private IItemHandler inputHandler = new InputHandler();

	public PartP2PItems( final ItemStack is )
	{
		super( is );
	}

	@Override
	public void onNeighborChanged()
	{
		final PartP2PItems input = this.getInput();
		if( input != null && this.isOutput() )
		{
			input.onTunnelNetworkChange();
		}
	}

	private IItemHandler getOutputInv()
	{
		IItemHandler output = null;

		if( this.getProxy().isActive() )
		{
			if( this.which.contains( this ) )
			{
				return null;
			}

			final TileEntity te = this.getTile().getWorld().getTileEntity( this.getTile().getPos().offset( this.getSide().getFacing() ) );

			if (te == null){
				return null;
			}

			this.which.push( this );

			IItemHandler handler = te.getCapability( CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.getSide().getFacing().getOpposite() );
			if ( handler != null )
			{
				output = handler;
			}
			/*else if( te instanceof TileEntityChest )
			{
				output = Platform.GetChestInv( te );
			}*/
			else if( te instanceof ISidedInventory )
			{
				output = new SidedInvWrapper( (ISidedInventory) te, this.getSide().getFacing().getOpposite() );
			}
			else if( te instanceof IInventory )
			{
				output = new InvWrapper((IInventory) te);
			}

			this.which.pop();
		}

		return output;
	}

	private void checkNumSlots()
	{
		final int olderSize = this.oldSize;
		this.oldSize = this.inputHandler.getSlots();
		if( olderSize != this.oldSize )
		{
			this.getHost().notifyNeighbors();
		}
	}

	@MENetworkEventSubscribe
	public void changeStateA( final MENetworkBootingStatusChange bs )
	{
		if( !this.isOutput() )
		{
			checkNumSlots();
		}
	}

	@MENetworkEventSubscribe
	public void changeStateB( final MENetworkChannelsChanged bs )
	{
		if( !this.isOutput() )
		{
			checkNumSlots();
		}
	}

	@MENetworkEventSubscribe
	public void changeStateC( final MENetworkPowerStatusChange bs )
	{
		if( !this.isOutput() )
		{
			checkNumSlots();
		}
	}

	@Override
	public void onTunnelNetworkChange()
	{
		if( !this.isOutput() )
		{
			checkNumSlots();
		}
		else
		{
			final PartP2PItems input = this.getInput();
			if( input != null )
			{
				input.getHost().notifyNeighbors();
			}
		}
	}

	public float getPowerDrainPerTick()
	{
		return 2.0f;
	}

	@Override
	public IPartModel getStaticModels()
	{
		return MODELS.getModel( isPowered(), isActive() );
	}

	@Override
	public boolean hasCapability( Capability<?> capabilityClass )
	{
		return capabilityClass == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability( capabilityClass );
	}

	@Override
	public <T> T getCapability( Capability<T> capabilityClass )
	{
		if ( capabilityClass == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
		{
			if (!this.isActive()){
				return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast( NULL_ITEM_HANDLER );
			}
			if ( this.isOutput() ) {
				return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast( outputHandler );
			} else {
				return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast( inputHandler );
			}
		}
		return super.getCapability( capabilityClass );
	}

	/**
	 *  Handler for the INPUT side of a tunnel. Insertion only. getStack is allowed as it shouldn't ever be modified
	 */
	private class InputHandler implements IItemHandler{

		@Override
		public int getSlots()
		{
			if (PartP2PItems.this.isOutput())
				return 0;
			int totalSlots = 0;
			try
			{
				for( PartP2PItems p : PartP2PItems.this.getOutputs() )
				{
					IItemHandler h = p.getOutputInv();
					if (h != null) {
						totalSlots += h.getSlots();
					}
				}
			} catch (GridAccessException ignored){}
			return totalSlots;
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot( int slot )
		{
			if (PartP2PItems.this.isOutput())
				return ItemStack.EMPTY;//because we've been cached somewhere
			int totalSlots = 0;
			try
			{
				for( PartP2PItems p : PartP2PItems.this.getOutputs() )
				{
					IItemHandler h = p.getOutputInv();
					if (h != null) {
						if (slot - totalSlots < h.getSlots()){
							return h.getStackInSlot( slot - totalSlots );
						} else {
							totalSlots += h.getSlots();
						}
					}
				}
			} catch (GridAccessException ignored){}
			return ItemStack.EMPTY;
		}

		@Nonnull
		@Override
		public ItemStack insertItem( int slot, @Nonnull ItemStack input, boolean simulate )
		{
			if (PartP2PItems.this.isOutput())
				return input;//because we've been cached somewhere
			int totalSlots = 0;
			try
			{
				for( PartP2PItems p : PartP2PItems.this.getOutputs() )
				{
					IItemHandler h = p.getOutputInv();
					if (h != null) {
						if (slot - totalSlots < h.getSlots()){
							return h.insertItem(slot - totalSlots, input, simulate );
						} else {
							totalSlots += h.getSlots();
						}
					}
				}
			} catch (GridAccessException ignored){}
			return input;
		}

		@Nonnull
		@Override
		public ItemStack extractItem( int slot, int amount, boolean simulate )
		{
			return ItemStack.EMPTY;//NOPE!
		}

		@Override
		public int getSlotLimit( int slot )
		{
			if (PartP2PItems.this.isOutput())
				return 0;//because we've been cached somewhere
			int totalSlots = 0;
			try
			{
				for( PartP2PItems p : PartP2PItems.this.getOutputs() )
				{
					IItemHandler h = p.getOutputInv();
					if (h != null) {
						if (slot - totalSlots < h.getSlots()){
							return h.getSlotLimit( slot - totalSlots );
						} else {
							totalSlots += h.getSlots();
						}
					}
				}
			} catch (GridAccessException ignored){}
			return 0;
		}
	}

	/**
	 *  Handler for the OUTPUT side of a tunnel. Extraction only. getStack is allowed as it shouldn't ever be modified
	 */
	private class OutputHandler implements IItemHandler {

		@Override
		public int getSlots()
		{
			if (!PartP2PItems.this.isActive() || !PartP2PItems.this.isOutput())
				return 0;
			PartP2PItems in = PartP2PItems.this.getInput();
			if (in != null){
				IItemHandler h = in.getOutputInv();
				if (h != null){
					return h.getSlots();
				}
			}
			return 0;
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot( int slot )
		{
			if (!PartP2PItems.this.isActive() || !PartP2PItems.this.isOutput())
				return ItemStack.EMPTY;
			PartP2PItems in = PartP2PItems.this.getInput();
			if (in != null){
				IItemHandler h = in.getOutputInv();
				if (h != null){
					return h.getStackInSlot(slot);
				}
			}
			return ItemStack.EMPTY;
		}

		@Nonnull
		@Override
		public ItemStack insertItem( int slot, @Nonnull ItemStack stack, boolean simulate )
		{
			return stack;//NOPE!
		}

		@Nonnull
		@Override
		public ItemStack extractItem( int slot, int amount, boolean simulate )
		{
			if (!PartP2PItems.this.isActive() || !PartP2PItems.this.isOutput())
				return ItemStack.EMPTY;
			PartP2PItems in = PartP2PItems.this.getInput();
			if (in != null){
				IItemHandler h = in.getOutputInv();
				if (h != null){
					return h.extractItem(slot, amount, simulate);
				}
			}
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit( int slot )
		{
			if (!PartP2PItems.this.isActive() || !PartP2PItems.this.isOutput())
				return 0;
			PartP2PItems in = PartP2PItems.this.getInput();
			if (in != null){
				IItemHandler h = in.getOutputInv();
				if (h != null){
					return h.getSlotLimit(slot);
				}
			}
			return 0;
		}
	}

}
