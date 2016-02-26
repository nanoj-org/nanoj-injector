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
package org.nanoj.injector;

import java.util.HashMap;
import java.util.LinkedList;

import javax.inject.Provider;

import org.nanoj.injector.aop.InterceptorProvider;
import org.nanoj.injector.tools.ClassTools;

public class InjectorConfiguration {

	private final static String[] VOID_STRING_ARRAY = new String[0];
	
	private final static InterceptorProvider[] VOID_INTERCEPTOR_PROVIDER_ARRAY = new InterceptorProvider[0];
	
	//-------------------------------------------------------------------------------------------------------------
    /**
     * Map of implementation classes indexed by interface (or abstract) canonical class name
     */
    private final HashMap<Class<?>, Class<?>>     _hmImplementationClasses   = new HashMap<Class<?>, Class<?>>(128);
    
    /**
     * Map of specific providers indexed by interface (or abstract) canonical class name
     */
    private final HashMap<Class<?>, Provider<?>>  _hmImplementationProviders = new HashMap<Class<?>, Provider<?>>(128);
    
    /**
     * List of implementation conventions patterns
     */
    private final LinkedList<String>              _implementationConventions = new LinkedList<String>() ;
    
//    /**
//     * Interceptor provider 
//     */
    //private InterceptorProvider _interceptorProvider = null ;
    /**
     * List of all the interceptor providers defined in this configuration
     */
    private final LinkedList<InterceptorProvider> _interceptorProviders = new LinkedList<InterceptorProvider>()  ;

	//-------------------------------------------------------------------------------------------------------------
	// Implementation classes
	//-------------------------------------------------------------------------------------------------------------
	/**
	 * Defines an implementation class for an interface or abstract class
	 * 
	 * @param abstractClass interface or abstract class 
	 * @param implementationClass implementation for the interface or abstract class
	 */
	public <T> void defineImplementationClass (Class<T> abstractClass, Class<? extends T> implementationClass) {

		//--- Implementation class must extends/implements the interface
		if ( ! abstractClass.isAssignableFrom(implementationClass) ) {
			throw new IllegalArgumentException(implementationClass.getCanonicalName() + " is not an immplementation of " + abstractClass.getCanonicalName());
		}
		//--- Implementation class must not be an interface
		if ( implementationClass.isInterface() ) {
			throw new IllegalArgumentException(implementationClass.getCanonicalName() + " must be a concrete class (not an interface) " );
		}
		if ( ClassTools.isAbstract(implementationClass) ) {
			throw new IllegalArgumentException(implementationClass.getCanonicalName() + " must be a concrete class (not an abstract class) " );
		}
		//--- Everything seems all right ...
		_hmImplementationClasses.put(abstractClass, implementationClass);
	}
	
	/**
	 * Returns the implementation classes map 
	 * @return
	 */
	protected HashMap<Class<?>, Class<?>> getImplementationClasses () {
		return _hmImplementationClasses ;
	}	
	
	//-------------------------------------------------------------------------------------------------------------
	// Implementation conventions
	//-------------------------------------------------------------------------------------------------------------
	/**
	 * Defines an implementation convention based on the given pattern.
	 * @param pattern
	 */
	public void defineImplementationConvention(String pattern) {
		_implementationConventions.add(pattern);
	}
	
	/**
	 * Returns an array of all the implementation conventions defined.
	 * @return
	 */
	protected String[] getImplementationConventions () {
		return _implementationConventions.toArray( VOID_STRING_ARRAY ) ;
	}	
	
	//-------------------------------------------------------------------------------------------------------------
	// Implementation providers
	//-------------------------------------------------------------------------------------------------------------
	/**
	 * Defines a specific provider that provides the implementation class
	 * 
	 * @param abstractClass
	 * @param provider
	 */
	public <T> void defineImplementationProvider(Class<T> abstractClass, Provider<T> provider) {
		//--- Store the provider
		_hmImplementationProviders.put(abstractClass, provider);
	}
	
	/**
	 * Returns the implementation providers map 
	 * @return
	 */
	protected HashMap<Class<?>, Provider<?>> getImplementationProviders () {
		return _hmImplementationProviders ;
	}	
	
	
	//-------------------------------------------------------------------------------------------------------------
	// Interceptor providers
	//-------------------------------------------------------------------------------------------------------------
	public void defineInterceptorProvider(InterceptorProvider interceptorProvider) {
		if ( null == interceptorProvider ) {
			throw new IllegalArgumentException("InterceptorProvider is null");
		}
		//this._interceptorProvider = interceptorProvider ;
		_interceptorProviders.add(interceptorProvider);
	}
	
	protected InterceptorProvider[] getInterceptorProviders() {
		return _interceptorProviders.toArray(VOID_INTERCEPTOR_PROVIDER_ARRAY) ;
	}
	
	
}
