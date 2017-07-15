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
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

import appeng.core.AppEng;


public enum Achievements
{
	// done
	SpatialIOExplorer( -4, -2, AchievementType.Custom, "spatial_io_explorer" ),

	// done
	Networking1( 4, -6, AchievementType.Custom, "networking_1" ),

	// done
	Networking2( 4, 0, AchievementType.Custom, "networking_2" ),

	// done
	Networking3( 4, 2, AchievementType.Custom, "networking_3" ),
	;

	private final AchievementType type;
	private final int x;
	private final int y;
	private final ResourceLocation advancementName;

	//private Achievement parent;
	//private Achievement stat;

	Achievements( final int x, final int y, final AchievementType type, String advancementName )
	{
		this.type = type;
		this.x = x;
		this.y = y;
		this.advancementName = new ResourceLocation( AppEng.MOD_ID, advancementName );
	}

	AchievementType getType()
	{
		return this.type;
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
