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
// Created by raver119 on 20.10.2017.
//
#include <graph/GraphExecutioner.h>
#include <graph/execution/LogicExecutor.h>
#include <graph/execution/LogicReturn.h>
#include <graph/execution/LogicWhile.h>

namespace sd {
namespace graph {
Status LogicWhile::processNode(Graph* graph, Node* node) {
  auto __variableSpace = graph->getVariableSpace();

  sd_debug("Starting on WHILE loop: [%i]\n", node->id());

  // total number of inputs. 2 last inputs are scopes
  int inputs = node->input()->size();

  if (inputs < 3) {
    sd_printf("While [%i]: loop should have at least 1 external variable announced\n", node->id());
    return Status::BAD_INPUT;
  }

  for (int e = 0; e < inputs - 2; e++) {
    std::pair<int, int> pair(node->id(), e);
    if (!__variableSpace->hasVariable(pair)) {
      __variableSpace->putVariable(pair, new Variable(nullptr, nullptr, node->id(), e));
    }

    auto va = node->input()->at(e);

    auto inputVar = __variableSpace->getVariable(va);

    auto innerVar = __variableSpace->getVariable(pair);
    if (innerVar->hasNDArray()) {
      // TODO: ???
    } else {
      // FIXME: in some cases it's possible to have no NDArray
      if (inputVar->hasNDArray()) innerVar->setNDArray(new NDArray(inputVar->getNDArray()->dup(inputVar->getNDArray()->ordering())));
    }
  }

  int scopeConditionIndex = node->input()->at(inputs - 2).first;
  int scopeBodyIndex = node->input()->at(inputs - 1).first;

  sd_debug("While [%i]: got [%i] inputs\n", node->id(), node->input()->size());

  // we're running condition nodes now
  auto scope = graph->scopeById(scopeConditionIndex);
  int breaker = 0;
  while (true && breaker < 10000000) {
    int lastNode = 0;
    // we're running condition scope first
    sd_debug("While [%i]: got [%i] ops in condition scope [%i]\n", node->id(), scope->nodes()->size(),
             scopeConditionIndex);

    for (Node* v : *scope->nodes()) {
      // v->getBlock()->updateVariables();
      if (v->opType() == ::graph::OpType_LOGIC) {
        sd_debug("Falling back to logic\n", "");
        LogicExecutor::processNode(graph, v);
      } else {
        sd_debug("Op [<%s>]\n", v->getName()->c_str());
        Status status = GraphExecutioner::executeFlatNode(graph, v, __variableSpace);
        if (status != Status::OK) return status;
      }

      lastNode = v->id();
    }

    if (!__variableSpace->hasVariable(lastNode)) {
      sd_printf("While [%i]: got no results out of conditional loop\n", node->id());
      return Status::KERNEL_FAILURE;
    }

    // now we should take result of the OpScope run, and evaluate it
    auto result = __variableSpace->getVariable(lastNode)->getNDArray();


    // if result evaluates to 0.0 - condition returned FALSE
    if (result->e<int>(0) == 0)
      break;
    else {
      auto scopeBody = graph->scopeById(scopeBodyIndex);
      size_t e = 0;
      sd_debug("While [%i] got [%i] ops in body scope [%i]\n", node->id(), scopeBody->nodes()->size(), scopeBodyIndex);
      for (; e < scopeBody->nodes()->size() - 1; e++) {
        Node* v = scopeBody->nodes()->at(e);

        if (v->opType() == ::graph::OpType_LOGIC) {
          sd_debug("Falling back to logic\n", "");
          LogicExecutor::processNode(graph, v);
        } else {
          sd_debug("Op [<%s>]\n", v->getName()->c_str());
          // v->getBlock()->updateVariables();
          Status status = GraphExecutioner::executeFlatNode(graph, v, __variableSpace);
          if (status != Status::OK) return status;
        }

      }

      // now execute return statement
      Node* ret = scopeBody->nodes()->at(e);
      LogicReturn::processNode(graph, ret);
    }

    breaker++;
  }

  // if we've hit breaker limit - we should notify about that
  if (breaker >= 10000000) {
    sd_printf("While condition seems to be never ending, aborting...\n", breaker);
    return Status::KERNEL_FAILURE;
  }

  return Status::OK;
}
}  // namespace graph
}  // namespace sd
