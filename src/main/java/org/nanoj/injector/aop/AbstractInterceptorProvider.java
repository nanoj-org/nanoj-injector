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
package org.nanoj.injector.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.nanoj.injector.InjectorException;

public abstract class AbstractInterceptorProvider  {

	protected <T extends Annotation> T  getAnnotationFromInterface(Class<T> annotationClass, Method interfaceMethod ) {
		Class<?> c = interfaceMethod.getDeclaringClass();
		if ( c.isInterface() ) {
			return interfaceMethod.getAnnotation(annotationClass) ;
		}
		else {
			throw new InjectorException("The class '" + c.getSimpleName() + "' is not an interface");
		}
	}

	protected <T extends Annotation> T  getAnnotationFromImplementation(Class<T> annotationClass, Method interfaceMethod, Object implementationInstance ) {
		
		Class<?> implementationClass = implementationInstance.getClass() ;
		
		String errMessage = "Cannot get method '" + interfaceMethod.getName() 
				+ "' from implementation class '" + implementationClass.getSimpleName() + "'";
		
		Method implementationMethod = null ;
		try {
			implementationMethod = implementationClass.getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes()) ;
		} catch (SecurityException e) {
			throw new InjectorException(errMessage, e);
		} catch (NoSuchMethodException e) {
			throw new InjectorException(errMessage, e);
		}
		
		return implementationMethod.getAnnotation(annotationClass) ;
	}

	protected <T extends Annotation> T  getAnnotation(Class<T> annotationClass, Method interfaceMethod, Object implementationInstance) {
		T annotation = null ;
		
		//--- Searh in interface first
		annotation = getAnnotationFromInterface(annotationClass, interfaceMethod );
		if ( annotation != null ) {
			return annotation ;
		}
		
		//--- Searh in implementation
		annotation = getAnnotationFromImplementation(annotationClass, interfaceMethod, implementationInstance );
		if ( annotation != null ) {
			return annotation ;
		}
		
		return null ;
	}
	
	/**
	 * Returns true if the given interface method has the given annotation
	 * @param annotationClass the annotation to be searched
	 * @param interfaceMethod the method defined in the interface
	 * @return
	 */
	protected boolean annotationExistsInInterface(Class<? extends Annotation> annotationClass, Method interfaceMethod ) {
		return ( getAnnotationFromInterface(annotationClass, interfaceMethod) != null ) ;
	}
	
	/**
	 * Returns true if the given interface method has the given annotation in the implementation class
	 * @param annotationClass the annotation to be searched
	 * @param interfaceMethod the method defined in the interface
	 * @param implementationInstance the implementation instance where to search the method annotation
	 * @return
	 */
	protected boolean annotationExistsInImplementation(Class<? extends Annotation> annotationClass, Method interfaceMethod, Object implementationInstance ) {
		
//		Class<?> implementationClass = implementationInstance.getClass() ;
//		
//		String errMessage = "Cannot get method '" + interfaceMethod.getName() 
//				+ "' from implementation class '" + implementationClass.getSimpleName() + "'";
//		
//		Method implementationMethod = null ;
//		try {
//			implementationMethod = implementationClass.getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes()) ;
//		} catch (SecurityException e) {
//			throw new TelosysInjectorException(errMessage, e);
//		} catch (NoSuchMethodException e) {
//			throw new TelosysInjectorException(errMessage, e);
//		}
//		
//		return implementationMethod.isAnnotationPresent(annotationClass);
//		
		return ( getAnnotationFromImplementation(annotationClass, interfaceMethod, implementationInstance) != null ) ;
	}
	
	/**
	 * Returns true if the given method has the given annotation in the interface class or in the implementation class
	 * @param annotationClass the annotation to be searched
	 * @param interfaceMethod the method defined in the interface
	 * @param implementationInstance the implementation instance
	 * @return
	 */
	protected boolean annotationExists(Class<? extends Annotation> annotationClass, Method interfaceMethod, Object implementationInstance) {
		if ( annotationExistsInInterface(annotationClass, interfaceMethod ) ) {
			return true ;
		}
		if ( annotationExistsInImplementation(annotationClass, interfaceMethod, implementationInstance ) ) {
			return true ;
		}
		return false ;
	}
	

}
