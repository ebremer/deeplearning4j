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

//
//  @author Yurii Shyrma, created on 05.12.2017
//

#include <system/op_boilerplate.h>
#if NOT_EXCLUDED(OP_sruCell)

#include <ops/declarable/CustomOperations.h>
#include <ops/declarable/helpers/sru.h>

namespace sd {
namespace ops {

//////////////////////////////////////////////////////////////////////////
CUSTOM_OP_IMPL(sruCell, 4, 2, false, 0, 0) {
  auto xt = INPUT_VARIABLE(0);    // input [bS x inSize], bS - batch size, inSize - number of features
  auto ct_1 = INPUT_VARIABLE(1);  // previous cell state ct  [bS x inSize], that is at previous time step t-1
  auto w = INPUT_VARIABLE(2);     // weights [inSize x 3*inSize]
  auto b = INPUT_VARIABLE(3);     // biases [2*inSize]

  auto ht = OUTPUT_VARIABLE(0);  // current cell output [bS x inSize], that is at current time step t
  auto ct = OUTPUT_VARIABLE(1);  // current cell state  [bS x inSize], that is at current time step t

  const int rank = xt->rankOf();
  const int bS = xt->sizeAt(0);
  const int inSize = xt->sizeAt(1);  // inSize - number of features

  // input shapes validation
  const std::vector<LongType> correctCt_1Shape = {bS, inSize};
  const std::vector<LongType> correctWShape = {inSize, 3 * inSize};
  const std::vector<LongType> correctBShape = {2 * inSize};

  REQUIRE_TRUE(ct_1->isSameShape(correctCt_1Shape), 0,
               "SRUCELL operation: wrong shape of previous cell state, expected is %s, but got %s instead !",
               ShapeUtils::shapeAsString(correctCt_1Shape).c_str(), ShapeUtils::shapeAsString(ct_1).c_str());
  REQUIRE_TRUE(w->isSameShape(correctWShape), 0,
               "SRUCELL operation: wrong shape of weights, expected is %s, but got %s instead !",
               ShapeUtils::shapeAsString(correctWShape).c_str(), ShapeUtils::shapeAsString(w).c_str());
  REQUIRE_TRUE(b->isSameShape(correctBShape), 0,
               "SRUCELL operation: wrong shape of biases, expected is %s, but got %s instead !",
               ShapeUtils::shapeAsString(correctBShape).c_str(), ShapeUtils::shapeAsString(b).c_str());

  // fixme: shitty initializer lists
  helpers::sruCell(block.launchContext(), xt, ct_1, w, b, ht, ct);

  return Status::OK;
}

DECLARE_TYPES(sruCell) {
  getOpDescriptor()->setAllowedInputTypes(ANY)->setAllowedOutputTypes({ALL_FLOATS});
}

DECLARE_SHAPE_FN(sruCell) {
  auto xtShapeInfo = inputShape->at(0);    // input [bS x inSize], bS - batch size, inSize - number of features
  auto ct_1ShapeInfo = inputShape->at(1);  // previous cell state ct  [bS x inSize], that is at previous time step t-1
  auto wShapeInfo = inputShape->at(2);     // weights [inSize x 3*inSize]
  auto bShapeInfo = inputShape->at(3);     // biases [2*inSize]

  const int rank = xtShapeInfo[0];
  const int bS = xtShapeInfo[1];
  const int inSize = xtShapeInfo[2];  // inSize - number of features

  // input shapes validation
  const std::vector<LongType> correctCt_1Shape = {bS, inSize};
  const std::vector<LongType> correctWShape = {inSize, 3 * inSize};
  const std::vector<LongType> correctBShape = {2 * inSize};

  REQUIRE_TRUE(ShapeUtils::areShapesEqual(ct_1ShapeInfo, correctCt_1Shape), 0,
               "SRUCELL operation: wrong shape of previous cell state, expected is %s, but got %s instead !",
               ShapeUtils::shapeAsString(correctCt_1Shape).c_str(), ShapeUtils::shapeAsString(ct_1ShapeInfo).c_str());
  REQUIRE_TRUE(ShapeUtils::areShapesEqual(wShapeInfo, correctWShape), 0,
               "SRUCELL operation: wrong shape of weights, expected is %s, but got %s instead !",
               ShapeUtils::shapeAsString(correctWShape).c_str(), ShapeUtils::shapeAsString(wShapeInfo).c_str());
  REQUIRE_TRUE(ShapeUtils::areShapesEqual(bShapeInfo, correctBShape), 0,
               "SRUCELL operation: wrong shape of biases, expected is %s, but got %s instead !",
               ShapeUtils::shapeAsString(correctBShape).c_str(), ShapeUtils::shapeAsString(bShapeInfo).c_str());

  // evaluate output shapeInfos
  LongType *hShapeInfo(nullptr), *cShapeInfo(nullptr);
  ALLOCATE(hShapeInfo, block.getWorkspace(), shape::shapeInfoLength(rank), sd::LongType);  // [bS x numProj]
  ALLOCATE(cShapeInfo, block.getWorkspace(), shape::shapeInfoLength(rank), sd::LongType);  // [bS x numUnits]

  hShapeInfo[0] = cShapeInfo[0] = rank;
  hShapeInfo[1] = cShapeInfo[1] = bS;
  hShapeInfo[2] = cShapeInfo[2] = inSize;

  ShapeUtils::updateStridesAndType(hShapeInfo, ct_1ShapeInfo, shape::order(ct_1ShapeInfo));
  ShapeUtils::updateStridesAndType(cShapeInfo, ct_1ShapeInfo, shape::order(ct_1ShapeInfo));

  return SHAPELIST(ConstantShapeHelper::getInstance().createFromExisting(hShapeInfo),
                   ConstantShapeHelper::getInstance().createFromExisting(cShapeInfo));
}

}  // namespace ops
}  // namespace sd

#endif
