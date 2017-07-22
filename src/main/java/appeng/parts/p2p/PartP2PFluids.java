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

package appeng.parts.p2p;


import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import appeng.api.parts.IPartModel;
import appeng.api.util.DimensionalCoord;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;


public class PartP2PFluids extends PartP2PTunnel<PartP2PFluids>
{

	private static final P2PModels MODELS = new P2PModels( "part/p2p/p2p_tunnel_fluids" );

	private static final ThreadLocal<Deque<PartP2PFluids>> DEPTH = new ThreadLocal<>();
	private static final FluidTankProperties[] INACTIVE_TANK = { new FluidTankProperties( null, 0, false, false ) };

	private int tmpUsed;

	private IFluidHandler inputHandler = new InputHandler();
	private IFluidHandler outputHandler = new OutputHandler();
	private final static IFluidHandler NULL_FLUID_HANDLER = new NullFluidHandler();

	public PartP2PFluids( final ItemStack is )
	{
		super( is );
	}

	@PartModels
	public static List<IPartModel> getModels()
	{
		return MODELS.getModels();
	}

	public float getPowerDrainPerTick()
	{
		return 2.0f;
	}

	@Override
	public void onTunnelNetworkChange()
	{
		if (this.isOutput())
		{
			final PartP2PFluids input = this.getInput();
			if( input != null )
			{
				input.getHost().notifyNeighbors();
			}
		} else {
			getHost().notifyNeighbors();
		}
	}

	@Override
	public void onNeighborChanged()
	{
		if( this.isOutput() )
		{
			final PartP2PFluids in = this.getInput();
			if( in != null )
			{
				in.onTunnelNetworkChange();
			}
		} else {
			try {
				for (PartP2PFluids p : this.getOutputs()){
					DimensionalCoord loc = p.getLocation();
					if (!loc.getWorld().isRemote) {
						MinecraftServer s = loc.getWorld().getMinecraftServer();
						if (s != null) {
							s.addScheduledTask( () -> {
								BlockPos pos = loc.getPos().offset( p.getSide().getFacing() );
								IBlockState state = loc.getWorld().getBlockState( pos );
								state.getBlock().onNeighborChange( loc.getWorld(), pos, loc.getPos() );
							} );
						}
					}
				}
			} catch (GridAccessException ignored){}
		}
	}

	@Override
	public boolean hasCapability( Capability<?> capabilityClass )
	{
		return capabilityClass == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability( capabilityClass );
	}

	@Override
	public <T> T getCapability( Capability<T> capabilityClass )
	{
		if( capabilityClass == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY )
		{
			if (!this.isActive()){
				return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast( NULL_FLUID_HANDLER );
			} else if (this.isOutput()){
				return (this.getInput() != null && this.getInput().getTarget() != null) ? CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast( outputHandler ) : CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast( NULL_FLUID_HANDLER );
			} else {
				return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast( inputHandler );
			}
		}

		return super.getCapability( capabilityClass );
	}

	@Override
	public IPartModel getStaticModels()
	{
		return MODELS.getModel( isPowered(), isActive() );
	}



	private List<PartP2PFluids> getOutputs( final Fluid input )
	{
		final List<PartP2PFluids> outs = new LinkedList<>();

		try
		{
			for( final PartP2PFluids l : this.getOutputs() )
			{
				final IFluidHandler handler = l.getTarget();

				if( handler != null )
				{
					outs.add( l );
				}
			}
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		return outs;
	}

	private IFluidHandler getTarget()
	{
		if( !this.getProxy().isActive() )
		{
			return null;
		}

		final EnumFacing opposite = this.getSide().getFacing().getOpposite();
		final TileEntity te = this.getTile().getWorld().getTileEntity( this.getTile().getPos().offset( this.getSide().getFacing() ) );

		if( te != null && te.hasCapability( CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, opposite ) )
		{
			return te.getCapability( CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, opposite );
		}

		return null;
	}

	private Deque<PartP2PFluids> getDepth()
	{
		Deque<PartP2PFluids> s = DEPTH.get();

		if( s == null )
		{
			DEPTH.set( s = new LinkedList<>() );
		}

		return s;
	}

	private class OutputHandler implements IFluidHandler {
		@Override
		public IFluidTankProperties[] getTankProperties()
		{
			if( PartP2PFluids.this.isOutput() )
			{
				final PartP2PFluids tun = PartP2PFluids.this.getInput();
				if( tun != null )
				{
					IFluidHandler inH = tun.getTarget();
					if (inH != null)
					{
						IFluidTankProperties[] t = inH.getTankProperties();
						if( t != null )
						{
							List<IFluidTankProperties> tanks = new ArrayList<>();
							for( IFluidTankProperties ft : t )
							{
								tanks.add( new FluidTankProxy( ft, false, true ) );
							}
							return tanks.toArray( new IFluidTankProperties[tanks.size()] );
						}
					}
				}
			}

			return INACTIVE_TANK;
		}

		@Override
		public int fill( FluidStack resource, boolean doFill )
		{
			return 0;
		}

		@Override
		public FluidStack drain( FluidStack resource, boolean doDrain )
		{
			if( PartP2PFluids.this.isOutput() )
			{
				final PartP2PFluids tun = PartP2PFluids.this.getInput();
				if( tun != null )
				{
					IFluidHandler inH = tun.getTarget();
					return inH != null ? inH.drain(resource,doDrain) : null;
				}
			}
			return null;
		}

		@Override
		public FluidStack drain( int maxDrain, boolean doDrain )
		{
			if( PartP2PFluids.this.isOutput() )
			{
				final PartP2PFluids tun = PartP2PFluids.this.getInput();
				if( tun != null )
				{
					IFluidHandler inH = tun.getTarget();
					return inH != null ? inH.drain(maxDrain, doDrain ) : null;
				}
			}
			return null;
		}

	}

	private class InputHandler implements IFluidHandler {
		@Override
		public IFluidTankProperties[] getTankProperties()
		{
			if( PartP2PFluids.this.isOutput() ){
				return INACTIVE_TANK;
			}
			List<IFluidTankProperties> tanks = new ArrayList<>();
			List<PartP2PFluids> outputs = PartP2PFluids.this.getOutputs(null);//returns all non null handler outputs
			for (PartP2PFluids p : outputs){
				IFluidTankProperties[] t = p.getTarget().getTankProperties();
				if (t != null){
					for ( IFluidTankProperties ft : t){
						tanks.add( new FluidTankProxy(ft, true, false) );
					}
				}
			}
			return tanks.toArray( new IFluidTankProperties[tanks.size()] );
		}

		@Override
		public int fill( FluidStack resource, boolean doFill )
		{
			final Deque<PartP2PFluids> stack = PartP2PFluids.this.getDepth();

			for( final PartP2PFluids t : stack )
			{
				if( t == PartP2PFluids.this )
				{
					return 0;
				}
			}

			stack.push( PartP2PFluids.this );

			final List<PartP2PFluids> list = PartP2PFluids.this.getOutputs( resource.getFluid() );
			int requestTotal = 0;

			Iterator<PartP2PFluids> i = list.iterator();

			while( i.hasNext() )
			{
				final PartP2PFluids l = i.next();
				final IFluidHandler tank = l.getTarget();
				if( tank != null )
				{
					l.tmpUsed = tank.fill( resource.copy(), false );
				}
				else
				{
					l.tmpUsed = 0;
				}

				if( l.tmpUsed <= 0 )
				{
					i.remove();
				}
				else
				{
					requestTotal += l.tmpUsed;
				}
			}

			if( requestTotal <= 0 )
			{
				if( stack.pop() != PartP2PFluids.this )
				{
					throw new IllegalStateException( "Invalid Recursion detected." );
				}

				return 0;
			}

			if( !doFill )
			{
				if( stack.pop() != PartP2PFluids.this )
				{
					throw new IllegalStateException( "Invalid Recursion detected." );
				}

				return Math.min( resource.amount, requestTotal );
			}

			int available = resource.amount;

			i = list.iterator();
			int used = 0;

			while( i.hasNext() )
			{
				final PartP2PFluids l = i.next();

				final FluidStack insert = resource.copy();
				insert.amount = (int) Math.ceil( insert.amount * ( (double) l.tmpUsed / (double) requestTotal ) );
				if( insert.amount > available )
				{
					insert.amount = available;
				}

				final IFluidHandler tank = l.getTarget();
				if( tank != null )
				{
					l.tmpUsed = tank.fill( insert.copy(), true );
				}
				else
				{
					l.tmpUsed = 0;
				}

				available -= insert.amount;
				used += insert.amount;
			}

			if( stack.pop() != PartP2PFluids.this )
			{
				throw new IllegalStateException( "Invalid Recursion detected." );
			}

			return used;
		}

		@Nullable
		@Override
		public FluidStack drain( FluidStack resource, boolean doDrain )
		{
			return null;
		}

		@Nullable
		@Override
		public FluidStack drain( int maxDrain, boolean doDrain )
		{
			return null;
		}
	}

	private static class NullFluidHandler implements IFluidHandler {

		@Override
		public IFluidTankProperties[] getTankProperties()
		{
			return new IFluidTankProperties[0];
		}

		@Override
		public int fill( FluidStack resource, boolean doFill )
		{
			return 0;
		}

		@Nullable
		@Override
		public FluidStack drain( FluidStack resource, boolean doDrain )
		{
			return null;
		}

		@Nullable
		@Override
		public FluidStack drain( int maxDrain, boolean doDrain )
		{
			return null;
		}
	}

	private static class FluidTankProxy implements IFluidTankProperties {

		private IFluidTankProperties parent;
		private boolean allowFill, allowDrain;

		FluidTankProxy(IFluidTankProperties parent, boolean allowFill, boolean allowDrain ){
			this.parent = parent;
			this.allowFill = allowFill;
			this.allowDrain = allowDrain;
		}

		@Nullable
		@Override
		public FluidStack getContents()
		{
			return parent.getContents();
		}

		@Override
		public int getCapacity()
		{
			return parent.getCapacity();
		}

		@Override
		public boolean canFill()
		{
			return this.allowFill && parent.canFill();
		}

		@Override
		public boolean canDrain()
		{
			return this.allowDrain && parent.canDrain();
		}

		@Override
		public boolean canFillFluidType( FluidStack fluidStack )
		{
			return this.allowFill && parent.canFillFluidType(fluidStack);
		}

		@Override
		public boolean canDrainFluidType( FluidStack fluidStack )
		{
			return this.allowDrain && parent.canDrainFluidType( fluidStack );
		}
	}
}
