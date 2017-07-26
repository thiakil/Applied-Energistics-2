package appeng.integration.modules.opencomputers.environment;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import li.cil.oc.api.internal.Database;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Component;
import li.cil.oc.api.network.Node;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.integration.modules.opencomputers.Craftable;
import appeng.integration.modules.opencomputers.ItemStackArrayValue;
import appeng.integration.modules.opencomputers.SaveableGridProxy;
import appeng.me.GridAccessException;


/**
 * Created by Thiakil on 24/04/2017.
 */

public class MENetworkEnvironment extends MENetworkEnvironmentBase {

	public MENetworkEnvironment(SaveableGridProxy tile, World world){
		super(tile, world);
	}

	@Callback(doc = "function():number -- Get the average power injection into the network.")
	public Object[] getAvgPowerInjection(Context context, Arguments args) {
		try {
			return new Object[] { gridProxy.getProxy().getEnergy().getAvgPowerInjection() };
		} catch (GridAccessException e){
			return new Object[] { null, "Error accessing grid."};
		} catch (Exception e){
			AELog.error("Unknown error accessing ME net.", e);
			return new Object[] { null, "Unknown internal error."};
		}
	}

	@Callback(doc = "function():number -- Get the average power usage of the network.")
	public Object[] getAvgPowerUsage(Context context, Arguments args) {
		try {
			return new Object[] { gridProxy.getProxy().getEnergy().getAvgPowerUsage() };
		} catch (GridAccessException e){
			return new Object[] { null, "Error accessing grid."};
		} catch (Exception e){
			AELog.error("Unknown error accessing ME net.", e);
			return new Object[] { null, "Unknown internal error."};
		}
	}

	@Callback(doc = "function():number -- Get the idle power usage of the network.")
	public Object[] getIdlePowerUsage(Context context, Arguments args) {
		try {
			return new Object[] { gridProxy.getProxy().getEnergy().getIdlePowerUsage() };
		} catch (GridAccessException e){
			return new Object[] { null, "Error accessing grid."};
		} catch (Exception e){
			AELog.error("Unknown error accessing ME net.", e);
			return new Object[] { null, "Unknown internal error."};
		}
	}

	@Callback(doc = "function():number -- Get the maximum stored power in the network.")
	public Object[] getMaxStoredPower(Context context, Arguments args) {
		try {
			return new Object[] { gridProxy.getProxy().getEnergy().getMaxStoredPower() };
		} catch (GridAccessException e){
			return new Object[] { null, "Error accessing grid."};
		} catch (Exception e){
			AELog.error("Unknown error accessing ME net.", e);
			return new Object[] { null, "Unknown internal error."};
		}
	}

	@Callback(doc = "function():number -- Get the stored power in the network. ")
	public Object[] getStoredPower(Context context, Arguments args) {
		try {
			return new Object[] { gridProxy.getProxy().getEnergy().getStoredPower() };
		} catch (GridAccessException e){
			return new Object[] { null, "Error accessing grid."};
		} catch (Exception e){
			AELog.error("Unknown error accessing ME net.", e);
			return new Object[] { null, "Unknown internal error."};
		}
	}

	@Callback(doc = "function():table -- Get a list of tables representing the available CPUs in the network.")
	public Object[] getCpus(Context context, Arguments args) {
		try {
			ImmutableSet<ICraftingCPU> cpus = gridProxy.getProxy().getCrafting().getCpus();
			ArrayList<HashMap<String,Object>> output = new ArrayList<>(cpus.size());
			for (ICraftingCPU cpu : cpus){
				HashMap<String,Object> table = new HashMap<>();
				table.put("name", cpu.getName());
				table.put("storage", cpu.getAvailableStorage());
				table.put("coprocessors", cpu.getCoProcessors());
				table.put("busy", cpu.isBusy());
				output.add(table);
			}
			return new Object[] { output };
		} catch (GridAccessException e){
			return new Object[] { null, "Error accessing grid."};
		} catch (Exception e){
			AELog.error("Unknown error accessing ME net.", e);
			return new Object[] { null, "Unknown internal error."};
		}
	}

	@Callback(doc = "function([filter:table]):table -- Get a list of the stored items in the network.")
	public Object[] getItemsInNetwork(Context context, Arguments args) {
		Map filter = args.optTable(0, null);
		try {
			IItemList<IAEItemStack> storageList = gridProxy.getProxy().getStorage().getItemInventory().getStorageList();
			ArrayList<ItemStack> output = new ArrayList<>(storageList.size());
			for (IAEItemStack stack : storageList){
				if (filter == null || filterMatches(stack, filter)){
					output.add(stack.getItemStack());
				}
			}
			return new Object[] { new ItemStackArrayValue(output.toArray(new ItemStack[output.size()])) };
		} catch (GridAccessException e){
			return new Object[] { null, "Error accessing grid."};
		} catch (Exception e){
			AELog.error("Unknown error accessing ME net.", e);
			return new Object[] { null, "Unknown internal error."};
		}
	}

	@Callback(doc = "function([filter:table]):table -- Get a list of known item recipes. These can be used to issue crafting requests.")
	public Object[] getCraftables(Context context, Arguments args) {
		Map filter = args.optTable(0, null);
		try {
			IItemList<IAEItemStack> storageList = gridProxy.getProxy().getStorage().getItemInventory().getStorageList();
			ArrayList<Craftable> craftables = new ArrayList<>(storageList.size());
			ICraftingGrid craftingGrid = gridProxy.getProxy().getCrafting();
			for (IAEItemStack stack : storageList){
				if (stack.isCraftable() && (filter == null || filterMatches(stack, filter))){
					ImmutableCollection<ICraftingPatternDetails> detailsCollection = craftingGrid.getCraftingFor(stack, null, 0, this.worldObj);
					for (ICraftingPatternDetails details : detailsCollection){
						boolean found = false;
						for (IAEItemStack outputStack : details.getCondensedOutputs()){
							if (outputStack.getItem() == stack.getItem()){
								found = true;
								break;
							}
						}
						if (found){
							craftables.add(new Craftable(this.gridProxy, details, stack));
						}
					}
				}
			}
			return new Object[] { craftables.toArray(new Craftable[craftables.size()]) };
		} catch (GridAccessException e){
			return new Object[] { null, "Error accessing grid."};
		} catch (Exception e){
			AELog.error("Unknown error accessing ME net.", e);
			return new Object[] { null, "Unknown internal error."};
		}
	}

	@Callback(doc = "function(filter:table, dbAddress:string[, startSlot:number[, count:number]]): Boolean -- Store items in the network matching the specified filter in the database with the specified address.")
	public Object[]  store(Context context, Arguments args) {
		Map filter = args.checkTable(0);
		Node addressed = node().network().node(args.checkString(1));

		if (addressed == null || !(addressed instanceof Component ) || !(addressed.host() instanceof Database)){
			throw new IllegalArgumentException(addressed == null ? "no such component" : "not a database");
		}

		Database database = (Database) addressed.host();
		int databaseSlots = database.size();

		int offset = args.optInteger(2, 1) - 1;//minus 1 to offset lua 1 indexing
		if (offset < 0 || offset > databaseSlots - 1){
			throw new IllegalArgumentException("invalid slot");
		}

		int count = Math.max(args.optInteger(3, database.size()), databaseSlots - offset);
		if (count < 1) {
			throw new IllegalArgumentException("count < 1");
		}

		try {
			IItemList<IAEItemStack> storageList = gridProxy.getProxy().getStorage().getItemInventory().getStorageList();
			ArrayList<ItemStack> meItems = new ArrayList<>(storageList.size());
			for (IAEItemStack stack : storageList){
				if (filter == null || filterMatches(stack, filter)){
					meItems.add(stack.getItemStack());
				}
			}

			if (meItems.size() > 0) {
				count = Math.max(count, meItems.size());
				int slot = offset;
				for (int i = 0; i < count && slot < databaseSlots; i++) {
					while ( !database.getStackInSlot( slot ).isEmpty() && slot < databaseSlots) slot += 1;
					if( database.getStackInSlot( slot ).isEmpty() )
					{
						database.setStackInSlot( slot, meItems.get( i ) );
					}
				}
			}

			return new Object[] { true };
		} catch (GridAccessException e){
			return new Object[] { false, "Error accessing grid."};
		} catch (Exception e){
			AELog.error("Unknown error accessing ME net.", e);
			return new Object[] { false, "Unknown internal error."};
		}

	}
}
