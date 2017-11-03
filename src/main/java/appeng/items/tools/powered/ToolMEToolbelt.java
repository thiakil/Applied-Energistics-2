package appeng.items.tools.powered;


import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import appeng.api.config.PowerMultiplier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
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
	private static final int HOTBAR_SIZE = InventoryPlayer.getHotbarSize();//unlikely this will ever change, but clearer this way
	private static final int HOTBAR_SLOT_OFFSET = 0;//offset of where hotbar slots start in InventoryPlayer

	public ToolMEToolbelt(){
		super( AEConfig.instance().getToolbeltBattery() );
	}

	public static void cycleHotbars(EntityPlayer player, ItemStack belt){
		if (!(belt.getItem() instanceof ToolMEToolbelt)){
			return;
		}
		ToolMEToolbelt beltItem = (ToolMEToolbelt)belt.getItem();
		NBTTagCompound tag = Platform.openNbtData( belt );
		Deque<NonNullList<ItemStack>> hotbars = new LinkedList<>();
		for (int i = 0; i<NUM_BARS_STORED; i++){
			NonNullList<ItemStack> bar = NonNullList.withSize(HOTBAR_SIZE, ItemStack.EMPTY);
			if (tag.hasKey( BAR_LIST_KEY+i ))
			{
				ItemStackHelper.loadAllItems(tag.getCompoundTag( BAR_LIST_KEY+i ), bar);
			}
			hotbars.addLast( bar );
		}
		int totalItems = 0;
		for (int i = 0; i<HOTBAR_SIZE; i++){
			totalItems += player.inventory.getStackInSlot( i+HOTBAR_SLOT_OFFSET ).getCount();
		}
		for (ItemStack is : hotbars.peekFirst()){
			totalItems += is.getCount();
		}
		if (beltItem.getAECurrentPower(belt) < PowerMultiplier.CONFIG.multiply(totalItems)){
			player.sendStatusMessage(new TextComponentTranslation("chat.appliedenergistics2.DeviceNotPowered"), false);
			return;
		}
		NonNullList<ItemStack> current = NonNullList.withSize( HOTBAR_SIZE, ItemStack.EMPTY);
		for (int i = 0; i<HOTBAR_SIZE; i++){
			current.set(i, player.inventory.getStackInSlot( i+HOTBAR_SLOT_OFFSET ));
		}
		hotbars.addLast( current );
		current = hotbars.removeFirst();
		for (int i = 0; i<HOTBAR_SIZE; i++){
			player.inventory.setInventorySlotContents(i+HOTBAR_SLOT_OFFSET , current.get(i));
		}
		Iterator<NonNullList<ItemStack>> it = hotbars.iterator();
		for (int i = 0; i<NUM_BARS_STORED; i++){
			NBTTagCompound subtag = new NBTTagCompound();
			ItemStackHelper.saveAllItems( subtag, it.next() );
			tag.setTag( BAR_LIST_KEY+i, subtag );
		}
		beltItem.extractAEPower(belt, PowerMultiplier.CONFIG.multiply(totalItems));
	}

	@Override
	public ICapabilityProvider initCapabilities( ItemStack stack, NBTTagCompound nbt )
	{
		ICapabilityProvider parent = super.initCapabilities( stack, nbt );

		return Capabilities.CAPABILITY_ITEM_BAUBLE != null ? new BaubleHandler(parent, BaubleType.BELT) : parent;
	}
}
