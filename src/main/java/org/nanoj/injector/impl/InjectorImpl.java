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

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Provider;

import org.nanoj.injector.Injector;
import org.nanoj.injector.InjectorException;
import org.nanoj.injector.aop.InterceptorProvider;
import org.nanoj.injector.tools.ClassTools;
import org.nanoj.util.ConsoleLoggerProvider;

/**
 * Injector implementation. A container managing dynamically components with basic
 * "Dependency Injection" <br>
 * It injects dependencies using the components constructors <br>
 * NB : each component managed by this type of container is supposed to have
 * only one constructor <br>
 * If a component has multiple constructors, they are searched in the following
 * order : <br>
 * 1/ the default constructor <br>
 * 2/ if no default constructor, the first other constructor found (with
 * parameters) <br>
 * <br>
 * 
 * @author Laurent Guerin
 * 
 */
public class InjectorImpl implements Injector {

	//private static final BasicLogger logger = BasicLogger.getLogger(InjectorImpl.class);
	private final static Logger logger = ConsoleLoggerProvider.getLogger(InjectorImpl.class, Level.INFO); 
	
	/**
	 * The symbolic name of the injector/container
	 */
	private final String name;

	/**
	 * Components held by the container, accessible by their class name Key :
	 * Component class or interface name (String class) Value : The component
	 * descriptor (Component class)
	 */
	private final HashMap<String, ComponentDefinition<?>>   componentsMap = new HashMap<String, ComponentDefinition<?>>(128);

	//---------------------------------------------------------------------------------
	// Configuration 
	//---------------------------------------------------------------------------------
	private final HashMap<Class<?>, Class<?>>     implementationClasses;
	
	private final String[]                        implementationConventions ;
	
	private final HashMap<Class<?>, Provider<?>>  implementationProviders ;
	
	//private final InterceptorProvider             interceptorProvider ;
	private final InterceptorProvider[]           interceptorProviders ;

	//---------------------------------------------------------------------------------
	/**
	 * Constructor
	 * 
	 * @param name
	 * @param implementationClasses 
	 * @param implementationConventions
	 * @param implementationProviders
	 * @param interceptorProviders
	 */
	public InjectorImpl(String name, 
			HashMap<Class<?>, Class<?>>     implementationClasses, 
			String[]                        implementationConventions,
			HashMap<Class<?>, Provider<?>>  implementationProviders,
			InterceptorProvider[]           interceptorProviders ) {
		super();
		this.name = name ;

		//--- Configuration 
		this.implementationClasses     = implementationClasses ;
		this.implementationConventions = implementationConventions ;
		this.implementationProviders   = implementationProviders ;
		this.interceptorProviders      = interceptorProviders ;
	}
	

	/* (non-Javadoc)
	 * @see org.telosys.injector.Injector#getName()
	 */
	public String getName() {
		return this.name;
	}
	
	/* (non-Javadoc)
	 * @see org.telosys.injector.Injector#getInterceptorProviders()
	 */
	public InterceptorProvider[] getInterceptorProviders() {
		return this.interceptorProviders ;
	}

	/* (non-Javadoc)
	 * @see org.telosys.injector.Injector#getInstance(java.lang.Class)
	 */
	public <T> T getInstance(Class<T> clazz) {

		T componentInstance = getComponentInstance(clazz);
		return componentInstance;
	}

	/**
	 * Returns a component instance for the given class <br>
	 * If the given class is an interface the implementation is returned.<br>
	 * The convention to find the implementation is defined in the
	 * "getImplementationClassName" <br>
	 * method defined in the subclass. <br>
	 * 
	 * @param componentClass
	 *            the requested component class or interface
	 * @return the component concrete class
	 */
	protected <T> T getComponentInstance(Class<T> componentClass) {
		logger.info("getInstance(" + componentClass + ")");

		String sClassName = componentClass.getName();
		//--- Search an existing resolved component in the container
		ComponentDefinition<?> component = componentsMap.get(sClassName);
		if (component != null) {
			
			//----- Component definition found : use it to get the component instance
			return (T) component.getInstance();
			
		} else {
			//----- Component definition not found => try to determine the component definition and store it the registry

			//--- 1) Is there a specific implementation class for this component ?
			Class<?> implementationClass = getImplementationClass(componentClass);
			if (implementationClass != null) {
				component = new ComponentDefinition(this, componentClass, implementationClass);
			} 
			
			//--- 2) Is there a specific provider for this component ?
			Provider<T> provider = (Provider<T>) getProvider(componentClass);
			if (provider != null) {
				component = new ComponentDefinition(this, componentClass, provider);
			} 
			
			//--- 3) Try to resolve the component definition from the conventions ?
			implementationClass = getImplementationClassByConvention(componentClass);
			if (implementationClass != null) {
				component = new ComponentDefinition(this, componentClass, implementationClass);
			} 
			
			//--- Still no component ?
			if ( null == component ) {
				if ( ClassTools.isInterfaceOrAbstract(componentClass) ) {
					throw new InjectorException("Cannot get implementation or provider for '"
							+ componentClass.getName() + "'");
				}
				else {
					//--- Concrete class (implementation not required)
//					component = new Component(this, componentClass, null);
					component = new ComponentDefinition(this, componentClass);
				}
			}
			
			//--- Store the component in the registry (for next time)
			componentsMap.put(sClassName, component);

			//--- Use the component definition to get the instance
			return (T) component.getInstance();
		}

	}

	/**
	 * Returns the implementation's class name for the given interface <br>
	 * This method must be implemented in the sub-class <br>
	 * 
	 * @param interfaceClassName
	 *            the interface's full class name
	 * @return the implementation's full class name, or null if none
	 */
	// protected abstract String getImplementationClassName(String
	// interfaceClassName );

	/**
	 * Returns a boolean value indicating if the given component is
	 * "mono-instance" or not <br>
	 * By default it returns TRUE if the given class has an annotation ending
	 * with "Singleton" <br>
	 * This method can be overridden in the sub-class if necessary <br>
	 * 
	 * @param cl
	 * @return
	 */
	protected boolean isMonoInstance(Class<?> cl) {
		boolean b = isMonoInstanceByAnnotation(cl); // Default behavior : has a
													// @Singleton annotation (
													// from any package )
		logger.info("isMonoInstance ( " + cl.getCanonicalName()
				+ " ) : " + b);
		return b;
	}

	/**
	 * Returns the implementation class for the given interface class
	 * 
	 * @param interfaceClass
	 * @return
	 */
//	public abstract Class<?> getImplementationClass(Class<?> interfaceClass);
	private Class<?> getImplementationClass(Class<?> interfaceClass) {
		return implementationClasses.get(interfaceClass);

	}

	/**
	 * Try to found an implementation class using the conventions
	 * @param interfaceClass
	 * @return
	 */
	private Class<?> getImplementationClassByConvention(Class<?> interfaceClass) {
		
		for ( String pattern : implementationConventions ) {
			String className = ClassTools.applyClassOnPattern(interfaceClass, pattern );
			ClassLoader classLoader = InjectorImpl.class.getClassLoader() ;
			
			//--- Try to load this class ...
			Class<?> implementationClass = null ;
			try {
				implementationClass = classLoader.loadClass(className);
			} catch (ClassNotFoundException e) {
				// No class with this name 
				implementationClass = null ;
			}
			if ( implementationClass != null ) {
				return implementationClass ;
			}
		}
		
		return null ;
	}
	
	private <T> Provider<T> getProvider(Class<T> interfaceClass) {
		Provider<?> provider = this.implementationProviders.get(interfaceClass);
		return (Provider<T>) provider ;
	}

	// private Class<?> getImplementationClass(Class<?> interfaceClass)
	// {
	// trace("getImplementationClass("+interfaceClass+")");
	//
	// //--- Call the subclass method to retrieve the implementation class
	// String sImplementationClassName = getImplementationClassName(
	// interfaceClass.getName() );
	//
	// //--- If the subclass has returned an implementation class name try to
	// load it
	// if ( sImplementationClassName != null )
	// {
	// Class<?> cImplementation = null ;
	// try {
	// cImplementation = Class.forName(sImplementationClassName);
	// } catch (ClassNotFoundException e) {
	// // This class doesn't exist !
	// cImplementation = null ;
	// }
	//
	// //--- Returns the implementation class
	// return cImplementation ;
	// }
	// else
	// {
	// //--- No implementation for this interface
	// return null ;
	// }
	// }

	/**
	 * Returns the "class name" of the given full class name <br>
	 * e.g. returns "String" for "java.lang.String"
	 * 
	 * @param sFullClassName
	 * @return
	 */
	protected String getClassName(String sFullClassName) {
		if (sFullClassName != null) {
			String s = sFullClassName.trim();
			int i = s.lastIndexOf('.');
			if (i >= 0) {
				return s.substring(i + 1);
			}
			return s;
		}
		return null;
	}

	/**
	 * Returns the "package name" of the given class name <br>
	 * e.g. returns "java.lang" for "java.lang.String"
	 * 
	 * @param sFullClassName
	 * @return
	 */
	protected String getPackageName(String sFullClassName) {
		if (sFullClassName != null) {
			String s = sFullClassName.trim();
			int i = s.lastIndexOf('.');
			if (i >= 0) {
				return s.substring(0, i);
			}
			return "";
		}
		return null;
	}

	/**
	 * Utility method to print all the container's components
	 * 
	 * @param out
	 */
	public void printAllComponents(PrintStream out) {
		// Set<String> keys = _hmComponents.keySet() ;

		Collection<ComponentDefinition<?>> collection = componentsMap.values();
		// Iterator iter = collection.iterator();
		// while ( iter.hasNext() )
		for (ComponentDefinition<?> component : collection) {
			// Component component = (Component) iter.next();
			out.println(component.toString());
		}
	}

	/**
	 * Method designed to be used in the "isMonoInstance" concrete method. <br>
	 * Returns true if the given component class implements the given interface
	 * used as a "mono-instance" marker
	 * 
	 * @param componentClass
	 *            the component class to introspect
	 * @param monoInstanceInterface
	 *            the class of the interface used as a marker
	 * @return
	 */
	protected boolean isMonoInstanceByInterface(Class<?> componentClass,
			Class<?> monoInstanceInterface) {
		if (null == componentClass)
			throw new IllegalArgumentException("componentClass is null");
		if (null == monoInstanceInterface)
			throw new IllegalArgumentException("fieldName is null");

		return monoInstanceInterface.isAssignableFrom(componentClass);
	}

	/**
	 * Method designed to be used in the "isMonoInstance" concrete method. <br>
	 * Returns "true" if there's a public static boolean field initialized to
	 * "true" in the class.<br>
	 * Returns "false" in all other cases : field not found, or not accessible,
	 * or initialized to "false" <br>
	 * 
	 * @param componentClass
	 *            the component class to introspect
	 * @param fieldName
	 *            the name of the field used as a marker <br>
	 *            ( searched as a public static boolean field initialized to
	 *            "true" )
	 * @return
	 */
	protected boolean isMonoInstanceByBooleanStaticField(
			Class<?> componentClass, String fieldName) {
		if (null == componentClass)
			throw new IllegalArgumentException("componentClass is null");
		if (null == fieldName)
			throw new IllegalArgumentException("fieldName is null");

		// --- Is there a static boolean field in this class (with the given
		// name) ?
		Field field = null;
		try {
			field = componentClass.getDeclaredField(fieldName);
		} catch (SecurityException e) {
			field = null;
		} catch (NoSuchFieldException e) {
			field = null;
		}

		if (field != null) // found
		{
			// --- Is it a "boolean" field ?
			Class<?> type = field.getType();
			logger.info("SINGLETON found : type = " + type);
			if ("boolean".equals(type.getName())) {
				// --- Has it a "TRUE" value ?
				boolean fieldValue = false;
				try {
					fieldValue = field.getBoolean(null);
				} catch (IllegalArgumentException e) {
					fieldValue = false;
				} catch (IllegalAccessException e) {
					fieldValue = false;
				}
				return fieldValue;
			}
		}
		return false;
	}

	/**
	 * Method designed to be used in the "isMonoInstance" concrete method. <br>
	 * Returns true if the given component class is marked with the given
	 * annotation <br>
	 * 
	 * @param componentClass
	 *            the component class to introspect
	 * @param annotationClass
	 *            the annotation to be searched
	 * @return
	 */
	protected boolean isMonoInstanceByAnnotation(Class<?> componentClass,
			Class<?> annotationClass) {
		if (null == componentClass)
			throw new IllegalArgumentException("componentClass is null");
		if (null == annotationClass)
			throw new IllegalArgumentException("annotationClass is null");

		Annotation[] annotations = componentClass.getDeclaredAnnotations();
		for (Annotation a : annotations) {
			if (annotationClass.equals(a.annotationType()))
			// if ( annotationClass.isAssignableFrom( a.annotationType() ) )
			{
				return true;
			}
		}
		return false;

	}

	protected boolean isMonoInstanceByAnnotation(Class<?> componentClass) {
		if (null == componentClass)
			throw new IllegalArgumentException("componentClass is null");

		Annotation[] annotations = componentClass.getDeclaredAnnotations();
		for (Annotation a : annotations) {
			//if (a.annotationType().getCanonicalName().endsWith(".Singleton")) 
			if ( AnnotationUtil.isSingleton(a) )
			{
				return true;
			}
		}
		return false;

	}
}
