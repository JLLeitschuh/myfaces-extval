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
package org.apache.myfaces.extensions.validator.trinidad.initializer.component;

import org.apache.myfaces.extensions.validator.core.metadata.CommonMetaDataKeys;
import org.apache.myfaces.extensions.validator.internal.ToDo;
import org.apache.myfaces.extensions.validator.internal.Priority;
import org.apache.myfaces.extensions.validator.internal.UsageInformation;
import org.apache.myfaces.extensions.validator.internal.UsageCategory;
import org.apache.myfaces.extensions.validator.trinidad.ExtValTrinidadClientValidatorWrapper;
import org.apache.myfaces.trinidad.validator.DoubleRangeValidator;
import org.apache.myfaces.trinidad.validator.ClientValidator;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.util.Map;

/**
 * @since 1.x.1
 */
@ToDo(value = Priority.MEDIUM, description = "skipValidationSupport for client-side validation")
@UsageInformation(UsageCategory.INTERNAL)
class DoubleRangeInitializer extends TrinidadComponentInitializer
{
    @Override
    public boolean configureTrinidadComponent(FacesContext facesContext, UIComponent uiComponent,
                                              Map<String, Object> metaData)
    {
        boolean informationAdded = false;
        DoubleRangeValidator doubleRangeValidator = (DoubleRangeValidator)facesContext.getApplication()
                                            .createValidator("org.apache.myfaces.trinidad.DoubleRange");

        Object min = null;
        if(metaData.containsKey(CommonMetaDataKeys.RANGE_MIN))
        {
            min = metaData.get(CommonMetaDataKeys.RANGE_MIN);
        }
        else if(metaData.containsKey(CommonMetaDataKeys.RANGE_MIN_DEFAULT))
        {
            min = metaData.get(CommonMetaDataKeys.RANGE_MIN_DEFAULT);
        }

        if(min instanceof Double)
        {
            doubleRangeValidator.setMinimum((Double)min);
            informationAdded = true;
        }

        Object max = null;
        if(metaData.containsKey(CommonMetaDataKeys.RANGE_MAX))
        {
            max = metaData.get(CommonMetaDataKeys.RANGE_MAX);
        }
        else if(metaData.containsKey(CommonMetaDataKeys.RANGE_MAX_DEFAULT))
        {
            max = metaData.get(CommonMetaDataKeys.RANGE_MAX_DEFAULT);
        }

        if(max instanceof Double)
        {
            doubleRangeValidator.setMaximum((Double)max);
            informationAdded = true;
        }

        if(informationAdded &&
                doubleRangeValidator instanceof ClientValidator && uiComponent instanceof EditableValueHolder)
        {
            ((EditableValueHolder)uiComponent).addValidator(
                    new ExtValTrinidadClientValidatorWrapper((ClientValidator)doubleRangeValidator));
            return true;
        }
        return false;
    }
}
