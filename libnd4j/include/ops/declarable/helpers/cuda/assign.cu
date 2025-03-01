/* ******************************************************************************
*
*
* This program and the accompanying materials are made available under the
* terms of the Apache License, Version 2.0 which is available at
* https://www.apache.org/licenses/LICENSE-2.0.
*
*  See the NOTICE file distributed with this work for additional
*  information regarding copyright ownership.
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations
* under the License.
*
* SPDX-License-Identifier: Apache-2.0
******************************************************************************/

#include <exceptions/cuda_exception.h>
#include <execution/cuda/LaunchDims.h>
#include <helpers/PointersManager.h>
#include <ops/declarable/helpers/assign.h>

#include "helpers/DebugHelper.h"
#include "helpers/ShapeUtils.h"

namespace sd {
namespace ops {
namespace helpers {

template <typename X, typename Z>
SD_KERNEL static void assignKernel(const void* vx, const LongType* xShapeInfo, void* vz, const LongType* zShapeInfo,
                                   const LongType xOffset, const LongType zOffset) {
  const auto x = reinterpret_cast<const X*>(vx);
  auto z = reinterpret_cast<Z*>(vz);

  __shared__ LongType len, totalThreads;
  __shared__ int rank;
  __shared__ const LongType *xShape;
  __shared__ const LongType *zShape;
  __shared__ const LongType *xStride;
  __shared__ const LongType *zStride;

  if (threadIdx.x == 0) {
    len = shape::length(zShapeInfo);
    totalThreads = gridDim.x * blockDim.x;
    rank = shape::rank(zShapeInfo);

    // Cache shapes and strides
    xShape = shape::shapeOf(xShapeInfo);
    zShape = shape::shapeOf(zShapeInfo);
    xStride = shape::stride(xShapeInfo);
    zStride = shape::stride(zShapeInfo);
  }
  __syncthreads();

  const auto tid = blockIdx.x * blockDim.x + threadIdx.x;

  LongType xCoords[SD_MAX_RANK], zCoords[SD_MAX_RANK];

  for (LongType i = tid; i < len; i += totalThreads) {
    INDEX2COORDS(i, rank, zShape, zCoords);
    INDEX2COORDS(i, rank, xShape, xCoords);

    LongType xIndex, zIndex;
    COORDS2INDEX(rank, xStride, xCoords, xIndex);
    COORDS2INDEX(rank, zStride, zCoords, zIndex);

    z[zIndex] = static_cast<Z>(x[xIndex]);
  }
}
template <typename X, typename Z>
SD_HOST static void assignCudaLauncher(const int blocksPerGrid, const int threadsPerBlock, const int sharedMem,
                                       const cudaStream_t* stream, const void* vx, const LongType* xShapeInfo,
                                       void* vz, const LongType* zShapeInfo, const LongType xOffset, const LongType zOffset) {
  assignKernel<X, Z><<<blocksPerGrid, threadsPerBlock, sharedMem, *stream>>>(vx, xShapeInfo, vz, zShapeInfo, xOffset, zOffset);
  DebugHelper::checkGlobalErrorCode("assignKernel(...) failed");
}

void assign(sd::LaunchContext* context, sd::NDArray* target, sd::NDArray* source) {
  if (target->lengthOf() != source->lengthOf()) {
    std::string errorMsg = "assign helper: Source and target arrays must have the same length. ";
    errorMsg += "Source shape: " + ShapeUtils::shapeAsString(source) + ", ";
    errorMsg += "Target shape: " + ShapeUtils::shapeAsString(target) + ", ";
    errorMsg += "Source datatype: " + DataTypeUtils::asString(source->dataType()) + ", ";
    errorMsg += "Target datatype: " + DataTypeUtils::asString(target->dataType());
    THROW_EXCEPTION(errorMsg.c_str());
  }

  NDArray::prepareSpecialUse({target}, {source});

  auto xType = source->dataType();
  auto zType = target->dataType();

  dim3 launchDims = traceDims(target->lengthOf());

  PointersManager manager(context, "helpers::assign");

  BUILD_DOUBLE_SELECTOR(xType, zType, assignCudaLauncher,
                        (launchDims.x, launchDims.y, launchDims.z, context->getCudaStream(),
                         source->specialBuffer(), source->specialShapeInfo(),
                         target->specialBuffer(), target->specialShapeInfo(),
                         source->offset(), target->offset()),
                        SD_COMMON_TYPES, SD_COMMON_TYPES);

  manager.synchronize();
  NDArray::registerSpecialUse({target}, {source});
}

}  // namespace helpers
}  // namespace ops
}  // namespace sd