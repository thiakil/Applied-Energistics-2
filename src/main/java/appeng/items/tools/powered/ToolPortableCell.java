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

package appeng.items.tools.powered;


import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import baubles.api.BaubleType;
import baubles.api.cap.BaubleItem;
import baubles.api.cap.IBaublesItemHandler;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.capabilities.Capabilities;
import appeng.container.implementations.ContainerMEPortableCell;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.contents.PortableCellViewer;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.storage.CellInventory;
import appeng.me.storage.CellInventoryHandler;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;


public class ToolPortableCell extends AEBasePoweredItem implements IStorageCell, IGuiItem, IItemGroup
{
	public ToolPortableCell()
	{
		super( AEConfig.instance().getPortableCellBattery() );
		MinecraftForge.EVENT_BUS.register( this );
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick( final World w, final EntityPlayer player, final EnumHand hand )
	{
		Platform.openGUI( player, GuiBridge.GUI_PORTABLE_CELL, player.getHeldItem( hand ) );
		return new ActionResult<ItemStack>( EnumActionResult.SUCCESS, player.getHeldItem( hand ) );
	}

	@SideOnly( Side.CLIENT )
	@Override
	public boolean isFull3D()
	{
		return false;
	}

	@Override
	public void addCheckedInformation( final ItemStack stack, final World world, final List<String> lines, final boolean displayMoreInfo )
	{
		super.addCheckedInformation( stack, world, lines, displayMoreInfo );

		final IMEInventory<IAEItemStack> cdi = AEApi.instance().registries().cell().getCellInventory( stack, null, StorageChannel.ITEMS );

		if( cdi instanceof CellInventoryHandler )
		{
			final ICellInventory cd = ( (ICellInventoryHandler) cdi ).getCellInv();
			if( cd != null )
			{
				lines.add( cd.getUsedBytes() + " " + GuiText.Of.getLocal() + ' ' + cd.getTotalBytes() + ' ' + GuiText.BytesUsed.getLocal() );
				lines.add( cd.getStoredItemTypes() + " " + GuiText.Of.getLocal() + ' ' + cd.getTotalItemTypes() + ' ' + GuiText.Types.getLocal() );
			}
		}
	}

	@Override
	public int getBytes( final ItemStack cellItem )
	{
		return 1024;
	}

	@Override
	public int getBytesPerType( final ItemStack cellItem )
	{
		return 8;
	}

	@Override
	public int getTotalTypes( final ItemStack cellItem )
	{
		return 64;
	}

	@Override
	public boolean isBlackListed( final ItemStack cellItem, final IAEItemStack requestedAddition )
	{
		return false;
	}

	@Override
	public boolean storableInStorageCell()
	{
		return false;
	}

	@Override
	public boolean isStorageCell( final ItemStack i )
	{
		return true;
	}

	@Override
	public double getIdleDrain()
	{
		return 0.5;
	}

	@Override
	public String getUnlocalizedGroupName( final Set<ItemStack> others, final ItemStack is )
	{
		return GuiText.StorageCells.getUnlocalized();
	}

	@Override
	public boolean isEditable( final ItemStack is )
	{
		return true;
	}

	@Override
	public IInventory getUpgradesInventory( final ItemStack is )
	{
		return new CellUpgrades( is, 2 );
	}

	@Override
	public IInventory getConfigInventory( final ItemStack is )
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode( final ItemStack is )
	{
		final String fz = Platform.openNbtData( is ).getString( "FuzzyMode" );
		try
		{
			return FuzzyMode.valueOf( fz );
		}
		catch( final Throwable t )
		{
			return FuzzyMode.IGNORE_ALL;
		}
	}

	@Override
	public void setFuzzyMode( final ItemStack is, final FuzzyMode fzMode )
	{
		Platform.openNbtData( is ).setString( "FuzzyMode", fzMode.name() );
	}

	@Override
	public IGuiItemObject getGuiObject( final ItemStack is, final World w, final BlockPos pos )
	{
		return new PortableCellViewer( is, pos.getX() );
	}

	@Override
	public boolean shouldCauseReequipAnimation( ItemStack oldStack, ItemStack newStack, boolean slotChanged ) 
	{
	        return slotChanged || oldStack.getItem() != newStack.getItem();
	}

	@SuppressWarnings( "unchecked" )
	private void checkAddToCell(ItemStack cell, ItemStack item, ItemStack playerRejected, EntityPlayer player){
		NBTTagCompound tag = Platform.openNbtData( cell );
		if (tag.hasKey( Settings.PORTABLE_CELL_AUTOPICKUP.name() ) && YesNo.valueOf( tag.getString( Settings.PORTABLE_CELL_AUTOPICKUP.name() )) == YesNo.YES){
			IMEInventoryHandler<IAEItemStack> cellInv = CellInventory.getCell( cell, null );
			IEnergySource pwrSrc = new PowerHandler( cell );
			if (player.openContainer instanceof ContainerMEPortableCell){
				IPortableCell pCell = ( (ContainerMEPortableCell) player.openContainer ).getPortableCell();
				if (pCell instanceof PortableCellViewer && pCell.getItemStack().equals( cell )){
					cellInv = ( (ContainerMEPortableCell) player.openContainer ).getCellInventory();
					pwrSrc = pCell;
				}
			}
			if (cellInv != null && cellInv.getChannel() == StorageChannel.ITEMS){
				IItemList<IAEItemStack> contents = cellInv.getAvailableItems( StorageChannel.ITEMS.createList() );
				if (contents.findPrecise( AEItemStack.create(item) ) != null){
					IAEItemStack reject = Platform.poweredInsert(pwrSrc, cellInv, AEItemStack.create(item), new BaseActionSource( ) );
					if (reject != null){
						item.setCount( (int)reject.getStackSize() );
					} else {
						item.setCount( 0 );
					}
				} else if (!playerRejected.isEmpty()){
					IAEItemStack reject = Platform.poweredInsert(pwrSrc, cellInv, AEItemStack.create(playerRejected), new BaseActionSource( ) );
					if (reject != null){
						item.shrink( playerRejected.getCount() - (int)reject.getStackSize() );
						playerRejected.setCount( (int)reject.getStackSize() );
					}
				}
			}
		}
	}

	private ItemStack getPlayerInvReject(IItemHandler playerInv, ItemStack is){
		ItemStack playerRejected = is.copy();
		for (int slot = 0; slot < playerInv.getSlots(); slot++)
		{
			playerRejected = playerInv.insertItem(slot, is, true);
			if (playerRejected.isEmpty()){
				return ItemStack.EMPTY;
			}
		}
		return playerRejected;
	}

	@SubscribeEvent
	public void pickupEvent(EntityItemPickupEvent ev ){
		if (ev.getEntityPlayer() != null){
			IItemHandler playerInv = ev.getEntityPlayer().getCapability( CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP );
			ItemStack isCollided = ev.getItem().getItem();
			int originalCount = isCollided.getCount();
			ItemStack playerRejected = getPlayerInvReject( playerInv, isCollided );
			if (Capabilities.CAPABILITY_BAUBLES != null){
				IBaublesItemHandler handler = ev.getEntityPlayer().getCapability( Capabilities.CAPABILITY_BAUBLES, null );
				if (handler != null){
					for (int slot : BaubleType.AMULET.getValidSlots()){
						if (handler.getStackInSlot( slot ).getItem() == this){
							checkAddToCell( handler.getStackInSlot( slot ), isCollided, playerRejected, ev.getEntityPlayer() );
						}
					}
				}
			}
			if (!isCollided.isEmpty()){
				for (int slot = 0; slot < playerInv.getSlots(); slot++){
					if (playerInv.getStackInSlot( slot ).getItem() == this){
						checkAddToCell( playerInv.getStackInSlot( slot ), isCollided, playerRejected, ev.getEntityPlayer() );
						if (isCollided.isEmpty()){
							break;
						}
					}
				}
			}
			if (isCollided.isEmpty()){
				ev.setCanceled( true );
				ev.setResult( Event.Result.DENY );
				//ev.getItem().setDead();
				ev.getItem().setInfinitePickupDelay();
				ev.getEntityPlayer().onItemPickup(ev.getItem(), originalCount);
			}
		}
	}

	@Override
	public ICapabilityProvider initCapabilities( ItemStack stack, NBTTagCompound nbt )
	{
		ICapabilityProvider parent = super.initCapabilities( stack, nbt );

		return Capabilities.CAPABILITY_ITEM_BAUBLE != null ? new BaubleHandler(parent, BaubleType.AMULET) : parent;
	}

	private static class PowerHandler implements IEnergySource{
		private final ItemStack target;
		private final IAEItemPowerStorage ips;

		public PowerHandler( final ItemStack is )
		{
			this.ips = (IAEItemPowerStorage) is.getItem();
			this.target = is;
		}

		@Override
		public double extractAEPower( double amt, final Actionable mode, final PowerMultiplier usePowerMultiplier )
		{
			amt = usePowerMultiplier.multiply( amt );

			if( mode == Actionable.SIMULATE )
			{
				return usePowerMultiplier.divide( Math.min( amt, this.ips.getAECurrentPower( this.target ) ) );
			}

			return usePowerMultiplier.divide( this.ips.extractAEPower( this.target, amt ) );
		}
	}
}
