package appeng.container.implementations;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.util.Platform;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class ContainerWirelessCraftingTerminal extends ContainerCraftingTerm {
	private final WirelessTerminalGuiObject wirelessTerminalGUIObject;

	public ContainerWirelessCraftingTerminal( final InventoryPlayer ip, final WirelessTerminalGuiObject gui )
	{
		super( ip, gui );
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

	@Override
	public void detectAndSendChanges()
	{
		detectAndSendChanges_MEPortableCell();

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

	private void detectAndSendChanges_MEPortableCell()
	{
		final ItemStack currentItem = this.slot < 0 ? this.getPlayerInv().getCurrentItem() : this.getPlayerInv().getStackInSlot( this.slot );

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
