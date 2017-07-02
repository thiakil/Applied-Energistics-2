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

package appeng.integration.modules.opencomputers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import li.cil.oc.api.Driver;
import li.cil.oc.api.IMC;
import li.cil.oc.api.Items;
import li.cil.oc.api.detail.ItemInfo;

import appeng.api.AEApi;
import appeng.api.IAppEngApi;
import appeng.api.config.TunnelType;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.parts.IPartHelper;
import appeng.core.AELog;
import appeng.helpers.Reflected;
import appeng.integration.IIntegrationModule;
import appeng.integration.IntegrationHelper;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;

import appeng.integration.modules.opencomputers.driver.*;



public class OpenComputers implements IIntegrationModule
{
	@Reflected
	public static OpenComputers instance;

	@Reflected
	public OpenComputers()
	{
		IntegrationHelper.testClassExistence( this, li.cil.oc.api.Items.class );
		IntegrationHelper.testClassExistence( this, li.cil.oc.api.Network.class );
		IntegrationHelper.testClassExistence( this, li.cil.oc.api.network.Environment.class );
		IntegrationHelper.testClassExistence( this, li.cil.oc.api.network.SidedEnvironment.class );
		IntegrationHelper.testClassExistence( this, li.cil.oc.api.network.Node.class );
		IntegrationHelper.testClassExistence( this, li.cil.oc.api.network.Message.class );
		IntegrationHelper.testClassExistence( this, li.cil.oc.api.IMC.class );
		IntegrationHelper.testClassExistence( this, li.cil.oc.api.Driver.class );
	}

	@Override
	public void init()
	{
		final IAppEngApi api = AEApi.instance();
		final IPartHelper partHelper = api.partHelper();

		if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.OpenComputers ) )
		{
			partHelper.registerNewLayer( "appeng.parts.layers.LayerSidedEnvironment", "li.cil.oc.api.network.SidedEnvironment" );
		}

		AELog.info("Adding AE2 OC Drivers");
		Driver.add(new NetworkDriver());
		Driver.add(new InterfaceDriver());
		Driver.add(new ExportBusDriver());
		Driver.add(new ImportBusDriver());

		//TODO not quite ready yet
		//Driver.add(new MEDriveDriver());
		//Driver.add(new MEChestDriver());

		Driver.add(new ConverterCellInventory());
		Driver.add(new ConverterAEStack());

		IMC.registerWrenchTool(this.getClass().getName()+".useWrench");
		IMC.registerWrenchToolCheck(this.getClass().getName()+".isWrench");
	}

	@Override
	public void postInit()
	{
		final IP2PTunnelRegistry registry = AEApi.instance().registries().p2pTunnel();

		addAttunement( registry, "cable" );
		addAttunement( registry, "adapter" );
		addAttunement( registry, "switch" );
		addAttunement( registry, "access_point" );
		addAttunement( registry, "lan_card" );
		addAttunement( registry, "linked_card" );
		addAttunement( registry, "wlan_card" );
		addAttunement( registry, "analyzer" );
	}

	private void addAttunement( IP2PTunnelRegistry registry, String name )
	{
		ItemInfo info = Items.get( name );
		if ( info != null )
		{
			registry.addNewAttunement( info.createItemStack( 1 ), TunnelType.COMPUTER_MESSAGE );
		}
	}

	public static boolean useWrench(EntityPlayer player, BlockPos pos, boolean changeDurability) {
		ItemStack mainhand = player.getHeldItem( EnumHand.MAIN_HAND);
		ItemStack offhand = player.getHeldItem( EnumHand.OFF_HAND);

		if (mainhand != null && mainhand.getItem() instanceof IAEWrench ){
			return ((IAEWrench)mainhand.getItem()).canWrench(mainhand, player, pos);
		}
		if (offhand != null && offhand.getItem() instanceof IAEWrench ){
			return ((IAEWrench)offhand.getItem()).canWrench(offhand, player, pos);
		}

		return false;
	}

	public static boolean isWrench(ItemStack stack) {
		return stack != null && !stack.isEmpty() && stack.getItem() instanceof IAEWrench;
	}
}