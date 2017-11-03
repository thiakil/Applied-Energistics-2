package appeng.core.sync.packets;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.core.AEConfig;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.IContainerCraftingPacket;
import appeng.items.storage.ItemViewCell;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;
import appeng.util.prioritylist.IPartitionList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.oredict.OreDictionary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PacketJEIProcessPattern extends AppEngPacket {

	private List<List<ItemStack>> inputs;
	private List<List<ItemStack>> outputs;

	public PacketJEIProcessPattern( final ByteBuf stream ) throws IOException{
		NBTTagCompound recipe = ByteBufUtils.readTag(stream);
		inputs = new ArrayList<>(9);
		outputs = new ArrayList<>(3);

		if (recipe != null && recipe.hasKey("input", Constants.NBT.TAG_LIST) && recipe.hasKey("output", Constants.NBT.TAG_LIST)){
			NBTTagList inputTagList = recipe.getTagList("input", Constants.NBT.TAG_LIST);
			int numInputs = Math.min(inputTagList.tagCount(), 9);//is capped to 9 in the JEI module, but safety first!
			for (int i =0; i < numInputs; i++) {
				NBTBase t = inputTagList.get(i);
				if (t instanceof NBTTagList) {
					NBTTagList inputEl = (NBTTagList)t;
					List<ItemStack> thisInput = new ArrayList<>(inputEl.tagCount());
					for (int j=0; j<inputEl.tagCount(); j++){
						ItemStack is = new ItemStack(inputEl.getCompoundTagAt(j));
						if (!is.isEmpty()){
							thisInput.add(is);
						}
					}
					if (!thisInput.isEmpty()){
						inputs.add(thisInput);
					}
				}
			}
			NBTTagList outputTagList = recipe.getTagList("output", Constants.NBT.TAG_LIST);
			int numOutputs = Math.min(outputTagList.tagCount(), 3);//is capped to 9 in the JEI module, but safety first!
			for (int i =0; i < numOutputs; i++) {
				NBTBase t = outputTagList.get(i);
				if (t instanceof NBTTagList) {
					NBTTagList outputEl = (NBTTagList)t;
					List<ItemStack> thisOutput = new ArrayList<>(outputEl.tagCount());
					for (int j=0; j<outputEl.tagCount(); j++){
						ItemStack is = new ItemStack(outputEl.getCompoundTagAt(j));
						if (!is.isEmpty()){
							thisOutput.add(is);
						}
					}
					if (!thisOutput.isEmpty()){
						outputs.add(thisOutput);
					}
				}
			}
		}
	}

	public PacketJEIProcessPattern( List<List<ItemStack>> in, List<List<ItemStack>> out ) throws IOException
	{
		final ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );

		/*
		 * Construct a NBT compound that has a list (slots) of lists (possible itemstacks) for inputs and outputs
		 */
		NBTTagCompound message = new NBTTagCompound();
		NBTTagList inputTags = new NBTTagList();
		message.setTag("input", inputTags);
		NBTTagList outputTags = new NBTTagList();
		message.setTag("output", outputTags);

		in.forEach(ingredientsList ->{
			NBTTagList options = new NBTTagList();
			inputTags.appendTag(options);
			ingredientsList.forEach(item->{
				options.appendTag(item.serializeNBT());
			});
		});

		out.forEach(ingredientsList ->{
			NBTTagList options = new NBTTagList();
			outputTags.appendTag(options);
			ingredientsList.forEach(item->{
				options.appendTag(item.serializeNBT());
			});
		});

		ByteBufUtils.writeTag(data, message);

		this.configureWrite( data );
	}

	@Override
	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player) {
		final EntityPlayerMP pmp = (EntityPlayerMP) player;


		final Container con = pmp.openContainer;

		if (con instanceof ContainerPatternTerm) {
			final ContainerPatternTerm cct = (ContainerPatternTerm) con;
			final IGridNode node = cct.getNetworkNode();
			if (node != null) {
				final IGrid grid = node.getGrid();
				if (grid == null) {
					return;
				}

				final IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
				final ISecurityGrid security = grid.getCache(ISecurityGrid.class);
				final IInventory craftMatrix = cct.getInventoryByName("crafting");
				final IInventory outputSlots = cct.getInventoryByName("output");

				if (storageGrid == null || outputSlots == null || this.inputs == null || this.outputs == null || security == null || !security.hasPermission(player, SecurityPermissions.EXTRACT)) {
					return;
				}

				if (!AEConfig.instance().getPatternTermRequiresItems()) {
					for (int x = 0; x < craftMatrix.getSizeInventory(); x++){
						if (inputs.get(x) != null){
							craftMatrix.setInventorySlotContents( x, inputs.get( x ).get( 0 ) );
						} else {
							craftMatrix.setInventorySlotContents( x, ItemStack.EMPTY );
						}
					}
					for (int x = 0; x < outputSlots.getSizeInventory(); x++){
						if (outputs.get(x) != null){
							outputSlots.setInventorySlotContents( x, outputs.get( x ).get( 0 ) );
						} else {
							outputSlots.setInventorySlotContents( x, ItemStack.EMPTY );
						}
					}
					return;
				}

				final IItemList<IAEItemStack> craftables = new ItemList();
				IItemList<IAEItemStack> storageList = storageGrid.getItemInventory().getStorageList();
				for (IAEItemStack stack : storageList) {
					if (stack.isCraftable()) {
						craftables.add(stack);
					}
				}

				final IPartitionList<IAEItemStack> filter = ItemViewCell.createFilter(cct.getViewCells());

				attemptTransfer( craftMatrix, this.inputs, craftables, filter, storageGrid.getItemInventory(), cct.getActionSource(), player, storageList );
				attemptTransfer( outputSlots, this.outputs, craftables, filter, storageGrid.getItemInventory(), cct.getActionSource(), player, storageList );

			}
		}
	}

	private static void attemptTransfer(IInventory matrix, List<List<ItemStack>> source, final IItemList<IAEItemStack> craftables, final IPartitionList<IAEItemStack> filter, IMEMonitor<IAEItemStack> itemInv, final BaseActionSource actionSrc, final EntityPlayer player, IItemList<IAEItemStack> storageList){
		for (int x = 0; x < matrix.getSizeInventory(); x++){
			if (source.size() > x){
				ItemStack found = ItemStack.EMPTY;
				for (ItemStack is : source.get(x)){
					found = hasStoredItem(is, filter, itemInv, actionSrc, storageList);
					if (!found.isEmpty()){
						break;
					}
					found = extractItemFromPlayerInventory(player, Actionable.SIMULATE, is);
					if (!found.isEmpty()){
						break;
					}
					Collection<IAEItemStack> matches = craftables.findFuzzy(AEItemStack.create(is), FuzzyMode.IGNORE_ALL);
					if (matches.size() > 0){
						found = matches.iterator().next().getDisplayItemStack();//display stack so it ensures it's not empty
						break;
					}
				}
				matrix.setInventorySlotContents(x, found);
			} else {
				matrix.setInventorySlotContents(x, ItemStack.EMPTY);
			}
		}
	}

	private static ItemStack hasStoredItem(ItemStack providedTemplate, final IPartitionList<IAEItemStack> filter, final IMEMonitor<IAEItemStack> src, final BaseActionSource mySrc, final IItemList<IAEItemStack> items){
		final AEItemStack ae_req = AEItemStack.create( providedTemplate );
		ae_req.setStackSize( 1 );

		if( filter == null || filter.isListed( ae_req ) )
		{
			final IAEItemStack ae_ext = src.extractItems( ae_req, Actionable.SIMULATE, mySrc );
			if( ae_ext != null )
			{

				return ae_ext.getItemStack();
			}
		}

		final boolean checkFuzzy = ae_req.isOre() || providedTemplate.getItemDamage() == OreDictionary.WILDCARD_VALUE || providedTemplate
				.hasTagCompound() || providedTemplate.isItemStackDamageable();

		if( items != null && checkFuzzy ) {
			for (final IAEItemStack x : items) {
				final ItemStack sh = x.getItemStack();
				if ((Platform.itemComparisons().isEqualItemType(providedTemplate,
						sh) || ae_req.sameOre(x))) { // Platform.isSameItemType( sh, providedTemplate )
					return sh;
				}
			}
		}

		return ItemStack.EMPTY;
	}

	private static ItemStack extractItemFromPlayerInventory( final EntityPlayer player, final Actionable mode, final ItemStack patternItem )
	{
		final InventoryAdaptor ia = InventoryAdaptor.getAdaptor( player, EnumFacing.UP );
		final AEItemStack request = AEItemStack.create( patternItem );
		final boolean isSimulated = mode == Actionable.SIMULATE;
		final boolean checkFuzzy = request.isOre() || patternItem.getItemDamage() == OreDictionary.WILDCARD_VALUE || patternItem.hasTagCompound() || patternItem.isItemStackDamageable();

		if( !checkFuzzy )
		{
			if( isSimulated )
			{
				return ia.simulateRemove( 1, patternItem, null );
			}
			else
			{
				return ia.removeItems( 1, patternItem, null );
			}
		}
		else
		{
			if( isSimulated )
			{
				return ia.simulateSimilarRemove( 1, patternItem, FuzzyMode.IGNORE_ALL, null );
			}
			else
			{
				return ia.removeSimilarItems( 1, patternItem, FuzzyMode.IGNORE_ALL, null );
			}
		}
	}

}
