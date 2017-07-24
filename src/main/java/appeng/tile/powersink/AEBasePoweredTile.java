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

package appeng.tile.powersink;


import java.util.EnumSet;

import net.minecraft.util.EnumFacing;

import cofh.redstoneflux.api.IEnergyReceiver;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;

import appeng.api.config.PowerUnits;
import appeng.coremod.annotations.Integration;
import appeng.integration.IntegrationType;
import appeng.integration.Integrations;
import appeng.integration.abstraction.IC2PowerSink;


@Integration.InterfaceList( value={
		@Integration.Interface( iname = IntegrationType.RF, iface = "cofh.api.energy.IEnergyReceiver" ),
		@Integration.Interface( iname = IntegrationType.IC2, iface = "ic2.api.energy.tile.IEnergySink" )
} )
public abstract class AEBasePoweredTile extends AERootPoweredTile implements IEnergyReceiver, IEnergySink
{
	private IC2PowerSink ic2Sink;

	public AEBasePoweredTile()
	{
		super();
		ic2Sink = Integrations.ic2().createPowerSink( this, this );
		ic2Sink.setValidFaces( EnumSet.allOf( EnumFacing.class ) );
	}

	//Begin RF
	@Override
	public final int receiveEnergy( final EnumFacing from, final int maxReceive, final boolean simulate )
	{
		final int networkRFDemand = (int) Math.floor( this.getExternalPowerDemand( PowerUnits.RF, maxReceive ) );
		final int usedRF = Math.min( maxReceive, networkRFDemand );

		if( !simulate )
		{
			this.injectExternalPower( PowerUnits.RF, usedRF );
		}

		return usedRF;
	}

	@Override
	public final int getEnergyStored( final EnumFacing from )
	{
		return (int) Math.floor( PowerUnits.AE.convertTo( PowerUnits.RF, this.getAECurrentPower() ) );
	}

	@Override
	public final int getMaxEnergyStored( final EnumFacing from )
	{
		return (int) Math.floor( PowerUnits.AE.convertTo( PowerUnits.RF, this.getAEMaxPower() ) );
	}

	@Override
	public final boolean canConnectEnergy( final EnumFacing from )
	{
		return this.getPowerSides().contains( from );
	}
	//End RF

	@Override
	protected void setPowerSides( EnumSet<EnumFacing> sides )
	{
		super.setPowerSides( sides );
		ic2Sink.setValidFaces( sides );
		// trigger re-calc!
	}

	@Override
	public void onReady()
	{
		super.onReady();

		ic2Sink.onLoad();
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();

		ic2Sink.onChunkUnload();
	}

	@Override
	public void invalidate()
	{
		super.invalidate();

		ic2Sink.invalidate();
	}

	//Begin IC2
	public double getDemandedEnergy()
	{
		return ic2Sink.getDemandedEnergy();
	}

	public int getSinkTier()
	{
		return ic2Sink.getSinkTier();
	}

	public double injectEnergy(EnumFacing var1, double var2, double var4)
	{
		return ic2Sink.injectEnergy( var1, var2, var4 );
	}

	@Integration.Method( iname = IntegrationType.IC2 )
	@Override
	public boolean acceptsEnergyFrom( IEnergyEmitter iEnergyEmitter, EnumFacing enumFacing )
	{
		return ic2Sink.acceptsEnergyFrom( iEnergyEmitter, enumFacing );
	}
	//End IC2
}
