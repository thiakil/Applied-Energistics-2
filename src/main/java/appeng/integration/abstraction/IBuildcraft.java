package appeng.integration.abstraction;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;

import appeng.integration.IIntegrationModule;


/**
 * Created by Thiakil on 4/07/2017.
 */
public interface IBuildcraft extends IIntegrationModule
{

	default boolean canWrench( EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace )
	{
		return false;
	}

	default void wrenchUsed( EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace ){}

	class Stub extends IIntegrationModule.Stub implements IBuildcraft
	{

	}
}
