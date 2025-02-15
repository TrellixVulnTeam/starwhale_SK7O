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

package ai.starwhale.mlops.domain.swds.objectstore;

import ai.starwhale.mlops.domain.swds.mapper.SwDatasetVersionMapper;
import ai.starwhale.mlops.domain.swds.po.SwDatasetVersionEntity;
import ai.starwhale.mlops.exception.SwValidationException;
import ai.starwhale.mlops.exception.SwValidationException.ValidSubject;
import ai.starwhale.mlops.storage.StorageAccessService;
import ai.starwhale.mlops.storage.StorageUri;
import ai.starwhale.mlops.storage.env.StorageEnv;
import ai.starwhale.mlops.storage.env.UserStorageAccessServiceBuilder;
import ai.starwhale.mlops.storage.env.UserStorageAuthEnv;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class StorageAccessParser {

    final StorageAccessService defaultStorageAccessService;

    final SwDatasetVersionMapper swDatasetVersionMapper;

    final UserStorageAccessServiceBuilder userStorageAccessServiceBuilder;

    ConcurrentHashMap<String, StorageAccessService> storageAccessServicePool = new ConcurrentHashMap<>();

    public StorageAccessParser(StorageAccessService defaultStorageAccessService,
            SwDatasetVersionMapper swDatasetVersionMapper,
            UserStorageAccessServiceBuilder userStorageAccessServiceBuilder) {
        this.defaultStorageAccessService = defaultStorageAccessService;
        this.swDatasetVersionMapper = swDatasetVersionMapper;
        this.userStorageAccessServiceBuilder = userStorageAccessServiceBuilder;
    }

    public StorageAccessService getStorageAccessServiceFromAuth(Long datasetId, String uri,
            String authName) {
        if (StringUtils.hasText(authName)) {
            authName = authName.toUpperCase(); // env vars are uppercase always
        }
        StorageAccessService cachedStorageAccessService = storageAccessServicePool.get(
                formatKey(datasetId, authName));
        if (null != cachedStorageAccessService) {
            return cachedStorageAccessService;
        }
        SwDatasetVersionEntity swDatasetVersionEntity = swDatasetVersionMapper.getVersionById(
                datasetId);
        String storageAuthsText = swDatasetVersionEntity.getStorageAuths();
        if (!StringUtils.hasText(storageAuthsText)) {
            return defaultStorageAccessService;
        }

        UserStorageAuthEnv storageAuths = new UserStorageAuthEnv(storageAuthsText);
        StorageEnv env = storageAuths.getEnv(authName);
        if (null == env) {
            return defaultStorageAccessService;
        }

        StorageAccessService storageAccessService = userStorageAccessServiceBuilder.build(env, new StorageUri(uri),
                authName);
        if (null == storageAccessService) {
            throw new SwValidationException(ValidSubject.SWDS).tip(
                    "file system not supported yet: " + env.getEnvType());
        }
        storageAccessServicePool.putIfAbsent(formatKey(datasetId, authName), storageAccessService);
        return storageAccessService;
    }

    String formatKey(Long datasetId, String authName) {
        return datasetId.toString() + authName;
    }

}
