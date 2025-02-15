/*
 * Copyright 2022 Starwhale, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.starwhale.mlops.domain.system.resourcepool;

import ai.starwhale.mlops.api.protocol.system.ResourcePoolVo;
import ai.starwhale.mlops.common.IdConvertor;
import ai.starwhale.mlops.domain.system.po.ResourcePoolEntity;
import ai.starwhale.mlops.domain.system.resourcepool.bo.ResourcePool;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * convert between ResourcePool ResourcePoolVo and ResourcePoolEntity
 */
@Slf4j
@Component
public class ResourcePoolConverter {

    @Resource
    private IdConvertor idConvertor;

    public ResourcePoolEntity toEntity(ResourcePool pool) {
        return ResourcePoolEntity.builder().label(pool.getLabel()).build();
    }

    public ResourcePool toResourcePool(ResourcePoolEntity entity) {
        if (entity == null) {
            return ResourcePool.empty();
        }

        return ResourcePool.builder().label(entity.getLabel()).build();
    }

    public ResourcePoolVo toResourcePoolVo(ResourcePoolEntity entity) {
        if (entity == null) {
            return ResourcePoolVo.empty();
        }

        return ResourcePoolVo.builder()
                .id(idConvertor.convert(entity.getId()))
                .label(entity.getLabel())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }
}
