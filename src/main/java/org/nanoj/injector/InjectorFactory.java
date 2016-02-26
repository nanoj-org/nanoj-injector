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

import javax.inject.Provider;

import org.nanoj.injector.aop.InterceptorProvider;
import org.nanoj.injector.impl.InjectorImpl;

public class InjectorFactory {

	/**
	 * Creates a new 'injector' with a specific configuration
	 * @param name
	 * @param configuration
	 * @return
	 */
	public final static Injector createInjector(String name, InjectorConfiguration configuration) {
		
		//--- Get configuration elements 
		
		HashMap<Class<?>, Class<?>>     implementationsClasses  = configuration.getImplementationClasses();
		 
		HashMap<Class<?>, Provider<?>>  implementationProviders = configuration.getImplementationProviders();
		
		String[] implementationsConventions = configuration.getImplementationConventions() ;
		 
		InterceptorProvider[] interceptorProviders = configuration.getInterceptorProviders();
		
		
		//--- Create the injector factory with the configuration elements
		Injector injector = new InjectorImpl(name, implementationsClasses, implementationsConventions, implementationProviders, interceptorProviders);
		
		return injector ;
	}

//	/**
//	 * Creates a new 'injector' with a specific configurator
//	 * @param name
//	 * @param configurator
//	 * @return
//	 */
//	public final static Injector createInjector(String name, InjectorConfigurator configurator) {
//		
//		//--- Injector configuration initialization
//		InjectorConfiguration configuration = new InjectorConfiguration();
//		
//		configurator.configure(configuration);
//
//		
////		//--- Get configuration elements 
////		
////		HashMap<Class<?>, Class<?>>     implementationsClasses  = configuration.getImplementationClasses();
////		 
////		HashMap<Class<?>, Provider<?>>  implementationProviders = configuration.getImplementationProviders();
////		
////		String[] implementationsConventions = configuration.getImplementationConventions() ;
////		 
////		InterceptorProvider[] interceptorProviders = configuration.getInterceptorProviders();
////		
////		
////		//--- Create the injector factory with the configuration elements
////		Injector injector = new InjectorImpl(name, implementationsClasses, implementationsConventions, implementationProviders, interceptorProviders);
//		
//		return createInjector(name, configuration) ;
//	}

	public final static Injector createInjector(String name) {
		
		//--- Injector configuration initialization
		InjectorConfiguration configuration = new InjectorConfiguration();
		
		//--- Default naming conventions 
		configuration.defineImplementationConvention("${package}.${class}Impl"); // 1st convention
		configuration.defineImplementationConvention("${package}.impl.${class}Impl"); // 2nd convention

		return createInjector(name, configuration) ;
	}
}
