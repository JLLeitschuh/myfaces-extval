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
package org.apache.myfaces.extensions.validator.core.storage;

import org.apache.myfaces.extensions.validator.core.renderkit.ExtValRendererProxy;
import org.apache.myfaces.extensions.validator.core.mapper.NameMapper;

/**
 * default storage-manager for renderer proxy entries
 *
 * @author Gerhard Petracek
 * @since x.x.3
 */
class DefaultRendererProxyStorageManager
    extends AbstractRequestScopeAwareStorageManager<RendererProxyStorage>
{
    DefaultRendererProxyStorageManager()
    {
        register(new NameMapper<String>(){

            public String createName(String source)
            {
                return (RendererProxyStorage.class.getName().equals(source)) ?
                        DefaultRendererProxyStorage.class.getName() : null;
            }
        });
    }

    public String getStorageManagerKey()
    {
        //for better backward compatibility
        return ExtValRendererProxy.class.getName() + ":STORAGE";
    }
}
