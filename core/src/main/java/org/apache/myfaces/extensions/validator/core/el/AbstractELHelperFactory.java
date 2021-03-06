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
package org.apache.myfaces.extensions.validator.core.el;

import org.apache.myfaces.extensions.validator.internal.UsageInformation;
import org.apache.myfaces.extensions.validator.internal.UsageCategory;

import java.util.logging.Logger;

/**
 * details
 * @see DefaultELHelper
 * 
 * @since 1.x.1
 */
@UsageInformation({UsageCategory.INTERNAL, UsageCategory.CUSTOMIZABLE})
public abstract class AbstractELHelperFactory
{
    protected final Logger logger = Logger.getLogger(getClass().getName());
    protected AbstractELHelperFactory customELHelperFactory;

    protected AbstractELHelperFactory()
    {
        logger.fine(getClass().getName() + " instantiated");
    }

    public void setCustomELHelperFactory(AbstractELHelperFactory elHelperFactory)
    {
        this.customELHelperFactory = elHelperFactory;
    }

    public final ELHelper create()
    {
        ELHelper result = null;

        if(this.customELHelperFactory != null)
        {
            result = this.customELHelperFactory.createELHelper();
        }

        if(result == null)
        {
            return createELHelper();
        }

        return result;
    }

    protected abstract ELHelper createELHelper();
}