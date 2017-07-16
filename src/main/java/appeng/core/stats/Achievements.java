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

package appeng.core.stats;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import appeng.core.AppEng;


/**
 * Since 1.12 these are only the advancements that require custom triggering, the rest are done in JSON
 */
public enum Achievements
{
	// done
	SpatialIOExplorer( "spatial_io_explorer" ),

	// done
	Networking1( "networking_1" ),

	// done
	Networking2( "networking_2" ),

	// done
	Networking3( "networking_3" ),

	Recursive( "recursive" )
	;

	private final ResourceLocation advancementName;

	//private Achievement parent;
	//private Achievement stat;

	Achievements( String advancementName )
	{
		this.advancementName = new ResourceLocation( AppEng.MOD_ID, advancementName );
	}

	public void addToPlayer(EntityPlayerMP p){
		Advancement a = p.getServerWorld().getAdvancementManager().getAdvancement( this.advancementName );
		if (a != null)
		{
			PlayerAdvancements adv = p.getAdvancements();
			AdvancementProgress prog = adv.getProgress( a );
			for ( String c : prog.getRemaningCriteria() ){
				adv.grantCriterion( a, c );
			}

		}
	}
}
