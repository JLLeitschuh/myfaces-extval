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
package org.apache.myfaces.extensions.validator.core.validation.strategy;

import org.apache.myfaces.extensions.validator.core.mapper.ClassMappingFactory;
import org.apache.myfaces.extensions.validator.core.WebXmlParameter;
import org.apache.myfaces.extensions.validator.core.ExtValContext;
import org.apache.myfaces.extensions.validator.core.CustomInfo;
import org.apache.myfaces.extensions.validator.core.loader.StaticMappingConfigLoader;
import org.apache.myfaces.extensions.validator.core.mapper.NameMapper;
import org.apache.myfaces.extensions.validator.core.loader.StaticMappingConfigEntry;
import org.apache.myfaces.extensions.validator.core.loader.StaticResourceBundleLoader;
import org.apache.myfaces.extensions.validator.core.loader.StaticMappingConfigLoaderNames;
import org.apache.myfaces.extensions.validator.core.validation.strategy.mapper
    .AnnotationToValidationStrategyBeanNameMapper;
import org.apache.myfaces.extensions.validator.core.validation.strategy.mapper
    .CustomConfiguredAnnotationToValidationStrategyNameMapper;
import org.apache.myfaces.extensions.validator.core.validation.strategy.mapper
    .CustomConventionAnnotationToValidationStrategyNameMapper;
import org.apache.myfaces.extensions.validator.core.validation.strategy.mapper
    .DefaultAnnotationToValidationStrategyNameMapper;
import org.apache.myfaces.extensions.validator.core.validation.strategy.mapper
    .SimpleAnnotationToValidationStrategyNameMapper;
import org.apache.myfaces.extensions.validator.util.ClassUtils;
import org.apache.myfaces.extensions.validator.util.ExtValUtils;
import org.apache.myfaces.extensions.validator.internal.ToDo;
import org.apache.myfaces.extensions.validator.internal.Priority;
import org.apache.myfaces.extensions.validator.internal.UsageInformation;
import org.apache.myfaces.extensions.validator.internal.UsageCategory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;


/**
 * Factory which creates the ValidationStrategy for a given annotation
 *
 * @author Gerhard Petracek
 * @since 1.x.1
 */
@ToDo(value = Priority.MEDIUM, description = "add generic java api (de-/register mapping)")
@UsageInformation({UsageCategory.INTERNAL, UsageCategory.CUSTOMIZABLE})
public class DefaultValidationStrategyFactory implements ClassMappingFactory<Annotation, ValidationStrategy>
{
    protected final Log logger = LogFactory.getLog(getClass());

    private static Map<String, String> annotationStrategyMapping = null;
    private static List<NameMapper<Annotation>> nameMapperList = new ArrayList<NameMapper<Annotation>>();

    static
    {
        nameMapperList
            .add(new CustomConfiguredAnnotationToValidationStrategyNameMapper());
        nameMapperList
            .add(new CustomConventionAnnotationToValidationStrategyNameMapper());
        nameMapperList
            .add(new DefaultAnnotationToValidationStrategyNameMapper());
        nameMapperList
            .add(new SimpleAnnotationToValidationStrategyNameMapper());

        nameMapperList
            .add(new AnnotationToValidationStrategyBeanNameMapper(
                new CustomConfiguredAnnotationToValidationStrategyNameMapper()));
        nameMapperList
            .add(new AnnotationToValidationStrategyBeanNameMapper(
                new CustomConventionAnnotationToValidationStrategyNameMapper()));
        nameMapperList.add(new AnnotationToValidationStrategyBeanNameMapper(
            new DefaultAnnotationToValidationStrategyNameMapper()));
        nameMapperList.add(new AnnotationToValidationStrategyBeanNameMapper(
            new SimpleAnnotationToValidationStrategyNameMapper()));
    }

    public DefaultValidationStrategyFactory()
    {
        if(logger.isDebugEnabled())
        {
            logger.debug(getClass().getName() + " instantiated");
        }
    }

    public ValidationStrategy create(Annotation annotation)
    {
        if (annotationStrategyMapping == null)
        {
            initStaticMappings();
        }

        String annotationName = annotation.annotationType().getName();

        if (annotationStrategyMapping.containsKey(annotationName))
        {
            return getValidationStrategyInstance(annotationStrategyMapping.get(annotationName));
        }

        ValidationStrategy validationStrategy;
        String strategyName;
        //null -> use name mappers
        for (NameMapper<Annotation> nameMapper : nameMapperList)
        {
            strategyName = nameMapper.createName(annotation);

            if (strategyName == null)
            {
                continue;
            }

            validationStrategy = getValidationStrategyInstance(strategyName);

            if (validationStrategy != null)
            {
                addMapping(annotationName, strategyName);
                return validationStrategy;
            }
        }

        return null;
    }

    private ValidationStrategy getValidationStrategyInstance(
        String validationStrategyName)
    {
        if (validationStrategyName
            .startsWith(AnnotationToValidationStrategyBeanNameMapper.PREFIX_FOR_BEAN_MAPPING))
        {
            return (ValidationStrategy) ExtValUtils.getELHelper().getBean(validationStrategyName
                    .substring(AnnotationToValidationStrategyBeanNameMapper.PREFIX_FOR_BEAN_MAPPING.length()));
        }
        else
        {
            return (ValidationStrategy) ClassUtils
                .tryToInstantiateClassForName(validationStrategyName);
        }
    }

    @ToDo(value = Priority.MEDIUM, description = "logging")
    private void addMapping(String annotationName, String validationStrategyName)
    {
        if(logger.isTraceEnabled())
        {
            logger.trace("adding annotation to validation strategy mapping: "
                + annotationName + " -> " + validationStrategyName);
        }

        synchronized (DefaultValidationStrategyFactory.class)
        {
            annotationStrategyMapping.put(annotationName, validationStrategyName);
        }
    }

    @ToDo(value = Priority.MEDIUM, description = "logging")
    private void initStaticMappings()
    {
        synchronized (DefaultValidationStrategyFactory.class)
        {
            annotationStrategyMapping = new HashMap<String, String>();

            //setup internal static mappings
            for (StaticMappingConfigLoader<String, String> staticMappingConfigLoader :
                ExtValContext.getContext().getStaticMappingConfigLoaders(
                    StaticMappingConfigLoaderNames.ANNOTATION_TO_VALIDATION_STRATEGY_CONFIG_LOADER))
            {
                setupStrategyMappings(staticMappingConfigLoader.getMapping());
            }

            StaticMappingConfigLoader<String, String> staticMappingConfigLoader = new StaticResourceBundleLoader();
            //try to setup mapping with base name by convention - overrides default mapping
            try
            {
                //build convention (strategy mapping)
                staticMappingConfigLoader.setSourceOfMapping(ExtValContext.getContext().getInformationProviderBean()
                    .get(CustomInfo.STATIC_STRATEGY_MAPPING_SOURCE));

                setupStrategyMappings(staticMappingConfigLoader.getMapping());
            }
            catch (Throwable t)
            {
                //do nothing - it was just a try
            }

            //setup custom mapping - overrides all other mappings
            String customMappingBaseName = WebXmlParameter.CUSTOM_STATIC_VALIDATION_STRATEGY_MAPPING;
            if (customMappingBaseName != null)
            {
                try
                {
                    staticMappingConfigLoader = new StaticResourceBundleLoader();
                    staticMappingConfigLoader.setSourceOfMapping(customMappingBaseName);
                    setupStrategyMappings(staticMappingConfigLoader.getMapping());
                }
                catch (MissingResourceException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setupStrategyMappings(List<StaticMappingConfigEntry<String,String>> mappings)
    {
        for(StaticMappingConfigEntry<String, String> mapping : mappings)
        {
            addMapping(mapping.getSource(), mapping.getTarget());
        }
    }
}