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
package org.apache.myfaces.extensions.validator.core.validation.message.resolver;

import org.apache.myfaces.extensions.validator.core.WebXmlParameter;
import org.apache.myfaces.extensions.validator.util.ExtValUtils;

/**
 * @author Gerhard Petracek
 */
public class DefaultValidationErrorMessageResolver extends AbstractValidationErrorMessageResolver {
    private static final String CUSTOM_BUNDLE = WebXmlParameter.CUSTOM_MESSAGE_BUNDLE;

    //not used at the moment - just for a convention
    protected String getBaseName() {
        return ExtValUtils.getInformationProviderBean().getConventionForModuleMessageBundle(getClass().getPackage().getName());
    }

    protected String getCustomBaseName() {
        return CUSTOM_BUNDLE;
    }
}