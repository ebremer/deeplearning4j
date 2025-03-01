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

package org.nd4j.linalg.api.ops.impl.shape;

import lombok.val;
import org.nd4j.autodiff.samediff.SDVariable;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.common.base.Preconditions;
import org.nd4j.common.util.ArrayUtil;
import org.nd4j.imports.descriptors.properties.PropertyMapping;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.DynamicCustomOp;
import org.nd4j.linalg.api.ops.impl.shape.bp.TileBp;
import org.nd4j.shade.guava.primitives.Ints;
import org.nd4j.shade.guava.primitives.Longs;
import org.tensorflow.framework.AttrValue;
import org.tensorflow.framework.GraphDef;
import org.tensorflow.framework.NodeDef;

import java.util.*;
import java.util.stream.Collectors;

public class Tile extends DynamicCustomOp {

    private long[] jaxis;
    private boolean is_static_reps = false;

    public Tile(SameDiff sameDiff, SDVariable i_v, long[] axis) {
        super(null,sameDiff, new SDVariable[]{i_v}, false);
        this.jaxis = axis;
        addArguments();
    }

    public Tile(SameDiff sameDiff, SDVariable i_v, SDVariable axis) {
        super(null,sameDiff, new SDVariable[]{i_v, axis}, false);
        this.jaxis = null;
    }

    public Tile(INDArray[] inputs, INDArray[] outputs, long[] axis, boolean is_static_reps) {
        super(null, inputs, outputs);
        this.jaxis = axis;
        this.is_static_reps = is_static_reps;
        addArguments();
    }


    public Tile(INDArray[] inputs, INDArray[] outputs, long[] axis) {
        this(inputs,outputs,axis,false);
    }

    public Tile(INDArray x, INDArray repeat){
        super(null, new INDArray[] {x, repeat}, null);
        this.jaxis = null;
    }

    public Tile(INDArray inputs, long... axis) {
        super(null, new INDArray[] {inputs}, null);
        this.jaxis = axis;
        this.is_static_reps = true;
        addArguments();
    }

    public Tile() {}

    public Tile(INDArray x, int[] repeat) {
        this(x, ArrayUtil.toLongArray(repeat));
    }

    public Tile(SameDiff sd, SDVariable x, int[] repeat) {
        this(sd, x, ArrayUtil.toLongArray(repeat));
    }

    public Tile(INDArray[] indArrays, INDArray[] indArrays1, int[] repeat) {
        this(indArrays, indArrays1, ArrayUtil.toLongArray(repeat));
    }

    public Tile(INDArray input, INDArray result, int...axes) {
        this(input, result, ArrayUtil.toLongArray(axes));
    }

    public Tile(INDArray input, INDArray result, long[] longArray) {
        super(null, new INDArray[]{input}, new INDArray[]{result});
        this.jaxis = longArray;
        addArguments();
    }

    private void addArguments() {
        this.is_static_reps = true;
        addIArgument(jaxis);
    }

    @Override
    public void initFromTensorFlow(NodeDef nodeDef, SameDiff initWith, Map<String, AttrValue> attributesForNode, GraphDef graph) {

    }

    @Override
    public void configureFromArguments() {
        if(!iArguments.isEmpty()) {
            this.jaxis = Longs.toArray(iArguments);
        }
    }

    @Override
    public void setPropertiesForFunction(Map<String, Object> properties) {
        if(properties.containsKey("dimensions")) {
            Long dimension = (Long) properties.get("dimensions");
            this.jaxis = Longs.toArray(Arrays.asList(dimension.intValue()));
        }
    }

    @Override
    public Map<String, Map<String, PropertyMapping>> mappingsForFunction() {
        Map<String,Map<String,PropertyMapping>> ret = new HashMap<>();
        Map<String,PropertyMapping> map = new HashMap<>();

        val axisMapping = PropertyMapping.builder()
                .onnxAttrName("axis")
                .tfInputPosition(-1)
                .propertyNames(new String[]{"axis"})
                .build();

        map.put("axis",axisMapping);

        ret.put(tensorflowName(),map);
        ret.put(onnxName(),map);

        return ret;
    }

    @Override
    public String opName() {
        return "tile";
    }

    @Override
    public String onnxName() {
        return "Tile";
    }

    @Override
    public String tensorflowName() {
        return "Tile";
    }


    @Override
    public List<SDVariable> doDiff(List<SDVariable> i_v) {
        if(jaxis != null){
            return new TileBp(sameDiff, arg(), i_v.get(0), jaxis).outputs();
        }else{
            return new TileBp(sameDiff, arg(0), arg(1), i_v.get(0)).outputs();
        }
    }

    @Override
    public List<DataType> calculateOutputDataTypes(List<DataType> dataTypes){
        //Output type is same as input type
        return Collections.singletonList(dataTypes.get(0));
    }
}
