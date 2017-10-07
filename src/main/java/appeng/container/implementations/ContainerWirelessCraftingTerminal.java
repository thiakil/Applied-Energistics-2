package appeng.container.implementations;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.capabilities.Capabilities;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.util.Platform;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import baubles.api.cap.IBaublesItemHandler;


/**
 * Created by Thiakil on 6/10/2017.
 *
 * An amalgamation of the portable cell container and the wireless terminal's, in order to extend crafting term
 *
 */

public class ContainerWirelessCraftingTerminal extends ContainerCraftingTerm {
	private final WirelessTerminalGuiObject wirelessTerminalGUIObject;

	public ContainerWirelessCraftingTerminal( final InventoryPlayer ip, final WirelessTerminalGuiObject gui )
	{
		super( ip, gui, false );
		this.wirelessTerminalGUIObject = gui;

		//from MEPortableCell
		if( gui instanceof IInventorySlotAware)
		{
			final int slotIndex = ( (IInventorySlotAware) gui ).getInventorySlot();
			this.lockPlayerInventorySlot( slotIndex );
			this.slot = slotIndex;
		}
		else
		{
			this.slot = -1;
			this.lockPlayerInventorySlot( ip.currentItem );
		}
		this.bindPlayerInventory( ip, 0, 0 );
		//end from MEPortableCell
	}

	private ItemStack getTerminalFromSlot(int slot){
		if (slot < 0){
			return this.getPlayerInv().getCurrentItem();
		}
		if (slot < this.getPlayerInv().getSizeInventory())
		{
			return this.getPlayerInv().getStackInSlot( slot );
		}
		if ( Capabilities.CAPABILITY_BAUBLES != null){
			IBaublesItemHandler handler = this.getPlayerInv().player.getCapability( Capabilities.CAPABILITY_BAUBLES, null );
			if (handler != null)
			{
				return handler.getStackInSlot( slot - this.getPlayerInv().getSizeInventory() );
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void detectAndSendChanges()
	{
		//from portable cell
		final ItemStack currentItem = getTerminalFromSlot( this.slot );

		if( this.wirelessTerminalGUIObject != null )
		{
			if( currentItem != this.wirelessTerminalGUIObject.getItemStack() )
			{
				if (!currentItem.isEmpty()) {
					if (Platform.itemComparisons().isEqualItem(this.wirelessTerminalGUIObject.getItemStack(), currentItem)) {
						this.getPlayerInv().setInventorySlotContents(this.getPlayerInv().currentItem, this.wirelessTerminalGUIObject.getItemStack());
					} else {
						this.setValidContainer(false);
					}
				} else {
					this.setValidContainer(false);
				}
			}
		}
		else
		{
			this.setValidContainer( false );
			return;
		}

		// drain 1 ae t
		this.ticks++;
		if( this.ticks > 10 )
		{
			this.wirelessTerminalGUIObject.extractAEPower( this.getPowerMultiplier() * this.ticks, Actionable.MODULATE, PowerMultiplier.CONFIG );
			this.ticks = 0;
		}
		super.detectAndSendChanges();
		//end from pirtable cell

		if( !this.wirelessTerminalGUIObject.rangeCheck() )
		{
			if( Platform.isServer() && this.isValidContainer() )
			{
				this.getPlayerInv().player.sendMessage( PlayerMessages.OutOfRange.get() );
			}

			this.setValidContainer( false );
		}
		else
		{
			this.setPowerMultiplier( AEConfig.instance().wireless_getDrainRate( this.wirelessTerminalGUIObject.getRange() ) );
		}
	}

	//from ContainerMEPortableCell
	private double powerMultiplier = 0.5;
	private int ticks = 0;
	private final int slot;

	private double getPowerMultiplier()
	{
		return this.powerMultiplier;
	}

	void setPowerMultiplier( final double powerMultiplier )
	{
		this.powerMultiplier = powerMultiplier;
	}
}
