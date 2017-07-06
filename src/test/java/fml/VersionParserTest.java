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

package fml;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.minecraftforge.fml.common.versioning.ComparableVersion;


/**
 * Tests for {@link ComparableVersion}
 */
public final class VersionParserTest
{
	private static final String GITHUB_VERSION = "rv2.beta.8";
	private static final String GITHUB_INVALID_REVISION = "2.beta.8";
	private static final String GITHUB_INVALID_CHANNEL = "rv2.gamma.8";
	private static final String GITHUB_INVALID_BUILD = "rv2.beta.b8";
	private static final String MOD_VERSION = "rv2-beta-8";
	private static final String MOD_INVALID_REVISION = "2-beta-8";
	private static final String MOD_INVALID_CHANNEL = "rv2-gamma-8";
	private static final String MOD_INVALID_BUILD = "rv2-beta-b8";
	private static final String GENERIC_MISSING_SEPARATOR = "foobar";
	private static final String GENERIC_INVALID_VERSION = "foo-bar";
	private static final String RV_LEXICAL = "rv10-beta-8";
	private static final String STABLE = "rv2.stable.8";
	private static final String ALPHA = "rv2.alpha.8";

	private static final ComparableVersion VERSION = new ComparableVersion( GITHUB_VERSION );


	public VersionParserTest()
	{
		
	}

	@Test
	public void testSameParsedGitHub()
	{
		final ComparableVersion version = new ComparableVersion( GITHUB_VERSION );

		assertEquals( version, version );
	}

	@Test
	public void testParseGitHub() 
	{
		assertTrue( new ComparableVersion( GITHUB_VERSION ).equals( VERSION ) );
	}

	@Test(  )
	public void parseGH_InvalidRevision() 
	{
		assertFalse( new ComparableVersion( GITHUB_INVALID_REVISION ).equals( VERSION ) );
	}

	@Test( )
	public void parseGH_InvalidChannel() 
	{
		assertFalse( new ComparableVersion( GITHUB_INVALID_CHANNEL ).equals( VERSION ) );
	}

	@Test( )
	public void parseGH_InvalidBuild() 
	{
		assertFalse( new ComparableVersion( GITHUB_INVALID_BUILD ).equals( VERSION ) );
	}

	@Test
	public void testParseMod() 
	{
		assertTrue( new ComparableVersion( MOD_VERSION ).equals( VERSION ) );
	}

	@Test( )
	public void parseMod_InvalidRevision() 
	{
		assertFalse( new ComparableVersion( MOD_INVALID_REVISION ).equals( VERSION ) );
	}

	@Test(  )
	public void parseMod_InvalidChannel() 
	{
		assertFalse( new ComparableVersion( MOD_INVALID_CHANNEL ).equals( VERSION ) );
	}

	@Test(  )
	public void parseMod_InvalidBuild() 
	{
		assertFalse( new ComparableVersion( MOD_INVALID_BUILD ).equals( VERSION ) );
	}

	@Test(  )
	public void parseGeneric_MissingSeparator() 
	{
		assertFalse( new ComparableVersion( GENERIC_MISSING_SEPARATOR ).equals( VERSION ) );
	}

	@Test(  )
	public void parseGeneric_InvalidVersion() 
	{
		assertFalse( new ComparableVersion( GENERIC_INVALID_VERSION ).equals( VERSION ) );
	}

	@Test()
	public void parseMod_Lexical()
	{
		assertFalse( new ComparableVersion( RV_LEXICAL ).equals( VERSION ) );
		assertTrue( new ComparableVersion( RV_LEXICAL ).compareTo( VERSION ) > 0 );
	}

	@Test()
	public void parseMod_Stable()
	{
		assertFalse( new ComparableVersion( STABLE ).equals( VERSION ) );
		assertTrue( new ComparableVersion( STABLE ).compareTo( VERSION ) > 0 );
	}

	@Test()
	public void parseMod_Alpha()
	{
		assertFalse( new ComparableVersion( ALPHA ).equals( VERSION ) );
		assertTrue( new ComparableVersion( ALPHA ).compareTo( VERSION ) < 0 );
	}
}
