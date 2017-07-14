package appeng.integration.modules.waila.tile;


import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

import appeng.helpers.ICustomNameObject;
import appeng.integration.modules.waila.BaseWailaDataProvider;


/**
 * Created by Thiakil on 14/07/2017.
 */
public class CustomNameProvider extends BaseWailaDataProvider
{
	@Override
	public List<String> getWailaHead( ItemStack itemStack, List<String> currentToolTip, IWailaDataAccessor accessor, IWailaConfigHandler config )
	{
		TileEntity te = accessor.getTileEntity();
		if (te instanceof ICustomNameObject && ( (ICustomNameObject) te ).hasAEDisplayName()){
			currentToolTip.add( TextFormatting.WHITE.toString()+TextFormatting.ITALIC.toString()+( (ICustomNameObject) te ).getAEDisplayName() );
		}
		return super.getWailaHead( itemStack, currentToolTip, accessor, config );
	}
}
