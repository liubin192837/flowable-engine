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
package org.flowable.eventregistry.impl.deployer;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.flowable.eventregistry.impl.parser.EventDefinitionParse;
import org.flowable.eventregistry.impl.parser.EventDefinitionParseFactory;
import org.flowable.eventregistry.impl.persistence.entity.EventDefinitionEntity;
import org.flowable.eventregistry.impl.persistence.entity.EventDeploymentEntity;
import org.flowable.eventregistry.impl.persistence.entity.EventResourceEntity;
import org.flowable.eventregistry.impl.util.CommandContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParsedDeploymentBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParsedDeploymentBuilder.class);

    public static final String[] EVENT_RESOURCE_SUFFIXES = new String[] { "event" };

    protected EventDeploymentEntity deployment;
    protected EventDefinitionParseFactory eventDefinitionParseFactory;

    public ParsedDeploymentBuilder(EventDeploymentEntity deployment, EventDefinitionParseFactory eventDefinitionParseFactory) {
        this.deployment = deployment;
        this.eventDefinitionParseFactory = eventDefinitionParseFactory;
    }

    public ParsedDeployment build() {
        List<EventDefinitionEntity> eventDefinitions = new ArrayList<>();
        Map<EventDefinitionEntity, EventDefinitionParse> eventDefinitionToParseMap = new LinkedHashMap<>();
        Map<EventDefinitionEntity, EventResourceEntity> eventDefinitionToResourceMap = new LinkedHashMap<>();

        for (EventResourceEntity resource : deployment.getResources().values()) {
            if (isEventResource(resource.getName())) {
                LOGGER.debug("Processing Event definition resource {}", resource.getName());
                EventDefinitionParse parse = createEventParseFromResource(resource);
                for (EventDefinitionEntity eventDefinition : parse.getEventDefinitions()) {
                    eventDefinitions.add(eventDefinition);
                    eventDefinitionToParseMap.put(eventDefinition, parse);
                    eventDefinitionToResourceMap.put(eventDefinition, resource);
                }
            }
        }

        return new ParsedDeployment(deployment, eventDefinitions, eventDefinitionToParseMap, eventDefinitionToResourceMap);
    }

    protected EventDefinitionParse createEventParseFromResource(EventResourceEntity resource) {
        String resourceName = resource.getName();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(resource.getBytes());

        EventDefinitionParse eventParse = eventDefinitionParseFactory.createParse()
                .sourceInputStream(inputStream)
                .setSourceSystemId(resourceName)
                .deployment(deployment)
                .name(resourceName);

        eventParse.execute(CommandContextUtil.getEventRegistryConfiguration());
        return eventParse;
    }

    protected boolean isEventResource(String resourceName) {
        for (String suffix : EVENT_RESOURCE_SUFFIXES) {
            if (resourceName.endsWith(suffix)) {
                return true;
            }
        }

        return false;
    }

}
