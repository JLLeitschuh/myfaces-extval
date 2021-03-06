/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.extensions.validator.generic.renderkit;

import org.apache.myfaces.extensions.validator.core.renderkit.ExtValRenderKit;
import org.apache.myfaces.extensions.validator.internal.UsageInformation;
import org.apache.myfaces.extensions.validator.internal.UsageCategory;
import org.apache.myfaces.extensions.validator.util.ProxyUtils;

import javax.faces.render.RenderKit;
import javax.faces.render.Renderer;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodProxy;

/**
 * @since 1.x.1
 */
@UsageInformation(UsageCategory.INTERNAL)
public class ExtValGenericRenderKit extends ExtValRenderKit implements MethodInterceptor
{
    public static RenderKit newInstance(RenderKit renderKit)
    {
        Class currentClass = renderKit.getClass();

        //it's not possible to wrap the converter again - occurs e.g. under solaris + bea weblogic
        if (ProxyUtils.isProxiedClass(currentClass))
        {
            return renderKit;
        }

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(renderKit.getClass());
        enhancer.setCallback(new ExtValGenericRenderKit(renderKit));
        enhancer.setClassLoader(Thread.currentThread().getContextClassLoader());

        return (RenderKit) enhancer.create();
    }

    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
    {
        if(method.getName().equals("getRenderer"))
        {
            return getRenderer((String)args[0], (String)args[1]);
        }
        else if(method.getName().equals("addRenderer"))
        {
            addRenderer((String)args[0], (String)args[1], (Renderer)args[2]);
        }
        else if(method.getName().equals("getResponseStateManager"))
        {
            return getResponseStateManager();
        }
        else if(method.getName().equals("createResponseStream"))
        {
            return createResponseStream((OutputStream)args[0]);
        }
        else if(method.getName().equals("createResponseWriter"))
        {
            return createResponseWriter((Writer)args[0], (String)args[1], (String)args[2]);
        }
        else
        {
            try
            {
                method.setAccessible(true);
                return method.invoke(obj, args);
            }
            catch (Throwable t)
            {
                return proxy.invokeSuper(obj, args);
            }
        }

        return null;
    }

    public ExtValGenericRenderKit(RenderKit wrapped)
    {
        super(wrapped);
    }

    @Override
    protected Renderer createWrapper(Renderer renderer)
    {
        return ExtValGenericRendererWrapper.newInstance(renderer);
    }
}
