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

package appeng.integration;


import appeng.integration.abstraction.IBuildcraft;
import appeng.integration.abstraction.ICofhHammer;
import appeng.integration.abstraction.IExU2;
import appeng.integration.abstraction.IIC2;
import appeng.integration.abstraction.IInvTweaks;
import appeng.integration.abstraction.IJEI;
import appeng.integration.abstraction.IMekanism;
import appeng.integration.abstraction.IRC;


/**
 * Provides convenient access to various integrations with other mods.
 */
public final class Integrations
{

	static IIC2 ic2 = new IIC2.Stub();

	static IJEI jei = new IJEI.Stub();

	static IRC rc = new IRC.Stub();

	static IBuildcraft bc = new IBuildcraft.Stub();

	static ICofhHammer cofhHammer = new ICofhHammer.Stub();

	static IMekanism mekanism = new IMekanism.Stub();

	static IInvTweaks invTweaks = new IInvTweaks.Stub();

	static IExU2 exu2 = new IExU2.Stub();

	private Integrations()
	{
	}

	public static IIC2 ic2()
	{
		return ic2;
	}

	public static IJEI jei()
	{
		return jei;
	}

	public static IRC rc()
	{
		return rc;
	}

	public static IBuildcraft bc()
	{
		return bc;
	}

	public static ICofhHammer cofhHammer()
	{
		return cofhHammer;
	}

	public static IMekanism mekanism()
	{
		return mekanism;
	}

	public static IInvTweaks invTweaks()
	{
		return invTweaks;
	}

	public static IExU2 exu2(){ return exu2; }

	static IIC2 setIc2( IIC2 ic2 )
	{
		Integrations.ic2 = ic2;
		return ic2;
	}

	static IJEI setJei( IJEI jei )
	{
		Integrations.jei = jei;
		return jei;
	}

	static IRC setRc( IRC rc )
	{
		Integrations.rc = rc;
		return rc;
	}

	static IBuildcraft setBc( IBuildcraft rc )
	{
		Integrations.bc = bc;
		return bc;
	}

	static ICofhHammer setCofhHammer( ICofhHammer ch )
	{
		Integrations.cofhHammer = ch;
		return ch;
	}

	static IMekanism setMekanism( IMekanism mekanism )
	{
		Integrations.mekanism = mekanism;
		return mekanism;
	}

	static IInvTweaks setInvTweaks( IInvTweaks invTweaks )
	{
		Integrations.invTweaks = invTweaks;
		return invTweaks;
	}

	static IExU2 setExU2( IExU2 exu2 ){
		Integrations.exu2 = exu2;
		return exu2;
	}

}
