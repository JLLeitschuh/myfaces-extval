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
package org.apache.myfaces.extensions.validator.core.validation.message.resolver.mapper;

import org.apache.myfaces.extensions.validator.core.validation.strategy.ValidationStrategy;
import org.apache.myfaces.extensions.validator.core.InternalConventionProvider;
import org.apache.myfaces.extensions.validator.core.InvocationOrder;
import org.apache.myfaces.extensions.validator.internal.UsageInformation;
import org.apache.myfaces.extensions.validator.internal.UsageCategory;
import org.apache.myfaces.extensions.validator.util.ProxyUtils;

/**
 * Default implementation which maps ExtVal ValidationStrategies to ExtVal MessageResolvers.
 *
 * @since 1.x.1
 */
@InvocationOrder(300)
@UsageInformation(UsageCategory.INTERNAL)
public class DefaultValidationStrategyToMsgResolverNameMapper extends
    AbstractValidationStrategyToMsgResolverNameMapper
{
    public String createName(ValidationStrategy validationStrategy)
    {
        Class<? extends ValidationStrategy> validationStrategyClass = ProxyUtils
                .getUnproxiedClass(validationStrategy.getClass(), ValidationStrategy.class);
        return InternalConventionProvider.getMessageResolverClassName(validationStrategyClass,
                                                     getClassName(validationStrategyClass.getSimpleName()));
    }

    protected String getClassName(String strategyClassName)
    {
        return InternalConventionProvider.getMessageResolverClassName(strategyClassName);
    }
}
