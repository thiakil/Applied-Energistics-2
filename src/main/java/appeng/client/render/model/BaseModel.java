package appeng.client.render.model;


import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nonnull;

import com.google.common.base.Function;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;


/**
 * Helper superclass for IModels that load a base model and then do extra things to it.
 * @author Thiakil
 * Adapted from existing code.
 * @since rv5
 */
public abstract class BaseModel implements IModel
{
	private IModel baseModel;
	protected final ResourceLocation baseLocation;

	public BaseModel( @Nonnull ResourceLocation base )
	{
		baseLocation = base;
	}

	@Override
	public IModelState getDefaultState()
	{
		return getBaseModel().getDefaultState();
	}

	protected IBakedModel getBakedBaseModel( IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter )
	{
		// Bake the base model
		try
		{
			return getBaseModel().bake( state, format, bakedTextureGetter );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	@Override
	public Collection<ResourceLocation> getDependencies()
	{
		return Collections.singletonList(baseLocation);
	}

	protected IModel getBaseModel()
	{
		if ( baseModel == null )
		{
			try
			{
				baseModel = ModelLoaderRegistry.getModel( baseLocation );
			}
			catch( Exception e )
			{
				throw new RuntimeException( e );
			}
		}
		return baseModel;
	}
}
