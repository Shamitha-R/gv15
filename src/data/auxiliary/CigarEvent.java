// Copyright 2009-2015 Information & Computational Sciences, JHI. All rights
// reserved. Use is subject to the accompanying licence terms.

package data.auxiliary;

import data.*;

public class CigarEvent
{
	private Read read;

	public CigarEvent(Read read)
	{
		this.read = read;
	}

	public Read getRead()
		{ return read; }
}