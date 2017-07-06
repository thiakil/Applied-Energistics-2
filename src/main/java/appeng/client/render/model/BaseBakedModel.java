package appeng.client.render.model;


import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;


/**
 * Base model to pass on IPerspectiveAware to parent/base model
 * Copied handPerspective method from Biometric Card model by Sebastian Hartte, as it had the only correct implementation.
 *
 * @author Thiakil
 * @since rv5
 */
public abstract class BaseBakedModel implements IPerspectiveAwareModel
{
	private IBakedModel parent;

	public BaseBakedModel( IBakedModel parent )
	{
		this.parent = parent;
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective( ItemCameraTransforms.TransformType type )
	{
		// Delegate to the base model if possible
		if( parent instanceof IPerspectiveAwareModel )
		{
			IPerspectiveAwareModel pam = (IPerspectiveAwareModel) parent;
			Pair<? extends IBakedModel, Matrix4f> pair = pam.handlePerspective( type );
			return Pair.of( this, pair.getValue() );
		}
		return Pair.of( this, TRSRTransformation.identity().getMatrix() );
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return parent.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return parent.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return parent.isGui3d();
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return parent.getParticleTexture();
	}

	@Override
	@Deprecated
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return parent.getItemCameraTransforms();
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return parent.getOverrides();
	}
}
