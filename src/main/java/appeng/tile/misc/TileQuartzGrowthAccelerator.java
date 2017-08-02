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

package appeng.tile.misc;


import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import appeng.api.AEApi;
import appeng.api.definitions.IItems;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.api.definitions.ApiItems;
import appeng.items.misc.ItemCrystalSeed;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;
import io.netty.buffer.ByteBuf;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.tiles.ICrystalGrowthAccelerator;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.util.Platform;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;


public class TileQuartzGrowthAccelerator extends AENetworkTile implements IPowerChannelState, ICrystalGrowthAccelerator
{

	private boolean hasPower = false;

	public TileQuartzGrowthAccelerator()
	{
		this.getProxy().setValidSides( EnumSet.noneOf( EnumFacing.class ) );
		this.getProxy().setFlags();
		this.getProxy().setIdlePowerUsage( 8 );
	}

	@MENetworkEventSubscribe
	public void onPower( final MENetworkPowerStatusChange ch )
	{
		this.markForUpdate();
	}

	@Override
	public AECableType getCableConnectionType( final AEPartLocation dir )
	{
		return AECableType.COVERED;
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TileQuartzGrowthAccelerator( final ByteBuf data )
	{
		final boolean hadPower = this.isPowered();
		this.setPowered( data.readBoolean() );
		return this.isPowered() != hadPower;
	}

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TileQuartzGrowthAccelerator( final ByteBuf data )
	{
		try
		{
			data.writeBoolean( this.getProxy().getEnergy().isNetworkPowered() );
		}
		catch( final GridAccessException e )
		{
			data.writeBoolean( false );
		}
	}

	@Override
	public void setOrientation( final EnumFacing inForward, final EnumFacing inUp )
	{
		super.setOrientation( inForward, inUp );
		this.getProxy().setValidSides( EnumSet.of( this.getUp(), this.getUp().getOpposite() ) );
	}

	@Override
	public boolean isPowered()
	{
		if( Platform.isServer() )
		{
			try
			{
				return this.getProxy().getEnergy().isNetworkPowered();
			}
			catch( final GridAccessException e )
			{
				return false;
			}
		}

		return this.hasPower;
	}

	@Override
	public boolean isActive()
	{
		return this.isPowered();
	}

	private void setPowered( final boolean hasPower )
	{
		this.hasPower = hasPower;
	}

	private class FluidBlockItemHandler implements IItemHandler {

		private BlockPos fluidBlock;
		private int slots = 1;
		private IItems aeItems = AEApi.instance().definitions().items();

		int redstoneID = OreDictionary.getOreID("dustRedstone");
		int quartzID = OreDictionary.getOreID("gemQuartz");

		FluidBlockItemHandler(BlockPos b){
			this.fluidBlock = b;
			if (aeItems.crystalSeed().isEnabled()) {
				ItemCrystalSeed crystalItem = (ItemCrystalSeed)aeItems.crystalSeed().maybeItem().orElseThrow(RuntimeException::new);
				NonNullList<ItemStack> subItems = NonNullList.create();
				crystalItem.getSubItems(crystalItem.getCreativeTab(), subItems);
				slots += subItems.size();
			}
		}

		private boolean isValidItem(ItemStack stack){
			Item item = stack.getItem();
			if (item == Items.REDSTONE || item == Items.QUARTZ){
				return true;
			} else if (aeItems.crystalSeed().isSameAs(stack)){
				return true;
			} else {
				int[] ids = OreDictionary.getOreIDs(stack);
				if (ids == null)
					return false;
				for (int id : ids){
					if (id == redstoneID || id == quartzID){
						return true;
					}
				}
			}
			return false;
		}

		private IItemList<IAEItemStack> getItemsInBlock(){
			List<EntityItem> itemEntities = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(fluidBlock));
			IItemList<IAEItemStack> validItems = new ItemList();
			itemEntities.forEach(entity -> {
				if (entity.isEntityAlive()) {
					ItemStack entityItem = entity.getItem();
					if (isValidItem(entityItem)){
						validItems.add(AEItemStack.create(entityItem));
					}
				}
			});
			return validItems;
		}

		/**
		 * Returns the number of slots available
		 *
		 * @return The number of slots available
		 **/
		@Override
		public int getSlots() {
			return slots;//one slot for each stage and one input
		}

		/**
		 * Returns the ItemStack in a given slot.
		 * <p>
		 * The result's stack size may be greater than the itemstacks max size.
		 * <p>
		 * If the result is null, then the slot is empty.
		 * If the result is not null but the stack size is zero, then it represents
		 * an empty slot that will only accept* a specific itemstack.
		 * <p>
		 * <p/>
		 * IMPORTANT: This ItemStack MUST NOT be modified. This method is not for
		 * altering an inventories contents. Any implementers who are able to detect
		 * modification through this method should throw an exception.
		 * <p/>
		 * SERIOUSLY: DO NOT MODIFY THE RETURNED ITEMSTACK
		 *
		 * @param slot Slot to query
		 * @return ItemStack in given slot. May be null.
		 **/
		@Nonnull
		@Override
		public ItemStack getStackInSlot(int slot) {
			if (slot == 0)
				return ItemStack.EMPTY;
			IItemList<IAEItemStack> list = getItemsInBlock();
			int iSlot = 1;
			for (IAEItemStack item : list){
				if (iSlot == slot){
					return item.getItemStack();
				}
				iSlot++;
			}
			return ItemStack.EMPTY;
		}

		/**
		 * Inserts an ItemStack into the given slot and return the remainder.
		 * The ItemStack should not be modified in this function!
		 * Note: This behaviour is subtly different from IFluidHandlers.fill()
		 *
		 * @param slot     Slot to insert into.
		 * @param stack    ItemStack to insert.
		 * @param simulate If true, the insertion is only simulated
		 * @return The remaining ItemStack that was not inserted (if the entire stack is accepted, then return ItemStack.EMPTY).
		 * May be the same as the input ItemStack if unchanged, otherwise a new ItemStack.
		 **/
		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			if (slot != 0)
				return stack;
			if (simulate)
				return isValidItem(stack) ? ItemStack.EMPTY : stack;
			if (!isValidItem(stack)){
				return stack;
			}
			EntityItem item = new EntityItem(world, fluidBlock.getX(), fluidBlock.getY(), fluidBlock.getZ(), stack);
			return world.spawnEntity(item) ? ItemStack.EMPTY : stack;
		}

		/**
		 * Extracts an ItemStack from the given slot. The returned value must be null
		 * if nothing is extracted, otherwise it's stack size must not be greater than amount or the
		 * itemstacks getMaxStackSize().
		 *
		 * @param slot     Slot to extract from.
		 * @param amount   Amount to extract (may be greater than the current stacks max limit)
		 * @param simulate If true, the extraction is only simulated
		 * @return ItemStack extracted from the slot, must be ItemStack.EMPTY, if nothing can be extracted
		 **/
		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (slot == 0){
				return ItemStack.EMPTY;
			}
			IItemList<IAEItemStack> list = getItemsInBlock();
			int iSlot = 1;
			for (IAEItemStack item : list){
				if (iSlot == slot){
					//ItemStack entityItem
					return item.getItemStack();
				}
				iSlot++;
			}
			return ItemStack.EMPTY;
		}

		/**
		 * Retrieves the maximum stack size allowed to exist in the given slot.
		 *
		 * @param slot Slot to query.
		 * @return The maximum stack size allowed in the slot.
		 */
		@Override
		public int getSlotLimit(int slot) {
			return 0;
		}
	}
}
