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

package appeng.integration;


import appeng.integration.modules.ActuallyAdditionsModule;
import appeng.integration.modules.BuildcraftModule;
import appeng.integration.modules.CofhHammerModule;
import appeng.integration.modules.ExU2Module;
import appeng.integration.modules.Mekanism;
import appeng.integration.modules.StorageDrawersModule;
import appeng.integration.modules.chisel.ChiselIMC;
import appeng.integration.modules.chisel.ChiselModule;
import appeng.integration.modules.opencomputers.OpenComputers;
import appeng.integration.modules.ic2.IC2Module;
import appeng.integration.modules.jei.JEIModule;
import appeng.integration.modules.rf.RFItemModule;
import appeng.integration.modules.rf.RFTileModule;
import appeng.integration.modules.theoneprobe.TheOneProbeModule;
import appeng.integration.modules.waila.WailaModule;


public enum IntegrationType
{
	IC2( IntegrationSide.BOTH, "Industrial Craft 2", "ic2" )
	{
		@Override
		public IIntegrationModule createInstance()
		{
			return Integrations.setIc2( new IC2Module() );
		}
	},

	BUILDCRAFT( IntegrationSide.BOTH, "Buildcraft", "buildcraft" )
	{
		@Override
		public IIntegrationModule createInstance()
		{
			return Integrations.setBc( new BuildcraftModule() );
		}
	},

	COFH_HAMMER( IntegrationSide.BOTH, "TF Hammer", "cofhapi|item" )
	{
		@Override
		public IIntegrationModule createInstance()
		{
			return Integrations.setCofhHammer( new CofhHammerModule() );
		}
	},

	RF( IntegrationSide.BOTH, "RedstoneFlux Power - Tiles", "cofhapi|energy" )
	{

		@Override
		public IIntegrationModule createInstance()
		{
			return new RFTileModule();
		}
	},

	RFItem( IntegrationSide.BOTH, "RedstoneFlux Power - Items", "cofhapi|energy" )
	{

		@Override
		public IIntegrationModule createInstance()
		{
			return new RFItemModule();
		}
	},

	Waila( IntegrationSide.BOTH, "Waila", "waila" )
	{
		@Override
		public IIntegrationModule createInstance()
		{
			return new WailaModule();
		}
	},

	JEI( IntegrationSide.CLIENT, "Just Enough Items", "jei" )
	{
		@Override
		public IIntegrationModule createInstance()
		{
			return Integrations.setJei( new JEIModule() );
		}
	},

	OpenComputers( IntegrationSide.BOTH, "OpenComputers", "opencomputers" )
	{
		@Override
		public IIntegrationModule createInstance()
		{
			return new OpenComputers();
		}
	},

	THE_ONE_PROBE( IntegrationSide.BOTH, "TheOneProbe", "theoneprobe" )
	{
		@Override
		public IIntegrationModule createInstance()
		{
			return new TheOneProbeModule();
		}
	},

	MEKANISM( IntegrationSide.BOTH, "Mekanism", "mekanism") {
		@Override
		public IIntegrationModule createInstance(){
			return Integrations.setMekanism( new Mekanism() );
		}
	},

	CHISEL( IntegrationSide.BOTH, "Chisel", ChiselIMC.CHISEL_MODID ){
		@Override
		public IIntegrationModule createInstance()
		{
			return new ChiselModule();
		}
	},

	STORAGE_DRAWERS(IntegrationSide.BOTH, "Storage Drawers", "storagedrawers") {
		@Override
		public IIntegrationModule createInstance() {
			return new StorageDrawersModule();
		}
	},
	
	EXU2( IntegrationSide.BOTH, "Extra Utils 2", "extrautils2" ){
		@Override
		public IIntegrationModule createInstance()
		{
			return Integrations.setExU2( new ExU2Module() );
		}
	},

	ACTUALLYADDITIONS( IntegrationSide.BOTH, "Actually Additions", "actuallyadditions"){
		@Override
		public IIntegrationModule createInstance()
		{
			return Integrations.setAA( new ActuallyAdditionsModule() );
		}
	}

	;

	public final IntegrationSide side;
	public final String dspName;
	public final String modID;

	IntegrationType( final IntegrationSide side, final String name, final String modid )
	{
		this.side = side;
		this.dspName = name;
		this.modID = modid;
	}

	public IIntegrationModule createInstance()
	{
		throw new MissingIntegrationModuleException( this.name() );
	}

}
