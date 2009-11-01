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
package org.apache.myfaces.extensions.validator.beanval;

import org.apache.commons.logging.Log;
import org.apache.myfaces.extensions.validator.beanval.validation.strategy.BeanValidationVirtualValidationStrategy;
import org.apache.myfaces.extensions.validator.beanval.util.BeanValidationUtils;
import org.apache.myfaces.extensions.validator.core.metadata.MetaDataEntry;
import org.apache.myfaces.extensions.validator.core.metadata.extractor.MetaDataExtractor;
import org.apache.myfaces.extensions.validator.core.metadata.transformer.MetaDataTransformer;
import org.apache.myfaces.extensions.validator.core.property.PropertyDetails;
import org.apache.myfaces.extensions.validator.core.property.PropertyInformation;
import org.apache.myfaces.extensions.validator.core.property.PropertyInformationKeys;
import org.apache.myfaces.extensions.validator.core.ValidationModuleKey;
import org.apache.myfaces.extensions.validator.internal.UsageCategory;
import org.apache.myfaces.extensions.validator.internal.UsageInformation;
import org.apache.myfaces.extensions.validator.internal.ToDo;
import org.apache.myfaces.extensions.validator.internal.Priority;
import org.apache.myfaces.extensions.validator.util.ExtValUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.validation.ConstraintViolation;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ElementDescriptor;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Gerhard Petracek
 * @since x.x.3
 */
@UsageInformation(UsageCategory.INTERNAL)
class BeanValidationInterceptorInternals
{
    private Log logger;

    BeanValidationInterceptorInternals(Log logger)
    {
        this.logger = logger;
    }

    PropertyDetails extractPropertyDetails(FacesContext facesContext, UIComponent uiComponent)
    {
        PropertyDetails result = getComponentMetaDataExtractor(uiComponent)
                .extract(facesContext, uiComponent)
                .getInformation(PropertyInformationKeys.PROPERTY_DETAILS, PropertyDetails.class);

        if (result.getBaseObject() == null && this.logger.isWarnEnabled())
        {
            this.logger.warn("no base object at " + result.getKey() +
                    " component-id: " + uiComponent.getClientId(facesContext));
        }

        return result.getBaseObject() != null ? result : null;
    }

    /*
     * also invokes meta-data extraction interceptors
     * (see e.g. ExtValBeanValidationMetaDataExtractionInterceptor)
     */
    MetaDataExtractor getComponentMetaDataExtractor(UIComponent uiComponent)
    {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(ValidationModuleKey.class.getName(), BeanValidationModuleKey.class);
        properties.put(UIComponent.class.getName(), uiComponent);

        return ExtValUtils.getComponentMetaDataExtractorWith(properties);
    }

    void initComponentWithPropertyDetails(
            FacesContext facesContext, UIComponent uiComponent, PropertyDetails propertyDetails)
    {
        Class[] foundGroups = resolveGroups(facesContext, uiComponent);

        if (foundGroups == null)
        {
            return;
        }
        else if(foundGroups.length == 0)
        {
            foundGroups = new Class[]{Default.class};
        }

        ElementDescriptor elementDescriptor = getDescriptorFor(
                propertyDetails.getBaseObject().getClass(), propertyDetails.getProperty());

        if (elementDescriptor == null)
        {
            return;
        }

        Map<String, Object> metaData;

        for (ConstraintDescriptor<?> constraintDescriptor :
                elementDescriptor.findConstraints().unorderedAndMatchingGroups(foundGroups).getConstraintDescriptors())
        {
            metaData = transformConstraintDescriptorToMetaData(
                    constraintDescriptor, elementDescriptor.getElementClass());

            if (metaData != null && !metaData.isEmpty())
            {
                ExtValUtils.configureComponentWithMetaData(facesContext, uiComponent, metaData);
            }
        }
    }

    @ToDo(value = Priority.MEDIUM, description = "ConstraintDescriptor#isReportAsSingleViolation")
    private Map<String, Object> transformConstraintDescriptorToMetaData(
            ConstraintDescriptor<?> constraintDescriptor, Class elementClass)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        MetaDataTransformer metaDataTransformer;

        metaDataTransformer = ExtValUtils.getMetaDataTransformerForValidationStrategy(
                new BeanValidationVirtualValidationStrategy(constraintDescriptor, elementClass));

        if (metaDataTransformer != null)
        {
            result.putAll(transformMetaData(metaDataTransformer, constraintDescriptor));
        }

        if(!constraintDescriptor.isReportAsSingleViolation())
        {
            Set<ConstraintDescriptor<?>> composingConstraints = constraintDescriptor.getComposingConstraints();
            if(composingConstraints != null && !composingConstraints.isEmpty())
            {
                result.putAll(transformComposingConstraints(composingConstraints, elementClass));
            }
        }

        return result;
    }

    private Map<String, Object> transformComposingConstraints(
            Set<ConstraintDescriptor<?>> composingConstraints, Class elementClass)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        for(ConstraintDescriptor constraintDescriptor : composingConstraints)
        {
            result.putAll(transformConstraintDescriptorToMetaData(constraintDescriptor, elementClass));
        }

        return result;
    }

    private Map<String, Object> transformMetaData(
            MetaDataTransformer metaDataTransformer, ConstraintDescriptor<?> constraintDescriptor)
    {
        MetaDataEntry entry;
        Map<String, Object> result;
        if (this.logger.isDebugEnabled())
        {
            this.logger.debug(metaDataTransformer.getClass().getName() + " instantiated");
        }

        entry = new MetaDataEntry();
        entry.setKey(constraintDescriptor.getAnnotation().annotationType().getName());
        entry.setValue(constraintDescriptor);

        result = metaDataTransformer.convertMetaData(entry);
        return result;
    }

    boolean hasBeanValidationConstraints(PropertyInformation propertyInformation)
    {
        PropertyDetails propertyDetails = ExtValUtils.getPropertyDetails(propertyInformation);

        return getDescriptorFor(propertyDetails.getBaseObject().getClass(), propertyDetails.getProperty()) != null;
    }

    @SuppressWarnings({"unchecked"})
    void validate(FacesContext facesContext,
                  UIComponent uiComponent,
                  Object convertedObject,
                  PropertyInformation propertyInformation)
    {
        Class baseBeanClass = getBaseClassType(propertyInformation);
        String propertyName = getPropertyToValidate(propertyInformation);

        Class[] groups = resolveGroups(facesContext, uiComponent);

        if (groups == null)
        {
            return;
        }

        Set<ConstraintViolation> violations = ExtValBeanValidationContext.getCurrentInstance().getValidatorFactory()
                .usingContext()
                .messageInterpolator(ExtValBeanValidationContext.getCurrentInstance().getMessageInterpolator())
                .getValidator()
                .validateValue(baseBeanClass, propertyName, convertedObject, groups);

        if(violations != null && !violations.isEmpty())
        {
            BeanValidationUtils
                    .processConstraintViolations(facesContext, uiComponent, convertedObject, violations, true);
        }
    }

    private Class getBaseClassType(PropertyInformation propertyInformation)
    {
        return ExtValUtils.getPropertyDetails(propertyInformation).getBaseObject().getClass();
    }

    private String getPropertyToValidate(PropertyInformation propertyInformation)
    {
        return ExtValUtils.getPropertyDetails(propertyInformation).getProperty();
    }

    private Class[] resolveGroups(FacesContext facesContext, UIComponent uiComponent)
    {
        return ExtValBeanValidationContext.getCurrentInstance().getGroups(
                facesContext.getViewRoot().getViewId(),
                uiComponent.getClientId(facesContext));
    }

    private ElementDescriptor getDescriptorFor(Class targetClass, String property)
    {
        BeanDescriptor beanDescriptor = ExtValBeanValidationContext.getCurrentInstance().getValidatorFactory()
                .getValidator().getConstraintsForClass(targetClass);

        return beanDescriptor.getConstraintsForProperty(property);
    }
}
