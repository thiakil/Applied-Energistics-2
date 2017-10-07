package appeng.helpers;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;


/**
 * Created by Thiakil on 6/10/2017.
 */
public class WirelessCraftingTerminalGuiObject extends WirelessTerminalGuiObject implements ISegmentedInventory, IAEAppEngInventory
{
	private static final String CRAFTING_KEY = "crafting_inv";
	private final AppEngInternalInventory craftingGrid = new AppEngInternalInventory( this, 9 );

	private ItemStack is;

	public WirelessCraftingTerminalGuiObject( IWirelessTermHandler wh, ItemStack is, EntityPlayer ep, World w, int slot )
	{
		super( wh, is, ep, w, slot );
		this.is = is;
		if (is.getTagCompound() == null){
			is.setTagCompound( new NBTTagCompound() );
		} else {
			NBTTagCompound tag = is.getTagCompound();
			if (tag.hasKey( CRAFTING_KEY )){
				craftingGrid.readFromNBT( tag, CRAFTING_KEY );
			}
		}
	}

	@Override
	public IInventory getInventoryByName( String name )
	{
		return name.equals( "crafting" ) ? craftingGrid : null;
	}

	@Override
	public void saveChanges()
	{

	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack )
	{
		craftingGrid.writeToNBT( is.getTagCompound(), CRAFTING_KEY );
	}
}
