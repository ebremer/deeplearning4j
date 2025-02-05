/*
 *  ******************************************************************************
 *  *
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Apache License, Version 2.0 which is available at
 *  * https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  *  See the NOTICE file distributed with this work for additional
 *  *  information regarding copyright ownership.
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  * License for the specific language governing permissions and limitations
 *  * under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  *****************************************************************************
 */

package org.nd4j.linalg.api.ops.impl.transforms;

import org.nd4j.autodiff.samediff.SDVariable;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.common.base.Preconditions;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ops.DynamicCustomOp;
import org.tensorflow.framework.AttrValue;
import org.tensorflow.framework.GraphDef;
import org.tensorflow.framework.NodeDef;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NthElement extends DynamicCustomOp {
    protected boolean reverse = false;

    @Override
    public String opName() {
        return "nth_element";
    }

    @Override
    public String tensorflowName() {
        return "NthElement";
    }

    @Override
    public void initFromTensorFlow(NodeDef nodeDef, SameDiff initWith, Map<String, AttrValue> attributesForNode, GraphDef graph) {
        throw new UnsupportedOperationException("Use the new Tensorflow Importer instead. This method is now removed.");

    }

    protected void addArgs() {
        // to be implemented
        iArguments.clear();
        iArguments.add(reverse ? 1L : 0L);
    }

    @Override
    public Map<String, Object> propertiesForFunction() {
        Map<String,Object> ret = new LinkedHashMap<>();
        ret.put("alignCorners", reverse);
        return ret;
    }

    @Override
    public List<SDVariable> doDiff(List<SDVariable> f1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DataType> calculateOutputDataTypes(List<DataType> inputDataTypes){  //Input and number
        Preconditions.checkState(inputDataTypes != null && inputDataTypes.size() == 2, "Expected exactly 2 input datatypes for %s, got %s", getClass(), inputDataTypes);
        return Collections.singletonList(inputDataTypes.get(0));
    }
}
