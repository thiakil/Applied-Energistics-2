package appeng.integration.modules.opencomputers.environment;


import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

import appeng.helpers.IInterfaceHost;
import appeng.tile.misc.TileInterface;
import appeng.integration.modules.opencomputers.driver.InterfaceDriver;


/**
 * Created by Thiakil on 24/04/2017.
 */
public class InterfaceEnvironment extends ConfigurableEnvironmentBase {

	private InterfaceType myType;

	public InterfaceEnvironment(TileEntity tileParent) {
		super("interface", tileParent);
		assert InterfaceDriver.hasInterface(tileParent);
		myType = (tileParent instanceof TileInterface ) ? InterfaceType.BLOCK : InterfaceType.PART;
	}

	@Callback(doc = "function(side:number[, slot:number]):table -- Get the configuration of the interface pointing in the specified direction.")//nb side relative to the ae tile
	public Object[] getInterfaceConfiguration(Context context, Arguments args){
		int side = args.optInteger(0, -1);
		if (myType == InterfaceType.PART && (side < 0 || side > 5))
			throw new IllegalArgumentException("invalid side");
		IInventory configInv = getConfigInventory( EnumFacing.values()[side], myType == InterfaceType.BLOCK, IInterfaceHost.class);
		int slot = args.optInteger(1, 1) - 1;//offset from lua slot
		if (!(slot > -1 && slot < configInv.getSizeInventory()))
			throw new IllegalArgumentException("invalid slot");
		return new Object[] { configInv.getStackInSlot(slot) };
	}

	@Callback(doc = "function(side:number[, slot:number][, database:address, entry:number[, size:number]]):boolean -- Configure the interface pointing in the specified direction.")
	//size = me config stack size
	public Object[]  setInterfaceConfiguration(Context context, Arguments args){
		int side = args.optInteger(0, -1);
		if (myType == InterfaceType.PART && (side < 0 || side > 5))
			throw new IllegalArgumentException("invalid side");
		IInventory configInv = getConfigInventory( EnumFacing.values()[side], myType == InterfaceType.BLOCK, IInterfaceHost.class);
		return setConfiguration(context, args, configInv);
	}

	@Callback(doc="function():table -- Get a list of all sides with an interface part. Returns -1 if an interface block")
	public Object[] getInterfaces(Context context, Arguments args) {
		return this.getValidSides(IInterfaceHost.class);
	}

	private enum InterfaceType{
		BLOCK,
		PART
	}
}
