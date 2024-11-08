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

package org.nd4j.linalg.jcublas.buffer;

import lombok.NonNull;
import lombok.val;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.ShortPointer;
import org.bytedeco.javacpp.indexer.Indexer;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.common.util.ArrayUtil;

import java.nio.ByteBuffer;

/**
 * Cuda BFloat16 buffer implementation
 *
 * @author raver119@gmail.com
 */
public class CudaBfloat16DataBuffer extends BaseCudaDataBuffer {

    /**
     * Meant for creating another view of a buffer
     *
     * @param pointer the underlying buffer to create a view from
     * @param indexer the indexer for the pointer
     * @param length  the length of the view
     */
    public CudaBfloat16DataBuffer(Pointer pointer, Indexer indexer, long length) {
        super(pointer, indexer, length);
    }

    public CudaBfloat16DataBuffer(Pointer pointer, Pointer specialPointer, Indexer indexer, long length){
        super(pointer, specialPointer, indexer, length);
    }

    public CudaBfloat16DataBuffer(ByteBuffer buffer, DataType dataType, long length, long offset) {
        super(buffer, dataType, length, offset);
    }

    public CudaBfloat16DataBuffer(long length, int elementSize, boolean initialize, @NonNull MemoryWorkspace workspace) {
        super(length, elementSize, initialize, workspace);
    }

    /**
     * Base constructor
     *
     * @param length the length of the buffer
     */
    public CudaBfloat16DataBuffer(long length) {
        super(length, 2, true); // elementSize for BFLOAT16 is 2 bytes
    }

    public CudaBfloat16DataBuffer(long length, boolean initialize) {
        super(length, 2, initialize);
    }

    public CudaBfloat16DataBuffer(long length, int elementSize) {
        super(length, elementSize);
    }

    public CudaBfloat16DataBuffer(ByteBuffer underlyingBuffer, DataType dataType, long length) {
        super(underlyingBuffer, dataType, length);
    }

    public CudaBfloat16DataBuffer(long length, boolean initialize, MemoryWorkspace workspace) {
        super(length, 2, initialize, workspace);
    }

    /**
     * Initialize the data type and element size for BFLOAT16
     */
    @Override
    protected void initTypeAndSize() {
        elementSize = 2;
        type = DataType.BFLOAT16;
    }

    /**
     * Constructor for float[] data
     *
     * @param buffer the float array to initialize the buffer with
     */
    public CudaBfloat16DataBuffer(float[] buffer) {
        super(buffer.length, 2, true); // Initialize with length, element size 2, and initialize flag
        setData(buffer); // Set the data using the overridden setData(float[]) method
    }

    /**
     * Constructor for double[] data
     *
     * @param data the double array to initialize the buffer with
     */
    public CudaBfloat16DataBuffer(double[] data) {
        super(data.length, 2, true); // Initialize with length, element size 2, and initialize flag
        setData(data); // Set the data using the overridden setData(double[]) method
    }

    /**
     * Constructor for int[] data
     *
     * @param data the int array to initialize the buffer with
     */
    public CudaBfloat16DataBuffer(int[] data) {
        super(data.length, 2, true); // Initialize with length, element size 2, and initialize flag
        setData(data); // Set the data using the overridden setData(int[]) method
    }

    @Override
    protected DataBuffer create(long length) {
        return new CudaBfloat16DataBuffer(length);
    }

    @Override
    public void setData(float[] data) {
        val pointer = new ShortPointer(ArrayUtil.toBfloats(data));
        copyDataFromSrc(pointer, data.length, 0, 0);
    }

    @Override
    public void setData(int[] data) {
        setData(ArrayUtil.toShorts(data));
    }

    @Override
    public void setData(long[] data) {
        setData(ArrayUtil.toShorts(data));
    }

    @Override
    public void setData(byte[] data) {
        float[] floats = new float[data.length];
        for(int i = 0; i < data.length; i++) {
            floats[i] = data[i];
        }
        setData(floats);
    }

    @Override
    public void setData(boolean[] data) {
        setData(ArrayUtil.toFloatArray(data));
    }

    @Override
    public void setData(short[] data) {
        val pointer = new ShortPointer(data);
        copyDataFromSrc(pointer, data.length, 0, 0);
    }

    @Override
    public void setData(double[] data) {
        val pointer = new ShortPointer(ArrayUtil.toBfloats(data));
        copyDataFromSrc(pointer, data.length, 0, 0);
    }

    @Override
    public DataType dataType() {
        return DataType.BFLOAT16;
    }

    @Override
    public float[] asFloat() {
        // Override to provide float representation from BFLOAT16
        return super.asFloat();
    }

    @Override
    public double[] asDouble() {
        return ArrayUtil.toDoubles(asFloat());
    }

    @Override
    public int[] asInt() {
        return ArrayUtil.toInts(asFloat());
    }

    @Override
    public DataBuffer create(double[] data) {
        return new CudaBfloat16DataBuffer(data);
    }

    @Override
    public DataBuffer create(float[] data) {
        return new CudaBfloat16DataBuffer(data);
    }

    @Override
    public DataBuffer create(int[] data) {
        return new CudaBfloat16DataBuffer(data);
    }

    @Override
    public void flush() {
        // Implement flush if necessary
    }
}
