package appeng.util.inv;


import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;


/**
 * Created by Thiakil on 11/07/2017.
 * Provides an empty IItemHandler that does absolutely nothing.
 */
public class NullItemHandler implements IItemHandler
{

	@Override
	public int getSlots()
	{
		return 0;
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot( int slot )
	{
		return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public ItemStack insertItem( int slot, @Nonnull ItemStack stack, boolean simulate )
	{
		return stack;
	}

	@Nonnull
	@Override
	public ItemStack extractItem( int slot, int amount, boolean simulate )
	{
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit( int slot )
	{
		return 0;
	}
}
