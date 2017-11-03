package appeng.integration.modules.jei;

import appeng.container.implementations.ContainerPatternTerm;
import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class UniversalPatternTransferHandler extends RecipeTransferHandler<ContainerPatternTerm> {

	public UniversalPatternTransferHandler() {
		super(ContainerPatternTerm.class);
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(ContainerPatternTerm container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
		if( !doTransfer )
		{
			return null;
		}

		//if it's a normal crafting recipe, set it to crafting mode & pass onto the normal handler
		if (recipeLayout.getRecipeCategory().getUid().equals(VanillaRecipeCategoryUid.CRAFTING)){
			container.getPatternTerminal().setCraftingRecipe(true);
			return super.transferRecipe(container, recipeLayout, player, maxTransfer, doTransfer);
		}

		List<List<ItemStack>> inputs = new ArrayList<>(9);
		List<List<ItemStack>> outputs = new ArrayList<>(3);

		Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients =  recipeLayout.getItemStacks().getGuiIngredients();
		for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> ingredientEntry : ingredients.entrySet()){
			IGuiIngredient<ItemStack> ingredient = ingredientEntry.getValue();
			if (ingredient.isInput()){
				inputs.add(ingredient.getAllIngredients());
			} else {
				outputs.add(ingredient.getAllIngredients());
			}
		}

		if (inputs.size()>9){
			return null;//cant handle it properly!
		}

		while (outputs.size() > 3){
			outputs.remove(3);//trim to 3
		}


		return null;
	}
}
