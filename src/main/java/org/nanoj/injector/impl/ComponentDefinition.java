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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.LinkedList;

import javax.inject.Provider;

import org.nanoj.injector.InjectorException;
import org.nanoj.injector.aop.InterceptorProvider;
import org.nanoj.injector.dynaproxy.GenericInvocationHandler;
import org.nanoj.injector.tools.ClassTools;


/**
 * Component definition held by a Container <br>
 * 
 * @author Laurent Guerin
 *
 */
/**
 * @author laguerin
 *
 * @param <T>
 */
class ComponentDefinition<T>
{
    private final static Field[] VOID_FIELD_ARRAY = new Field[0];
    
	//-----------------------------------------------------------------------------------------

	/**
	 * The container that held the component (this object)  
	 */
	private InjectorImpl _container = null ;

	//-----------------------------------------------------------------------------------------
	
	/**
	 * The class identifying the component (concrete class or interface or abstract class) 
	 */
	private Class<T>  _componentClass = null ;

	/**
	 * The concrete class used to create component's instances <br>
	 * Can be null if there's a specific provider <br>
	 * Same as component class if the component class is a concrete class (not an interface or an abstract class)
	 */
	private Class<? extends T>  _implementationClass    = null ; 

	/**
	 * Specific provider to be used for instance creation (usually null)
	 */
	private Provider<T>         _implementationProvider = null ; 
	
	//-----------------------------------------------------------------------------------------
	
	/**
	 * The "mono-instance" flag indicating if this component can be used as a single instance shared by multiple threads
	 */
	private boolean  _bMonoInstance = false ; // by default NOT "mono-instance"
	
	/**
	 * The single instance kept here to be reused ( for "mono-instance" components only )
	 */
	private T   _singleInstance = null ;

	//-----------------------------------------------------------------------------------------
	
	/**
	 * The component's default constructor (without dependencies), or null if dependencies required
	 */
	private Constructor<? extends T> _defaultConstructor = null ;
	
	/**
	 * The component's constructor with dependencies, or null if no dependencies
	 */
	private Constructor<? extends T> _constructorWithDependencies = null ;
	
	/**
	 * The fields to be injected in the component instance after construction<br>
	 * The fields with a "@Inject" annotation
	 * ( null if none )
	 */
	private Field[] _fieldsToBeInjected = null ;
	

	
	//-----------------------------------------------------------------------------------------
	/**
	 * Initialization after construction
	 * @param container
	 */
	private void init( InjectorImpl container )
	{
		//--- Container
		if ( container == null )
		{
			throw new IllegalArgumentException("Container parameter is null");
		}
		_container = container ;
		
		if ( _implementationClass != null ) {
			
			//--- Constructors 
			Constructor<? extends T> constructorToBeUsed = getConstructorToBeUsed(_implementationClass);
			if ( constructorToBeUsed.getParameterTypes().length == 0 ) {
				_defaultConstructor = constructorToBeUsed ;
			}
			else {
				_constructorWithDependencies = constructorToBeUsed ;
			}
	
			//--- Fields to be injected 
			_fieldsToBeInjected = getFieldsToBeInjected(_implementationClass);
					
			//--- Mono-Instance flag 
			_bMonoInstance = _container.isMonoInstance(_implementationClass);
		}
	}
	
	//-----------------------------------------------------------------------------------------
	/**
	 * Constructor for a component definition
	 * @param container
	 * @param componentClass the component class (can be an interface or a concrete class)
	 * @param implementationClass the implementation class (used and required if the component class is an interface)
	 */
	public ComponentDefinition( InjectorImpl container, Class<T> componentClass, Class<? extends T> implementationClass ) 
	{
		super();
		if ( null == componentClass ) {
			throw new IllegalArgumentException("Component class parameter is null");
		}
		_componentClass = componentClass ;
		if ( ClassTools.isInterfaceOrAbstract(componentClass) )
		{
    		//--- Interface or Abstract class => implementation/concrete class must be provided
    		if ( implementationClass != null )
    		{
	    		_implementationClass = implementationClass ;
    		}
    		else
    		{
    			throw new IllegalArgumentException("Implementation/concrete class parameter is null");
    		}
    	}
    	else
    	{
    		//--- Not an interface => implementation is the same class
    		_implementationClass = componentClass ;
    	}
		
		//--- No specific provider 
		_implementationProvider = null ;

		init( container );
	}
	
	public ComponentDefinition( InjectorImpl container, Class<T> componentClass, Provider<T> provider ) 
	{
		super();
		if ( null == componentClass ) {
			throw new IllegalArgumentException("Component class parameter is null");
		}
		if ( null == provider ) {
			throw new IllegalArgumentException("Provider parameter is null");
		}
		
		_componentClass = componentClass ;
   		_implementationClass = null ;
   		_implementationProvider = provider ;
		init( container );
	}
	
	/**
	 * Constructor for a component with a concrete class ( without implementation class )
	 * @param container
	 * @param componentClass the unique class for the component (supposed to be concrete)
	 */
	public ComponentDefinition( InjectorImpl container, Class<T> componentClass  ) 
	{
		super();
		if ( null == componentClass ) {
			throw new IllegalArgumentException("Component class parameter is null");
		}
		if ( ClassTools.isInterfaceOrAbstract(componentClass) ) {
			throw new IllegalArgumentException("Cannot create component for interface/abstract class without implementation");
		}
		_componentClass = componentClass ;
		//--- Not an interface or abstract class => implementation is the same class
		_implementationClass = componentClass ;
		//--- No specific provider 
		_implementationProvider = null ;
		init( container );
	}
	
	//-----------------------------------------------------------------------------------------
    private final Constructor<? extends T> convert(Constructor<?> constructor1) {
    	@SuppressWarnings("unchecked")
    	Constructor<? extends T> constructor2 = (Constructor<? extends T>)constructor1 ;
    	return constructor2;
    }
    
	//-----------------------------------------------------------------------------------------
    /**
     * Returns the constructor that will be used for Dependency Injection
     * @param clazz
     * @return the constructor
     */
    private final Constructor<? extends T> getConstructorToBeUsed(Class<? extends T> clazz)
	{
		Constructor<?>[] constructors = clazz.getConstructors(); // all public constructors
		if ( constructors.length == 0 ) {
			throw new IllegalStateException("No accessible constructor for class '" + clazz.getCanonicalName() + "'");
		}
		else {
			if ( constructors.length == 1 ) {
				// Only one constructor found => use it
				//return constructors[0];
				return convert(constructors[0]);
			}
			else {
				// Multiple constructors found => use the only one with "@Inject" 
				Constructor<? extends T> defaultConstructor  = null ;
				Constructor<? extends T> lastConstructorWithInject = null ;
				int injectCount = 0 ;
				for ( Constructor<?> constructor : constructors ) {
					//if ( constructor.getAnnotation(Inject.class) != null ) {
					if ( AnnotationUtil.hasInjectAnnotation(constructor) ) {
						lastConstructorWithInject = convert(constructor) ;
						injectCount++;
					}
					if ( constructor.getParameterTypes().length == 0 ) {
						defaultConstructor = convert(constructor)  ;
					}
				}
				if ( injectCount == 1 ) {
					//--- Priority 1 : @Inject specified => use the annotated constructor
					return lastConstructorWithInject ;
				}
				if ( injectCount == 0 ) {
					//--- Priority 2 : @Inject not specified => use the default constructor if it exists 
					if ( defaultConstructor != null ) {
						//--- There's a default constructor => use it 
						return defaultConstructor ;
					}
					else {
						//--- No default constructor and no @Inject annotation => cannot choose
						throw new IllegalStateException("Class '" + clazz.getCanonicalName() + "' : " 
								+ constructors.length + " constructors without default constructor, @Inject is required " );
					}
				}
				if ( injectCount > 1 ) {
					throw new IllegalStateException("Class '" + clazz.getCanonicalName() + "' : " 
													+ injectCount + " constructors with @Inject, only one expected" );
				}				
			}
		}
		throw new IllegalStateException("Unexpected error while serching the constructor for class '" + clazz.getCanonicalName() + "'");
	}

	//-----------------------------------------------------------------------------------------
    /**
     * Returns an array of fields that require a Dependency Injection
     * @param clazz
     * @return the fields array or null if none
     */
    private Field[] getFieldsToBeInjected(Class<? extends T> clazz) {
    	LinkedList<Field> fieldsToBeInjected = new LinkedList<Field>();
    	
    	Field[] declaredFields = clazz.getDeclaredFields() ;
    	for ( Field field : declaredFields ) {
    		//if ( field.getAnnotation(Inject.class) != null ) {
        	if ( AnnotationUtil.hasInjectAnnotation(field) ) {
    			fieldsToBeInjected.add(field);
    		}
    	}
    	if ( fieldsToBeInjected.size() > 0 ) {
    		return fieldsToBeInjected.toArray(VOID_FIELD_ARRAY);
    	}
    	else {
    		return null ;
    	}
    }
    
//	//-----------------------------------------------------------------------------------------
//    /**
//     * Returns the default constructor (if any) for the given class
//     * @param cl
//     * @return the default constructor, or null if none
//     */
//    private final Constructor<? extends T> getDefaultConstructor(Class<? extends T> cl)
//	{
//    	// for each public constructor...
//		for ( Constructor<?> constructor : cl.getConstructors() ) {
//			if ( constructor.getParameterTypes().length == 0 ) {
//				// no parameters => default constructor
//				return convert(constructor) ;
//			}
//		}
//		return null ;
//	}

//    //-----------------------------------------------------------------------------------------
//    /**
//     * Returns the first constructor found with at least one parameter
//     * @param cl
//     * @return the constructor with parameters, or null if none
//     */
//    private final Constructor<? extends T> getConstructorWithParameters(Class<? extends T> cl)
//	{
//    	// for each public constructor...
//		for ( Constructor<?> constructor : cl.getConstructors() ) {
//			if ( constructor.getParameterTypes().length > 0 ) {
//				return convert(constructor) ;
//			}
//		}
//		return null ;
//	}

	//-----------------------------------------------------------------------------------------
	/**
	 * Returns the class that identifies the component (concrete class or interface) 
	 * @return
	 */
	public Class<?> getComponentClass() 
	{
		return _componentClass ;
	}

	//-----------------------------------------------------------------------------------------
	/**
	 * Returns the concrete class used to create component's instance
	 * @return
	 */
	public Class<?> getImplementationClass() 
	{
		return _implementationClass ;
	}

	//-----------------------------------------------------------------------------------------
    /**
     * Return the concrete component class instance. <br>
     * If the component is an interface an instance of the implementation class is returned
     * @return
     */
    public T getInstance()
    {
        if ( _bMonoInstance )
        {
        	// "mono-instance" => reuse the single instance
        	if ( _singleInstance == null )
        	{
        		_singleInstance = createInstance(); // (_implementationClass);
        	}
        	return _singleInstance ;
        }
        else
        {
        	// Not "mono-instance" => always create a new instance
        	return createInstance(); // ( _implementationClass );
        }
    }
    
	//-----------------------------------------------------------------------------------------
    private T createInstance()
    {
    	T newInstance = createBasicInstance() ;

    	//--- Interface and implementation ?
    	if ( _componentClass.isInterface() ) {
    		
    		//--- Is there at least one interceptor provider for this container ?
    		InterceptorProvider[] interceptorProviders = _container.getInterceptorProviders();
    		if ( interceptorProviders != null ) {
			
        		//--- Create a dynamic proxy for interceptor notification
        		InvocationHandler invocationHandler = new GenericInvocationHandler( newInstance, interceptorProviders );
        		
        		Class<?>[] interfaces = { _componentClass } ;
        		Object proxy = Proxy.newProxyInstance(_componentClass.getClassLoader(), interfaces, invocationHandler ) ;
        		
        		@SuppressWarnings("unchecked")
        		T proxyInstance = (T) proxy ;
        		return proxyInstance ;
    		}
    	}
		//--- Return the new instance as is, without dynamic proxy handler 
		return newInstance ;
    }
	//-----------------------------------------------------------------------------------------
    /**
     * Creates a new component instance using the given class, with or without dependencies
     * @param componentClass
     * @return
     */
    //private T createInstance(Class<? extends T> componentClass)
    private T createBasicInstance()
    {
    	T newInstance = null ;
    	//--- 1) Build a new instance (with provider or implementation class constructor)
    	if ( _implementationProvider != null ) {
        	//--- There is a specific provider for this class
    		newInstance = _implementationProvider.get();
    	}
    	else
    	{    		
	    	//--- Use the predefined constructor to create a new instance of the component
			if ( _defaultConstructor != null )
			{
				//--- No Dependency Injection
				newInstance = createWithDefaultConstructor(_implementationClass); //(componentClass);
			}
			else if ( _constructorWithDependencies != null )
			{
				//--- Dependency Injection by constructor
				newInstance = createWithDependencies(_implementationClass); //(componentClass);
			}
			else 
			{
				throw new InjectorException("Cannot create instance for class " + _implementationClass + " : no constructor ");
			}
    	}
			
		//--- 2) Dependency Injection by fields (if any)
		if ( _fieldsToBeInjected != null ) {
			for ( Field field : _fieldsToBeInjected ) {
				injectField(newInstance, field);
			}
		}
		return newInstance ;
    }
	
	//-----------------------------------------------------------------------------------------
	/**
	 * Creates a new component instance using the default constructor ( without dependencies )
	 * @param componentClass
	 * @return
	 */
	private final T createWithDefaultConstructor(Class<? extends T> componentClass)
	{
    	T obj = null ;
    	try {
   			obj = componentClass.newInstance();
		} catch (InstantiationException e) {
            throw new InjectorException("Cannot create instance for class " + componentClass, e);
		} catch (IllegalAccessException e) {
            throw new InjectorException("Cannot create instance for class " + componentClass, e);
		}
		return obj ;
	}
	
	//-----------------------------------------------------------------------------------------
	/**
	 * Creates a new component instance using a constructor with parameters ( with dependencies )
	 * @param componentClass
	 * @return
	 */
	private final T createWithDependencies(Class<? extends T> componentClass)
	{
		Constructor<? extends T> constructor = _constructorWithDependencies ;
		
		//--- Get the constructor parameters instances
		Class<?>[] paramTypes = constructor.getParameterTypes();
		Object[] params = new Object[paramTypes.length];
		for ( int i = 0 ; i < paramTypes.length ; i++ )
		{
			//--- Use the container to get the required dependencies
			params[i] = _container.getComponentInstance( paramTypes[i] );
		}
		
		//--- Call the constructor with parameters		
		T obj = null ;
		try {
			obj = constructor.newInstance(params);
		} catch (IllegalArgumentException e) {
            throw new InjectorException("Cannot create instance for class " + componentClass, e);
		} catch (InstantiationException e) {
            throw new InjectorException("Cannot create instance for class " + componentClass, e);
		} catch (IllegalAccessException e) {
            throw new InjectorException("Cannot create instance for class " + componentClass, e);
		} catch (InvocationTargetException e) {
            throw new InjectorException("Cannot create instance for class " + componentClass, e);
		}
		return obj ;
	}
	//-----------------------------------------------------------------------------------------
	/**
	 * Injects a instance in the component field
	 * @param componentInstance
	 * @param field
	 */
	private final void injectField(Object componentInstance, Field field)
	{
		//--- Get the instance to be injected
		Class<?> fieldType = field.getType();
		Object instanceToBeInjected = _container.getComponentInstance( fieldType );
		
		//--- Inject the instance in the field
		field.setAccessible(true);
		try {
			field.set(componentInstance, instanceToBeInjected);
		} catch (IllegalArgumentException e) {
			throw new InjectorException("Cannot inject field '" + field.getName() + "' (IllegalArgumentException)", e);
		} catch (IllegalAccessException e) {
			throw new InjectorException("Cannot inject field '" + field.getName() + "' (IllegalAccessException)", e);
		}
	}
	
	//-----------------------------------------------------------------------------------------
	@Override
	public String toString() 
	{
		String sSingleton = "singleton = " + ( _bMonoInstance ? "true" : "false" ) ;
		String sImplem = "" ;
		if ( _implementationProvider != null ) {
			sImplem = "implementation provider = '" + _implementationProvider.getClass().getCanonicalName() + "'" ;
		}
		else {
			if ( _implementationClass != null ) {
				sImplem = "implementation class = '" + _implementationClass.getCanonicalName() + "'" ;
			}
		}
		return _componentClass.getCanonicalName() + " : " + sImplem + ", " + sSingleton ;
	}

}
