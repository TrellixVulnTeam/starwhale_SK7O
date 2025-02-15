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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.starwhale.mlops.datastore.ColumnTypeScalar;
import ai.starwhale.mlops.domain.swds.mapper.SwDatasetVersionMapper;
import ai.starwhale.mlops.domain.swds.po.SwDatasetVersionEntity;
import ai.starwhale.mlops.storage.LengthAbleInputStream;
import ai.starwhale.mlops.storage.StorageAccessService;
import ai.starwhale.mlops.storage.StorageObjectInfo;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DsFileGetterTest {

    @Test
    public void testFileGetter() throws IOException {
        StorageAccessParser storageAccessParser = mock(StorageAccessParser.class);
        StorageAccessService storageAccessService = mock(
                StorageAccessService.class);
        when(storageAccessService.get(eq("bdc/bdcsd"), anyLong(), anyLong())).thenReturn(
                new LengthAbleInputStream(new ByteArrayInputStream("abc".getBytes()), 3));
        when(storageAccessService.head("bdcsd")).thenReturn(new StorageObjectInfo(false, 1L, null));
        when(storageAccessService.head("bdc/bdcsd")).thenReturn(new StorageObjectInfo(true, 1L, null));
        when(storageAccessParser.getStorageAccessServiceFromAuth(anyLong(), anyString(), anyString())).thenReturn(
                storageAccessService);
        SwDatasetVersionMapper versionMapper = mock(SwDatasetVersionMapper.class);
        when(versionMapper.getVersionById(anyLong())).thenReturn(
                SwDatasetVersionEntity.builder().storagePath("bdc").build());
        DsFileGetter fileGetter = new DsFileGetter(storageAccessParser, versionMapper);
        byte[] bytes = fileGetter.dataOf(1L, "bdcsd", "", (String) ColumnTypeScalar.INT64.encode(1, false),
                (String) ColumnTypeScalar.INT64.encode(1, false));
        Assertions.assertEquals("abc", new String(bytes));

    }

}