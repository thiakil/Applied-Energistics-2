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

package appeng.client.gui.implementations;


import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.config.Settings;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerMEPortableCell;
import appeng.helpers.WirelessTerminalGuiObject;


public class GuiMEPortableCell extends GuiMEMonitorable
{

	private GuiImgButton pickupMode;
	private final IConfigManager configSrc;
	private final IPortableCell cell;

	public GuiMEPortableCell( final InventoryPlayer inventoryPlayer, final IPortableCell te )
	{
		super( inventoryPlayer, te, new ContainerMEPortableCell( inventoryPlayer, te ) );
		this.configSrc = ( (IConfigurableObject) this.inventorySlots ).getConfigManager();
		this.cell = te;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		if (!(cell instanceof WirelessTerminalGuiObject ))
		{
			this.buttonList.add( this.pickupMode = new GuiImgButton( this.guiLeft - 18 * 2, this.guiTop + 8, Settings.PORTABLE_CELL_AUTOPICKUP, this.configSrc.getSetting( Settings.PORTABLE_CELL_AUTOPICKUP ) ) );
		}
	}

	int defaultGetMaxRows()
	{
		return super.getMaxRows();
	}

	@Override
	int getMaxRows()
	{
		return 3;
	}
}
