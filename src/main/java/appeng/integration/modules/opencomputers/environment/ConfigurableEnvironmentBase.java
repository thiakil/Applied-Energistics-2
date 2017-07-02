package appeng.integration.modules.opencomputers.environment;


import java.util.ArrayList;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import li.cil.oc.api.Network;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.internal.Database;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Component;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;

import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.helpers.IInterfaceHost;


/**
 * Created by Thiakil on 28/04/2017.
 */
public class ConfigurableEnvironmentBase extends li.cil.oc.api.prefab.AbstractManagedEnvironment implements NamedBlock
{

	public static String ENVIRONMENT_NAME = "me_network";
	protected TileEntity tileParent;
	protected String meName;

	public ConfigurableEnvironmentBase(String name, TileEntity tileParent) {
		//this.ENVIRONMENT_NAME = name;
		this.meName = name;
		this.tileParent = tileParent;
		setNode( Network.newNode(this, Visibility.Network).
				withComponent(ENVIRONMENT_NAME).
				create());
	}

	protected IInventory getConfigInventory(EnumFacing side, boolean isBlock, Class<? extends ISegmentedInventory> expectedClass){
		if (isBlock){
			return ((IInterfaceHost)this.tileParent).getInventoryByName("config");
		}
		IPart part = ((IPartHost)tileParent).getPart(side);
		if (!expectedClass.isInstance(part))
			throw new IllegalArgumentException("no "+meName);
		return ((ISegmentedInventory)part).getInventoryByName("config");
	}

	//side:number[, slot:number][, database:address, entry:number[, size:number]]; side is handled externally
	protected Object[] setConfiguration(Context context, Arguments args, IInventory configInv) {
		int slot = 0; String dbAddress; int entry; int size;
		if (args.isInteger(1)){
			slot = args.checkInteger(1) - 1;
			if (!(slot > -1 && slot < configInv.getSizeInventory()))
				throw new IllegalArgumentException("invalid slot");
		}

		ItemStack stackToSet = ItemStack.EMPTY;

		if (args.count() > 2) {
			if (args.isString(1)) {
				dbAddress = args.checkString(1);
				entry = args.checkInteger(2)-1;
				size = args.optInteger(3, 1);
			} else {
				dbAddress = args.checkString(2);
				entry = args.checkInteger(3)-1;
				size = args.optInteger(4, 1);
			}

			Node addressed = node().network().node(dbAddress);

			if (addressed == null || !(addressed instanceof Component ) || !(addressed.host() instanceof Database)){
				throw new IllegalArgumentException(addressed == null ? "no such component" : "not a database");
			}

			Database database = (Database)addressed.host();
			int databaseSlots = database.size();

			if (entry < 0 || entry > databaseSlots - 1){
				throw new IllegalArgumentException("invalid entry number");
			}

			stackToSet = database.getStackInSlot(entry);
			if (!stackToSet.isEmpty()){
				stackToSet = stackToSet.copy();
				stackToSet.setCount( Math.min( size, stackToSet.getMaxStackSize() ) );;
			}

		}
		configInv.setInventorySlotContents(slot, stackToSet);
		context.pause(0.5);
		return new Object[]{ true };
	}

	protected Object[] getValidSides(Class<? extends ISegmentedInventory> expectedClass){
		ArrayList<Integer> sides = new ArrayList<>();
		if (this.tileParent instanceof IPartHost ){
			IPartHost host = (IPartHost)this.tileParent;
			for (EnumFacing side : EnumFacing.values()){
				IPart part = host.getPart(side);
				if (part != null && expectedClass.isInstance(part))
					sides.add(side.getIndex());
			}
		} else {
			sides.add(-1);
		}
		return new Object[] { sides };
	}

	@Override
	public String preferredName() {
		return ENVIRONMENT_NAME;
	}

	@Override
	public int priority() {
		return 5;
	}

}
