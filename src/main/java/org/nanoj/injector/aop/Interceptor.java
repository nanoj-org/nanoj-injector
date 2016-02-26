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
package org.nanoj.injector.aop;

import java.lang.reflect.Method;

public interface Interceptor {

	/**
	 * Triggered before a method call
	 * @param instance
	 * @param method
	 * @param args
	 */
	void beforeCall(Object instance, Method method, Object[] args );
	
	/**
	 * Triggered after a normal method (if no exception has been thrown by the method)
	 * @param instance
	 * @param method
	 * @param args
	 * @param result
	 */
	void afterCall(Object instance, Method method, Object[] args, Object result);
	
	/**
	 * Triggered if the called method has thrown an exception
	 * @param instance
	 * @param method
	 * @param args
	 * @param exception
	 */
	void onError(Object instance, Method method, Object[] args, Exception exception);
	
}
