package appeng.integration.modules;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import cofh.api.item.IToolHammer;

import appeng.integration.IntegrationHelper;
import appeng.integration.abstraction.ICofhHammer;


/**
 * Created by Thiakil on 4/07/2017.
 */
public class CofhHammerModule implements ICofhHammer
{

	public CofhHammerModule(){
		IntegrationHelper.testClassExistence( this, IToolHammer.class);
	}

	@Override
	public boolean isUsable( ItemStack var1, EntityLivingBase var2, BlockPos var3 )
	{
		return var1.getItem() instanceof IToolHammer && ( (IToolHammer) var1.getItem() ).isUsable( var1, var2, var3 );
	}

	@Override
	public boolean isUsable( ItemStack var1, EntityLivingBase var2, Entity var3 )
	{
		return var1.getItem() instanceof IToolHammer && ( (IToolHammer) var1.getItem() ).isUsable( var1, var2, var3 );
	}

	@Override
	public void toolUsed( ItemStack var1, EntityLivingBase var2, BlockPos var3 )
	{
		if ( var1.getItem() instanceof IToolHammer )
		{
			( (IToolHammer) var1.getItem() ).toolUsed( var1, var2, var3 );
		}
	}

	@Override
	public void toolUsed( ItemStack var1, EntityLivingBase var2, Entity var3 )
	{
		if ( var1.getItem() instanceof IToolHammer )
		{
			( (IToolHammer) var1.getItem() ).toolUsed( var1, var2, var3 );
		}
	}
}
