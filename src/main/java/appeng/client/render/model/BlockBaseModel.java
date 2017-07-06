package appeng.client.render.model;


import net.minecraft.util.ResourceLocation;


/**
 * Extension of the base model proxy to use just the default states & transforms of a basic block.
 */
public abstract class BlockBaseModel extends BaseModel
{
	private static final ResourceLocation blockModel = new ResourceLocation( "block/block" );

	public BlockBaseModel()
	{
		super(blockModel);
	}
}
