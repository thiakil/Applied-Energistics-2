package appeng.integration.modules.opencomputers;


import net.minecraft.nbt.NBTTagCompound;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.prefab.AbstractValue;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingLink;


/**
 * Created by Thiakil on 26/04/2017.
 */
public class CraftingStatus extends AbstractValue
{
	private boolean isComputing = true;
	private ICraftingLink link;
	private boolean failed = false;
	private String reason = "no link";

	public void setLink(ICraftingLink value) {
		isComputing = false;
		link = value;
	}

	public void fail(String reason) {
		isComputing = false;
		failed = true;
		this.reason = "request failed: "+reason;
	}

	@Callback(doc = "function():boolean -- Get whether the crafting request has been canceled.")
	public Object[]  isCanceled(Context context, Arguments args) {
		if (isComputing)
			return new Object[] {false, "computing"};
		if (link != null)
			return new Object[] {link.isCanceled()};
		else
			return new Object[] {failed, reason};
	}

	@Callback(doc = "function():boolean -- Get whether the crafting request is done.")
	public Object[]  isDone(Context context, Arguments args) {
		if (isComputing)
			return new Object[] {false, "computing"};
		if (link != null)
			return new Object[] {link.isDone()};
		else
			return new Object[] {!failed, reason};
	}

	@Override
	public void load(NBTTagCompound nbt) {
		super.load(nbt);
		isComputing = nbt.getBoolean("isComputing");
		failed = nbt.getBoolean("failed");
		reason = nbt.getString("reason");
		if (nbt.hasKey("link")){
			link = AEApi.instance().storage().loadCraftingLink(nbt.getCompoundTag("link"), null);
		}
	}

	@Override
	public void save(NBTTagCompound nbt) {
		super.save(nbt);

		nbt.setBoolean("isComputing", isComputing);
		if (link != null) {
			NBTTagCompound linkNBT = new NBTTagCompound();
			link.writeToNBT(linkNBT);
			nbt.setTag("link", linkNBT);
		}
		nbt.setBoolean("failed", failed);
		nbt.setString("reason", reason);

		//AELog.info("Saving NBT: "+nbt.toString());
	}

}
