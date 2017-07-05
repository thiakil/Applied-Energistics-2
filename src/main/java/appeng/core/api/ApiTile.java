package appeng.core.api;


import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.tileentity.TileEntity;

import appeng.core.AELog;
import appeng.tile.AEBaseTile;
import appeng.tile.layers.TileLayerBase;


/**
 * Internal class that can combine layers on the tile entity
 */
public class ApiTile
{
	private final LoadingCache<ApiTile.CacheKey, Class<? extends AEBaseTile>> cache = CacheBuilder.newBuilder().build( new CacheLoader<ApiTile.CacheKey, Class<? extends AEBaseTile>>()
	{
		@Override
		public Class<? extends AEBaseTile> load( ApiTile.CacheKey key ) throws Exception
		{
			return generateCombinedClass( key );
		}
	} );

	private final Map<Class<?>, String> interfaces2Layer = new HashMap<>();
	private final List<String> desc = new LinkedList<String>();
	private final Map<String, Class<? extends AEBaseTile>> layerDependencies = new HashMap<>();

	/**
	 * Registers a new layer for AEBaseTile, must specify at least AEBaseTile as required super
	 * @param layer layer name
	 * @param layerInterface layer class name
	 * @param requiredSuper dependency
	 * @return true if no errors, false otherwise
	 */
	public boolean registerNewLayer( final String layer, final String layerInterface, final Class<? extends AEBaseTile> requiredSuper )
	{
		try
		{
			final Class<?> layerInterfaceClass = Class.forName( layerInterface );
			if( this.interfaces2Layer.get( layerInterfaceClass ) == null )
			{
				this.interfaces2Layer.put( layerInterfaceClass, layer );
				this.layerDependencies.put( layerInterface, requiredSuper );
				this.desc.add( layerInterface );
				return true;
			}
			else
			{
				AELog.info( "Layer " + layer + " not registered, " + layerInterface + " already has a layer." );
			}
		}
		catch( final Throwable ignored )
		{
		}

		return false;
	}

	/**
	 * Conceptually this method will build a new class hierarchy that is rooted at the given base class, and includes a chain of all registered layers.
	 * <p/>
	 * To accomplish this, it takes the first registered layer, replaces it's inheritance from LayerBase with an inheritance from the given baseClass,
	 * and uses the resulting class as the parent class for the next registered layer, for which it repeats this process. This process is then repeated
	 * until a class hierarchy of all layers is formed. While janking out the inheritance from LayerBase, it'll make also sure that calls to that
	 * classes method will instead be forwarded to the superclass that was inserted as part of the described process.
	 * <p/>
	 * Example: If layers A and B are registered, and TileCableBus is passed in as the baseClass, a synthetic class A_B_TileCableBus should be returned,
	 * which has A_B_TileCableBus -extends-> B_TileCableBus -extends-> TileCableBus as it's class hierarchy, where A_B_TileCableBus has been generated
	 * from A, and B_TileCableBus has been generated from B.
	 */
	public Class<? extends AEBaseTile> getCombinedInstance( final Class<? extends AEBaseTile> baseClass )
	{
		if( this.desc.isEmpty() )
		{
			// No layers registered...
			return baseClass;
		}

		List<String> applicableLayers = new LinkedList<String>();
		for ( String layer : this.desc )
		{
			Class<? extends AEBaseTile> dep = layerDependencies.get(layer);
			if ( dep != null && dep.isAssignableFrom( baseClass ) )
			{
				AELog.info( "adding "+layer+" to "+baseClass.getName() );
				applicableLayers.add( layer );
			}
		}

		if ( applicableLayers.isEmpty() )
		{
			AELog.info( "No applicable layers for "+baseClass.getName() );
			return baseClass;
		}

		return cache.getUnchecked( new CacheKey( baseClass, applicableLayers ) );
	}

	private Class<? extends AEBaseTile> generateCombinedClass( CacheKey cacheKey )
	{
		final Class<? extends AEBaseTile> parentClass;

		// Get the list of interfaces that still need to be implemented beyond the current one
		List<String> remainingInterfaces = cacheKey.getInterfaces().subList( 1, cacheKey.getInterfaces().size() );

		// We are not at the root of the class hierarchy yet
		if( !remainingInterfaces.isEmpty() )
		{
			CacheKey parentKey = new CacheKey( cacheKey.getBaseClass(), remainingInterfaces );
			parentClass = cache.getUnchecked( parentKey );
		}
		else
		{
			parentClass = cacheKey.getBaseClass();
		}

		// Which interface should be implemented in this layer?
		String interfaceName = cacheKey.getInterfaces().get( 0 );

		try
		{
			// This is the particular interface that this layer was registered for. Loading the class may fail if i.e. an API is broken or not present
			// and in this case, the layer will be skipped!
			Class<?> interfaceClass = Class.forName( interfaceName );
			String layerImpl = this.interfaces2Layer.get( interfaceClass );

			return this.getClassByDesc( parentClass, layerImpl );
		}
		catch( final Throwable t )
		{
			AELog.warn( "Error loading " + interfaceName );
			AELog.debug( t );
			return parentClass;
		}

	}

	@SuppressWarnings( "unchecked" )
	private Class<? extends AEBaseTile> getClassByDesc( Class<? extends AEBaseTile> baseClass, final String next )
	{
		final ClassWriter cw = new ClassWriter( ClassWriter.COMPUTE_MAXS );
		final ClassNode n = this.getReader( next );
		final String originalName = n.name;

		try
		{
			n.name = n.name + '_' + baseClass.getSimpleName();
			n.superName = baseClass.getName().replace( '.', '/' );
		}
		catch( final Throwable t )
		{
			AELog.debug( t );
		}

		for( final MethodNode mn : n.methods )
		{
			final Iterator<AbstractInsnNode> i = mn.instructions.iterator();
			while( i.hasNext() )
			{
				this.processNode( i.next(), n.superName );
			}
		}

		final DefaultPackageClassNameRemapper remapper = new DefaultPackageClassNameRemapper();
		remapper.inputOutput.put( "appeng/tile/layers/TileLayerBase", n.superName );
		remapper.inputOutput.put( originalName, n.name );
		n.accept( new RemappingClassAdapter( cw, remapper ) );
		// n.accept( cw );

		// n.accept( new TraceClassVisitor( new PrintWriter( System.out ) ) );
		final byte[] byteArray = cw.toByteArray();
		final int size = byteArray.length;
		final Class clazz = this.loadClass( n.name.replace( "/", "." ), byteArray );

		try
		{
			final Object fish = clazz.newInstance();

			boolean hasError = false;

			if( !baseClass.isInstance( fish ) )
			{
				hasError = true;
				AELog.error( "Error, Expected layer to implement " + baseClass + " did not." );
			}

			if( fish instanceof TileLayerBase )
			{
				hasError = true;
				AELog.error( "Error, Expected layer to NOT implement TileLayerBase but it DID." );
			}

			if( !( fish instanceof AEBaseTile ) )
			{
				hasError = true;
				AELog.error( "Error, Expected layer to implement AEBaseTile but it did not." );
			}

			/*if( !( fish instanceof TileEntity ) )
			{
				hasError = true;
				AELog.error( "Error, Expected layer to implement TileEntity did not." );
			}*/

			if( !hasError )
			{
				AELog.info( "TileLayer: " + n.name + " loaded successfully - " + size + " bytes" );
			}
		}
		catch( final Throwable t )
		{
			AELog.error( "Layer: " + n.name + " Failed." );
			AELog.debug( t );
		}

		return clazz;
	}

	private ClassNode getReader( final String name )
	{
		final String path = '/' + name.replace( ".", "/" ) + ".class";
		final InputStream is = this.getClass().getResourceAsStream( path );
		try
		{
			final ClassReader cr = new ClassReader( is );

			final ClassNode cn = new ClassNode();
			cr.accept( cn, ClassReader.EXPAND_FRAMES );

			return cn;
		}
		catch( final IOException e )
		{
			throw new IllegalStateException( "Error loading " + name, e );
		}
	}

	private void processNode( final AbstractInsnNode next, final String nePar )
	{
		if( next instanceof MethodInsnNode )
		{
			final MethodInsnNode min = (MethodInsnNode) next;
			if( min.owner.equals( "appeng/tile/layers/TileLayerBase" ) )
			{
				min.owner = nePar;
			}
		}
	}

	private Class loadClass( final String name, byte[] b )
	{
		// override classDefine (as it is protected) and define the class.
		Class clazz = null;
		try
		{
			final ClassLoader loader = this.getClass().getClassLoader();// ClassLoader.getSystemClassLoader();
			final Class<ClassLoader> root = ClassLoader.class;
			final Class<? extends ClassLoader> cls = loader.getClass();
			final Method defineClassMethod = root.getDeclaredMethod( "defineClass", String.class, byte[].class, int.class, int.class );
			final Method runTransformersMethod = cls.getDeclaredMethod( "runTransformers", String.class, String.class, byte[].class );

			runTransformersMethod.setAccessible( true );
			defineClassMethod.setAccessible( true );
			try
			{
				final Object[] argsA = {
						name,
						name,
						b
				};
				b = (byte[]) runTransformersMethod.invoke( loader, argsA );

				final Object[] args = {
						name,
						b,
						0,
						b.length
				};
				clazz = (Class) defineClassMethod.invoke( loader, args );
			}
			finally
			{
				runTransformersMethod.setAccessible( false );
				defineClassMethod.setAccessible( false );
			}
		}
		catch( final Exception e )
		{
			AELog.debug( e );
			throw new IllegalStateException( "Unable to manage tile API.", e );
		}
		return clazz;
	}

	private static class LayerWithDeps
	{
		public Class<?> layerClass;
		public Class<? extends AEBaseTile> dep;

		public LayerWithDeps(Class<?> l, Class<? extends AEBaseTile> d)
		{
			layerClass = l;
			dep = d;
		}
	}

	private static class DefaultPackageClassNameRemapper extends Remapper
	{

		private final HashMap<String, String> inputOutput = new HashMap<>();

		@Override
		public String map( final String typeName )
		{
			final String o = this.inputOutput.get( typeName );
			if( o == null )
			{
				return typeName;
			}
			return o;
		}
	}

	private static class CacheKey
	{
		private final Class<? extends AEBaseTile> baseClass;

		private final List<String> interfaces;

		private CacheKey( Class<? extends AEBaseTile> baseClass, List<String> interfaces )
		{
			this.baseClass = baseClass;
			this.interfaces = ImmutableList.copyOf( interfaces );
		}

		private Class<? extends AEBaseTile> getBaseClass()
		{
			return baseClass;
		}

		private List<String> getInterfaces()
		{
			return interfaces;
		}

		@Override
		public boolean equals( Object o )
		{
			if( this == o )
			{
				return true;
			}
			if( o == null || getClass() != o.getClass() )
			{
				return false;
			}

			CacheKey cacheKey = (CacheKey) o;

			return baseClass.equals( cacheKey.baseClass )/* && interfaces.equals( cacheKey.interfaces )*/;
		}

		@Override
		public int hashCode()
		{
			int result = baseClass.hashCode();
			result = 31 * result + interfaces.hashCode();
			return result;
		}
	}
}
