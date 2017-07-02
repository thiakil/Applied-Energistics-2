package appeng.integration.modules.opencomputers.environment;


import appeng.integration.modules.opencomputers.driver.ExportBusDriver;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

import appeng.parts.automation.PartExportBus;


/**
 * Created by Thiakil on 24/04/2017.
 */
public class ExportBusEnvironment extends ConfigurableEnvironmentBase {

	public ExportBusEnvironment(TileEntity tileParent) {
		super("export bus", tileParent);
		assert ExportBusDriver.hasExportBus(tileParent);
	}

	@Callback(doc = "function(side:number[, slot:number]):table -- Get the configuration of the export bus pointing in the specified direction.")//nb side relative to the ae tile
	public Object[] getExportConfiguration(Context context, Arguments args){
		int side = args.optInteger(0, -1);
		if (side < 0 || side > 5)
			throw new IllegalArgumentException("invalid side");
		IInventory configInv = getConfigInventory( EnumFacing.values()[side], false, PartExportBus.class);
		int slot = args.optInteger(1, 1) - 1;//offset from lua slot
		if (!(slot > -1 && slot < configInv.getSizeInventory()))
			throw new IllegalArgumentException("invalid slot");
		return new Object[] { configInv.getStackInSlot(slot) };
	}

	@Callback(doc = "function(side:number[, slot:number][, database:address, entry:number[, size:number]]):boolean -- Configure the export bus pointing in the specified direction.")
	//size = me config stack size
	public Object[]  setExportConfiguration(Context context, Arguments args){
		int side = args.optInteger(0, -1);
		if (side < 0 || side > 5)
			throw new IllegalArgumentException("invalid side");
		IInventory configInv = getConfigInventory( EnumFacing.values()[side], false, PartExportBus.class);
		return setConfiguration(context, args, configInv);
	}

	@Callback(doc="function():table -- Get a list of all sides with an export bus")
	public Object[] getExportBusses(Context context, Arguments args) {
		return this.getValidSides(PartExportBus.class);
	}

}
