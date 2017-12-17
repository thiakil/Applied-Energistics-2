package appeng.integration.modules;


import com.rwtema.extrautils2.api.machine.XUMachineCrusher;

import net.minecraft.item.ItemStack;

import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;

import appeng.integration.IntegrationHelper;
import appeng.integration.abstraction.IActuallyAdditions;


/**
 * Created by Thiakil on 17/12/2017.
 */
public class ActuallyAdditionsModule implements IActuallyAdditions
{
	public ActuallyAdditionsModule(){
		IntegrationHelper.testClassExistence( this, ActuallyAdditionsAPI.class );
	}

	@Override
	public void addCrusherRecipe( ItemStack in, ItemStack out )
	{
		ActuallyAdditionsAPI.addCrusherRecipe( in, out, ItemStack.EMPTY, 0 );
	}
}
