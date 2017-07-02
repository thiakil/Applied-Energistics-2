package appeng.integration.modules.opencomputers.driver;


import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import li.cil.oc.api.driver.DriverBlock;
import li.cil.oc.api.network.ManagedEnvironment;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.core.AELog;
import appeng.me.helpers.IGridProxyable;
import appeng.parts.misc.PartInterface;
import appeng.tile.misc.TileInterface;
import appeng.integration.modules.opencomputers.environment.InterfaceEnvironment;


/**
 * Created by Thiakil on 23/04/2017.
 */
public class InterfaceDriver implements DriverBlock {

	public static boolean isInterface(World world, BlockPos pos, EnumFacing side){
		try {
			if ( AEApi.instance().definitions().blocks().multiPart().isSameAs(world, pos)){
				TileEntity te = world.getTileEntity(pos);
				if (te != null && te instanceof IPartHost ){
					IPart part = ((IPartHost) te).getPart(side.getOpposite());
					return part != null && part instanceof PartInterface;
				}
			}
			return AEApi.instance().definitions().blocks().iface().isSameAs(world, pos);
		} catch (Exception e){
			return false;
		}
	}

	public static boolean hasInterface(TileEntity tile){
		if (tile == null)
			return false;///i mean really...
		if (tile instanceof TileInterface )
			return true;
		if (tile instanceof IPartHost ){
			IPartHost host = (IPartHost)tile;
			for (EnumFacing side : EnumFacing.values()){
				IPart part = host.getPart(side);
				if (part != null && part instanceof PartInterface )
					return true;
			}
		}
		return false;
	}

	public static boolean hasInterface(World world, BlockPos pos){
		try {
			if (AEApi.instance().definitions().blocks().multiPart().isSameAs(world, pos)){
				TileEntity te = world.getTileEntity(pos);
				if (te != null && te instanceof IPartHost ){
					IPartHost host = (IPartHost)te;
					for (EnumFacing side : EnumFacing.values()){
						IPart part = host.getPart(side);
						if (part != null && part instanceof PartInterface )
							return true;
					}
				}
			}
			return AEApi.instance().definitions().blocks().iface().isSameAs(world, pos);
		} catch (Exception e){
			return false;
		}
	}

	@Override
	public boolean worksWith(World world, BlockPos pos, EnumFacing side) {
		return hasInterface(world,pos);
	}

	@Override
	public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
		if (worksWith(world,pos,side)) {
			try {
				TileEntity tile = world.getTileEntity(pos);
				if (tile != null) {
					return new InterfaceEnvironment(tile);
				}
			} catch (Exception e){
				AELog.error("Error occurred during InterfaceEnvironment creation.", e);
			}
		}
		return null;
	}

	public static IGridProxyable getIfaceGrid(TileEntity tile, EnumFacing side){
		try {
			if (tile != null && tile instanceof IGridProxyable ) {
				return (IGridProxyable) tile;
			} else if (tile != null && tile instanceof IPartHost ){
				//AELog.info("its a part host");
				IPart part = ((IPartHost) tile).getPart(side.getOpposite());
				//AELog.info("part is "+(part == null ? "NULL" : part.getClass().toString()));
				if (part != null && part instanceof PartInterface ) {
					return (IGridProxyable) part;
				}
			} else if (tile != null) {
				AELog.error(tile.getClass().toString()+" does not implement IGridProxyable!");
			}
		} catch (Exception e){
			AELog.error("Error occurred getting IGridProxyable creation.", e);
		}
		return null;
	}
}
