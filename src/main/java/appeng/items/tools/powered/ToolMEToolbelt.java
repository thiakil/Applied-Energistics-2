package appeng.items.tools.powered;


import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import baubles.api.BaubleType;

import appeng.capabilities.Capabilities;
import appeng.core.AEConfig;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.Platform;


/**
 * Created by Thiakil on 18/10/2017.
 */
public class ToolMEToolbelt extends AEBasePoweredItem
{
	private static final String BAR_LIST_KEY = "hotbar#";
	private static final int NUM_BARS_STORED = 3;

	public ToolMEToolbelt(){
		super( AEConfig.instance().getPortableCellBattery() );
	}

	public static void cycleHotbars(EntityPlayer player, ItemStack belt){
		NBTTagCompound tag = Platform.openNbtData( belt );
		Deque<NonNullList<ItemStack>> hotbars = new LinkedList<>();
		for (int i = 0; i<NUM_BARS_STORED; i++){
			NonNullList<ItemStack> bar = NonNullList.withSize(InventoryPlayer.getHotbarSize(), ItemStack.EMPTY);
			if (tag.hasKey( BAR_LIST_KEY+i ))
			{
				ItemStackHelper.loadAllItems(tag.getCompoundTag( BAR_LIST_KEY+i ), bar);
			}
			hotbars.addLast( bar );
		}
		NonNullList<ItemStack> current = NonNullList.withSize( InventoryPlayer.getHotbarSize(), ItemStack.EMPTY);
		for (int i = 0; i<InventoryPlayer.getHotbarSize(); i++){
			current.set(i, player.inventory.getStackInSlot( i ));
		}
		hotbars.addLast( current );
		current = hotbars.removeFirst();
		for (int i = 0; i<InventoryPlayer.getHotbarSize(); i++){
			player.inventory.setInventorySlotContents(i , current.get(i));
		}
		Iterator<NonNullList<ItemStack>> it = hotbars.iterator();
		for (int i = 0; i<NUM_BARS_STORED; i++){
			NBTTagCompound subtag = new NBTTagCompound();
			ItemStackHelper.saveAllItems( subtag, it.next() );
			tag.setTag( BAR_LIST_KEY+i, subtag );
		}
	}

	@Override
	public ICapabilityProvider initCapabilities( ItemStack stack, NBTTagCompound nbt )
	{
		ICapabilityProvider parent = super.initCapabilities( stack, nbt );

		return Capabilities.CAPABILITY_ITEM_BAUBLE != null ? new BaubleHandler(parent, BaubleType.BELT) : parent;
	}
}
