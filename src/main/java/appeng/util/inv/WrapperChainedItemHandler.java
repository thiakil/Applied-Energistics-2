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

package appeng.util.inv;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandler;


public class WrapperChainedItemHandler implements IItemHandler
{

	private int fullSize = 0;
	private List<IItemHandler> l;
	private Map<Integer, InvOffset> offsets;

	public WrapperChainedItemHandler( final IItemHandler... inventories )
	{
		this.setInventory( inventories );
	}

	private void setInventory( final IItemHandler... a )
	{
		this.l = ImmutableList.copyOf( a );
		this.calculateSizes();
	}

	private void calculateSizes()
	{
		this.offsets = new HashMap<Integer, WrapperChainedItemHandler.InvOffset>();

		int offset = 0;
		for( final IItemHandler in : this.l )
		{
			final InvOffset io = new InvOffset();
			io.offset = offset;
			io.size = in.getSlots();
			io.i = in;

			for( int y = 0; y < io.size; y++ )
			{
				this.offsets.put( y + io.offset, io );
			}

			offset += io.size;
		}

		this.fullSize = offset;
	}

	public WrapperChainedItemHandler( final List<IItemHandler> inventories )
	{
		this.setInventory( inventories );
	}

	private void setInventory( final List<IItemHandler> a )
	{
		this.l = a;
		this.calculateSizes();
	}

	public void cycleOrder()
	{
		if( this.l.size() > 1 )
		{
			final List<IItemHandler> newOrder = new ArrayList<>( this.l.size() );
			newOrder.add( this.l.get( this.l.size() - 1 ) );
			for( int x = 0; x < this.l.size() - 1; x++ )
			{
				newOrder.add( this.l.get( x ) );
			}
			this.setInventory( newOrder );
		}
	}

	public IItemHandler getInv( final int idx )
	{
		final InvOffset io = this.offsets.get( idx );
		if( io != null )
		{
			return io.i;
		}
		return null;
	}

	public int getInvSlot( final int idx )
	{
		final InvOffset io = this.offsets.get( idx );
		if( io != null )
		{
			return idx - io.offset;
		}
		return 0;
	}

	@Override
	public int getSlots()
	{
		return this.fullSize;
	}

	@Override
	public ItemStack getStackInSlot( final int idx )
	{
		final InvOffset io = this.offsets.get( idx );
		if( io != null )
		{
			return io.i.getStackInSlot( idx - io.offset );
		}
		return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public ItemStack insertItem( int slot, @Nonnull ItemStack stack, boolean simulate )
	{
		final InvOffset io = this.offsets.get( slot );
		if( io != null )
		{
			return io.i.insertItem( slot - io.offset, stack, simulate );
		}
		return stack;
	}

	@Nonnull
	@Override
	public ItemStack extractItem( int slot, int amount, boolean simulate )
	{
		final InvOffset io = this.offsets.get( slot );
		if( io != null )
		{
			return io.i.extractItem( slot - io.offset, amount, simulate );
		}
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit( int slot )
	{
		final InvOffset io = this.offsets.get( slot );
		if( io != null )
		{
			return io.i.getSlotLimit( slot - io.offset );
		}

		return 0;
	}

	private static class InvOffset
	{

		private int offset;
		private int size;
		private IItemHandler i;
	}


}
