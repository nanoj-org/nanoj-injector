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


/**
 * Interface to be implemented by each interceptor provider <br>.
 * 
 * 
 * @author Laurent Guerin
 *
 */
public interface InterceptorProvider {

	/**
	 * Returns the interceptor to be used for the given method, or null if none.<br>
	 * <br>
	 * Called before each method invocation by the dynamic proxy handler.<br>
	 * The provider is responsible to create a new instance if necessary <br>
	 * in order to provide a thread safe interceptor.<br> 
	 * 
	 * @param interfaceMethod the invoked method
	 * @param componentInstance the targeted component instance
	 * @return
	 */
	Interceptor getInterceptor(Method interfaceMethod, Object componentInstance);
}
