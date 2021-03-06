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

package appeng.core.sync.packets;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import appeng.api.networking.crafting.ICraftingGrid;
import appeng.core.AEConfig;
import appeng.integration.modules.opencomputers.Craftable;
import appeng.util.item.ItemList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.IContainerCraftingPacket;
import appeng.items.storage.ItemViewCell;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.prioritylist.IPartitionList;


public class PacketJEIRecipe extends AppEngPacket
{

	private ItemStack[][] recipe;

	// automatic.
	public PacketJEIRecipe( final ByteBuf stream ) throws IOException
	{
		final ByteBufInputStream bytes = new ByteBufInputStream( stream );
		//bytes.skip( stream.readerIndex() );
		final NBTTagCompound comp = CompressedStreamTools.readCompressed( bytes );
		if( comp != null )
		{
			this.recipe = new ItemStack[9][];
			for( int x = 0; x < this.recipe.length; x++ )
			{
				final NBTTagList list = comp.getTagList( "#" + x, 10 );
				if( list.tagCount() > 0 )
				{
					this.recipe[x] = new ItemStack[list.tagCount()];
					for( int y = 0; y < list.tagCount(); y++ )
					{
						this.recipe[x][y] = new ItemStack( list.getCompoundTagAt( y ) );
					}
				}
			}
		}
	}

	// api
	public PacketJEIRecipe( final NBTTagCompound recipe ) throws IOException
	{
		final ByteBuf data = Unpooled.buffer();

		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		final DataOutputStream outputStream = new DataOutputStream( bytes );

		data.writeInt( this.getPacketID() );

		CompressedStreamTools.writeCompressed( recipe, outputStream );
		data.writeBytes( bytes.toByteArray() );

		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( final INetworkInfo manager, final AppEngPacket packet, final EntityPlayer player )
	{
		final EntityPlayerMP pmp = (EntityPlayerMP) player;


		final Container con = pmp.openContainer;

		if (con instanceof IContainerCraftingPacket) {
			final IContainerCraftingPacket cct = (IContainerCraftingPacket) con;
			final IGridNode node = cct.getNetworkNode();
			if (node != null) {
				final IGrid grid = node.getGrid();
				if (grid == null) {
					return;
				}

				final IStorageGrid inv = grid.getCache(IStorageGrid.class);
				final IEnergyGrid energy = grid.getCache(IEnergyGrid.class);
				final ISecurityGrid security = grid.getCache(ISecurityGrid.class);
				final IInventory craftMatrix = cct.getInventoryByName("crafting");
				final IInventory playerInventory = cct.getInventoryByName("player");

				final Actionable realForFake = cct.useRealItems() ? Actionable.MODULATE : Actionable.SIMULATE;

				final IItemList<IAEItemStack> craftables = new ItemList();
				if (realForFake == Actionable.SIMULATE){
					IItemList<IAEItemStack> storageList = inv.getItemInventory().getStorageList();
					for (IAEItemStack stack : storageList) {
						if (stack.isCraftable()) {
							craftables.add(stack);
						}
					}
				}

				if (inv != null && this.recipe != null && security != null) {
					final InventoryCrafting testInv = new InventoryCrafting(new ContainerNull(), 3, 3);
					for (int x = 0; x < 9; x++) {
						if (this.recipe[x] != null && this.recipe[x].length > 0) {
							testInv.setInventorySlotContents(x, this.recipe[x][0]);
						}
					}

					final IRecipe r = Platform.findMatchingRecipe(testInv, pmp.world);

					if (r != null && security.hasPermission(player, SecurityPermissions.EXTRACT)) {
						final ItemStack is = r.getCraftingResult(testInv);

						if (!is.isEmpty()) {
							final IMEMonitor<IAEItemStack> storage = inv.getItemInventory();
							final IItemList all = storage.getStorageList();
							final IPartitionList<IAEItemStack> filter = ItemViewCell.createFilter(cct.getViewCells());

							for (int x = 0; x < craftMatrix.getSizeInventory(); x++) {
								final ItemStack patternItem = testInv.getStackInSlot(x);

								ItemStack currentItem = craftMatrix.getStackInSlot(x);
								if (!currentItem.isEmpty()) {
									testInv.setInventorySlotContents(x, currentItem);
									final ItemStack newItemStack = r.matches(testInv, pmp.world) ? r.getCraftingResult(testInv) : ItemStack.EMPTY;
									testInv.setInventorySlotContents(x, patternItem);

									if (newItemStack.isEmpty() || !Platform.itemComparisons().isSameItem(newItemStack, is)) {
										final IAEItemStack in = AEItemStack.create(currentItem);
										if (in != null) {
											final IAEItemStack out = realForFake == Actionable.SIMULATE ? null : Platform.poweredInsert(energy, storage, in, cct.getActionSource());
											if (out != null) {
												craftMatrix.setInventorySlotContents(x, out.getItemStack());
											} else {
												craftMatrix.setInventorySlotContents(x, ItemStack.EMPTY);
											}

											currentItem = craftMatrix.getStackInSlot(x);
										}
									}
								}

								// True if we need to fetch an item for the recipe
								if (!patternItem.isEmpty() && currentItem.isEmpty()) {
									// Grab from network by recipe
									ItemStack whichItem = Platform.extractItemsByRecipe(energy, cct.getActionSource(), storage, player.world, r, is, testInv, patternItem, x, all, realForFake, filter);

									// If that doesn't get it, check the other possible items from the JEI packet
									if (whichItem.isEmpty()) {
										for (int y = 0; y < this.recipe[x].length; y++) {
											whichItem = Platform.extractItemsByRecipe(energy, cct.getActionSource(), storage, player.world, r, is, testInv, this.recipe[x][y], x, all, realForFake, filter);
											if (!whichItem.isEmpty()) {
												break;
											}
										}
									}

									// If that doesn't work, grab from the player's inventory
									if (whichItem.isEmpty() && playerInventory != null) {
										whichItem = this.extractItemFromPlayerInventory(player, realForFake, patternItem);
									}

									// if pattern term, check if it's craftable
									if (whichItem.isEmpty() && realForFake == Actionable.SIMULATE){
										for (int y = 0; y < this.recipe[x].length; y++) {
											Collection<IAEItemStack> matches = craftables.findFuzzy(AEItemStack.create(this.recipe[x][y]), FuzzyMode.IGNORE_ALL);
											if (matches.size() > 0){
												whichItem = matches.iterator().next().getDisplayItemStack();//display stack so it ensures it's not empty
												break;
											}
										}

									}

									// If all else fails, check if they want to always allow it.
									if (whichItem.isEmpty() && realForFake == Actionable.SIMULATE && !AEConfig.instance().getPatternTermRequiresItems()){
										whichItem = patternItem;
									}

									craftMatrix.setInventorySlotContents(x, whichItem);
								}
							}
							con.onCraftMatrixChanged(craftMatrix);
						}
					}
				}
			}
		}
	}

	/**
	 * Tries to extract an item from the player inventory. Does account for fuzzy items.
	 *
	 * @param player the {@link EntityPlayer} to extract from
	 * @param mode the {@link Actionable} to simulate or modulate the operation
	 * @param patternItem which {@link ItemStack} to extract
	 * @return null or a found {@link ItemStack}
	 */
	private ItemStack extractItemFromPlayerInventory( final EntityPlayer player, final Actionable mode, final ItemStack patternItem )
	{
		final InventoryAdaptor ia = InventoryAdaptor.getAdaptor( player, EnumFacing.UP );
		final AEItemStack request = AEItemStack.create( patternItem );
		final boolean isSimulated = mode == Actionable.SIMULATE;
		final boolean checkFuzzy = request.isOre() || patternItem.getItemDamage() == OreDictionary.WILDCARD_VALUE || patternItem.hasTagCompound() || patternItem.isItemStackDamageable();

		if( !checkFuzzy )
		{
			if( isSimulated )
			{
				return ia.simulateRemove( 1, patternItem, null );
			}
			else
			{
				return ia.removeItems( 1, patternItem, null );
			}
		}
		else
		{
			if( isSimulated )
			{
				return ia.simulateSimilarRemove( 1, patternItem, FuzzyMode.IGNORE_ALL, null );
			}
			else
			{
				return ia.removeSimilarItems( 1, patternItem, FuzzyMode.IGNORE_ALL, null );
			}
		}
	}
}
