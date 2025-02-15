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

package ai.starwhale.mlops.datastore;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

public interface MemoryTable {

    TableSchema getSchema();

    void updateFromWal(Wal.WalEntry entry);

    void update(TableSchemaDesc schema, List<Map<String, Object>> records);

    @Data
    @AllArgsConstructor
    class RecordResult {

        Object key;
        Map<String, Object> values;
    }

    List<RecordResult> query(Map<String, String> columns,
            List<OrderByDesc> orderBy,
            TableQueryFilter filter,
            int start,
            int limit,
            boolean keepNone,
            boolean rawResult);

    List<RecordResult> scan(
            Map<String, String> columns,
            String start,
            boolean startInclusive,
            String end,
            boolean endInclusive,
            int limit,
            boolean keepNone);

    void lock();

    void unlock();
}
