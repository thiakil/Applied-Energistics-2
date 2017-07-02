package appeng.integration.modules.opencomputers.environment;


import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import li.cil.oc.api.Network;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.network.Visibility;

import appeng.api.storage.data.IAEItemStack;
import appeng.core.AELog;
import appeng.integration.modules.opencomputers.SaveableGridProxy;


/**
 * Created by Thiakil on 25/04/2017.
 */
public class MENetworkEnvironmentBase extends li.cil.oc.api.prefab.AbstractManagedEnvironment implements NamedBlock
{
	protected SaveableGridProxy gridProxy;
	public String ENVIRONMENT_NAME;
	protected World worldObj;

	public MENetworkEnvironmentBase(SaveableGridProxy tile, World world){
		this(tile, "me_network", world);
	}

	public MENetworkEnvironmentBase(SaveableGridProxy tile, String name, World world) {
		this.gridProxy = tile;
		this.ENVIRONMENT_NAME = name;
		this.worldObj = world;
		setNode( Network.newNode(this, Visibility.Network).
				withComponent(ENVIRONMENT_NAME).
				create());
	}

	@Override
	public String preferredName() {
		return ENVIRONMENT_NAME;
	}

	@Override
	public int priority() {
		return 5;
	}

	protected boolean filterMatches(IAEItemStack stack, Map filter){

		return (filter.get("damage") == null || filter.get("damage").equals((double)stack.getItemDamage())) &&
				(filter.get("maxDamage") == null || filter.get("maxDamage").equals((double)stack.getItemStack().getMaxDamage())) &&
				(filter.get("size") == null || filter.get("size").equals((double)stack.getStackSize()) || filter.get("size").equals(0D)) &&
				(filter.get("maxSize") == null || filter.get("maxSize").equals((double)stack.getItemStack().getMaxStackSize())) &&
				(filter.get("hasTag") == null || filter.get("hasTag").equals(stack.hasTagCompound())) &&
				(filter.get("name") == null || filter.get("name").equals(stack.getItem().getRegistryName().toString())) &&
				(filter.get("label") == null || filter.get("label").equals(stack.getItemStack().getDisplayName()));
	}

	private static final String GRIDPROXY_KEY = "gridproxy";
	private static final String WORLD_DIM_KEY = "dimension";

	@Override
	public void load(NBTTagCompound nbt) {
		super.load(nbt);
		if (gridProxy == null) {//not sure these actually are needed, but added just to be sure it loads
			AELog.info("load called, gridproxy is null!");
			gridProxy.load(nbt.getCompoundTag(GRIDPROXY_KEY));
		}
		if (worldObj == null){
			AELog.info("load called, worldobj is null!");
			worldObj = DimensionManager.getWorld(nbt.getInteger(WORLD_DIM_KEY));
		}
	}

	@Override
	public void save(NBTTagCompound nbt) {
		super.save(nbt);
		nbt.setTag(GRIDPROXY_KEY, gridProxy.save());
		nbt.setInteger(WORLD_DIM_KEY, worldObj.provider.getDimension());
	}
}
