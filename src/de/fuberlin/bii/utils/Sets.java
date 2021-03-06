/*
 * 
 * Copyright 2012 lexergen.
 * This file is part of lexergen.
 * 
 * lexergen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * lexergen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with lexergen.  If not, see <http://www.gnu.org/licenses/>.
 *  
 * lexergen:
 * A tool to chunk source code into tokens for further processing in a compiler chain.
 * 
 * Projectgroup: bi, bii
 * 
 * Authors: Benjamin Weißenfels
 * 
 * Module:  Softwareprojekt Übersetzerbau 2012 
 * 
 * Created: Apr. 2012 
 * Version: 1.0
 *
 */

package de.fuberlin.bii.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * 
 * @author Johannes Dahlke
 * 
 */
public class Sets {

	
	/**
	 * Merge to collections to a union.
	 * @param c1
	 * @param c2
	 * @return a HashSet with all elements of both lists once.
	 */
	public static <T, C extends Collection<T>> C unionCollections(  C c1,
			 C c2) {

		C list = null;

		try {
			list = (C) c1.getClass().newInstance();
		} catch (Exception e) {
			//Notification.printDebugException( e);
			try {
				list = (C) c2.getClass().newInstance();
			} catch ( Exception e2) {
				Notification.printDebugException( e2);
			}		
		} 

		if (Test.isUnassigned( list))
			return null;

		if ( Test.isAssigned( c1) && Test.isAssigned( c2)) {
			list.addAll( c1);

			for ( T t : c2) {
				if ( !list.contains( t))
					list.add( t);
			}
		} else if ( Test.isAssigned( c1)) {
			list.addAll( c1);
		} else if ( Test.isAssigned( c2)) {
			list.addAll( c2);
		}
		return list;

	
	}
	
	
	public static <T> Set<T> setMinus( Set<T> setA, Set<T> setB) {
		Set<T> result = new HashSet<T>();
		boolean found = false;
		for ( T a : setA) {
			if ( !setB.contains( a))
				result.add( a);
		}
		return result;
	}
	
	public static <T> Collection<T> setMinus( Collection<T> setA, Collection<T> setB) {
		return setMinus( new HashSet<T>( setA), new HashSet<T>( setB));
	}

}
