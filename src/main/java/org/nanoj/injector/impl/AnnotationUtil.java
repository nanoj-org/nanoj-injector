/**
 *  Copyright (C) 2013-2016 Laurent GUERIN - NanoJ project org. ( http://www.nanoj.org/ )
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
package org.nanoj.injector.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

class AnnotationUtil {

	/**
	 * Returns true if the given annotation can be considered as a "@Singleton" annotation
	 * @param annotation
	 * @return
	 */
	static boolean isSingleton(Annotation annotation)  {
		return  annotation.annotationType().getCanonicalName().endsWith(".Singleton") ;
	}

	/**
	 * Returns true if the given annotation can be considered as a "@Inject" annotation
	 * @param annotation
	 * @return
	 */
	static boolean isInject(Annotation annotation)  {
		return  annotation.annotationType().getCanonicalName().endsWith(".Inject") ;
	}
	
	/**
	 * Returns true if the given constructor has an "@Inject" annotation
	 * @param constructor
	 * @return
	 */
	static boolean hasInjectAnnotation(Constructor<?> constructor)  {
		Annotation[] annotations = constructor.getDeclaredAnnotations() ;
		for ( Annotation a : annotations ) {
			if ( AnnotationUtil.isInject(a) ) {
				return true ;
			}
		}
		return false ;
	}

	/**
	 * Returns true if the given field has an "@Inject" annotation
	 * @param field
	 * @return
	 */
	static boolean hasInjectAnnotation(Field field)  {
		Annotation[] annotations = field.getDeclaredAnnotations() ;
		for ( Annotation a : annotations ) {
			if ( AnnotationUtil.isInject(a) ) {
				return true ;
			}
		}
		return false ;
	}

	
}
