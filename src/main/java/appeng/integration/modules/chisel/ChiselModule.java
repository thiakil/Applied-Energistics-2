package appeng.integration.modules.chisel;


import net.minecraftforge.fml.common.event.FMLInterModComms;

import appeng.api.definitions.IBlockDefinition;
import appeng.core.Api;
import appeng.core.api.definitions.ApiBlocks;
import appeng.integration.IIntegrationModule;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;


/**
 * Created by Thiakil on 3/08/2017.
 * Registers Smooth/Brick/SmallBrick variants with chisel. Unsmelted sky stone not included.
 */
public class ChiselModule implements IIntegrationModule
{
	public ChiselModule() {
		try
		{
			Class.forName( "team.chisel.api.ChiselAPIProps" );
		}
		catch( ClassNotFoundException e )
		{
			throw new RuntimeException( e );
		}
	}

	@Override
	public void init() throws Throwable
	{
		ApiBlocks blocks = Api.INSTANCE.definitions().blocks();

		addBlockToGroup( blocks.smoothSkyStoneBlock(), "skyStone" );
		addBlockToGroup( blocks.skyStoneBrick(), "skyStone" );
		addBlockToGroup( blocks.skyStoneSmallBrick(), "skyStone" );

		addBlockToGroup( blocks.smoothSkyStoneSlab(), "skyStoneSlab" );
		addBlockToGroup( blocks.skyStoneSmallBrickSlab(), "skyStoneSlab" );
		addBlockToGroup( blocks.skyStoneBrickSlab(), "skyStoneSlab" );

		addBlockToGroup( blocks.smoothSkyStoneStairs(), "skyStoneStair" );
		addBlockToGroup( blocks.skyStoneSmallBrickStairs(), "skyStoneStair" );
		addBlockToGroup( blocks.skyStoneBrickStairs(), "skyStoneStair" );

		addBlockToGroup( blocks.quartzBlock(), "certusQuartzBlock" );
		addBlockToGroup( blocks.quartzPillar(), "certusQuartzBlock" );
		addBlockToGroup( blocks.chiseledQuartzBlock(), "certusQuartzBlock" );

		addBlockToGroup( blocks.quartzSlab(), "certusQuartzSlab" );
		addBlockToGroup( blocks.quartzPillarSlab(), "certusQuartzSlab" );
		addBlockToGroup( blocks.chiseledQuartzSlab(), "certusQuartzSlab" );

		addBlockToGroup( blocks.quartzStairs(), "certusQuartzStair" );
		addBlockToGroup( blocks.quartzPillarStairs(), "certusQuartzStair" );
		addBlockToGroup( blocks.chiseledQuartzStairs(), "certusQuartzStair" );
	}

	private void addBlockToGroup(IBlockDefinition block, String group){
		block.maybeStack( 1 ).ifPresent( stack ->{
			//"mygroup|minecraft:dirt|1"
			FMLInterModComms.sendMessage( ChiselIMC.CHISEL_MODID, ChiselIMC.ADD_VARIATION.toString(), group+"|"+ stack.getItem().getRegistryName().toString()+"|"+stack.getMetadata() );
		});
	}
}
