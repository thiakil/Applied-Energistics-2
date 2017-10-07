/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.items.tools.powered;


import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaubleItem;
import baubles.api.render.IRenderBauble;

import appeng.api.AEApi;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.util.IConfigManager;
import appeng.capabilities.Capabilities;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;

@Optional.Interface( iface = "baubles.api.render.IRenderBauble", modid = "baubles" )
public class ToolWirelessTerminal extends AEBasePoweredItem implements IWirelessTermHandler, IRenderBauble
{

	private static String EGG_KEY = "hasQuantumEgg";

	public ToolWirelessTerminal()
	{
		super( AEConfig.instance().getWirelessTerminalBattery() );
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick( final World w, final EntityPlayer player, final EnumHand hand )
	{
		AEApi.instance().registries().wireless().openWirelessTerminalGui( player.getHeldItem( hand ), w, player );
		return new ActionResult<ItemStack>( EnumActionResult.SUCCESS, player.getHeldItem( hand ) );
	}

	@SideOnly( Side.CLIENT )
	@Override
	public boolean isFull3D()
	{
		return false;
	}

	@Override
	public void addCheckedInformation( final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
	{
		super.addCheckedInformation( stack, player, lines, displayMoreInfo );

		if( stack.hasTagCompound() )
		{
			final NBTTagCompound tag = Platform.openNbtData( stack );
			if( tag != null )
			{
				final String encKey = tag.getString( "encryptionKey" );

				if( encKey == null || encKey.isEmpty() )
				{
					lines.add( GuiText.Unlinked.getLocal() );
				}
				else
				{
					lines.add( GuiText.Linked.getLocal() );
				}

				if (getHasQuantumEgg( stack )){
					lines.add( net.minecraft.client.resources.I18n.format( "gui.tooltips.appliedenergistics2.egg_upgrade" ));
				}
			}
		}
		else
		{
			lines.add( I18n.translateToLocal( "AppEng.GuiITooltip.Unlinked" ) );
		}
	}

	@Override
	public boolean canHandle( final ItemStack is )
	{
		return AEApi.instance().definitions().items().wirelessTerminal().isSameAs( is );
	}

	@Override
	public boolean usePower( final EntityPlayer player, final double amount, final ItemStack is )
	{
		return this.extractAEPower( is, amount ) >= amount - 0.5;
	}

	@Override
	public boolean hasPower( final EntityPlayer player, final double amt, final ItemStack is )
	{
		return this.getAECurrentPower( is ) >= amt;
	}

	@Override
	public IConfigManager getConfigManager( final ItemStack target )
	{
		final ConfigManager out = new ConfigManager( new IConfigManagerHost(){

			@Override
			public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
			{
				final NBTTagCompound data = Platform.openNbtData( target );
				manager.writeToNBT( data );
			}
		} );

		out.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		out.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		out.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );

		out.readFromNBT( (NBTTagCompound) Platform.openNbtData( target ).copy() );
		return out;
	}

	@Override
	public String getEncryptionKey( final ItemStack item )
	{
		final NBTTagCompound tag = Platform.openNbtData( item );
		return tag.getString( "encryptionKey" );
	}

	@Override
	public void setEncryptionKey( final ItemStack item, final String encKey, final String name )
	{
		final NBTTagCompound tag = Platform.openNbtData( item );
		tag.setString( "encryptionKey", encKey );
		tag.setString( "name", name );
	}

    @Override
    public boolean shouldCauseReequipAnimation( ItemStack oldStack, ItemStack newStack, boolean slotChanged )
	{
        return slotChanged;
    }

	public static void setHasQuantumEgg(ItemStack is, boolean val){
		if (is.getTagCompound() == null){
			is.setTagCompound( new NBTTagCompound() );
		}
		is.getTagCompound().setBoolean( EGG_KEY, val );
	}

	public static boolean getHasQuantumEgg(ItemStack is){
		if (is.getTagCompound() != null && is.getTagCompound().hasKey( EGG_KEY )){
			return is.getTagCompound().getBoolean( EGG_KEY );
		}
		return false;
	}

	@Override
	public ICapabilityProvider initCapabilities( ItemStack stack, NBTTagCompound nbt )
	{
		ICapabilityProvider parent = super.initCapabilities( stack, nbt );

		return Capabilities.CAPABILITY_ITEM_BAUBLE != null ? new BaubleHandler(parent) : parent;
	}

	@SideOnly( Side.CLIENT )
	@Optional.Method( modid = "baubles" )
	@Override
	public void onPlayerBaubleRender( ItemStack stack, EntityPlayer player, RenderType type, float partialTicks )
	{
		if (type == RenderType.HEAD)
		{
			IRenderBauble.Helper.translateToHeadLevel( player );
			if( player.isSneaking() )
			{
				IRenderBauble.Helper.rotateIfSneaking( player );
			}
			Minecraft.getMinecraft().getRenderItem().renderItem( stack, ItemCameraTransforms.TransformType.HEAD );
		}
	}

	private static class BaubleHandler implements ICapabilityProvider {

		private final @Nullable ICapabilityProvider parent;

		private final BaubleItem bauble = new BaubleItem( BaubleType.HEAD ) {
			@Override
			public boolean willAutoSync( ItemStack itemstack, EntityLivingBase player )
			{
				return true;
			}
		};

		public BaubleHandler(ICapabilityProvider p){
			parent = p;
		}

		@Override
		public boolean hasCapability( @Nonnull Capability<?> capability, @Nullable EnumFacing facing )
		{
			return capability == Capabilities.CAPABILITY_ITEM_BAUBLE || parent != null && parent.hasCapability( capability, facing );
		}

		@Nullable
		@Override
		public <T> T getCapability( @Nonnull Capability<T> capability, @Nullable EnumFacing facing )
		{
			if (capability == Capabilities.CAPABILITY_ITEM_BAUBLE){
				return Capabilities.CAPABILITY_ITEM_BAUBLE.cast(bauble);
			}
			return parent != null ? parent.getCapability( capability, facing ) : null;
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean hasEffect( ItemStack stack )
	{
		return getHasQuantumEgg( stack );
	}
}
