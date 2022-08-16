// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.nereids.trees.plans.physical;

import org.apache.doris.nereids.memo.GroupExpression;
import org.apache.doris.nereids.properties.LogicalProperties;
import org.apache.doris.nereids.trees.expressions.Expression;
import org.apache.doris.nereids.trees.plans.JoinType;
import org.apache.doris.nereids.trees.plans.Plan;
import org.apache.doris.nereids.trees.plans.PlanType;
import org.apache.doris.nereids.trees.plans.visitor.PlanVisitor;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Optional;

/**
 * Physical hash join plan.
 */
public class PhysicalHashJoin<
        LEFT_CHILD_TYPE extends Plan,
        RIGHT_CHILD_TYPE extends Plan>
        extends AbstractPhysicalJoin<LEFT_CHILD_TYPE, RIGHT_CHILD_TYPE> {

    public PhysicalHashJoin(JoinType joinType, Optional<Expression> condition, LogicalProperties logicalProperties,
                            LEFT_CHILD_TYPE leftChild, RIGHT_CHILD_TYPE rightChild) {
        this(joinType, condition, Optional.empty(), logicalProperties, leftChild, rightChild);
    }

    /**
     * Constructor of PhysicalHashJoinNode.
     *
     * @param joinType Which join type, left semi join, inner join...
     * @param condition join condition.
     */
    public PhysicalHashJoin(JoinType joinType, Optional<Expression> condition,
                            Optional<GroupExpression> groupExpression, LogicalProperties logicalProperties,
                            LEFT_CHILD_TYPE leftChild, RIGHT_CHILD_TYPE rightChild) {
        super(PlanType.PHYSICAL_HASH_JOIN, joinType, condition,
                groupExpression, logicalProperties, leftChild, rightChild);
    }

    @Override
    public <R, C> R accept(PlanVisitor<R, C> visitor, C context) {
        return visitor.visitPhysicalHashJoin((PhysicalHashJoin<Plan, Plan>) this, context);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PhysicalHashJoin ([").append(joinType).append("]");
        condition.ifPresent(
                expression -> sb.append(", [").append(expression).append("]")
        );
        sb.append(")");
        return sb.toString();
    }

    @Override
    public PhysicalBinary<Plan, Plan> withChildren(List<Plan> children) {
        Preconditions.checkArgument(children.size() == 2);
        return new PhysicalHashJoin<>(joinType, condition, logicalProperties, children.get(0), children.get(1));
    }

    @Override
    public Plan withGroupExpression(Optional<GroupExpression> groupExpression) {
        return new PhysicalHashJoin<>(joinType, condition, groupExpression, logicalProperties, left(), right());
    }

    @Override
    public Plan withLogicalProperties(Optional<LogicalProperties> logicalProperties) {
        return new PhysicalHashJoin<>(joinType, condition, Optional.empty(),
            logicalProperties.get(), left(), right());
    }
}