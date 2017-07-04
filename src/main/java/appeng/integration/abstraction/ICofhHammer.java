package appeng.integration.abstraction;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import appeng.integration.IIntegrationModule;


/**
 * Created by Thiakil on 4/07/2017.
 */
public interface ICofhHammer extends IIntegrationModule
{
	default boolean isUsable(ItemStack var1, EntityLivingBase var2, BlockPos var3){ return false; }

	default boolean isUsable(ItemStack var1, EntityLivingBase var2, Entity var3){ return false; }

	default void toolUsed(ItemStack var1, EntityLivingBase var2, BlockPos var3){}

	default void toolUsed(ItemStack var1, EntityLivingBase var2, Entity var3){}

	class Stub extends IIntegrationModule.Stub implements ICofhHammer
	{

	}
}
