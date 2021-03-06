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
package org.apache.myfaces.extensions.validator.test.propval.crossval;

import org.apache.myfaces.extensions.validator.crossval.annotation.Equals;

import java.util.Date;

/**
 */
public class PropertyChainCrossValTestDateBean
{
    private static final Date DEFAULT_DATE = new Date();

    @Equals("subBean.date2")
    private Date date1 = DEFAULT_DATE;

    private PropertyChainCrossValTestDateSubBean subBean = new PropertyChainCrossValTestDateSubBean();

    public Date getDate1()
    {
        return date1;
    }

    public void setDate1(Date date1)
    {
        this.date1 = date1;
    }

    public PropertyChainCrossValTestDateSubBean getSubBean()
    {
        return subBean;
    }

    public void setSubBean(PropertyChainCrossValTestDateSubBean subBean)
    {
        this.subBean = subBean;
    }
}