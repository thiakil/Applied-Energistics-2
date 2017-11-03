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

package appeng.core;


import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import appeng.api.AEApi;
import appeng.core.crash.CrashInfo;
import appeng.core.crash.IntegrationCrashEnhancement;
import appeng.core.crash.ModCrashEnhancement;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.worlddata.WorldData;
import appeng.hooks.TickHandler;
import appeng.integration.IntegrationRegistry;
import appeng.recipes.CustomRecipeConfig;
import appeng.recipes.CustomRecipeForgeConfiguration;
import appeng.server.AECommand;
import appeng.services.export.ExportConfig;
import appeng.services.export.ExportProcess;
import appeng.services.export.ForgeExportConfig;
import appeng.util.Platform;


@Mod(
		modid = AppEng.MOD_ID,
		acceptedMinecraftVersions = "[1.12,1.13)",
		name = AppEng.MOD_NAME,
		version = AEConfig.VERSION,
		dependencies = AppEng.MOD_DEPENDENCIES,
		certificateFingerprint = "471969e66a70afa721905d87e393ae397574ebf7",
		guiFactory = "appeng.client.gui.config.AEConfigGuiFactory",
		updateJSON = "https://raw.githubusercontent.com/thiakil/Applied-Energistics-2/rv5-1.12/forge_update.json"
)
public final class AppEng
{
	@SidedProxy( clientSide = "appeng.client.ClientHelper", serverSide = "appeng.server.ServerHelper", modId = AppEng.MOD_ID )
	public static CommonHelper proxy;
	
	public static final String MOD_ID = "appliedenergistics2";
	public static final String MOD_NAME = "Applied Llamagistics";

	public static final String ASSETS = "appliedenergistics2:";

	public static final String MOD_DEPENDENCIES =
			// a few mods, AE should load after, probably.
			// required-after:AppliedEnergistics2API|all;
			"after:gregtech_addon;after:mekanism;after:ic2;after:thermalexpansion;after:buildcraft;after:opencomputers@[1.7.0,];" +
			"after:baubles@[1.5.1,];" +
			"after:jei@[4.7.6,];" +//needed for the universal transfer handler
			// depend on version of forge used for build.
			"required-after:appliedenergistics2-core;";// + "required-after:Forge@[" // require forge.
					//+ net.minecraftforge.common.ForgeVersion.majorVersion + '.' // majorVersion
					//+ net.minecraftforge.common.ForgeVersion.minorVersion + '.' // minorVersion
					//+ net.minecraftforge.common.ForgeVersion.revisionVersion + '.' // revisionVersion
					//+ net.minecraftforge.common.ForgeVersion.buildVersion + ",)"; // buildVersion

	@Nonnull
	private static final AppEng INSTANCE = new AppEng();

	private final Registration registration;

	private File configDirectory;
	private CustomRecipeConfig customRecipeConfig;

	/**
	 * Folder for recipes
	 *
	 * used for CSV item names and the recipes
	 */
	private File recipeDirectory;

	/**
	 * determined in pre-init but used in init
	 */
	private ExportConfig exportConfig;

	private AppEng()
	{
		FMLCommonHandler.instance().registerCrashCallable( new ModCrashEnhancement( CrashInfo.MOD_VERSION ) );

		this.registration = new Registration();
	}

	@Nonnull
	@Mod.InstanceFactory
	public static AppEng instance()
	{
		return INSTANCE;
	}

	@Nonnull
	public final Registration getRegistration()
	{
		return this.registration;
	}

	@EventHandler
	private void preInit( final FMLPreInitializationEvent event )
	{
		if( !Loader.isModLoaded( "appliedenergistics2-core" ) )
		{
			AppEng.proxy.missingCoreMod();
		}

		final Stopwatch watch = Stopwatch.createStarted();
		this.configDirectory = new File( event.getModConfigurationDirectory().getPath(), "AppliedEnergistics2" );
		this.recipeDirectory = new File( this.configDirectory, "recipes" );

		final File configFile = new File( this.configDirectory, "AppliedEnergistics2.cfg" );
		final File facadeFile = new File( this.configDirectory, "Facades.cfg" );
		final File recipeFile = new File( this.configDirectory, "CustomRecipes.cfg" );
		final Configuration recipeConfiguration = new Configuration( recipeFile );

		AEConfig.init( configFile );
		FacadeConfig.init( facadeFile );

		this.customRecipeConfig = new CustomRecipeForgeConfiguration( recipeConfiguration );
		this.exportConfig = new ForgeExportConfig( recipeConfiguration );

		AELog.debug( "Pre Initialization ( started )" );

		CreativeTab.init();
		if( AEConfig.instance().isFeatureEnabled( AEFeature.FACADES ) )
		{
			CreativeTabFacade.init();
		}

		this.registration.preInitialize( event, this.recipeDirectory, this.customRecipeConfig );

		AppEng.proxy.preinit();

		AELog.debug( "Pre Initialization ( ended after " + watch.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );

		// Instantiate all Plugins
		List<Object> injectables = Lists.newArrayList(
				AEApi.instance() );
		new PluginLoader().loadPlugins( injectables, event.getAsmData() );
		IntegrationRegistry.INSTANCE.preInit();
	}

	private void startService( final String serviceName, final Thread thread )
	{
		thread.setName( serviceName );
		thread.setPriority( Thread.MIN_PRIORITY );

		AELog.info( "Starting " + serviceName );
		thread.start();
	}

	@EventHandler
	private void init( final FMLInitializationEvent event )
	{
		final Stopwatch start = Stopwatch.createStarted();
		AELog.debug( "Initialization ( started )" );

		if( this.exportConfig.isExportingItemNamesEnabled() )
		{
			if( FMLCommonHandler.instance().getSide().isClient() )
			{
				final ExportProcess process = new ExportProcess( this.recipeDirectory, this.exportConfig );
				final Thread exportProcessThread = new Thread( process );

				this.startService( "AE2 CSV Export", exportProcessThread );
			}
			else
			{
				AELog.info( "Disabling item.csv export for custom recipes, since creative tab information is only available on the client." );
			}
		}

		this.registration.initialize( event );
		IntegrationRegistry.INSTANCE.init();

		AppEng.proxy.init();

		AELog.debug( "Initialization ( ended after " + start.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );
	}

	@EventHandler
	private void postInit( final FMLPostInitializationEvent event )
	{
		final Stopwatch start = Stopwatch.createStarted();
		AELog.debug( "Post Initialization ( started )" );

		this.registration.postInit( event );
		IntegrationRegistry.INSTANCE.postInit();
		FMLCommonHandler.instance().registerCrashCallable( new IntegrationCrashEnhancement() );

		AppEng.proxy.postInit();
		AEConfig.instance().save();

		NetworkRegistry.INSTANCE.registerGuiHandler( this, GuiBridge.GUI_Handler );
		NetworkHandler.init( "AE2" );

		AELog.debug( "Post Initialization ( ended after " + start.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );
	}

	@EventHandler
	private void handleIMCEvent( final FMLInterModComms.IMCEvent event )
	{
		final IMCHandler imcHandler = new IMCHandler();

		imcHandler.handleIMCEvent( event );
	}

	@EventHandler
	private void serverAboutToStart( final FMLServerAboutToStartEvent evt )
	{
		WorldData.onServerAboutToStart( evt.getServer() );
	}

	@EventHandler
	private void serverStopping( final FMLServerStoppingEvent event )
	{
		WorldData.instance().onServerStopping();
	}

	@EventHandler
	private void serverStopped( final FMLServerStoppedEvent event )
	{
		WorldData.instance().onServerStoppped();
		TickHandler.INSTANCE.shutdown();
	}

	@EventHandler
	private void serverStarting( final FMLServerStartingEvent evt )
	{
		evt.registerServerCommand( new AECommand( evt.getServer() ) );
	}

	@EventHandler
	void fingerprintError(final FMLFingerprintViolationEvent ev){
		AELog.error("Fingerprint did not match. En Garde! Be wary of where you got this mod from!");
		AELog.info( "Found signatures:" );
		for (String s : ev.getFingerprints()){
			AELog.info( s );
		}
	}
}
