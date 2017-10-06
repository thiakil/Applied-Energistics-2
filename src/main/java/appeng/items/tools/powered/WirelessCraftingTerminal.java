package appeng.items.tools.powered;


import net.minecraft.item.ItemStack;

import appeng.api.AEApi;

/**
 * Created by Thiakil on 6/10/2017.
 */

public class WirelessCraftingTerminal extends ToolWirelessTerminal {
	@Override
	public boolean canHandle( ItemStack is )
	{
		return AEApi.instance().definitions().items().wirelessCraftingTerminal().isSameAs( is );
	}
}
