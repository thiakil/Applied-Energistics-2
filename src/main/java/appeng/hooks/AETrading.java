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
package appeng.hooks;


import java.util.Optional;
import java.util.Random;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import appeng.api.AEApi;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import appeng.core.AELog;


/**
 * Handles adding AE trades to Villagers. 1.11+: Adds to Smith Profession under a new career. Currently all at Level 1
 * @author AlgorithmX2
 * @author thatsIch
 * @author yueh
 * @since rv2
 */
public class AETrading implements EntityVillager.ITradeList
{

	public static void registerVillageTrades()
	{
		VillagerRegistry.VillagerProfession smith = VillagerRegistry.instance().getRegistry().getValue( new ResourceLocation( "minecraft", "smith" ) );
		if ( smith == null )
		{
			AELog.error( "Could not get smith profession." );
			return;
		}
		VillagerRegistry.VillagerCareer career = new VillagerRegistry.VillagerCareer( smith, "certus" );
		career.addTrade( 1, new AETrading() );
	}

	@Override
	public void addMerchantRecipe( IMerchant villager, MerchantRecipeList recipeList, Random random )
	{
		final IMaterials materials = AEApi.instance().definitions().materials();
		this.addMerchant( recipeList, materials.silicon(), 1, random, 2 );
		this.addMerchant( recipeList, materials.certusQuartzCrystal(), 2, random, 4 );
		this.addMerchant( recipeList, materials.certusQuartzDust(), 1, random, 3 );
		this.addTrade( recipeList, materials.certusQuartzDust(), materials.certusQuartzCrystal(), random, 2 );
	}

	private void addMerchant( MerchantRecipeList list, IItemDefinition item, int emera, Random rand, int greed )
	{
		item.maybeStack( 1 ).ifPresent( itemStack ->
		{
			// Sell
			ItemStack from = itemStack.copy();
			ItemStack to = new ItemStack( Items.EMERALD );
			int multiplier = ( Math.abs( rand.nextInt() ) % 6 );
			final int emeraldCost = emera + ( Math.abs( rand.nextInt() ) % greed ) - multiplier;
			int mood = rand.nextInt() % 2;
			from.setCount( multiplier + mood );
			to.setCount( multiplier * emeraldCost - mood );
			if( to.getCount() < 0 )
			{
				from.setCount( from.getCount() - to.getCount() );
				to.setCount( 0 );
			}
			this.addToList( list, from, to );
			// Buy
			ItemStack reverseTo = from.copy();
			ItemStack reverseFrom = to.copy();
			reverseFrom.setCount( (int)( reverseFrom.getCount() * rand.nextFloat() * 3.0f + 1.0f ) );
			this.addToList( list, reverseFrom, reverseTo );
		} );
	}

	private void addTrade( MerchantRecipeList list, IItemDefinition inputDefinition, IItemDefinition outputDefinition, Random rand, int conversionVariance )
	{
		final Optional<ItemStack> maybeInputStack = inputDefinition.maybeStack( 1 );
		final Optional<ItemStack> maybeOutputStack = outputDefinition.maybeStack( 1 );
		if( maybeInputStack.isPresent() && maybeOutputStack.isPresent() )
		{
			// Sell
			ItemStack inputStack = maybeInputStack.get().copy();
			ItemStack outputStack = maybeOutputStack.get().copy();
			inputStack.setCount( 1 + ( Math.abs( rand.nextInt() ) % ( 1 + conversionVariance ) ) );
			outputStack.setCount( 1 );
			this.addToList( list, inputStack, outputStack );
		}
	}

	private void addToList( MerchantRecipeList l, ItemStack a, ItemStack b )
	{
		if( a.getCount() < 1 )
		{
			a.setCount( 1 );
		}
		if( b.getCount() < 1 )
		{
			b.setCount( 1 );
		}
		if( a.getCount() > a.getMaxStackSize() )
		{
			a.setCount( a.getMaxStackSize() );
		}
		if( b.getCount() > b.getMaxStackSize() )
		{
			b.setCount( b.getMaxStackSize() );
		}
		l.add( new MerchantRecipe( a, b ) );
	}

}
