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
package org.apache.myfaces.extensions.validator.initializer.trinidad.component;

import org.apache.myfaces.extensions.validator.core.initializer.component.ComponentInitializer;
import org.apache.myfaces.extensions.validator.core.metadata.MetaDataKeys;
import org.apache.myfaces.trinidad.validator.RegExpValidator;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.util.Map;

/**
 * @author Gerhard Petracek
 * @since 1.x.1
 */
public class PatternInitializer implements ComponentInitializer
{
    public void configureComponent(FacesContext facesContext, UIComponent uiComponent, Map<String, Object> metaData)
    {
        if(!metaData.containsKey(MetaDataKeys.PATTERN))
        {
            return;
        }

        String[] patterns = (String[])metaData.get(MetaDataKeys.PATTERN);

        RegExpValidator regExpValidator;

        for(String pattern : patterns)
        {
            regExpValidator = (RegExpValidator)facesContext.getApplication()
                                                .createValidator("org.apache.myfaces.trinidad.RegExp");

            regExpValidator.setPattern(pattern);
            regExpValidator.setMessageDetailNoMatch((String)metaData.get(
                MetaDataKeys.PATTERN_VALIDATION_ERROR_MESSAGE));

            ((EditableValueHolder)uiComponent).addValidator(regExpValidator);
        }
    }
}