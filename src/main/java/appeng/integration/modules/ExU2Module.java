package appeng.integration.modules;


import com.rwtema.extrautils2.api.machine.XUMachineCrusher;

import net.minecraft.item.ItemStack;

import appeng.integration.IntegrationHelper;
import appeng.integration.abstraction.IExU2;


/**
 * Created by Thiakil on 17/12/2017.
 */
public class ExU2Module implements IExU2
{
	public ExU2Module(){
		IntegrationHelper.testClassExistence( this, XUMachineCrusher.class );
	}

	@Override
	public void addCrusherRecipe( ItemStack in, ItemStack out )
	{
		XUMachineCrusher.addRecipe( in, out );
	}
}
