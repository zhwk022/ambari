/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.server.orm.dao;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.apache.ambari.server.orm.entities.ActionStatusEntity;
import org.apache.ambari.server.orm.entities.ActionStatusEntityPK;

import javax.persistence.EntityManager;

public class ActionStatusDAO {
  @Inject
  EntityManager entityManager;

  public ActionStatusEntity findByPK(ActionStatusEntityPK actionStatusEntityPK) {
    return entityManager.find(ActionStatusEntity.class, actionStatusEntityPK);
  }

  @Transactional
  public void create(ActionStatusEntity actionStatusEntity) {
    entityManager.persist(actionStatusEntity);
  }

  @Transactional
  public ActionStatusEntity merge(ActionStatusEntity actionStatusEntity) {
    return entityManager.merge(actionStatusEntity);
  }

  @Transactional
  public void remove(ActionStatusEntity actionStatusEntity) {
    entityManager.remove(actionStatusEntity);
  }

  @Transactional
  public void removeByPK(ActionStatusEntityPK actionStatusEntityPK) {
    remove(findByPK(actionStatusEntityPK));
  }

}
