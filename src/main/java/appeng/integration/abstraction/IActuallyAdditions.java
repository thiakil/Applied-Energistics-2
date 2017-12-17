package appeng.integration.abstraction;


import net.minecraft.item.ItemStack;

import appeng.integration.IIntegrationModule;


/**
 * Created by Thiakil on 17/12/2017.
 */
public interface IActuallyAdditions extends IIntegrationModule
{
	default void addCrusherRecipe( ItemStack in, ItemStack out )
	{
	}

	class Stub extends IIntegrationModule.Stub implements IActuallyAdditions
	{
	}
}
