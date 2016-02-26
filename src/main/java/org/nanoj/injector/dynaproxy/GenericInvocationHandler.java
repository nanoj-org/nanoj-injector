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
package org.nanoj.injector.dynaproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.LinkedList;

import org.nanoj.injector.aop.Interceptor;
import org.nanoj.injector.aop.InterceptorProvider;
import org.nanoj.injector.tools.BasicLogger;
import org.nanoj.injector.tools.MethodInvoker;

/**
 * Generic handler used by the dynamic proxies for AOP.<br>
 * 
 * There's an instance of this class for each component instance<br>
 * Created when the component is instantiated (if the container manages interceptors)<br>
 * 
 * @author Laurent GUERIN
 *
 */
public class GenericInvocationHandler implements InvocationHandler {

	private static final BasicLogger logger = BasicLogger.getLogger(GenericInvocationHandler.class);

	private final static Interceptor[] VOID_INTERCEPTOR_ARRAY = new Interceptor[0];
    
	//-----------------------------------------------------------------------------------------
	
	private final Object                 componentInstance ;

	private final InterceptorProvider[]  interceptorProviders ;
	
	//-----------------------------------------------------------------------------------------

	/**
	 * Constructor
	 * @param componentInstance
	 * @param interceptorProviders
	 */
	public GenericInvocationHandler(Object componentInstance, InterceptorProvider[] interceptorProviders) {
		super();
		if ( null == componentInstance ) {
			throw new IllegalArgumentException("Component instance is null");
		}
		this.componentInstance = componentInstance ; // Not null
		this.interceptorProviders = interceptorProviders ; // Can be null
	}

//	/**
//	 * Returns the interceptor for the given method if any
//	 * @param method
//	 * @return 
//	 */
//	private Interceptor getInterceptor( Method method )  {
//		if ( this.interceptorProvider != null ) {
//			return this.interceptorProvider.getInterceptor(method, componentInstance);
//		}
//		return null ;
//	}
	
	/**
	 * Returns all the active interceptors for the given method according with the "pointcuts" or specific providers conditions
	 * @param method
	 * @return
	 */
	private Interceptor[] getActiveInterceptorsForMethod( Method method )  {

		//InterceptorProvider[] interceptorProviders = this.interceptorProviders;
		if ( interceptorProviders != null ) {
			//--- There are some interceptor providers (at least one)
			LinkedList<Interceptor> activeInterceptors = new LinkedList<Interceptor>() ;
			for ( int i = 0 ; i < interceptorProviders.length ; i++ ) {
				//--- Is this interceptor active for this method ?
				InterceptorProvider interceptorProvider = interceptorProviders[i];
				Interceptor interceptor = interceptorProvider.getInterceptor(method, componentInstance);
				if ( interceptor != null ) {
					//--- Active => keep it
					activeInterceptors.add(interceptor);
				}
			}
			if ( activeInterceptors.size() > 0 ) {
				return activeInterceptors.toArray( VOID_INTERCEPTOR_ARRAY );
			}
		}
		return null ; // No active interceptor
	}
	
	/**
	 * Triggered the "beforeCall" event for all the interceptors
	 * @param interceptors
	 * @param method
	 * @param args
	 */
	private void notifyInterceptorsBeforeCall(Interceptor[] interceptors, Method method, Object[] args) {
		//--- Notify all the interceptors in the defined order
		for ( int i = 0 ; i < interceptors.length ; i++ ) {
			Interceptor interceptor = interceptors[i];
			if ( interceptor != null ) {
				interceptor.beforeCall(componentInstance, method, args);
			}
		}
	}
	
	/**
	 * Triggered the "afterCall" event for all the interceptors
	 * @param interceptors
	 * @param method
	 * @param args
	 * @param result
	 */
	private void notifyInterceptorsAfterCall(Interceptor[] interceptors, Method method, Object[] args, Object result ) {
		//--- Notify all the interceptors in the reversed order
		for ( int i = interceptors.length-1 ; i >= 0 ; i-- ) {
			Interceptor interceptor = interceptors[i];
			if ( interceptor != null ) {
				interceptor.afterCall(componentInstance, method, args, result);
			}
		}
	}
	
	/**
	 * Triggered the "onError" event for all the interceptors
	 * @param interceptors
	 * @param method
	 * @param args
	 * @param exception
	 */
	private void notifyInterceptorsOnError(Interceptor[] interceptors, Method method, Object[] args, Exception exception) {
		//--- Notify all the interceptors in the reversed order
		for ( int i = interceptors.length-1 ; i >= 0 ; i-- ) {
			Interceptor interceptor = interceptors[i];
			if ( interceptor != null ) {
				interceptor.onError(componentInstance, method, args, exception);
			}
		}
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Exception  {
		
		//Interceptor interceptor = getInterceptor(method); // NB : interface method (not implementation class method)
		Interceptor[] interceptors = getActiveInterceptorsForMethod(method);
		if ( interceptors != null ) {

			//--- Before Call
			logger.println("===== [ PROXY ] : Before call / " + componentInstance.getClass().getSimpleName() + " " + method.getName() );
			//interceptor.beforeCall(componentInstance, method, args);
			notifyInterceptorsBeforeCall(interceptors, method, args);
			
			//--- Call ( invoke the method )
			Exception exception = null ;
			Object result = null ;
			try {
				result = MethodInvoker.invoke(componentInstance, method, args) ;
			} catch (Exception e) {
				exception = e ;
			}
			
			if ( exception != null ) {
				//--- Call error
				logger.println("===== [ PROXY ] : Call error / " + componentInstance.getClass().getSimpleName() + " " + method.getName() );
				//interceptor.onError(componentInstance, method, args, exception);
				notifyInterceptorsOnError(interceptors, method, args, exception);
				
				//--- Exception propagation 
				throw exception ;
			}
			else {
				//--- After Call
				logger.println("===== [ PROXY ] : After call / " + componentInstance.getClass().getSimpleName() + " " + method.getName() );
				//interceptor.afterCall(componentInstance, method, args, result);
				notifyInterceptorsAfterCall(interceptors, method, args, result);
			}
			
			return result;
		}
		else {
			//--- No interceptor 
			Object result = MethodInvoker.invoke(componentInstance, method, args) ;
			return result ;
		}
	}

}
