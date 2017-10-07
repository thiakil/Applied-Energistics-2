package appeng.recipes.game;


import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.definitions.IItemDefinition;
import appeng.items.tools.powered.ToolWirelessTerminal;


/**
 * Created by Thiakil on 6/10/2017.
 */
public class WirelessTerminalUpgradeRecipe extends ShapelessRecipes{

	private final IItemDefinition termDef;

	public WirelessTerminalUpgradeRecipe(IItemDefinition termType){
		super(
				termType.maybeStack( 1 ).orElseThrow( RuntimeException::new ),
				Lists.newArrayList(
						withUpgrade(termType),
						AEApi.instance().definitions().materials().quantumDragonEgg()
							.maybeStack( 1 ).orElseThrow( RuntimeException::new )
				)
		);
		this.termDef = termType;
	}

	private static ItemStack withUpgrade(IItemDefinition t){
		ItemStack is = t.maybeStack( 1 ).orElseThrow( RuntimeException::new );
		ToolWirelessTerminal.setHasQuantumEgg( is, true );
		return is;
	}

	@Override
	public ItemStack getCraftingResult( InventoryCrafting inv )
	{
		ItemStack terminal = ItemStack.EMPTY;
		for (int i = 0; i < inv.getHeight() && terminal.isEmpty(); ++i)
		{
			for( int j = 0; j < inv.getWidth(); ++j )
			{
				ItemStack itemstack = inv.getStackInRowAndColumn( j, i );

				if( !itemstack.isEmpty() && termDef.isSameAs( itemstack ))
				{
					terminal = itemstack.copy();
					break;
				}
			}
		}
		if (terminal.isEmpty()){
			return ItemStack.EMPTY;
		}

		ToolWirelessTerminal.setHasQuantumEgg( terminal, true );

		return terminal;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems( InventoryCrafting inv )
	{
		return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
	}

	public boolean matches(InventoryCrafting inv, World worldIn)
	{
		List<ItemStack> list = Lists.newArrayList(this.recipeItems);

		for (int i = 0; i < inv.getHeight(); ++i)
		{
			for (int j = 0; j < inv.getWidth(); ++j)
			{
				ItemStack itemstack = inv.getStackInRowAndColumn(j, i);

				if (!itemstack.isEmpty())
				{
					boolean flag = false;

					for (ItemStack itemstack1 : list)
					{
						if (itemstack.getItem() == itemstack1.getItem() && (itemstack1.getMetadata() == 32767 || itemstack.getMetadata() == itemstack1.getMetadata()))
						{
							if (!termDef.isSameAs(itemstack1) || !ToolWirelessTerminal.getHasQuantumEgg( itemstack ))
							{
								flag = true;
								list.remove( itemstack1 );
								break;
							}
						}
					}

					if (!flag)
					{
						return false;
					}
				}
			}
		}

		return list.isEmpty();
	}
}