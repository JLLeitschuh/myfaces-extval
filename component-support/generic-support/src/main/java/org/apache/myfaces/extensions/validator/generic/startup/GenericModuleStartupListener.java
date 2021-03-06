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
package org.apache.myfaces.extensions.validator.generic.startup;

import org.apache.myfaces.extensions.validator.core.ExtValContext;
import org.apache.myfaces.extensions.validator.core.factory.FactoryNames;
import org.apache.myfaces.extensions.validator.core.renderkit.AbstractRenderKitWrapperFactory;
import org.apache.myfaces.extensions.validator.core.startup.AbstractStartupListener;
import org.apache.myfaces.extensions.validator.generic.renderkit.GenericRenderKitWrapperFactory;
import org.apache.myfaces.extensions.validator.internal.UsageCategory;
import org.apache.myfaces.extensions.validator.internal.UsageInformation;

/**
 * @since 1.x.1
 */
@UsageInformation(UsageCategory.INTERNAL)
public class GenericModuleStartupListener extends AbstractStartupListener
{
    private static final long serialVersionUID = 4392156032877519556L;

    protected void init()
    {
        ExtValContext.getContext().getFactoryFinder().getFactory(FactoryNames.RENDERKIT_WRAPPER_FACTORY,
                AbstractRenderKitWrapperFactory.class).addRenderKitWrapperFactory(new GenericRenderKitWrapperFactory());
    }
}
