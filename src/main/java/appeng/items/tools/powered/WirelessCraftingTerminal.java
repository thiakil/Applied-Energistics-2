package appeng.items.tools.powered;


import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
