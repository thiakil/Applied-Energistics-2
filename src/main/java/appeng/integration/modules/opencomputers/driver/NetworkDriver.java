package appeng.integration.modules.opencomputers.driver;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import li.cil.oc.api.driver.DriverBlock;
import li.cil.oc.api.network.ManagedEnvironment;

import appeng.api.AEApi;
import appeng.core.AELog;
import appeng.integration.modules.opencomputers.environment.MENetworkEnvironment;
import appeng.integration.modules.opencomputers.SaveableGridProxy;
import appeng.me.helpers.IGridProxyable;


/**
 * Created by Thiakil on 23/04/2017.
 */
public class NetworkDriver implements DriverBlock {

	@Override
	public boolean worksWith(World world, BlockPos pos, EnumFacing side) {
		/*Block wantedBlock = world.getBlockState(pos).getBlock();
		String wantedBlockName = wantedBlock.getRegistryName().toString();*/
		/*AELog.info("Got called for "+pos.toString());
		AELog.info("Which is a"+world.getBlockState(pos).getBlock().getRegistryName().toString());
		TileEntity te = world.getTileEntity(pos);
		if (te != null)
			AELog.info("And a "+te.getClass().toString());
		*/
		try {
			return ( AEApi.instance().definitions().blocks().controller().isSameAs(world, pos) || InterfaceDriver.isInterface(world,pos,side));
		} catch (Exception e){
			return false;
		}
	}

	public static IGridProxyable getGridProxyable(World world, BlockPos pos, EnumFacing side){
		try {
			TileEntity tile = world.getTileEntity(pos);
			if (tile != null) {
				if (tile instanceof IGridProxyable )
					return (IGridProxyable)tile;
				else {
					return InterfaceDriver.getIfaceGrid(tile, side);
				}
			}
		} catch (Exception e){
			AELog.error("Error occurred during MENetworkEnvironment creation.", e);
		}
		return null;
	}

	@Override
	public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
		if (worksWith(world,pos,side)) {
			IGridProxyable proxyable = getGridProxyable(world, pos, side);
			if (proxyable != null){
				return new MENetworkEnvironment(new SaveableGridProxy(proxyable, side), world);
			}
		}
		return null;
	}


}
