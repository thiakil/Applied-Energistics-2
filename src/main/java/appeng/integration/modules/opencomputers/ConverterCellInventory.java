package appeng.integration.modules.opencomputers;


import java.util.Map;

import net.minecraft.item.ItemStack;

import li.cil.oc.api.driver.Converter;

import appeng.api.AEApi;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;


public final class ConverterCellInventory implements Converter
{

	public static void convertCellInv(ICellInventory cell, final Map<Object, Object> output){
		output.put("storedItemTypes", cell.getStoredItemTypes());
		output.put("storedItemCount", cell.getStoredItemCount());
		output.put("remainingItemCount", cell.getRemainingItemCount());
		output.put("remainingItemTypes", cell.getRemainingItemTypes());

		output.put("getTotalItemTypes", cell.getTotalItemTypes());
		//output.put("getAvailableItems", cell.getAvailableItems(AEApi.instance().storage().createItemList()));

		output.put("totalBytes", cell.getTotalBytes());
		output.put("freeBytes", cell.getFreeBytes());
		output.put("usedBytes", cell.getUsedBytes());
		output.put("unusedItemCount", cell.getUnusedItemCount());
		output.put("canHoldNewItem", cell.canHoldNewItem());
		//output.put("getPreformattedItems",cell.getConfigInventory());

		output.put("fuzzyMode", cell.getFuzzyMode().toString());
		output.put("name", cell.getItemStack().getDisplayName());
	}

	@Override
	public void convert(final Object value, final Map<Object, Object> output) {
		if (value instanceof ICellInventory ) {
			final ICellInventory cell = (ICellInventory) value;
			convertCellInv(cell, output);
		} else if (value instanceof ICellInventoryHandler ) {
			convert(((ICellInventoryHandler) value).getCellInv(), output);
		} else if (value instanceof ItemStack ){
			IMEInventoryHandler handler = AEApi.instance().registries().cell().getCellInventory((ItemStack)value, null, StorageChannel.ITEMS );
			if (handler == null)
				handler = AEApi.instance().registries().cell().getCellInventory((ItemStack)value, null, StorageChannel.FLUIDS);
			if(handler != null && handler instanceof ICellInventoryHandler ){
				convert(handler, output);
			}
		}
	}
}