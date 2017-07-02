package appeng.integration.modules.opencomputers;


import appeng.integration.modules.opencomputers.driver.NetworkDriver;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import appeng.api.util.DimensionalCoord;
import appeng.core.AELog;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;


/**
 * Created by Thiakil on 25/04/2017.
 */
public class SaveableGridProxy  {

	private IGridProxyable gridProxy;
	private EnumFacing side;

	private BlockPos cachedBlockPos;
	private int cachedDimension;

	private boolean loaded;

	public SaveableGridProxy(){
		loaded = false;
	}

	public SaveableGridProxy(IGridProxyable iGridProxyable, EnumFacing side){
		this.gridProxy = iGridProxyable;
		this.side = side;
		loaded = true;
	}

	public NBTTagCompound save(){
		NBTTagCompound nbt = new NBTTagCompound();
		if (loaded) {
			DimensionalCoord location = this.gridProxy.getLocation();
			//AELog.info("Got these coords: " + location.toString());
			nbt.setInteger("x", location.x);
			nbt.setInteger("y", location.y);
			nbt.setInteger("z", location.z);
			nbt.setInteger("dimension", location.getWorld().provider.getDimension());
			nbt.setInteger("side", side.getIndex());
		} else {
			nbt.setInteger("x", cachedBlockPos.getX());
			nbt.setInteger("y", cachedBlockPos.getY());
			nbt.setInteger("z", cachedBlockPos.getY());
			nbt.setInteger("dimension", cachedDimension);
			nbt.setInteger("side", side.getIndex());
		}

		return nbt;
	}

	public void tryLoadGridProxy(){
		World world = DimensionManager.getWorld(cachedDimension);
		this.gridProxy = NetworkDriver.getGridProxyable(world, this.cachedBlockPos, this.side);
		if (this.gridProxy != null) {
			loaded = true;
			//AELog.info("Seemed to load ok, gridproxy = "+gridProxy.getClass().toString());
		}
	}

	public IGridProxyable getGridProxyable(){
		if (!this.loaded | this.gridProxy == null) {
			tryLoadGridProxy();
		}
		return this.gridProxy;
	}

	public AENetworkProxy getProxy(){
		if (getGridProxyable() != null) {
			return getGridProxyable().getProxy();
		}
		AELog.error("Gridproxy not loaded, and network driver couldnt find it!");
		return null;
	}

	public void load(NBTTagCompound saved){
		try {
			cachedDimension = saved.getInteger("dimension");
			this.cachedBlockPos = new BlockPos(saved.getInteger("x"), saved.getInteger("y"), saved.getInteger("z"));
			this.side = EnumFacing.getFront(saved.getInteger("side"));
			//AELog.info("Loaded cached values for "+cachedBlockPos.toString()+" @ "+side.getName());
		} catch (Exception e){
			AELog.error("Couldn't load SaveableGridProxy", e);
			AELog.info(saved.toString());
		}
	}
}
