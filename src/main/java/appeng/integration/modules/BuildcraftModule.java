package appeng.integration.modules;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;

import buildcraft.api.tools.IToolWrench;

import appeng.integration.IntegrationHelper;
import appeng.integration.abstraction.IBuildcraft;


/**
 * Created by Thiakil on 4/07/2017.
 */
public class BuildcraftModule implements IBuildcraft
{

	public BuildcraftModule(){
		IntegrationHelper.testClassExistence( this, buildcraft.api.tools.IToolWrench.class);
	}

	@Override
	public boolean canWrench( EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace )
	{
		return wrench.getItem() instanceof IToolWrench && ( (IToolWrench) wrench.getItem() ).canWrench( player, hand, wrench, rayTrace );
	}

	@Override
	public void wrenchUsed( EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace )
	{
		if ( wrench.getItem() instanceof IToolWrench )
		{
			( (IToolWrench) wrench.getItem() ).wrenchUsed( player, hand, wrench, rayTrace );
		}
	}
}
