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

package appeng.coremod.transformer;


import java.util.Iterator;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import net.minecraft.launchwrapper.IClassTransformer;

import appeng.coremod.annotations.Integration;
import appeng.helpers.Reflected;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;


@Reflected
public final class ASMIntegration implements IClassTransformer
{
	private static Logger logger = LogManager.getLogger("AE2-CORE");

	@Reflected
	public ASMIntegration()
	{

		/**
		 * Side, Display Name, ModID ClassPostFix
		 */

		for( final IntegrationType type : IntegrationType.values() )
		{
			IntegrationRegistry.INSTANCE.add( type );
		}

		// integrationModules.add( IntegrationSide.BOTH, "Thermal Expansion", "ThermalExpansion", IntegrationType.TE );
		// integrationModules.add( IntegrationSide.BOTH, "Mystcraft", "Mystcraft", IntegrationType.Mystcraft );
		// integrationModules.add( IntegrationSide.BOTH, "Greg Tech", "gregtech_addon", IntegrationType.GT );
		// integrationModules.add( IntegrationSide.BOTH, "Universal Electricity", null, IntegrationType.UE );
		// integrationModules.add( IntegrationSide.BOTH, "Logistics Pipes", "LogisticsPipes|Main", IntegrationType.LP );
		// integrationModules.add( IntegrationSide.BOTH, "Better Storage", IntegrationType.betterstorage );
		// integrationModules.add( IntegrationSide.BOTH, "Forestry", "Forestry", IntegrationType.Forestry );
		// integrationModules.add( IntegrationSide.BOTH, "Mekanism", "Mekanism", IntegrationType.Mekanism );

	}

	@Nullable
	@Override
	public byte[] transform( final String name, final String transformedName, final byte[] basicClass )
	{
		if( basicClass == null || transformedName.startsWith( "appeng.coremod" ) )
		{
			return basicClass;
		}

		if( transformedName.startsWith( "appeng." ) )
		{
			final ClassNode classNode = new ClassNode();
			final ClassReader classReader = new ClassReader( basicClass );
			classReader.accept( classNode, 0 );

			try
			{
				final boolean reWrite = this.removeOptionals( classNode );

				if( reWrite )
				{
					final ClassWriter writer = new ClassWriter( ClassWriter.COMPUTE_MAXS );
					classNode.accept( writer );
					return writer.toByteArray();
				}
			}
			catch( final Throwable t )
			{
				t.printStackTrace();
			}
		} else if (transformedName.equals("net.minecraft.tileentity.TileEntity")){
			final ClassNode classNode = new ClassNode();
			final ClassReader classReader = new ClassReader( basicClass );
			classReader.accept( classNode, 0 );
			boolean foundMethod = false;
			for (MethodNode methodNode : classNode.methods){
				if (methodNode.name.equals("create") && methodNode.desc != null && methodNode.desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/tileentity/TileEntity;")){
					logger.info("Patching TileEntity.create to allow legacy name remapping.");
					patchTileEntityCreate(methodNode);
					foundMethod = true;
					break;
				}
			}
			if (foundMethod){
				final ClassWriter writer = new ClassWriter( ClassWriter.COMPUTE_MAXS );
				classNode.accept( writer );
				return writer.toByteArray();
			}
		}
		return basicClass;
	}

	private boolean removeOptionals( final ClassNode classNode )
	{
		boolean changed = false;

		if( classNode.visibleAnnotations != null )
		{
			for( final AnnotationNode an : classNode.visibleAnnotations )
			{
				if( this.hasAnnotation( an, Integration.Interface.class ) )
				{
					if( this.stripInterface( classNode, Integration.Interface.class, an ) )
					{
						changed = true;
					}
				}
				else if( this.hasAnnotation( an, Integration.InterfaceList.class ) )
				{
					for( final Object o : ( (Iterable) an.values.get( 1 ) ) )
					{
						if( this.stripInterface( classNode, Integration.InterfaceList.class, (AnnotationNode) o ) )
						{
							changed = true;
						}
					}
				}
			}
		}

		final Iterator<MethodNode> i = classNode.methods.iterator();
		while( i.hasNext() )
		{
			final MethodNode mn = i.next();

			if( mn.visibleAnnotations != null )
			{
				for( final AnnotationNode an : mn.visibleAnnotations )
				{
					if( this.hasAnnotation( an, Integration.Method.class ) )
					{
						if( this.stripMethod( classNode, mn, i, Integration.Method.class, an ) )
						{
							changed = true;
						}
					}
				}
			}
		}

		if( changed )
		{
			logger.debug( "Updated " + classNode.name );
		}

		return changed;
	}

	private boolean hasAnnotation( final AnnotationNode ann, final Class<?> annotation )
	{
		return ann.desc.equals( Type.getDescriptor( annotation ) );
	}

	private boolean stripInterface( final ClassNode classNode, final Class<?> class1, final AnnotationNode an )
	{
		if( an.values.size() != 4 )
		{
			throw new IllegalArgumentException( "Unable to handle Interface annotation on " + classNode.name );
		}

		String iFace = null;

		if( an.values.get( 0 ).equals( "iface" ) )
		{
			iFace = (String) an.values.get( 1 );
		}
		else if( an.values.get( 2 ).equals( "iface" ) )
		{
			iFace = (String) an.values.get( 3 );
		}

		String iName = null;
		if( an.values.get( 0 ).equals( "iname" ) )
		{
			iName = ( (String[]) an.values.get( 1 ) )[1];
		}
		else if( an.values.get( 2 ).equals( "iname" ) )
		{
			iName = ( (String[]) an.values.get( 3 ) )[1];
		}

		if( iName != null && iFace != null )
		{
			final IntegrationType type = IntegrationType.valueOf( iName );
			if( !IntegrationRegistry.INSTANCE.isEnabled( type ) )
			{
				logger.info( "Removing Interface " + iFace + " from " + classNode.name + " because " + iName + " integration is disabled." );
				classNode.interfaces.remove( iFace.replace( '.', '/' ) );
				return true;
			}
			else
			{
				logger.debug( "Allowing Interface " + iFace + " from " + classNode.name + " because " + iName + " integration is enabled." );
			}
		}
		else
		{
			throw new IllegalStateException( "Unable to handle Method annotation on " + classNode.name );
		}

		return false;
	}

	private boolean stripMethod( final ClassNode classNode, final MethodNode mn, final Iterator<MethodNode> i, final Class class1, final AnnotationNode an )
	{
		if( an.values.size() != 2 )
		{
			throw new IllegalArgumentException( "Unable to handle Method annotation on " + classNode.name );
		}

		String iName = null;

		if( an.values.get( 0 ).equals( "iname" ) )
		{
			iName = ( (String[]) an.values.get( 1 ) )[1];
		}

		if( iName != null )
		{
			final IntegrationType type = IntegrationType.valueOf( iName );
			if( !IntegrationRegistry.INSTANCE.isEnabled( type ) )
			{
				logger.info( "Removing Method " + mn.name + " from " + classNode.name + " because " + iName + " integration is disabled." );
				i.remove();
				return true;
			}
			else
			{
				logger.debug( "Allowing Method " + mn.name + " from " + classNode.name + " because " + iName + " integration is enabled." );
			}
		}
		else
		{
			throw new IllegalStateException( "Unable to handle Method annotation on " + classNode.name );
		}

		return false;
	}

	private void patchTileEntityCreate( MethodNode mn ){
		InsnList insertList = new InsnList();
		LabelNode l0 = new LabelNode(new Label());
		//mv.visitLabel(l0);
		insertList.add(l0);
		//mv.visitLineNumber(14, l0);
		insertList.add(new LineNumberNode(0xdeadbeef, l0));
		//mv.visitVarInsn(ALOAD, 1);
		insertList.add(new VarInsnNode(ALOAD, 1));
		//mv.visitMethodInsn(INVOKESTATIC, "appeng/tester", "replaceOldIDs", "(Lnet/minecraft/nbt/NBTTagCompound;)V", false);
		insertList.add(new MethodInsnNode(INVOKESTATIC, "appeng/util/TileEntityMappingFixer", "replaceOldIDs", "(Lnet/minecraft/nbt/NBTTagCompound;)V", false));
		mn.instructions.insert(insertList);
	}

}
