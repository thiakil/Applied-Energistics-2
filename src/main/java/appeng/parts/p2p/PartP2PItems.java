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
import javax.annotation.Nullable;

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
import appeng.me.cache.helpers.TunnelCollection;
import appeng.util.Platform;
import appeng.util.inv.NullItemHandler;
import appeng.util.inv.WrapperChainedItemHandler;


// TODO: BC Integration
//@Interface( iface = "buildcraft.api.transport.IPipeConnection", iname = IntegrationType.BuildCraftTransport )
public class PartP2PItems extends PartP2PTunnel<PartP2PItems>// implements /* IPipeConnection, */IItemHandler//, IGridTickable
{

	private static final P2PModels MODELS = new P2PModels( "part/p2p/p2p_tunnel_items" );

	@PartModels
	public static List<IPartModel> getModels()
	{
		return MODELS.getModels();
	}

	private final LinkedList<IItemHandler> which = new LinkedList<IItemHandler>();
	private int oldSize = 0;
	//private boolean requested;

	private static IItemHandler NULL_ITEM_HANDLER = new NullItemHandler();

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

	@Nullable
	private IItemHandler getDestination()
	{
		//this.requested = true;

		/*if( this.cachedInv != null )
		{
			return this.cachedInv;
		}*/

		final List<IItemHandler> outs = new LinkedList<>();
		final TunnelCollection<PartP2PItems> itemTunnels;

		try
		{
			itemTunnels = this.getOutputs();
		}
		catch( final GridAccessException e )
		{
			return null;
		}

		for( final PartP2PItems t : itemTunnels )
		{
			final IItemHandler inv = t.getOutputInv();
			if( inv != null )
			{
				if( Platform.getRandomInt() % 2 == 0 )
				{
					outs.add( inv );
				}
				else
				{
					outs.add( 0, inv );
				}
			}
		}

		return new WrapperChainedItemHandler( outs );
	}

	private IItemHandler getOutputInv()
	{
		IItemHandler output = null;

		if( this.getProxy().isActive() )
		{
			final TileEntity te = this.getTile().getWorld().getTileEntity( this.getTile().getPos().offset( this.getSide().getFacing() ) );

			if( this.which.contains( this ) || te == null )
			{
				return null;
			}

			//this.which.add( this );

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

			//this.which.pop();
		}

		return output;
	}

	/*@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		return new TickingRequest( TickRates.ItemTunnel.getMin(), TickRates.ItemTunnel.getMax(), false, false );
	}

	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int ticksSinceLastCall )
	{
		final boolean wasReq = this.requested;

//		if( this.requested && this.cachedInv != null )
//		{
//			( (WrapperChainedInventory) this.cachedInv ).cycleOrder();
//		}

		this.requested = false;
		return wasReq ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
	}*/

	private void checkNumSlots()
	{
		final int olderSize = this.oldSize;
		IItemHandler out = this.getDestination();
		this.oldSize = out != null ? out.getSlots() : 0;
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

	/*@Override
	public int getSlots()
	{
		IItemHandler out = this.getDestination();
		return out != null? out.getSlots() : 0;
	}

	@Override
	public ItemStack getStackInSlot( final int i )
	{
		IItemHandler out = this.getDestination();
		return out != null ? out.getStackInSlot( i ) : ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public ItemStack insertItem( int slot, @Nonnull ItemStack stack, boolean simulate )
	{
		IItemHandler out = this.getDestination();
		return out != null ? out.insertItem( slot, stack, simulate ) : stack;
	}

	@Nonnull
	@Override
	public ItemStack extractItem( int slot, int amount, boolean simulate )
	{
		IItemHandler out = this.getDestination();
		return out != null ? out.extractItem( slot, amount, simulate ) : ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit( int slot )
	{
		IItemHandler out = this.getDestination();
		return out != null ? out.getSlotLimit(slot) : 0;
	}*/

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
		return ( !this.isOutput() && capabilityClass == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ) || super.hasCapability( capabilityClass );
	}

	@Override
	public <T> T getCapability( Capability<T> capabilityClass )
	{
		if ( !this.isOutput() && capabilityClass == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
		{
			IItemHandler out = getDestination();
			if ( out != null )
				return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast( out );
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast( NULL_ITEM_HANDLER );//fallback incase it fails between has and get cap. (and avoid the op in has)
		}
		return super.getCapability( capabilityClass );
	}

}
