/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.core.sync;


import java.lang.reflect.Constructor;
import java.util.List;

import appeng.capabilities.Capabilities;
import appeng.container.implementations.*;
import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import baubles.api.cap.IBaublesItemHandler;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.definitions.IComparableDefinition;
import appeng.api.definitions.IMaterials;
import appeng.api.exceptions.AppEngException;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.storage.ITerminalHost;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.GuiNull;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerNull;
import appeng.container.ContainerOpenContext;
import appeng.core.AELog;
import appeng.core.stats.Achievements;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.helpers.WirelessCraftingTerminalGuiObject;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.items.contents.QuartzKnifeObj;
import appeng.items.tools.ToolNetworkTool;
import appeng.items.tools.powered.WirelessCraftingTerminal;
import appeng.parts.automation.PartFormationPlane;
import appeng.parts.automation.PartLevelEmitter;
import appeng.parts.misc.PartStorageBus;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.tile.crafting.TileCraftingTile;
import appeng.tile.crafting.TileMolecularAssembler;
import appeng.tile.grindstone.TileGrinder;
import appeng.tile.misc.TileCellWorkbench;
import appeng.tile.misc.TileCondenser;
import appeng.tile.misc.TileInscriber;
import appeng.tile.misc.TileSecurityStation;
import appeng.tile.misc.TileVibrationChamber;
import appeng.tile.networking.TileWireless;
import appeng.tile.qnb.TileQuantumBridge;
import appeng.tile.spatial.TileSpatialIOPort;
import appeng.tile.storage.TileChest;
import appeng.tile.storage.TileDrive;
import appeng.tile.storage.TileIOPort;
import appeng.tile.storage.TileSkyChest;
import appeng.util.Platform;


public enum GuiBridge implements IGuiHandler
{
	GUI_Handler(),

	GUI_GRINDER( ContainerGrinder.class, TileGrinder.class, GuiHostType.WORLD, null ),

	GUI_QNB( ContainerQNB.class, TileQuantumBridge.class, GuiHostType.WORLD, SecurityPermissions.BUILD ),

	GUI_SKYCHEST( ContainerSkyChest.class, TileSkyChest.class, GuiHostType.WORLD, null ),

	GUI_CHEST( ContainerChest.class, TileChest.class, GuiHostType.WORLD, SecurityPermissions.BUILD ),

	GUI_WIRELESS( ContainerWireless.class, TileWireless.class, GuiHostType.WORLD, SecurityPermissions.BUILD ),

	GUI_ME( ContainerMEMonitorable.class, ITerminalHost.class, GuiHostType.WORLD, null ),

	GUI_PORTABLE_CELL( ContainerMEPortableCell.class, IPortableCell.class, GuiHostType.ITEM, null ),

	GUI_WIRELESS_TERM( ContainerWirelessTerm.class, WirelessTerminalGuiObject.class, GuiHostType.ITEM, null ),

	GUI_NETWORK_STATUS( ContainerNetworkStatus.class, INetworkTool.class, GuiHostType.ITEM, null ),

	GUI_CRAFTING_CPU( ContainerCraftingCPU.class, TileCraftingTile.class, GuiHostType.WORLD, SecurityPermissions.CRAFT ),

	GUI_NETWORK_TOOL( ContainerNetworkTool.class, INetworkTool.class, GuiHostType.ITEM, null ),

	GUI_QUARTZ_KNIFE( ContainerQuartzKnife.class, QuartzKnifeObj.class, GuiHostType.ITEM, null ),

	GUI_DRIVE( ContainerDrive.class, TileDrive.class, GuiHostType.WORLD, SecurityPermissions.BUILD ),

	GUI_VIBRATION_CHAMBER( ContainerVibrationChamber.class, TileVibrationChamber.class, GuiHostType.WORLD, null ),

	GUI_CONDENSER( ContainerCondenser.class, TileCondenser.class, GuiHostType.WORLD, null ),

	GUI_INTERFACE( ContainerInterface.class, IInterfaceHost.class, GuiHostType.WORLD, SecurityPermissions.BUILD ),

	GUI_BUS( ContainerUpgradeable.class, IUpgradeableHost.class, GuiHostType.WORLD, SecurityPermissions.BUILD ),

	GUI_IOPORT( ContainerIOPort.class, TileIOPort.class, GuiHostType.WORLD, SecurityPermissions.BUILD ),

	GUI_STORAGEBUS( ContainerStorageBus.class, PartStorageBus.class, GuiHostType.WORLD, SecurityPermissions.BUILD ),

	GUI_FORMATION_PLANE( ContainerFormationPlane.class, PartFormationPlane.class, GuiHostType.WORLD, SecurityPermissions.BUILD ),

	GUI_PRIORITY( ContainerPriority.class, IPriorityHost.class, GuiHostType.WORLD, SecurityPermissions.BUILD ),

	GUI_SECURITY( ContainerSecurityStation.class, TileSecurityStation.class, GuiHostType.WORLD, SecurityPermissions.SECURITY ),

	GUI_CRAFTING_TERMINAL( ContainerCraftingTerm.class, PartCraftingTerminal.class, GuiHostType.WORLD, SecurityPermissions.CRAFT ),

	GUI_PATTERN_TERMINAL( ContainerPatternTerm.class, PartPatternTerminal.class, GuiHostType.WORLD, SecurityPermissions.CRAFT ),

	// extends (Container/Gui) + Bus
	GUI_LEVEL_EMITTER( ContainerLevelEmitter.class, PartLevelEmitter.class, GuiHostType.WORLD, SecurityPermissions.BUILD ),

	GUI_SPATIAL_IO_PORT( ContainerSpatialIOPort.class, TileSpatialIOPort.class, GuiHostType.WORLD, SecurityPermissions.BUILD ),

	GUI_INSCRIBER( ContainerInscriber.class, TileInscriber.class, GuiHostType.WORLD, null ),

	GUI_CELL_WORKBENCH( ContainerCellWorkbench.class, TileCellWorkbench.class, GuiHostType.WORLD, null ),

	GUI_MAC( ContainerMAC.class, TileMolecularAssembler.class, GuiHostType.WORLD, null ),

	GUI_CRAFTING_AMOUNT( ContainerCraftAmount.class, ITerminalHost.class, GuiHostType.ITEM_OR_WORLD, SecurityPermissions.CRAFT ),

	GUI_CRAFTING_CONFIRM( ContainerCraftConfirm.class, ITerminalHost.class, GuiHostType.ITEM_OR_WORLD, SecurityPermissions.CRAFT ),

	GUI_INTERFACE_TERMINAL( ContainerInterfaceTerminal.class, PartInterfaceTerminal.class, GuiHostType.WORLD, SecurityPermissions.BUILD ),

	GUI_CRAFTING_STATUS( ContainerCraftingStatus.class, ITerminalHost.class, GuiHostType.ITEM_OR_WORLD, SecurityPermissions.CRAFT ),

	GUI_WIRELESS_CRAFTING_TERM( ContainerWirelessCraftingTerminal.class, WirelessTerminalGuiObject.class, GuiHostType.ITEM, null ),

	;

	private final Class tileClass;
	private final Class containerClass;
	private Class guiClass;
	private GuiHostType type;
	private SecurityPermissions requiredPermission;

	GuiBridge()
	{
		this.tileClass = null;
		this.guiClass = null;
		this.containerClass = null;
	}

	GuiBridge( final Class containerClass, final SecurityPermissions requiredPermission )
	{
		this.requiredPermission = requiredPermission;
		this.containerClass = containerClass;
		this.tileClass = null;
		this.getGui();
	}

	/**
	 * I honestly wish I could just use the GuiClass Names myself, but I can't access them without MC's Server
	 * Exploding.
	 */
	private void getGui()
	{
		if( Platform.isClient() )
		{
			AEBaseGui.class.getName();

			final String start = this.containerClass.getName();
			final String guiClass = start.replaceFirst( "container.", "client.gui." ).replace( ".Container", ".Gui" );

			if( start.equals( guiClass ) )
			{
				throw new IllegalStateException( "Unable to find gui class" );
			}
			this.guiClass = ReflectionHelper.getClass( this.getClass().getClassLoader(), guiClass );
			if( this.guiClass == null )
			{
				throw new IllegalStateException( "Cannot Load class: " + guiClass );
			}
		}
	}

	GuiBridge( final Class containerClass, final Class tileClass, final GuiHostType type, final SecurityPermissions requiredPermission )
	{
		this.requiredPermission = requiredPermission;
		this.containerClass = containerClass;
		this.type = type;
		this.tileClass = tileClass;
		this.getGui();
	}

	@Override
	public Object getServerGuiElement( final int modGuiID, final EntityPlayer player, final World w, final int x, final int y, final int z )
	{
		final GuiBridge ID = decodeModGuiType( modGuiID );
		if( ID.type.isItem() )
		{
			int invSlot = decodeModGui2( modGuiID );
			ItemStack it = ItemStack.EMPTY;
			IItemHandler playerInv = player.getCapability( CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null );//combined inv handler
			if ( playerInv == null )
			{
				AELog.error( "Could not get player inv handler!" );
				return new ContainerNull();
			}
			if( invSlot == -1 )
			{
				AELog.error( "Should have been supplied slotid, but wasn't" );
				it = player.inventory.getCurrentItem();
			}
			else if( invSlot >= 0 && invSlot < playerInv.getSlots() )
			{
				it = playerInv.getStackInSlot( invSlot );
			} else if ( Capabilities.CAPABILITY_BAUBLES != null){
				IBaublesItemHandler handler = player.getCapability( Capabilities.CAPABILITY_BAUBLES, null );
				if (handler != null)
				{
					it = handler.getStackInSlot( invSlot - playerInv.getSlots() );
				}
			}
			final Object myItem = this.getGuiObject( it, player, w, (it.getItem() instanceof ToolNetworkTool ) ? x : invSlot, y, z );
			if( myItem != null && ID.CorrectTileOrPart( myItem ) )
			{
				return this.updateGui( ID.ConstructContainer( player.inventory, AEPartLocation.INTERNAL, myItem ), w, x, y, z, AEPartLocation.INTERNAL, myItem );
			}
		}
		if( ID.type.isTile() )
		{
			final AEPartLocation side = AEPartLocation.fromOrdinal( decodeModGui2( modGuiID ) );
			final TileEntity TE = w.getTileEntity( new BlockPos( x, y, z ) );
			if( TE instanceof IPartHost )
			{
				( (IPartHost) TE ).getPart( side );
				final IPart part = ( (IPartHost) TE ).getPart( side );
				if( ID.CorrectTileOrPart( part ) )
				{
					return this.updateGui( ID.ConstructContainer( player.inventory, side, part ), w, x, y, z, side, part );
				}
			}
			else
			{
				if( ID.CorrectTileOrPart( TE ) )
				{
					return this.updateGui( ID.ConstructContainer( player.inventory, side, TE ), w, x, y, z, side, TE );
				}
			}
		}
		return new ContainerNull();
	}

	private Object getGuiObject( final ItemStack it, final EntityPlayer player, final World w, final int x, final int y, final int z )
	{
		if (!it.isEmpty()) {
			if (it.getItem() instanceof IGuiItem) {
				return ((IGuiItem) it.getItem()).getGuiObject(it, w, new BlockPos(x, y, z));
			}

			final IWirelessTermHandler wh = AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
			if (wh != null) {
				if (wh instanceof WirelessCraftingTerminal){
					return new WirelessCraftingTerminalGuiObject( wh, it, player, w, x );
				}
				return new WirelessTerminalGuiObject(wh, it, player, w, x);//x == slot!
			}
		}

		return null;
	}

	public boolean CorrectTileOrPart( final Object tE )
	{
		if( this.tileClass == null )
		{
			throw new IllegalArgumentException( "This Gui Cannot use the standard Handler." );
		}

		return this.tileClass.isInstance( tE );
	}

	private Object updateGui( final Object newContainer, final World w, final int x, final int y, final int z, final AEPartLocation side, final Object myItem )
	{
		if( newContainer instanceof AEBaseContainer )
		{
			final AEBaseContainer bc = (AEBaseContainer) newContainer;
			bc.setOpenContext( new ContainerOpenContext( myItem ) );
			bc.getOpenContext().setWorld( w );
			bc.getOpenContext().setX( x );
			bc.getOpenContext().setY( y );
			bc.getOpenContext().setZ( z );
			bc.getOpenContext().setSide( side );
		}

		return newContainer;
	}

	public Object ConstructContainer( final InventoryPlayer inventory, final AEPartLocation side, final Object tE )
	{
		try
		{
			final Constructor[] c = this.containerClass.getConstructors();
			if( c.length == 0 )
			{
				throw new AppEngException( "Invalid Gui Class" );
			}

			final Constructor target = this.findConstructor( c, inventory, tE );

			if( target == null )
			{
				throw new IllegalStateException( "Cannot find " + this.containerClass.getName() + "( " + this.typeName( inventory ) + ", " + this.typeName( tE ) + " )" );
			}

			final Object o = target.newInstance( inventory, tE );

			/**
			 * triggers achievement when the player sees presses.
			 */
			if( o instanceof AEBaseContainer )
			{
				final AEBaseContainer bc = (AEBaseContainer) o;
				for( final Object so : bc.inventorySlots )
				{
					if( so instanceof Slot )
					{
						final ItemStack is = ( (Slot) so ).getStack();

						final IMaterials materials = AEApi.instance().definitions().materials();
						this.addPressAchievementToPlayer( is, materials, inventory.player );
					}
				}
			}

			return o;
		}
		catch( final Throwable t )
		{
			throw new IllegalStateException( t );
		}
	}

	private Constructor findConstructor( final Constructor[] c, final InventoryPlayer inventory, final Object tE )
	{
		for( final Constructor con : c )
		{
			final Class[] types = con.getParameterTypes();
			if( types.length == 2 )
			{
				if( types[0].isAssignableFrom( inventory.getClass() ) && types[1].isAssignableFrom( tE.getClass() ) )
				{
					return con;
				}
			}
		}
		return null;
	}

	private String typeName( final Object inventory )
	{
		if( inventory == null )
		{
			return "NULL";
		}

		return inventory.getClass().getName();
	}

	private void addPressAchievementToPlayer( final ItemStack newItem, final IMaterials possibleMaterials, final EntityPlayer player )
	{
		final IComparableDefinition logic = possibleMaterials.logicProcessorPress();
		final IComparableDefinition eng = possibleMaterials.engProcessorPress();
		final IComparableDefinition calc = possibleMaterials.calcProcessorPress();
		final IComparableDefinition silicon = possibleMaterials.siliconPress();

		final List<IComparableDefinition> presses = Lists.newArrayList( logic, eng, calc, silicon );

		for( final IComparableDefinition press : presses )
		{
			if( press.isSameAs( newItem ) )
			{
				Achievements.Presses.addToPlayer( player );

				return;
			}
		}
	}

	@Override
	public Object getClientGuiElement( final int modGuiID, final EntityPlayer player, final World w, final int x, final int y, final int z )
	{
		final GuiBridge ID = decodeModGuiType( modGuiID );
		if( ID.type.isItem() )
		{
			int invSlot = decodeModGui2( modGuiID );
			ItemStack it = ItemStack.EMPTY;
			IItemHandler playerInv = player.getCapability( CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null );//combined inv handler
			if ( playerInv == null )
			{
				AELog.error( "Could not get player inv handler!" );
				return new GuiNull( new ContainerNull() );
			}
			if( invSlot == -1 )
			{
				AELog.error( "Should have been supplied slotid, but wasn't" );
				it = player.inventory.getCurrentItem();
			}
			else if( invSlot >= 0 && invSlot < playerInv.getSlots() )
			{
				it = playerInv.getStackInSlot( invSlot );
			} else if (Capabilities.CAPABILITY_BAUBLES != null){
				IBaublesItemHandler handler = player.getCapability( Capabilities.CAPABILITY_BAUBLES, null );
				if (handler != null)
				{
					it = handler.getStackInSlot( invSlot - playerInv.getSlots() );
				}
			}
			final Object myItem = this.getGuiObject( it, player, w, (it.getItem() instanceof ToolNetworkTool ) ? x : invSlot, y, z );
			if( myItem != null && ID.CorrectTileOrPart( myItem ) )
			{
				return ID.ConstructGui( player.inventory, AEPartLocation.INTERNAL, myItem );
			}
		}
		if( ID.type.isTile() )
		{
			final AEPartLocation side = AEPartLocation.fromOrdinal( decodeModGui2( modGuiID ) );
			final TileEntity TE = w.getTileEntity( new BlockPos( x, y, z ) );
			if( TE instanceof IPartHost )
			{
				( (IPartHost) TE ).getPart( side );
				final IPart part = ( (IPartHost) TE ).getPart( side );
				if( ID.CorrectTileOrPart( part ) )
				{
					return ID.ConstructGui( player.inventory, side, part );
				}
			}
			else
			{
				if( ID.CorrectTileOrPart( TE ) )
				{
					return ID.ConstructGui( player.inventory, side, TE );
				}
			}
		}
		return new GuiNull( new ContainerNull() );
	}

	public Object ConstructGui( final InventoryPlayer inventory, final AEPartLocation side, final Object tE )
	{
		try
		{
			final Constructor[] c = this.guiClass.getConstructors();
			if( c.length == 0 )
			{
				throw new AppEngException( "Invalid Gui Class" );
			}

			final Constructor target = this.findConstructor( c, inventory, tE );

			if( target == null )
			{
				throw new IllegalStateException( "Cannot find " + this.containerClass.getName() + "( " + this.typeName( inventory ) + ", " + this.typeName( tE ) + " )" );
			}

			return target.newInstance( inventory, tE );
		}
		catch( final Throwable t )
		{
			throw new IllegalStateException( t );
		}
	}

	public boolean hasPermissions( final TileEntity te, final int x, final int y, final int z, final AEPartLocation side, final EntityPlayer player )
	{
		final World w = player.getEntityWorld();
		final BlockPos pos = new BlockPos( x, y, z );

		if( Platform.hasPermissions( te != null ? new DimensionalCoord( te ) : new DimensionalCoord( player.world, pos ), player ) )
		{
			if( this.type.isItem() )
			{
				final ItemStack it = player.inventory.getCurrentItem();
				if(!it.isEmpty() && it.getItem() instanceof IGuiItem )
				{
					final Object myItem = ( (IGuiItem) it.getItem() ).getGuiObject( it, w, pos );
					if( this.CorrectTileOrPart( myItem ) )
					{
						return true;
					}
				}
			}

			if( this.type.isTile() )
			{
				final TileEntity TE = w.getTileEntity( pos );
				if( TE instanceof IPartHost )
				{
					( (IPartHost) TE ).getPart( side );
					final IPart part = ( (IPartHost) TE ).getPart( side );
					if( this.CorrectTileOrPart( part ) )
					{
						return this.securityCheck( part, player );
					}
				}
				else
				{
					if( this.CorrectTileOrPart( TE ) )
					{
						return this.securityCheck( TE, player );
					}
				}
			}
		}
		return false;
	}

	private boolean securityCheck( final Object te, final EntityPlayer player )
	{
		if( te instanceof IActionHost && this.requiredPermission != null )
		{

			final IGridNode gn = ( (IActionHost) te ).getActionableNode();
			if( gn != null )
			{
				final IGrid g = gn.getGrid();
				if( g != null )
				{
					final boolean requirePower = false;
					if( requirePower )
					{
						final IEnergyGrid eg = g.getCache( IEnergyGrid.class );
						if( !eg.isNetworkPowered() )
						{
							return false;
						}
					}

					final ISecurityGrid sg = g.getCache( ISecurityGrid.class );
					if( sg.hasPermission( player, this.requiredPermission ) )
					{
						return true;
					}
				}
			}

			return false;
		}
		return true;
	}

	public GuiHostType getType()
	{
		return this.type;
	}

	public static int encodeModGui( GuiBridge type, short short2 )
	{
		return ( short2 << 16 ) | (short)type.ordinal();
	}

	public static GuiBridge decodeModGuiType( int modguiID )
	{
		int id = modguiID & 0xFFFF;
		if ( id > GuiBridge.GUI_Handler.ordinal() && id < GuiBridge.values().length )
			return GuiBridge.values()[id];
		throw new IllegalStateException( "Invalid modgui id supplied" );
	}

	public static short decodeModGui2( int modguiID )
	{
		return (short)( ( modguiID >> 16 ) & 0xFFFF);
	}
}
