package appeng.integration.modules.waila.part;


import java.util.List;

import net.minecraft.util.text.TextFormatting;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

import appeng.api.parts.IPart;
import appeng.helpers.ICustomNameObject;


/**
 * Created by Thiakil on 14/07/2017.
 */
public class PartCustomNameProvider extends BasePartWailaDataProvider
{
	@Override
	public List<String> getWailaHead( IPart part, List<String> currentToolTip, IWailaDataAccessor accessor, IWailaConfigHandler config )
	{
		if (part instanceof ICustomNameObject && ( (ICustomNameObject) part ).hasAEDisplayName()){
			currentToolTip.add(TextFormatting.WHITE.toString()+TextFormatting.ITALIC.toString()+( (ICustomNameObject) part ).getAEDisplayName() );
		}
		return super.getWailaHead( part, currentToolTip, accessor, config );
	}
}
