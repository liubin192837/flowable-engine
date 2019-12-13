/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.eventregistry.impl.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.FlowableIllegalArgumentException;
import org.flowable.eventregistry.api.EventDeployment;
import org.flowable.eventregistry.api.model.EventModelBuilder;
import org.flowable.eventregistry.converter.EventJsonConverter;
import org.flowable.eventregistry.impl.EventRepositoryServiceImpl;
import org.flowable.eventregistry.model.EventCorrelationParameterDefinition;
import org.flowable.eventregistry.model.EventModel;
import org.flowable.eventregistry.model.EventPayloadDefinition;

/**
 * @author Joram Barrez
 */
public class EventModelBuilderImpl implements EventModelBuilder {

    protected EventRepositoryServiceImpl eventRepository;
    
    protected String deploymentName;
    protected String resourceName;
    protected String category;
    protected String parentDeploymentId;
    protected String tenantId;

    protected String key;
    protected Collection<String> inboundChannelKeys;
    protected Collection<String> outboundChannelKeys;
    protected Map<String, EventCorrelationParameterDefinition> correlationParameterDefinitions = new LinkedHashMap<>();
    protected Map<String, EventPayloadDefinition> eventPayloadDefinitions = new LinkedHashMap<>();

    public EventModelBuilderImpl(EventRepositoryServiceImpl eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public EventModelBuilder key(String key) {
        this.key = key;
        return this;
    }
    
    @Override
    public EventModelBuilder deploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
        return this;
    }
    
    @Override
    public EventModelBuilder resourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
    }
    
    @Override
    public EventModelBuilder category(String category) {
        this.category = category;
        return this;
    }
    
    @Override
    public EventModelBuilder parentDeploymentId(String parentDeploymentId) {
        this.parentDeploymentId = parentDeploymentId;
        return this;
    }
    
    @Override
    public EventModelBuilder tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @Override
    public EventModelBuilder inboundChannelKey(String channelKey) {
        if (inboundChannelKeys == null) {
            inboundChannelKeys = new HashSet<>();
        }
        inboundChannelKeys.add(channelKey);
        return this;
    }

    @Override
    public EventModelBuilder inboundChannelKeys(Collection<String> channelKeys) {
        channelKeys.forEach(this::inboundChannelKey);
        return this;
    }

    @Override
    public EventModelBuilder outboundChannelKey(String channelKey) {
        if (outboundChannelKeys == null) {
            outboundChannelKeys = new HashSet<>();
        }
        outboundChannelKeys.add(channelKey);
        return this;
    }

    @Override
    public EventModelBuilder outboundChannelKeys(Collection<String> channelKeys) {
        channelKeys.forEach(this::outboundChannelKey);
        return this;
    }

    @Override
    public EventModelBuilder correlationParameter(String name, String type) {
        correlationParameterDefinitions.put(name, new EventCorrelationParameterDefinition(name, type));
        payload(name, type);
        return this;
    }

    @Override
    public EventModelBuilder payload(String name, String type) {
        eventPayloadDefinitions.put(name, new EventPayloadDefinition(name, type));
        return this;
    }
    @Override
    public EventModel createEventModel() {
        return buildEventModel();
    }

    @Override
    public EventDeployment deploy() {
        if (resourceName == null) {
            throw new FlowableIllegalArgumentException("A resource name is mandatory");
        }
        
        EventModel eventModel = buildEventModel();

        EventDeployment eventDeployment = eventRepository.createDeployment()
            .name(deploymentName)
            .addEventDefinition(resourceName, new EventJsonConverter().convertToJson(eventModel))
            .category(category)
            .parentDeploymentId(parentDeploymentId)
            .tenantId(tenantId)
            .deploy();

        return eventDeployment;
    }

    protected EventModel buildEventModel() {
        EventModel eventModel = new EventModel();

        if (StringUtils.isNotEmpty(key)) {
            eventModel.setKey(key);
        } else {
            throw new FlowableIllegalArgumentException("An event definition key is mandatory");
        }

        if (inboundChannelKeys != null) {
            eventModel.setInboundChannelKeys(inboundChannelKeys);
        }

        if (outboundChannelKeys != null) {
            eventModel.setOutboundChannelKeys(outboundChannelKeys);
        }

        eventModel.getCorrelationParameters().addAll(correlationParameterDefinitions.values());
        eventModel.getPayload().addAll(eventPayloadDefinitions.values());
        
        return eventModel;
    }
}
