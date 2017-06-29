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

package appeng.integration.modules.ic2;


import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.oredict.OreDictionary;

//import ic2.api.recipe.RecipeInputItemStack;

import ic2.api.recipe.IRecipeInput;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.TunnelType;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.integration.IntegrationHelper;
import appeng.integration.abstraction.IC2PowerSink;
import appeng.integration.abstraction.IIC2;
import appeng.tile.powersink.IExternalPowerSink;
import appeng.util.Platform;


public class IC2Module implements IIC2
{

	private static final String[] IC2_CABLE_TYPES = { "copper", "glass", "gold", "iron", "tin", "detector", "splitter" };

	public IC2Module()
	{
		IntegrationHelper.testClassExistence( this, ic2.api.energy.tile.IEnergyTile.class );
		IntegrationHelper.testClassExistence( this, ic2.api.item.IC2Items.class );
		IntegrationHelper.testClassExistence( this, ic2.api.recipe.Recipes.class );
		//IntegrationHelper.testClassExistence( this, ic2.api.recipe.RecipeInputItemStack.class );
	}

	@Override
	public void postInit()
	{
		final IP2PTunnelRegistry reg = AEApi.instance().registries().p2pTunnel();

		for( String string : IC2_CABLE_TYPES )
		{
			reg.addNewAttunement( this.getCable( string ), TunnelType.IC2_POWER );
		}
	}

	private ItemStack getItem( final String name, String variant )
	{
		return ic2.api.item.IC2Items.getItem( name, variant );
	}

	private ItemStack getCable( final String type )
	{
		return this.getItem( "cable", "type:" + type );
	}

	/**
	 * Create an IC2 power sink for the given external sink.
	 */
	@Override
	public IC2PowerSink createPowerSink( TileEntity tileEntity, IExternalPowerSink externalSink )
	{
		return new IC2PowerSinkAdapter( tileEntity, externalSink );
	}

	@Override
	public void maceratorRecipe( ItemStack in, ItemStack out )
	{
		ic2.api.recipe.Recipes.macerator.addRecipe( new RecipeInputItemStack( in, in.getCount() ), null, false, out );
	}

	public class RecipeInputItemStack implements IRecipeInput
	{
		public RecipeInputItemStack(ItemStack input) {
			this(input, input.getCount());
		}

		public RecipeInputItemStack(ItemStack input, int amount) {
			this.input = input;
			this.amount = amount;
		}

		@Override
		public boolean matches(ItemStack subject) {
			return !subject.isEmpty() && Platform.itemComparisons().isFuzzyEqualItem( subject, input, FuzzyMode.IGNORE_ALL );
		}

		@Override
		public int getAmount() {
			return amount;
		}

		@Override
		public List<ItemStack> getInputs() {
			return Arrays.asList(input);
		}

		public final ItemStack input;
		public final int amount;
	}
}
