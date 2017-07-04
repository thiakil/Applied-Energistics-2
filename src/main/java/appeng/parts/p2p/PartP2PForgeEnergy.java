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


import java.util.List;
import java.util.Stack;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import appeng.api.config.PowerUnits;
import appeng.api.parts.IPartModel;
import appeng.api.util.DimensionalCoord;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.util.Platform;


public final class PartP2PForgeEnergy extends PartP2PTunnel<PartP2PForgeEnergy>
{
	private static final ThreadLocal<Stack<PartP2PForgeEnergy>> THREAD_STACK = new ThreadLocal<Stack<PartP2PForgeEnergy>>();
	/**
	 * Default element based on the null element pattern
	 */
	private static final IEnergyStorage NULL_HANDLER = new EnergyStorage(0);
	private final FEProxy inputCap = new FEProxy();
	private TileEntity myHost;
	private BlockPos facingPos;

	private static final P2PModels MODELS = new P2PModels( "part/p2p/p2p_tunnel_fe" );

	@PartModels
	public static List<IPartModel> getModels()
	{
		return MODELS.getModels();
	}

	public PartP2PForgeEnergy( ItemStack is )
	{
		super( is );
	}

	@Override
	public void onTunnelNetworkChange()
	{
		this.getHost().notifyNeighbors();
	}

	private Stack<PartP2PForgeEnergy> getDepth()
	{
		Stack<PartP2PForgeEnergy> s = THREAD_STACK.get();

		if( s == null )
		{
			THREAD_STACK.set( s = new Stack<PartP2PForgeEnergy>() );
		}

		return s;
	}

	private IEnergyStorage getOutput()
	{
		if( this.isOutput() )
		{
			IEnergyStorage outputTarget = null;
			TileEntity te = myHost.getWorld().getTileEntity( facingPos );
			if ( te != null && te.hasCapability( CapabilityEnergy.ENERGY, this.getSide().getOpposite().getFacing() ) )
			{
				outputTarget = te.getCapability( CapabilityEnergy.ENERGY, this.getSide().getOpposite().getFacing() );
			}

			if( outputTarget == null )
			{
				return NULL_HANDLER;
			}

			return outputTarget;
		}
		return NULL_HANDLER;
	}

	@Override
	public boolean hasCapability( Capability<?> capabilityClass )
	{
		if ( !this.isOutput() && capabilityClass == CapabilityEnergy.ENERGY )
			return true;
		return super.hasCapability( capabilityClass );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public <T> T getCapability( Capability<T> capabilityClass )
	{
		if ( !this.isOutput() && capabilityClass == CapabilityEnergy.ENERGY )
			return (T)inputCap;
		return super.getCapability( capabilityClass );
	}

	@Override
	public void addToWorld()
	{
		super.addToWorld();
		this.myHost = getTile();
		this.facingPos = this.getHost().getLocation().getPos().offset( this.getSide().getFacing() );
	}

	@Override
	public IPartModel getStaticModels()
	{
		return MODELS.getModel( isPowered(), isActive() );
	}

	private class FEProxy implements IEnergyStorage
	{

		@Override
		public int receiveEnergy( int maxReceive, boolean simulate )
		{
			if( PartP2PForgeEnergy.this.isOutput() )
			{
				return 0;
			}

			if( PartP2PForgeEnergy.this.isActive() )
			{
				Stack<PartP2PForgeEnergy> stack = PartP2PForgeEnergy.this.getDepth();

				for( PartP2PForgeEnergy t : stack )
				{
					if( t == PartP2PForgeEnergy.this )
					{
						return 0;
					}
				}

				stack.push( PartP2PForgeEnergy.this );

				int total = 0;

				try
				{
					for( PartP2PForgeEnergy t : PartP2PForgeEnergy.this.getOutputs() )
					{
						if( Platform.getRandomInt() % 2 > 0 )
						{
							int receiver = t.getOutput().receiveEnergy( maxReceive, simulate );
							maxReceive -= receiver;
							total += receiver;

							if( maxReceive <= 0 )
							{
								break;
							}
						}
					}

					if( maxReceive > 0 )
					{
						for( PartP2PForgeEnergy t : PartP2PForgeEnergy.this.getOutputs() )
						{
							int receiver = t.getOutput().receiveEnergy(  maxReceive, simulate );
							maxReceive -= receiver;
							total += receiver;

							if( maxReceive <= 0 )
							{
								break;
							}
						}
					}

					PartP2PForgeEnergy.this.queueTunnelDrain( PowerUnits.RF, total );
				}
				catch( GridAccessException ignored )
				{
				}

				if( stack.pop() != PartP2PForgeEnergy.this )
				{
					throw new IllegalStateException( "Invalid Recursion detected." );
				}

				return total;
			}

			return 0;
		}

		@Override
		public int getEnergyStored()
		{
			if( PartP2PForgeEnergy.this.isOutput() || !PartP2PForgeEnergy.this.isActive() )
			{
				return 0;
			}

			int total = 0;

			Stack<PartP2PForgeEnergy> stack = PartP2PForgeEnergy.this.getDepth();

			for( PartP2PForgeEnergy t : stack )
			{
				if( t == PartP2PForgeEnergy.this )
				{
					return 0;
				}
			}

			stack.push( PartP2PForgeEnergy.this );

			try
			{
				for( PartP2PForgeEnergy t : PartP2PForgeEnergy.this.getOutputs() )
				{
					total += t.getOutput().getEnergyStored();
				}
			}
			catch( GridAccessException e )
			{
				return 0;
			}

			if( stack.pop() != PartP2PForgeEnergy.this )
			{
				throw new IllegalStateException( "Invalid Recursion detected." );
			}

			return total;
		}

		@Override
		public int getMaxEnergyStored()
		{
			if( PartP2PForgeEnergy.this.isOutput() || !PartP2PForgeEnergy.this.isActive() )
			{
				return 0;
			}

			int total = 0;

			Stack<PartP2PForgeEnergy> stack = PartP2PForgeEnergy.this.getDepth();

			for( PartP2PForgeEnergy t : stack )
			{
				if( t == PartP2PForgeEnergy.this )
				{
					return 0;
				}
			}

			stack.push( PartP2PForgeEnergy.this );

			try
			{
				for( PartP2PForgeEnergy t : PartP2PForgeEnergy.this.getOutputs() )
				{
					total += t.getOutput().getMaxEnergyStored();
				}
			}
			catch( GridAccessException e )
			{
				return 0;
			}

			if( stack.pop() != PartP2PForgeEnergy.this )
			{
				throw new IllegalStateException( "Invalid Recursion detected." );
			}

			return total;
		}

		@Override
		public boolean canExtract()
		{
			return false;
		}

		@Override
		public boolean canReceive()
		{
			return !isOutput();
		}

		@Override
		public int extractEnergy( int maxExtract, boolean simulate )
		{
			return 0;
		}
	}
}
