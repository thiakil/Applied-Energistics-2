package appeng.integration.modules.opencomputers;


import java.util.ArrayList;
import java.util.concurrent.Future;

import com.google.common.collect.ImmutableSet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.prefab.AbstractValue;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingCallback;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.core.AELog;
import appeng.me.GridAccessException;
import appeng.util.item.AEItemStack;


/**
 * Created by Thiakil on 25/04/2017.
 */
public class Craftable extends AbstractValue implements ICraftingRequester
{

	protected SaveableGridProxy gridProxy = null;
	protected IAEItemStack[] condensedOutputs;
	protected IAEItemStack output;

	protected ArrayList<ICraftingLink> links = new ArrayList<>();

	public Craftable(){
		//hopefully we're loading, or this will end badly
	}

	public Craftable(SaveableGridProxy gridProxy, ICraftingPatternDetails pattern, IAEItemStack output){
		this.gridProxy = gridProxy;
		this.output = output;
		this.condensedOutputs = pattern.getCondensedOutputs();
	}

	@Callback(doc = "function():table -- Returns the item stack representation of the crafting result (primary output).")
	public Object[] getItemStack(Context context, Arguments args) {
		return new Object[] {output.getItemStack()};
	}

	@Callback(doc = "function():table -- Returns the item stack representation of the complete crafting result (all outputs).")
	public Object[] getOutputs(Context context, Arguments args) {
		try {
			ItemStack[] outputs = new ItemStack[condensedOutputs.length];
			for (int i=0; i< condensedOutputs.length; i++){
				outputs[i] = condensedOutputs[i].getItemStack();
			}
			return new Object[] {outputs};
		} catch (Exception e){
			AELog.error("Unknown error accessing outputs.", e);
			return new Object[] { null, "Unknown internal error."};
		}
	}

	@Callback(doc = "function([amount:int[, prioritizePower:boolean[, cpuName:string]]]):userdata -- Requests the item to be crafted, returning an object that allows tracking the crafting status.")
	public Object[] request(Context context, Arguments args){
		int count = args.optInteger(0, 1);
		boolean prioritizePower = args.optBoolean(1, true);
		String cpuName = args.optString(2, "");
		MachineSource source = new MachineSource(this);
		IAEItemStack request = this.output.copy();
		request.setStackSize(count);

		CraftingStatus returnStatus = new CraftingStatus();

		try {

			ICraftingGrid craftingGrid = gridProxy.getProxy().getCrafting();

			ICraftingCPU wantedCPU = null;
			if (!cpuName.isEmpty()) {
				ImmutableSet<ICraftingCPU> cpus = gridProxy.getProxy().getCrafting().getCpus();
				for (ICraftingCPU cpu : cpus) {
					if (cpu.getName().equals(cpuName)) {
						wantedCPU = cpu;
						break;
					}
				}

			}

			final ICraftingCPU finalCPU = wantedCPU;

			Future<ICraftingJob> craftingJobFuture = craftingGrid.beginCraftingJob( gridProxy.getProxy().getNode().getWorld(), gridProxy.getProxy().getGrid(), source, request, new ICraftingCallback() {
				@Override
				public void calculationComplete(ICraftingJob job) {
					try {
						ICraftingLink link = craftingGrid.submitJob(job, Craftable.this, finalCPU, prioritizePower, source);
						if (link != null) {
							returnStatus.setLink(link);
							links.add(link);
						}
						else {
							returnStatus.fail("could not submit crafting job.");
						}
					} catch (Exception e){
						AELog.error("Error submitting job to AE2.", e);
						returnStatus.fail(e.toString());
					}
				}
			});

		} catch (GridAccessException e){
			return new Object[] { null, "Error accessing grid."};
		} catch (Exception e){
			AELog.error("Unknown error accessing ME net.", e);
			return new Object[] { null, "Unknown internal error."};
		}
		return new Object[] { returnStatus };
	}

	@Override
	public void load(NBTTagCompound nbt) {
		super.load(nbt);
		if (nbt.hasKey("gridproxy")) {
			this.gridProxy = new SaveableGridProxy();
			this.gridProxy.load(nbt.getCompoundTag("gridproxy"));
		}
		if (nbt.hasKey("output")) {
			this.output = AEItemStack.loadItemStackFromNBT(nbt.getCompoundTag("output"));
		}
		if (nbt.hasKey("condensed_output")){
			NBTTagList list = nbt.getTagList("condensed_output", nbt.getId());
			this.condensedOutputs = new IAEItemStack[list.tagCount()];
			for (int i = 0; i < list.tagCount(); i++){
				this.condensedOutputs[i] = AEItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
			}
		}
	}

	@Override
	public void save(NBTTagCompound nbt) {
		super.save(nbt);
		nbt.setTag("gridproxy", this.gridProxy.save());
		NBTTagCompound stackNBTOutput = new NBTTagCompound();
		this.output.writeToNBT(stackNBTOutput);
		nbt.setTag("output", stackNBTOutput);
		NBTTagList condensed = new NBTTagList();
		for (IAEItemStack stack : this.condensedOutputs){
			if (stack != null){
				NBTTagCompound stackNBT = new NBTTagCompound();
				stack.writeToNBT(stackNBT);
				condensed.appendTag(stackNBT);
			}
		}
		nbt.setTag("condensed_output", condensed);

		//AELog.info("Saving NBT: "+nbt.toString());
	}

	@Override
	public ImmutableSet<ICraftingLink> getRequestedJobs() {
		return ImmutableSet.of();
	}

	@Override
	public IAEItemStack injectCraftedItems(ICraftingLink iCraftingLink, IAEItemStack iaeItemStack, Actionable actionable) {
		return iaeItemStack;
	}

	@Override
	public void jobStateChange(ICraftingLink iCraftingLink) {
		links.remove(iCraftingLink);
	}

	@Override
	public IGridNode getActionableNode() {
		return gridProxy.getProxy().getNode();
	}

	@Override
	public IGridNode getGridNode(AEPartLocation aePartLocation) {
		return gridProxy.getProxy().getNode();
	}

	@Override
	public AECableType getCableConnectionType(AEPartLocation aePartLocation) {
		return gridProxy.getGridProxyable() != null ? gridProxy.getGridProxyable().getCableConnectionType(aePartLocation) : null;
	}

	@Override
	public void securityBreak() {
		if (gridProxy.getGridProxyable() != null)
			gridProxy.getGridProxyable().securityBreak();
	}

}
