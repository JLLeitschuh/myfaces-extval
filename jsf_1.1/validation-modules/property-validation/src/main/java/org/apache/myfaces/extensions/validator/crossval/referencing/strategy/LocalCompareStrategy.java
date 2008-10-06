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
package org.apache.myfaces.extensions.validator.crossval.referencing.strategy;

import org.apache.myfaces.extensions.validator.crossval.ProcessedInformationEntry;
import org.apache.myfaces.extensions.validator.crossval.CrossValidationStorage;
import org.apache.myfaces.extensions.validator.crossval.CrossValidationStorageEntry;
import org.apache.myfaces.extensions.validator.crossval.strategy.AbstractCompareStrategy;
import org.apache.myfaces.extensions.validator.util.CrossValidationUtils;
import org.apache.myfaces.extensions.validator.util.ExtValUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.util.Map;

/**
 * "[property_name]" ... local validation -> cross-component, but no cross-entity validation
 *
 * @author Gerhard Petracek
 */
public class LocalCompareStrategy implements ReferencingStrategy
{
    protected final Log logger = LogFactory.getLog(getClass());

    public boolean evalReferenceAndValidate(
            CrossValidationStorageEntry crossValidationStorageEntry,
            CrossValidationStorage crossValidationStorage,
            String validationTarget, AbstractCompareStrategy compareStrategy)
    {
        return tryToValidateLocally(
            crossValidationStorageEntry,
            crossValidationStorage,
            validationTarget,
            compareStrategy);
    }

    protected boolean tryToValidateLocally(
            CrossValidationStorageEntry crossValidationStorageEntry,
            CrossValidationStorage crossValidationStorage,
            String validationTarget,
            AbstractCompareStrategy compareStrategy)
    {
        String targetValueBindingExpression;

        targetValueBindingExpression
            = createTargetValueBindingExpression(crossValidationStorageEntry, validationTarget);

        return targetValueBindingExpression != null &&
            validateELExpression(crossValidationStorageEntry,
                                 crossValidationStorage,
                                 targetValueBindingExpression,
                                 compareStrategy);

    }

    protected String createTargetValueBindingExpression(CrossValidationStorageEntry crossValidationStorageEntry,
                                                        String validationTarget)
    {
        String baseValueBindingExpression =
            crossValidationStorageEntry.getAnnotationEntry().getValueBindingExpression();

        baseValueBindingExpression =
            baseValueBindingExpression.substring(0, baseValueBindingExpression.lastIndexOf("."));

        String targetValueBindingExpression;

        FacesContext facesContext = FacesContext.getCurrentInstance();

        targetValueBindingExpression = baseValueBindingExpression + "."
                + validationTarget + "}";

        if (!ExtValUtils.getELHelper().isExpressionValid(facesContext, targetValueBindingExpression))
        {
            throw new IllegalStateException(
                "invalid reference used: "
                + validationTarget
                + " please check your extval annotations");
        }

        return targetValueBindingExpression;
    }

    protected boolean validateELExpression(CrossValidationStorageEntry crossValidationStorageEntry,
                                           CrossValidationStorage crossValidationStorage,
                                           String validationTarget,
                                           AbstractCompareStrategy compareStrategy)
    {
        boolean violationFound = false;
        Map<String, ProcessedInformationEntry> valueBindingConvertedValueMapping = CrossValidationUtils
                .getOrInitValueBindingConvertedValueMapping();
        ProcessedInformationEntry validationTargetEntry;

        if (!valueBindingConvertedValueMapping.containsKey(validationTarget))
        {
            return false;
        }
        validationTargetEntry = compareStrategy.resolveValidationTargetEntry(
                valueBindingConvertedValueMapping,
                validationTarget, crossValidationStorageEntry.getBean());

        if (validationTargetEntry == null)
        {
            if(logger.isWarnEnabled())
            {
                logger.warn("couldn't find converted object for " + validationTarget);
            }

            return false;
        }

        if (compareStrategy.isViolation(crossValidationStorageEntry
                .getConvertedObject(), validationTargetEntry
                .getConvertedValue(), crossValidationStorageEntry
                .getAnnotationEntry().getAnnotation()))
        {

            CrossValidationStorageEntry tmpCrossValidationStorageEntry = new CrossValidationStorageEntry();
            if (compareStrategy.useTargetComponentToDisplayErrorMsg(crossValidationStorageEntry))
            {
                tmpCrossValidationStorageEntry.setComponent(validationTargetEntry.getComponent());
            }
            else
            {
                tmpCrossValidationStorageEntry.setComponent(crossValidationStorageEntry.getComponent());
            }
            tmpCrossValidationStorageEntry.setConvertedObject(validationTargetEntry.getConvertedValue());
            tmpCrossValidationStorageEntry.setValidationStrategy(compareStrategy);

            compareStrategy
                    .processTargetComponentAfterViolation(crossValidationStorageEntry, tmpCrossValidationStorageEntry);

            violationFound = true;
        }

        if (violationFound)
        {
            compareStrategy.processSourceComponentAfterViolation(crossValidationStorageEntry);
        }

        return true;
    }
}
