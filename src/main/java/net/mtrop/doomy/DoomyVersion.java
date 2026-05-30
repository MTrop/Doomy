/*******************************************************************************
 * Copyright (c) 2019-2026 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy;

/**
 * A Doomy version object.
 * @author Matthew Tropiano
 */
public class DoomyVersion implements Comparable<DoomyVersion>
{
	/** Current version. */
	public static final DoomyVersion CURRENT = parse(DoomyCommon.getVersionString("doomy"));
	
	/** Major number. */
	private final int major;
	/** Minor number. */
	private final int minor;
	/** Release number. */
	private final int release;
	
	/**
	 * Creates a new version.
	 * @param major the major number.
	 * @param minor the minor number.
	 * @param release the release number.
	 */
	public DoomyVersion(int major, int minor, int release)
	{
		this.major = major;
		this.minor = minor;
		this.release = release;
	}
	
	/**
	 * Creates a new version from a version string.
	 * @param versionString the version string.
	 * @return a new version.
	 * @throws IllegalArgumentException if a version number couldn't be parsed.
	 */
	public static DoomyVersion parse(String versionString)
	{
		String[] split = versionString.split("\\.");
		try {
			return new DoomyVersion(
				Integer.parseInt(split[0]),
				Integer.parseInt(split[1]),
				Integer.parseInt(split[2])
			);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Could not parse version.", e);
		}
	}
	
	@Override
	public String toString()
	{
		return major + "." + minor + "." + release;
	}

	@Override
	public int compareTo(DoomyVersion o)
	{
		return major == o.major 
			?  minor == o.minor
			?  release - o.release
			:  minor - o.minor
			:  major - o.major
		;
	}
	
}
