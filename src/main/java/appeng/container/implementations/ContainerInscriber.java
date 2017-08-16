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

package appeng.container.implementations;


import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.definitions.IItemDefinition;
import appeng.api.features.IInscriberRecipe;
import appeng.container.guisync.GuiSync;
import appeng.container.interfaces.IProgressProvider;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.misc.TileInscriber;
import appeng.util.Platform;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class ContainerInscriber extends ContainerUpgradeable implements IProgressProvider
{

	private final TileInscriber ti;

	private final Slot top;
	private final Slot middle;
	private final Slot bottom;

	@GuiSync( 2 )
	public int maxProcessingTime = -1;

	@GuiSync( 3 )
	public int processingTime = -1;

	public ContainerInscriber( final InventoryPlayer ip, final TileInscriber te )
	{
		super( ip, te );
		this.ti = te;

		this.addSlotToContainer( this.top = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.INSCRIBER_PLATE, this.ti, 0, 45, 16, this.getInventoryPlayer() ) );
		this.addSlotToContainer( this.bottom = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.INSCRIBER_PLATE, this.ti, 1, 45, 62, this.getInventoryPlayer() ) );
		this.addSlotToContainer( this.middle = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.INSCRIBER_INPUT, this.ti, 2, 63, 39, this.getInventoryPlayer() ) );

		this.addSlotToContainer( new SlotOutput( this.ti, 3, 113, 40, -1 ) );
	}

	@Override
	protected int getHeight()
	{
		return 176;
	}

	@Override
	/**
	 * Overridden super.setupConfig to prevent setting up the fake slots
	 */protected void setupConfig()
	{
		this.setupUpgrades();
	}

	@Override
	protected boolean supportCapacity()
	{
		return false;
	}

	@Override
	public int availableUpgrades()
	{
		return 5;
	}

	@Override
	protected int getToolboxY()
	{
		return 22 + this.availableUpgrades() * 18 ;
	}

	@Override
	public void detectAndSendChanges()
	{
		this.standardDetectAndSendChanges();

		if( Platform.isServer() )
		{
			this.maxProcessingTime = this.ti.getMaxProcessingTime();
			this.processingTime = this.ti.getProcessingTime();
		}
	}

	@Override
	public boolean isValidForSlot( final Slot s, final ItemStack is )
	{
		if (s.inventory == this.ti)
		{
			return this.ti.isItemValidForSlot( s.getSlotIndex(), is );
		}
		return super.isValidForSlot( s, is );
	}

	@Override
	public int getCurrentProgress()
	{
		return this.processingTime;
	}

	@Override
	public int getMaxProgress()
	{
		return this.maxProcessingTime;
	}
}
