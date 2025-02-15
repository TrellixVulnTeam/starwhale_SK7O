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

package ai.starwhale.mlops.domain.swmp;

import ai.starwhale.mlops.api.protocol.swmp.SwModelPackageVo;
import ai.starwhale.mlops.common.Convertor;
import ai.starwhale.mlops.common.IdConvertor;
import ai.starwhale.mlops.domain.swmp.po.SwModelPackageEntity;
import ai.starwhale.mlops.domain.user.UserConvertor;
import ai.starwhale.mlops.exception.ConvertException;
import org.springframework.stereotype.Component;

@Component
public class SwmpConvertor implements Convertor<SwModelPackageEntity, SwModelPackageVo> {

    private final IdConvertor idConvertor;
    private final UserConvertor userConvertor;

    public SwmpConvertor(IdConvertor idConvertor, UserConvertor userConvertor) {
        this.idConvertor = idConvertor;
        this.userConvertor = userConvertor;
    }

    @Override
    public SwModelPackageVo convert(SwModelPackageEntity entity)
            throws ConvertException {
        if (entity == null) {
            return SwModelPackageVo.empty();
        }
        return SwModelPackageVo.builder()
                .id(idConvertor.convert(entity.getId()))
                .name(entity.getSwmpName())
                .owner(userConvertor.convert(entity.getOwner()))
                .createdTime(entity.getCreatedTime().getTime())
                .build();
    }

    @Override
    public SwModelPackageEntity revert(SwModelPackageVo vo) throws ConvertException {
        throw new UnsupportedOperationException();
    }
}
