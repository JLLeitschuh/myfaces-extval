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
package org.apache.myfaces.extensions.validator.beanval.startup;

import org.apache.myfaces.extensions.validator.beanval.BeanValidationModuleValidationInterceptor;
import org.apache.myfaces.extensions.validator.beanval.HtmlCoreComponentsComponentInitializer;
import org.apache.myfaces.extensions.validator.beanval.MappedConstraintSourceBeanValidationModuleValidationInterceptor;
import org.apache.myfaces.extensions.validator.beanval.ValidatorFactoryProxy;
import org.apache.myfaces.extensions.validator.beanval.payload.ViolationSeverity;
import org.apache.myfaces.extensions.validator.beanval.payload.DisableClientSideValidation;
import org.apache.myfaces.extensions.validator.beanval.interceptor.ExtValBeanValidationMetaDataExtractionInterceptor;
import org.apache.myfaces.extensions.validator.beanval.validation.ModelValidationPhaseListener;
import org.apache.myfaces.extensions.validator.beanval.metadata.transformer.mapper.SizeNameMapper;
import org.apache.myfaces.extensions.validator.beanval.metadata.transformer.mapper.NotNullNameMapper;
import org.apache.myfaces.extensions.validator.beanval.storage.DefaultModelValidationStorageManager;
import org.apache.myfaces.extensions.validator.beanval.storage.ModelValidationStorage;
import org.apache.myfaces.extensions.validator.beanval.storage.mapper.BeanValidationGroupStorageNameMapper;
import org.apache.myfaces.extensions.validator.beanval.storage.mapper.ModelValidationStorageNameMapper;
import org.apache.myfaces.extensions.validator.core.ExtValContext;
import org.apache.myfaces.extensions.validator.core.factory.AbstractNameMapperAwareFactory;
import org.apache.myfaces.extensions.validator.core.factory.FactoryNames;
import org.apache.myfaces.extensions.validator.core.startup.AbstractStartupListener;
import org.apache.myfaces.extensions.validator.core.storage.GroupStorage;
import org.apache.myfaces.extensions.validator.core.storage.StorageManager;
import org.apache.myfaces.extensions.validator.core.storage.StorageManagerHolder;
import org.apache.myfaces.extensions.validator.internal.UsageCategory;
import org.apache.myfaces.extensions.validator.internal.UsageInformation;
import org.apache.myfaces.extensions.validator.util.ExtValUtils;
import org.apache.myfaces.extensions.validator.util.JsfUtils;

import javax.validation.ValidatorFactory;

/**
 * @since x.x.3
 */
@UsageInformation(UsageCategory.INTERNAL)
public class BeanValidationStartupListener extends AbstractStartupListener
{
    private static final long serialVersionUID = -5025748399876833394L;

    protected void init()
    {
        registerValidatorFactory();
        registerBeanValidationInterceptors();
        registerMetaDataTransformerNameMapper();
        registerGroupStorageNameMapper();
        registerModelValidationStorageNameMapper();
        registerComponentInitializers();
        registerMetaDataExtractionInterceptors();
        registerPhaseListeners();
        registerViolationSeverityPayload();
        registerDisableClientSideValidationPayload();
    }

    protected void registerValidatorFactory()
    {
        ExtValContext.getContext()
                .addGlobalProperty(ValidatorFactory.class.getName(), new ValidatorFactoryProxy(), false);
    }

    protected void registerBeanValidationInterceptors()
    {
        ExtValContext.getContext().registerRendererInterceptor(
                new BeanValidationModuleValidationInterceptor());
        ExtValContext.getContext().registerRendererInterceptor(
                new MappedConstraintSourceBeanValidationModuleValidationInterceptor());
    }

    protected void registerMetaDataTransformerNameMapper()
    {
        ExtValUtils.registerValidationStrategyToMetaDataTransformerNameMapper(new SizeNameMapper());
        ExtValUtils.registerValidationStrategyToMetaDataTransformerNameMapper(new NotNullNameMapper());
    }

    @SuppressWarnings({"unchecked"})
    protected void registerGroupStorageNameMapper()
    {
        StorageManager storageManager = getStorageManagerHolder().getStorageManager(GroupStorage.class);

        if (storageManager instanceof AbstractNameMapperAwareFactory)
        {
            ((AbstractNameMapperAwareFactory<String>) storageManager)
                    .register(new BeanValidationGroupStorageNameMapper());
        }
        else
        {
            this.logger.warning(storageManager.getClass().getName() +
                    " has to implement AbstractNameMapperAwareFactory " + getClass().getName() +
                    " couldn't register " + BeanValidationGroupStorageNameMapper.class.getName());
        }
    }

    protected void registerModelValidationStorageNameMapper()
    {
        DefaultModelValidationStorageManager modelValidationStorageManager = new DefaultModelValidationStorageManager();
        modelValidationStorageManager.register(new ModelValidationStorageNameMapper());
        getStorageManagerHolder().setStorageManager(ModelValidationStorage.class, modelValidationStorageManager, false);
    }

    protected void registerComponentInitializers()
    {
        ExtValContext.getContext().addComponentInitializer(new HtmlCoreComponentsComponentInitializer());
    }

    protected StorageManagerHolder getStorageManagerHolder()
    {
        return (ExtValContext.getContext()
                .getFactoryFinder()
                .getFactory(FactoryNames.STORAGE_MANAGER_FACTORY, StorageManagerHolder.class));
    }

    protected void registerMetaDataExtractionInterceptors()
    {
        ExtValContext.getContext()
                .addMetaDataExtractionInterceptor(new ExtValBeanValidationMetaDataExtractionInterceptor());
    }

    protected void registerPhaseListeners()
    {
        JsfUtils.registerPhaseListener(new ModelValidationPhaseListener());
    }

    protected void registerViolationSeverityPayload()
    {
        ExtValContext extValContext = ExtValContext.getContext();

        extValContext.addGlobalProperty(ViolationSeverity.Info.class.getName(), ViolationSeverity.Info.class, false);
        extValContext.addGlobalProperty(ViolationSeverity.Warn.class.getName(), ViolationSeverity.Warn.class, false);
        extValContext.addGlobalProperty(ViolationSeverity.Fatal.class.getName(), ViolationSeverity.Fatal.class, false);

        //no need to register "error" it's the default
    }

    private void registerDisableClientSideValidationPayload()
    {
        ExtValContext.getContext().addGlobalProperty(
                DisableClientSideValidation.class.getName(), DisableClientSideValidation.class, false);
    }
}
