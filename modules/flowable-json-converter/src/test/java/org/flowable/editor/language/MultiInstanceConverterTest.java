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
package org.flowable.editor.language;

import static org.assertj.core.api.Assertions.assertThat;

import org.flowable.bpmn.model.Activity;
import org.flowable.bpmn.model.BpmnModel;
import org.junit.jupiter.api.Test;

public class MultiInstanceConverterTest extends AbstractConverterTest {

    @Test
    public void convertJsonToModel() throws Exception {
        BpmnModel bpmnModel = readJsonFile();
        validateModel(bpmnModel);
    }

    @Test
    public void doubleConversionValidation() throws Exception {
        BpmnModel bpmnModel = readJsonFile();
        bpmnModel = convertToJsonAndBack(bpmnModel);
        validateModel(bpmnModel);
    }

    @Override
    protected String getResource() {
        return "test.multiinstance.json";
    }

    private void validateModel(BpmnModel model) {
        Activity activity = (Activity) model.getFlowElement("multi-instance");
        assertThat(activity.getLoopCharacteristics().isSequential()).isTrue();
        assertThat(activity.getLoopCharacteristics().getLoopCardinality()).isEqualTo("3");
        assertThat(activity.getLoopCharacteristics().getElementVariable()).isEqualTo("instanceVar");
        assertThat(activity.getLoopCharacteristics().getInputDataItem()).isEqualTo("collection");
        assertThat(activity.getLoopCharacteristics().getElementIndexVariable()).isEqualTo("index");
        assertThat(activity.getLoopCharacteristics().getCompletionCondition()).isEqualTo("completionCondition");
    }

}
