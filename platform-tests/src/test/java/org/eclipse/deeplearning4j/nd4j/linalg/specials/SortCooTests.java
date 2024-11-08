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

package org.eclipse.deeplearning4j.nd4j.linalg.specials;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bytedeco.javacpp.LongPointer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.nd4j.linalg.BaseNd4jTestWithBackends;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.rng.Random;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.factory.Nd4jBackend;
import org.nd4j.nativeblas.NativeOpsHolder;

import java.util.Arrays;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@Slf4j

public class SortCooTests extends BaseNd4jTestWithBackends {

    DataType initialType = Nd4j.dataType();
    DataType initialDefaultType = Nd4j.defaultFloatingPointType();



    @BeforeEach
    public void setUp() {
        Nd4j.setDefaultDataTypes(DataType.FLOAT, DataType.FLOAT);
    }

    @AfterEach
    public void setDown() {
        Nd4j.setDefaultDataTypes(initialType, Nd4j.defaultFloatingPointType());
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void sortSparseCooIndicesSort1(Nd4jBackend backend) {
        // FIXME: we don't want this test running on cuda for now
        if (Nd4j.getExecutioner().getClass().getCanonicalName().toLowerCase().contains("cuda"))
            return;

        val indices = new long[] {
                1, 0, 0,
                0, 1, 1,
                0, 1, 0,
                1, 1, 1};

        // we don't care about
        double values[] = new double[] {2, 1, 0, 3};
        val expIndices = new long[] {
                0, 1, 0,
                0, 1, 1,
                1, 0, 0,
                1, 1, 1};
        double expValues[] = new double[] {0, 1, 2, 3};

        for (DataType dataType : new DataType[]{DataType.FLOAT, DataType.DOUBLE, DataType.FLOAT16, DataType.INT64, DataType.INT32, DataType.INT16, DataType.INT8}) {
            DataBuffer idx = Nd4j.getDataBufferFactory().createLong(indices);
            DataBuffer val = Nd4j.createTypedBuffer(values, dataType);
            DataBuffer shapeInfo = Nd4j.getShapeInfoProvider().createShapeInformation(new long[]{2, 2, 2}, val.dataType()).getFirst();
           Nd4j.getNativeOps().sortCooIndices(null, (LongPointer) idx.addressPointer(),
                    val.addressPointer(), 4, (LongPointer) shapeInfo.addressPointer());

            assertArrayEquals(expIndices, idx.asLong());
            assertArrayEquals(expValues, val.asDouble(), 1e-5);

        }
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void sortSparseCooIndicesSort2(Nd4jBackend backend) {
        // FIXME: we don't want this test running on cuda for now
        if (Nd4j.getExecutioner().getClass().getCanonicalName().toLowerCase().contains("cuda"))
            return;

        val indices = new long[] {
                0, 0, 0,
                2, 2, 2,
                1, 1, 1};

        // we don't care about
        double values[] = new double[] {2, 1, 3};
        val expIndices = new long[] {
                0, 0, 0,
                1, 1, 1,
                2, 2, 2};
        double expValues[] = new double[] {2, 3, 1};


        for (DataType dataType : new DataType[]{DataType.FLOAT, DataType.DOUBLE, DataType.FLOAT16, DataType.INT64, DataType.INT32, DataType.INT16, DataType.INT8}) {
            DataBuffer idx = Nd4j.getDataBufferFactory().createLong(indices);
            DataBuffer val = Nd4j.createTypedBuffer(values, dataType);
            DataBuffer shapeInfo = Nd4j.getShapeInfoProvider().createShapeInformation(new long[]{2, 2, 2}, val.dataType()).getFirst();
           Nd4j.getNativeOps().sortCooIndices(null, (LongPointer) idx.addressPointer(),
                    val.addressPointer(), 3, (LongPointer) shapeInfo.addressPointer());

            assertArrayEquals(expIndices, idx.asLong());
            assertArrayEquals(expValues, val.asDouble(), 1e-5);

        }


    }

    /**
     * Workaround for missing method in DataBuffer interface:
     *      long[] DataBuffer.GetLongsAt(long i, long length)
     *
     *
     * When method is added to interface, this workaround should be deleted!
     * @return
     */
    static long[] getLongsAt(DataBuffer buffer, long i, long length){
        return LongStream.range(i, i + length).map(buffer::getLong).toArray();
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void sortSparseCooIndicesSort3(Nd4jBackend backend) {
        // FIXME: we don't want this test running on cuda for now
        if (Nd4j.getExecutioner().getClass().getCanonicalName().toLowerCase().contains("cuda"))
            return;

        Random rng = Nd4j.getRandom();
        rng.setSeed(12040483421383L);
        long shape[] = {50,50,50};
        int nnz = 100;
        val indices = Nd4j.rand(new int[]{nnz, shape.length}, rng).muli(50).ravel().toLongVector();
        val values = Nd4j.rand(new long[]{nnz}).ravel().toDoubleVector();


        DataBuffer indiceBuffer = Nd4j.getDataBufferFactory().createLong(indices);
        DataBuffer valueBuffer = Nd4j.createBuffer(values);
        DataBuffer shapeInfo = Nd4j.getShapeInfoProvider().createShapeInformation(new long[]{3,3,3}, valueBuffer.dataType()).getFirst();
        INDArray indMatrix = Nd4j.create(indiceBuffer).reshape(new long[]{nnz, shape.length});

       Nd4j.getNativeOps().sortCooIndices(null, (LongPointer) indiceBuffer.addressPointer(),
                valueBuffer.addressPointer(), nnz, (LongPointer) shapeInfo.addressPointer());

        for (long i = 1; i < nnz; ++i){
            for(long j = 0; j < shape.length; ++j){
                long prev = indiceBuffer.getLong(((i - 1) * shape.length + j));
                long current = indiceBuffer.getLong((i * shape.length + j));
                if (prev < current){
                    break;
                } else if(prev > current){
                    long[] prevRow = getLongsAt(indiceBuffer, (i - 1) * shape.length, shape.length);
                    long[] currentRow = getLongsAt(indiceBuffer, i * shape.length, shape.length);
                    throw new AssertionError(String.format("indices are not correctly sorted between element %d and %d. %s > %s",
                            i - 1, i, Arrays.toString(prevRow), Arrays.toString(currentRow)));
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void sortSparseCooIndicesSort4(Nd4jBackend backend) {
        // FIXME: we don't want this test running on cuda for now
        if (Nd4j.getExecutioner().getClass().getCanonicalName().toLowerCase().contains("cuda"))
            return;

        val indices = new long[] {
                0,2,7,
                2,36,35,
                3,30,17,
                5,12,22,
                5,43,45,
                6,32,11,
                8,8,32,
                9,29,11,
                5,11,22,
                15,26,16,
                17,48,49,
                24,28,31,
                26,6,23,
                31,21,31,
                35,46,45,
                37,13,14,
                6,38,18,
                7,28,20,
                8,29,39,
                8,32,30,
                9,42,43,
                11,15,18,
                13,18,45,
                29,26,39,
                30,8,25,
                42,31,24,
                28,33,5,
                31,27,1,
                35,43,26,
                36,8,37,
                39,22,14,
                39,24,42,
                42,48,2,
                43,26,48,
                44,23,49,
                45,18,34,
                46,28,5,
                46,32,17,
                48,34,44,
                49,38,39,
        };

        val expIndices = new long[] {
                0, 2, 7,
                2, 36, 35,
                3, 30, 17,
                5, 11, 22,
                5, 12, 22,
                5, 43, 45,
                6, 32, 11,
                6, 38, 18,
                7, 28, 20,
                8, 8, 32,
                8, 29, 39,
                8, 32, 30,
                9, 29, 11,
                9, 42, 43,
                11, 15, 18,
                13, 18, 45,
                15, 26, 16,
                17, 48, 49,
                24, 28, 31,
                26, 6, 23,
                28, 33, 5,
                29, 26, 39,
                30, 8, 25,
                31, 21, 31,
                31, 27, 1,
                35, 43, 26,
                35, 46, 45,
                36, 8, 37,
                37, 13, 14,
                39, 22, 14,
                39, 24, 42,
                42, 31, 24,
                42, 48, 2,
                43, 26, 48,
                44, 23, 49,
                45, 18, 34,
                46, 28, 5,
                46, 32, 17,
                48, 34, 44,
                49, 38, 39,
        };

        double values[] = new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
                22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39};

        DataBuffer idx = Nd4j.getDataBufferFactory().createLong(indices);
        DataBuffer val = Nd4j.createBuffer(values);
        DataBuffer shapeInfo = Nd4j.getShapeInfoProvider().createShapeInformation(new long[]{3,3,3}, val.dataType()).getFirst();

       Nd4j.getNativeOps().sortCooIndices(null, (LongPointer) idx.addressPointer(),
                val.addressPointer(), 40, (LongPointer) shapeInfo.addressPointer());

        // just check the indices. sortSparseCooIndicesSort1 and sortSparseCooIndicesSort2 checks that
        // indices and values are both swapped. This test just makes sure index sort works for larger arrays.
        assertArrayEquals(expIndices, idx.asLong());
    }
    @Override
    public char ordering() {
        return 'c';
    }
}
