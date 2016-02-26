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
package org.nanoj.injector.tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.nanoj.injector.InjectorException;

public class MethodInvoker {

	/**
	 * Invokes the given method on the given instance <br>
	 * The exception thrown can be either the exception thrown by the called method, <br>
	 * or a TelosysInjectorException for any other cases 
	 * @param componentInstance
	 * @param method
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public final static Object invoke(Object componentInstance, Method method, Object[] args) throws Exception
	{
		
		Object result = null ;
		try {
			result = method.invoke(componentInstance, args);
		} catch (IllegalArgumentException e) {
			//--- Exception caused by a failure in the reflection call 
			throw new InjectorException("Error when invoking method '" + method.getName() + "' : IllegalArgumentException", e) ;
		} catch (IllegalAccessException e) {
			//--- Exception caused by a failure in the reflection call 
			throw new InjectorException("Error when invoking method '" + method.getName() + "' : IllegalAccessException", e) ;
		} catch (InvocationTargetException e) {
			//--- The called method has thrown an exception 
			Throwable originalException = e.getCause();
			// Throwable : Exception or Error ?
			if ( originalException instanceof Exception ) {
				//--- Exception (checked or not)
				throw (Exception) originalException ;
			}
			else {
				//--- Other : unexpected Error 
				throw new InjectorException("Error when invoking method '" + method.getName() + "' : Throwable/Error", e) ;
			}
		}
		return result ;
		
	}
}
