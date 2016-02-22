/**
 *  Copyright (C) 2008-2013  Telosys project org. ( http://www.telosys.org/ )
 *
 *  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.gnu.org/licenses/lgpl.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.nanoj.injector.tools;

import java.lang.reflect.Modifier;

public class ClassTools {

	public static boolean isInterfaceOrAbstract(Class<?> clazz) {
		if ( clazz.isInterface() ) {
			return true ;
		}
		int modifiers = clazz.getModifiers() ;
		if ( Modifier.isAbstract(modifiers) ) {
			return true ;
		}
		return false ;
	}

	public static boolean isAbstract(Class<?> clazz) {
		int modifiers = clazz.getModifiers() ;
		if ( Modifier.isAbstract(modifiers) ) {
			return true ;
		}
		return false ;
	}

	//-----------------------------------------------------------------------------------------------------
	
    /**
     * Replace the given variable name by the given value <br>
     * Only the first occurrence of the variable is replaced.
     * 
     * @param s the initial string ( e.g. "bla bla bla ${MyVar} bla bla" )
     * @param sVarName the variable name ( e.g. "${MyVar}" )
     * @param sVarValue the variable value
     * @return the string after variable replacement
     */
    private static String replaceVar( String s, String sVarName, String sVarValue )
    {
        String sNewString = s ;
        int i = s.indexOf(sVarName);
        if ( i >= 0 ) 
        {
        	int j = i + sVarName.length() ;
            String s1 = s.substring(0,i);
            String s2 = s.substring(j,s.length());
            //--- Replace the var name by the var value
            sNewString = s1 + sVarValue + s2 ;
        	
        }
        return sNewString ;
    }

	/**
	 * Apply the given class on the given pattern <br>
	 * ${package-N} means 'package without N last levels'
	 * @param clazz
	 * @param pattern  (ie : "${package}.${class}Impl", "${package-1}.impl.${class}Impl" 
	 * @return the resulting class name 
	 */
	public static String applyClassOnPattern(Class<?> clazz, String pattern) {
		String packageName = clazz.getPackage().getName() ;
		String className = clazz.getSimpleName() ;

		String s1 = pattern ;
		if ( pattern.contains("${package}") ) {
			s1 = replaceVar(pattern, "${package}", packageName ) ;
		}
		else if ( pattern.contains("${package-") ) {
			int    n = getPackageLevel(pattern);
			String v = getPackageVar(pattern);
			// remove N last level
			for ( int i = 0 ; i < n ; i++ ) {
				// 'aaa.bbb.ccc' --> 'aaa.bbb' --> 'aaa'
				packageName = cutLastLevel(packageName);
			}
			if ( v != null ) {
				s1 = replaceVar(pattern, v, packageName ) ;
			}
		}
		
		String s2 = replaceVar(s1, "${class}", className ) ;
		
		//--- Last check 
		if ( s2.charAt(0) == '.' ) {
			return s2.substring(1);
		}
		return s2 ;
	}
	
	public static int getPackageLevel(String pattern) {
		StringBuffer sb = new StringBuffer();
		int i = pattern.indexOf("${package-");
		if ( i >= 0 ) {
			int index = i + 10 ;
			char c = pattern.charAt(index) ;
			while ( c >= '0' && c <= '9' ) {
				sb.append(c);
				index++ ;
				c = pattern.charAt(index) ;
			}
			int level = Integer.parseInt( sb.toString() ) ;
			return level ;
		}
		else {
			//--- No "${package-" in the pattern
			return 0 ;
		}
	}
	
	public static String getPackageVar(String pattern) {
		StringBuffer sb = new StringBuffer();
		int i = pattern.indexOf("${package");
		if ( i >= 0 ) {
			int index = i ;
			char c = pattern.charAt(index) ;
			while ( c != '}' ) {
				sb.append(c);
				index++ ;
				c = pattern.charAt(index) ;
			}
			sb.append('}');
			return sb.toString() ;
		}
		return null ;
	}
	
	public static String cutLastLevel(String packageName) {
		int i = packageName.length() ;
		if ( i > 0 ) {
			i-- ;
			while ( i >= 0 && packageName.charAt(i) != '.' ) {
				i-- ;
			}
			if ( i >= 0 && packageName.charAt(i) == '.' ) {
				return packageName.substring(0, i);
			}
		}
		//--- Void or no '.' : always return ""
		return "" ;
	}
}
