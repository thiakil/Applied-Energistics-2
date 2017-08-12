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


import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Upgrades;
import appeng.api.definitions.IComparableDefinition;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.ITileDefinition;
import appeng.api.features.IInscriberRecipe;
import appeng.api.features.IInscriberRecipeBuilder;
import appeng.api.features.InscriberProcessType;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.core.settings.TickRates;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.parts.automation.DefinitionUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkPowerTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.WrapperInventoryRange;
import appeng.util.item.AEItemStack;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class TileInscriber extends AENetworkPowerTile implements IGridTickable, IUpgradeableHost, IConfigManagerHost
{

	private static final int SLOT_TOP = 0;
	private static final int SLOT_BOTTOM = 1;
	private static final int SLOT_MIDDLE = 2;
	private static final int SLOT_OUT = 3;

	private final int maxProcessingTime = 100;
	private final int[] allSlots = { SLOT_TOP, SLOT_BOTTOM, SLOT_MIDDLE, SLOT_OUT };
	private final AppEngInternalInventory inv = new AppEngInternalInventory( this, 4 );
	private final IConfigManager settings;
	private final UpgradeInventory upgrades;
	private int processingTime = 0;
	// cycles from 0 - 16, at 8 it preforms the action, at 16 it re-enables the normal routine.
	private boolean smash;
	private int finalStep;
	private long clientStart;

	private final IItemHandler itemHandler;

	@Reflected
	public TileInscriber()
	{
		this.getProxy().setValidSides( EnumSet.noneOf( EnumFacing.class ) );
		this.setInternalMaxPower( 1500 );
		this.getProxy().setIdlePowerUsage( 0 );
		this.settings = new ConfigManager( this );

		final ITileDefinition inscriberDefinition = AEApi.instance().definitions().blocks().inscriber();
		this.upgrades = new DefinitionUpgradeInventory( inscriberDefinition, this, this.getUpgradeSlots() );

		itemHandler = new InscriberItemHandler();
	}

	private int getUpgradeSlots()
	{
		return 5;
	}

	@Override
	public AECableType getCableConnectionType( final AEPartLocation dir )
	{
		return AECableType.COVERED;
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileInscriber( final NBTTagCompound data )
	{
		this.inv.writeToNBT( data, "inscriberInv" );
		this.upgrades.writeToNBT( data, "upgrades" );
		this.settings.writeToNBT( data );
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileInscriber( final NBTTagCompound data )
	{
		this.inv.readFromNBT( data, "inscriberInv" );
		this.upgrades.readFromNBT( data, "upgrades" );
		this.settings.readFromNBT( data );
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TileInscriber( final ByteBuf data ) throws IOException
	{
		final int slot = data.readByte();

		final boolean oldSmash = this.isSmash();
		final boolean newSmash = ( slot & 64 ) == 64;

		if( oldSmash != newSmash && newSmash )
		{
			this.setSmash( true );
			this.setClientStart( System.currentTimeMillis() );
		}

		for( int num = 0; num < this.inv.getSizeInventory(); num++ )
		{
			if( ( slot & ( 1 << num ) ) > 0 )
			{
				this.inv.setInventorySlotContents( num, AEItemStack.loadItemStackFromPacket( data ).getItemStack() );
			}
			else
			{
				this.inv.setInventorySlotContents(num, ItemStack.EMPTY);
			}
		}

		return false;
	}

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TileInscriber( final ByteBuf data ) throws IOException
	{
		int slot = this.isSmash() ? 64 : 0;

		for( int num = 0; num < this.inv.getSizeInventory(); num++ )
		{
			if (!this.inv.getStackInSlot(num).isEmpty()) {
				slot |= (1 << num);
			}
		}

		data.writeByte( slot );
		for( int num = 0; num < this.inv.getSizeInventory(); num++ )
		{
			if( ( slot & ( 1 << num ) ) > 0 )
			{
				final AEItemStack st = AEItemStack.create( this.inv.getStackInSlot( num ) );
				st.writeToPacket( data );
			}
		}
	}

	@Override
	public void setOrientation( final EnumFacing inForward, final EnumFacing inUp )
	{
		super.setOrientation( inForward, inUp );
		this.getProxy().setValidSides( EnumSet.complementOf( EnumSet.of( this.getForward() ) ) );
		this.setPowerSides( EnumSet.complementOf( EnumSet.of( this.getForward() ) ) );
	}

	@Override
	public void getDrops( final World w, final BlockPos pos, final List<ItemStack> drops )
	{
		super.getDrops( w, pos, drops );

		for( int h = 0; h < this.upgrades.getSizeInventory(); h++ )
		{
			final ItemStack is = this.upgrades.getStackInSlot( h );
			if (!is.isEmpty()) {
				drops.add(is);
			}
		}
	}

	@Override
	public boolean requiresTESR()
	{
		return true;
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.inv;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{
		if( this.isSmash() || i == SLOT_OUT )
		{
			return false;
		}

		final ItemStack top = this.getStackInSlot( 0 );
		final ItemStack bot = this.getStackInSlot( 1 );

		if( i == SLOT_MIDDLE )
		{
			for( final ItemStack optional : AEApi.instance().registries().inscriber().getOptionals() )
			{
				if( Platform.itemComparisons().isSameItem( optional, itemstack ) )
				{
					return false;
				}
			}

			boolean matches = false;
			boolean found = false;

			for( final IInscriberRecipe recipe : AEApi.instance().registries().inscriber().getRecipes() )
			{
				final boolean matchA = (top.isEmpty() && !recipe.getTopOptional().isPresent() ) || ( Platform.itemComparisons().isSameItem( top, recipe.getTopOptional().orElse(ItemStack.EMPTY) ) ) && // and...
						(bot.isEmpty() && !recipe.getBottomOptional().isPresent() ) | ( Platform.itemComparisons().isSameItem( bot, recipe.getBottomOptional().orElse(ItemStack.EMPTY) ) );

				final boolean matchB = (bot.isEmpty() && !recipe.getTopOptional().isPresent() ) || ( Platform.itemComparisons().isSameItem( bot, recipe.getTopOptional().orElse(ItemStack.EMPTY) ) ) && // and...
						(top.isEmpty() && !recipe.getBottomOptional().isPresent() ) | ( Platform.itemComparisons().isSameItem( top, recipe.getBottomOptional().orElse(ItemStack.EMPTY) ) );

				if( matchA || matchB )
				{
					matches = true;
					for( final ItemStack option : recipe.getInputs() )
					{
						if( Platform.itemComparisons().isSameItem( itemstack, option ) )
						{
							found = true;
						}
					}
				}
			}

			if( matches && !found )
			{
				return false;
			}
		}

		if( (i == SLOT_TOP && !bot.isEmpty() ) || (i == SLOT_BOTTOM && !top.isEmpty() ) )
		{
			ItemStack otherSlot = getStackInSlot( i == SLOT_TOP ? SLOT_BOTTOM : SLOT_TOP );

			// name presses
			final IItemDefinition namePress = AEApi.instance().definitions().materials().namePress();
			if( namePress.isSameAs( otherSlot ) )
			{
				return namePress.isSameAs( itemstack );
			}

			// everything else
			boolean isValid = false;
			for( final IInscriberRecipe recipe : AEApi.instance().registries().inscriber().getRecipes() )
			{
				if( Platform.itemComparisons().isSameItem( recipe.getTopOptional().orElse(ItemStack.EMPTY), otherSlot ) )
				{
					isValid = Platform.itemComparisons().isSameItem( itemstack, recipe.getBottomOptional().orElse(ItemStack.EMPTY) );
				}
				else if( Platform.itemComparisons().isSameItem( recipe.getBottomOptional().orElse(ItemStack.EMPTY), otherSlot ) )
				{
					isValid = Platform.itemComparisons().isSameItem( itemstack, recipe.getTopOptional().orElse(ItemStack.EMPTY) );
				}

				if( isValid )
				{
					break;
				}
			}

			if( !isValid )
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean canInsertItem( int slotIndex, ItemStack insertingItem, EnumFacing side )
	{
		return super.canInsertItem( slotIndex, insertingItem, side ) && getStackInSlot( slotIndex ).isEmpty() || getStackInSlot( slotIndex ).isItemEqual( insertingItem );
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{
		try
		{
			if( mc != InvOperation.markDirty )
			{
				ItemStack inTheSlot = inv.getStackInSlot(slot);
				if( slot != SLOT_OUT && // if it's not the output slot, reset the status if the slot is not empty, or items area not equal to what's already there
						(
							inTheSlot.isEmpty() ||
							(!removed.isEmpty() && !removed.isItemEqual(inTheSlot) && !ItemStack.areItemStackTagsEqual(inTheSlot, removed) ) ||
							(!added.isEmpty() && !added.isItemEqual(inTheSlot) && !ItemStack.areItemStackTagsEqual(inTheSlot, added) )
						)
				)
				{
					this.setProcessingTime( 0 );
				}

				if( !this.isSmash() )
				{
					this.markForUpdate();
				}

				this.getProxy().getTick().wakeDevice( this.getProxy().getNode() );
			}
		}
		catch( final GridAccessException e )
		{
			// :P
		}
	}

	@Override
	public boolean canExtractItem( final int slotIndex, final ItemStack extractedItem, final EnumFacing side )
	{
		if( this.isSmash() )
		{
			return false;
		}

		return slotIndex == SLOT_TOP || slotIndex == SLOT_BOTTOM || slotIndex == SLOT_OUT;
	}

	@Override
	public int[] getAccessibleSlotsBySide( final EnumFacing d )
	{
		return this.allSlots;
	}

	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		return new TickingRequest( TickRates.Inscriber.getMin(), TickRates.Inscriber.getMax(), !this.hasWork(), false );
	}

	private boolean hasWork()
	{
		if( this.getTask() != null )
		{
			return true;
		}

		this.setProcessingTime( 0 );
		return this.isSmash();
	}

	@Nullable
	public IInscriberRecipe getTask()
	{
		final ItemStack plateA = this.getStackInSlot( SLOT_TOP );
		final ItemStack plateB = this.getStackInSlot( SLOT_BOTTOM );
		ItemStack renamedItem = this.getStackInSlot( SLOT_MIDDLE );



		if(renamedItem.isEmpty() /*&& renamedItem.getCount() > 1 */)
		{
			return null;
		}

		final IComparableDefinition namePress = AEApi.instance().definitions().materials().namePress();
		final boolean isNameA = namePress.isSameAs( plateA );
		final boolean isNameB = namePress.isSameAs( plateB );

		if( ( isNameA || isNameB ) && (isNameA || plateA.isEmpty() ) && (isNameB || plateB.isEmpty() ) )
		{
			if (!renamedItem.isEmpty()) {
				String name = "";

				if (!plateA.isEmpty()) {
					final NBTTagCompound tag = Platform.openNbtData(plateA);
					name += tag.getString("InscribeName");
				}

				if (!plateB.isEmpty()) {
					final NBTTagCompound tag = Platform.openNbtData(plateB);
					if (name.length() > 0) {
						name += " ";
					}
					name += tag.getString("InscribeName");
				}

				final ItemStack startingItem = renamedItem.copy();
				renamedItem = renamedItem.copy();
				final NBTTagCompound tag = Platform.openNbtData(renamedItem);

				final NBTTagCompound display = tag.getCompoundTag("display");
				tag.setTag("display", display);

				if (name.length() > 0) {
					display.setString("Name", name);
				} else {
					display.removeTag("Name");
				}

				final List<ItemStack> inputs = Lists.newArrayList(startingItem);
				final InscriberProcessType type = InscriberProcessType.INSCRIBE;

				final IInscriberRecipeBuilder builder = AEApi.instance().registries().inscriber().builder();
				return builder.withInputs(inputs)
						.withOutput(renamedItem)
						.withTopOptional(plateA)
						.withBottomOptional(plateB)
						.withProcessType(type)
						.build();
			}
		}

		for( final IInscriberRecipe recipe : AEApi.instance().registries().inscriber().getRecipes() )
		{

			final boolean matchA = (plateA.isEmpty() && !recipe.getTopOptional().isPresent() ) || ( Platform.itemComparisons().isSameItem( plateA,
					recipe.getTopOptional().orElse(ItemStack.EMPTY) ) ) && // and...
					(plateB.isEmpty() && !recipe.getBottomOptional().isPresent() ) | ( Platform.itemComparisons().isSameItem( plateB,
							recipe.getBottomOptional().orElse(ItemStack.EMPTY) ) );

			final boolean matchB = (plateB.isEmpty() && !recipe.getTopOptional().isPresent() ) || ( Platform.itemComparisons().isSameItem( plateB,
					recipe.getTopOptional().orElse(ItemStack.EMPTY) ) ) && // and...
					(plateA.isEmpty() && !recipe.getBottomOptional().isPresent() ) | ( Platform.itemComparisons().isSameItem( plateA,
							recipe.getBottomOptional().orElse(ItemStack.EMPTY) ) );

			if( matchA || matchB )
			{
				ItemStack middleSlot = this.getStackInSlot( SLOT_MIDDLE );
				for( final ItemStack option : recipe.getInputs() )
				{
					if( Platform.itemComparisons().isSameItem( option, middleSlot ) )
					{
						return recipe;
					}
				}
			}
		}
		return null;
	}

	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int ticksSinceLastCall )
	{
		if( this.isSmash() )
		{
			this.finalStep++;
			if( this.finalStep == 8 )
			{
				final IInscriberRecipe out = this.getTask();
				if( out != null )
				{
					final ItemStack outputCopy = out.getOutput().copy();
					final InventoryAdaptor ad = InventoryAdaptor.getAdaptor( new WrapperInventoryRange( this.inv, SLOT_OUT, 1, true ), EnumFacing.UP );

					if (ad.addItems(outputCopy).isEmpty()) {
						this.setProcessingTime(0);
						if (out.getProcessType() == InscriberProcessType.PRESS) {
							ItemStack topStack = this.getStackInSlot(SLOT_TOP);
							topStack.shrink(1);
							this.setInventorySlotContents(SLOT_TOP, topStack);
							ItemStack bottomStack = this.getStackInSlot(SLOT_BOTTOM);
							bottomStack.shrink(1);
							this.setInventorySlotContents(SLOT_BOTTOM, bottomStack);
						}
						ItemStack middleStack = this.getStackInSlot(SLOT_MIDDLE);
						middleStack.shrink(1);
						this.setInventorySlotContents(SLOT_MIDDLE, middleStack);
					}
				}

				this.markDirty();
			}
			else if( this.finalStep == 16 )
			{
				this.finalStep = 0;
				this.setSmash( false );
				this.markForUpdate();
			}
		}
		else
		{
			try
			{
				final IEnergyGrid eg = this.getProxy().getEnergy();
				IEnergySource src = this;

				// Base 1, increase by 1 for each card
				final int speedFactor = 1 + this.upgrades.getInstalledUpgrades( Upgrades.SPEED );
				final int powerConsumption = 10 * speedFactor;
				final double powerThreshold = powerConsumption - 0.01;
				double powerReq = this.extractAEPower( powerConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG );

				if( powerReq <= powerThreshold )
				{
					src = eg;
					powerReq = eg.extractAEPower( powerConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG );
				}

				if( powerReq > powerThreshold )
				{
					src.extractAEPower( powerConsumption, Actionable.MODULATE, PowerMultiplier.CONFIG );

					if( this.getProcessingTime() == 0 )
					{
						this.setProcessingTime( this.getProcessingTime() + speedFactor );
					}
					else
					{
						this.setProcessingTime( this.getProcessingTime() + ticksSinceLastCall * speedFactor );
					}
				}
			}
			catch( final GridAccessException e )
			{
				// :P
			}

			if( this.getProcessingTime() > this.getMaxProcessingTime() )
			{
				this.setProcessingTime( this.getMaxProcessingTime() );
				final IInscriberRecipe out = this.getTask();
				if( out != null )
				{
					final ItemStack outputCopy = out.getOutput().copy();
					final InventoryAdaptor ad = InventoryAdaptor.getAdaptor( new WrapperInventoryRange( this.inv, SLOT_OUT, 1, true ), EnumFacing.UP );
					if (ad.simulateAdd(outputCopy).isEmpty()) {
						this.setSmash(true);
						this.finalStep = 0;
						this.markForUpdate();
					}
				}
			}
		}

		return this.hasWork() ? TickRateModulation.URGENT : TickRateModulation.SLEEP;
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.settings;
	}

	@Override
	public IInventory getInventoryByName( final String name )
	{
		if( name.equals( "inv" ) )
		{
			return this.inv;
		}

		if( name.equals( "upgrades" ) )
		{
			return this.upgrades;
		}

		return null;
	}

	@Override
	public boolean hasCapability( Capability<?> capability, EnumFacing facing )
	{
		if( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
		{
			return true;
		}

		return super.hasCapability( capability, facing );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public <T> T getCapability( Capability<T> capability, @Nullable EnumFacing facing )
	{
		if( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
		{
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast( itemHandler );
		}

		return super.getCapability( capability, facing );
	}

	@Override
	public int getInstalledUpgrades( final Upgrades u )
	{
		return this.upgrades.getInstalledUpgrades( u );
	}

	@Override
	public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
	{
	}

	public long getClientStart()
	{
		return this.clientStart;
	}

	private void setClientStart( final long clientStart )
	{
		this.clientStart = clientStart;
	}

	public boolean isSmash()
	{
		return this.smash;
	}

	public void setSmash( final boolean smash )
	{
		this.smash = smash;
	}

	public int getMaxProcessingTime()
	{
		return this.maxProcessingTime;
	}

	public int getProcessingTime()
	{
		return this.processingTime;
	}

	private void setProcessingTime( final int processingTime )
	{
		this.processingTime = processingTime;
	}

	private class InscriberItemHandler extends InvWrapper
	{
		InscriberItemHandler(){
			super(TileInscriber.this);
		}

		@Override
		public int getSlots()
		{
			return 4;
		}

		@Override
		public ItemStack insertItem( int slot, ItemStack stack, boolean simulate )
		{
			if( stack.isEmpty() || slot == SLOT_OUT )
			{
				return stack;
			}

			ItemStack currentStack = inv.getStackInSlot( slot );
			//empty slot, check if it's valid
			if( currentStack.isEmpty() )
			{
				if( !isItemValidForSlot( slot, stack ) )
				{
					return stack;
				}
			}
			else if( !currentStack.isItemEqual( stack ) || !ItemStack.areItemStackTagsEqual( currentStack, stack ) )
			{
				return stack;
			}

			return super.insertItem( slot, stack, simulate );
		}

		@Override
		public ItemStack extractItem( int slot, int amount, boolean simulate )
		{
			if( slot != SLOT_OUT || amount == 0 )
			{
				return ItemStack.EMPTY;
			}

			return super.extractItem( slot, amount, simulate );
		}

	/*	@Override
		public int getSlotLimit( int slot )
		{
			// TODO Auto-generated method stub
			return 64;
		}*/
	}

}
