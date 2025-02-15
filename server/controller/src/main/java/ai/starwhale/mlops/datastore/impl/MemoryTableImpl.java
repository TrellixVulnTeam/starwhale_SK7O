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

package ai.starwhale.mlops.datastore.impl;

import ai.starwhale.mlops.datastore.ColumnSchema;
import ai.starwhale.mlops.datastore.ColumnSchemaDesc;
import ai.starwhale.mlops.datastore.ColumnType;
import ai.starwhale.mlops.datastore.ColumnTypeList;
import ai.starwhale.mlops.datastore.ColumnTypeObject;
import ai.starwhale.mlops.datastore.ColumnTypeScalar;
import ai.starwhale.mlops.datastore.MemoryTable;
import ai.starwhale.mlops.datastore.OrderByDesc;
import ai.starwhale.mlops.datastore.TableQueryFilter;
import ai.starwhale.mlops.datastore.TableSchema;
import ai.starwhale.mlops.datastore.TableSchemaDesc;
import ai.starwhale.mlops.datastore.Wal;
import ai.starwhale.mlops.datastore.WalManager;
import ai.starwhale.mlops.exception.SwProcessException;
import ai.starwhale.mlops.exception.SwProcessException.ErrorType;
import ai.starwhale.mlops.exception.SwValidationException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import lombok.NonNull;

public class MemoryTableImpl implements MemoryTable {

    private final String tableName;

    private final WalManager walManager;

    private TableSchema schema = null;

    private final TreeMap<Object, Map<String, Object>> recordMap = new TreeMap<>();

    // used only for initialization from WAL
    private final Map<Integer, ColumnSchema> indexMap = new HashMap<>();

    private final Lock lock = new ReentrantLock();

    public MemoryTableImpl(String tableName, WalManager walManager) {
        this.tableName = tableName;
        this.walManager = walManager;
    }

    public void lock() {
        this.lock.lock();
    }

    public void unlock() {
        this.lock.unlock();
    }

    public TableSchema getSchema() {
        return this.schema == null ? null : new TableSchema(this.schema);
    }

    @Override
    public void updateFromWal(Wal.WalEntry entry) {
        if (entry.hasTableSchema()) {
            var schemaDesc = this.parseTableSchema(entry.getTableSchema());
            if (this.schema == null) {
                this.schema = new TableSchema(schemaDesc);
            } else {
                this.schema.merge(schemaDesc);
            }
        }
        var recordList = entry.getRecordsList();
        if (!recordList.isEmpty()) {
            this.insertRecords(recordList.stream().map(this::parseRecord).collect(Collectors.toList()));
        }
    }

    private TableSchemaDesc parseTableSchema(Wal.TableSchema tableSchema) {
        var ret = new TableSchemaDesc();
        var keyColumn = tableSchema.getKeyColumn();
        if (!keyColumn.isEmpty()) {
            ret.setKeyColumn(keyColumn);
        }
        var columnList = new ArrayList<>(tableSchema.getColumnsList());
        columnList.sort(Comparator.comparingInt(Wal.ColumnSchema::getColumnIndex));
        var columnSchemaList = new ArrayList<ColumnSchemaDesc>();
        for (var col : columnList) {
            var colDesc = this.parseColumnSchema(col);
            columnSchemaList.add(colDesc);
            this.indexMap.put(col.getColumnIndex(), new ColumnSchema(colDesc, col.getColumnIndex()));
        }
        ret.setColumnSchemaList(columnSchemaList);
        return ret;
    }

    private ColumnSchemaDesc parseColumnSchema(Wal.ColumnSchema columnSchema) {
        var ret = ColumnSchemaDesc.builder()
                .name(columnSchema.getColumnName())
                .type(columnSchema.getColumnType());
        if (!columnSchema.getPythonType().isEmpty()) {
            ret.pythonType(columnSchema.getPythonType());
        }
        if (columnSchema.hasElementType()) {
            ret.elementType(this.parseColumnSchema(columnSchema.getElementType()));
        }
        if (columnSchema.getAttributesCount() > 0) {
            ret.attributes(columnSchema.getAttributesList().stream()
                    .map(this::parseColumnSchema)
                    .collect(Collectors.toList()));
        }
        return ret.build();
    }

    private Map<String, Object> parseRecord(Wal.Record record) {
        Map<String, Object> ret = new HashMap<>();
        for (var col : record.getColumnsList()) {
            if (col.getIndex() == -1) {
                ret.put("-", true);
            } else {
                var colSchema = this.indexMap.get(col.getIndex());
                ret.put(colSchema.getName(), colSchema.getType().fromWal(col));
            }
        }
        return ret;
    }

    @Override
    public void update(TableSchemaDesc schema, List<Map<String, Object>> records) {
        var logEntryBuilder = Wal.WalEntry.newBuilder()
                .setEntryType(Wal.WalEntry.Type.UPDATE)
                .setTableName(this.tableName);
        TableSchema newSchema = this.schema;
        if (schema == null) {
            if (this.schema == null) {
                throw new SwValidationException(SwValidationException.ValidSubject.DATASTORE,
                        "schema should not be null for the first update");
            }
        } else {
            var logSchemaBuilder = Wal.TableSchema.newBuilder();
            List<ColumnSchema> diff;
            if (this.schema == null) {
                newSchema = new TableSchema(schema);
                diff = newSchema.getColumnSchemas();
                logSchemaBuilder.setKeyColumn(newSchema.getKeyColumn());
            } else {
                newSchema = new TableSchema(this.schema);
                diff = newSchema.merge(schema);
            }
            for (var col : diff) {
                logSchemaBuilder.addColumns(
                        MemoryTableImpl.writeColumnSchema(col.getIndex(), col.getName(), col.getType()));
            }
            logEntryBuilder.setTableSchema(logSchemaBuilder);
        }
        List<Map<String, Object>> decodedRecords = null;
        if (records != null) {
            decodedRecords = new ArrayList<>();
            for (var record : records) {
                var key = record.get(newSchema.getKeyColumn());
                if (key == null) {
                    throw new SwValidationException(SwValidationException.ValidSubject.DATASTORE,
                            MessageFormat.format("key column {0} is null", newSchema.getKeyColumn()));
                }
                if (record.get("-") != null) {
                    decodedRecords.add(Map.of(newSchema.getKeyColumn(),
                            newSchema.getKeyColumnType().decode(key),
                            "-",
                            true));
                } else {
                    decodedRecords.add(MemoryTableImpl.decodeRecord(newSchema, record));
                }
            }
            for (var record : decodedRecords) {
                logEntryBuilder.addRecords(MemoryTableImpl.writeRecord(newSchema, record));
            }
        }
        this.walManager.append(logEntryBuilder.build());
        this.schema = newSchema;
        if (decodedRecords != null) {
            this.insertRecords(decodedRecords);
        }
    }

    private void insertRecords(List<Map<String, Object>> records) {
        for (var record : records) {
            var key = record.get(this.schema.getKeyColumn());
            if (record.get("-") != null) {
                this.recordMap.remove(key);
            } else {
                var old = this.recordMap.putIfAbsent(key, record);
                if (old != null) {
                    old.putAll(record);
                }
            }
        }
    }

    private static Wal.ColumnSchema.Builder writeColumnSchema(
            int columnIndex, String columnName, ColumnType columnType) {
        var ret = Wal.ColumnSchema.newBuilder()
                .setColumnIndex(columnIndex)
                .setColumnName(columnName)
                .setColumnType(columnType.getTypeName());
        if (columnType instanceof ColumnTypeList) {
            ret.setElementType(
                    MemoryTableImpl.writeColumnSchema(0, "", ((ColumnTypeList) columnType).getElementType()));
        } else if (columnType instanceof ColumnTypeObject) {
            ret.setPythonType(((ColumnTypeObject) columnType).getPythonType());
            ret.addAllAttributes(((ColumnTypeObject) columnType).getAttributes().entrySet().stream()
                    .map(entry -> MemoryTableImpl.writeColumnSchema(0, entry.getKey(), entry.getValue()).build())
                    .collect(Collectors.toList()));
        }
        return ret;
    }


    private static Wal.Record.Builder writeRecord(TableSchema schema, Map<String, Object> record) {
        var ret = Wal.Record.newBuilder();
        for (var entry : record.entrySet()) {
            if (Objects.equals(entry.getKey(), "-")) {
                ret.addColumns(ColumnTypeScalar.BOOL.toWal(-1, true));
            } else {
                var columnSchema = schema.getColumnSchemaByName(entry.getKey());
                if (columnSchema == null) {
                    throw new IllegalArgumentException("invalid column " + entry.getKey());
                }
                ret.addColumns(columnSchema.getType().toWal(columnSchema.getIndex(), entry.getValue()));
            }
        }
        return ret;
    }

    @Override
    public List<RecordResult> query(
            @NonNull Map<String, String> columns,
            List<OrderByDesc> orderBy,
            TableQueryFilter filter,
            int start,
            int limit,
            boolean keepNone,
            boolean rawResult) {
        if (this.schema == null) {
            return Collections.emptyList();
        }
        this.schema.getColumnTypeMapping(columns); // check if all column names are valid
        if (orderBy != null) {
            for (var col : orderBy) {
                if (col == null) {
                    throw new SwValidationException(SwValidationException.ValidSubject.DATASTORE,
                            "order by column should not be null");
                }
                var colSchema = this.schema.getColumnSchemaByName(col.getColumnName());
                if (colSchema == null) {
                    throw new SwValidationException(SwValidationException.ValidSubject.DATASTORE,
                            "unknown orderBy column " + col);
                }
            }
        }
        if (filter != null) {
            this.checkFilter(filter);
        }
        List<Map<String, Object>> results = new ArrayList<>();
        for (var record : this.recordMap.values()) {
            if (filter == null || this.match(filter, record)) {
                results.add(record);
            }
        }
        if (start < 0) {
            start = 0;
        }
        if (limit < 0) {
            limit = results.size();
        }
        int end = start + limit;
        if (end > results.size()) {
            end = results.size();
        }
        if (orderBy != null) {
            results.sort((a, b) -> {
                for (var col : orderBy) {
                    var columnType = this.schema.getColumnSchemaByName(col.getColumnName()).getType();
                    var result = ColumnType.compare(
                            columnType,
                            a.get(col.getColumnName()),
                            columnType,
                            b.get(col.getColumnName()));
                    if (result != 0) {
                        if (col.isDescending()) {
                            return -result;
                        }
                        return result;
                    }
                }
                return 0;
            });
        }

        return results.subList(start, end).stream().map(record -> {
            var r = new HashMap<String, Object>();
            for (var entry : columns.entrySet()) {
                var value = record.get(entry.getKey());
                if (keepNone || value != null) {
                    r.put(entry.getValue(), value);
                }
            }
            return new RecordResult(record.get(this.schema.getKeyColumn()), r);
        }).collect(Collectors.toList());
    }


    @Override
    public List<RecordResult> scan(
            @NonNull Map<String, String> columns,
            String start,
            boolean startInclusive,
            String end,
            boolean endInclusive,
            int limit,
            boolean keepNone) {
        if (this.schema == null) {
            return Collections.emptyList();
        }
        if (this.recordMap.isEmpty() || limit == 0) {
            return Collections.emptyList();
        }

        var startKey = MemoryTableImpl.this.schema.getKeyColumnType().decode(start);
        var endKey = MemoryTableImpl.this.schema.getKeyColumnType().decode(end);
        if (startKey == null) {
            startKey = MemoryTableImpl.this.recordMap.firstKey();
            startInclusive = true;
        }
        if (endKey == null) {
            endKey = MemoryTableImpl.this.recordMap.lastKey();
            endInclusive = true;
        }
        //noinspection rawtypes,unchecked
        if (((Comparable) startKey).compareTo(endKey) > 0) {
            return Collections.emptyList();
        }
        var keyColumn = this.schema.getKeyColumn();
        var records = new ArrayList<RecordResult>();
        for (var record : MemoryTableImpl.this.recordMap.subMap(startKey, startInclusive, endKey, endInclusive)
                .values()) {
            var values = new HashMap<String, Object>();
            for (var entry : columns.entrySet()) {
                var columnName = entry.getKey();
                var alias = entry.getValue();
                var value = record.get(columnName);
                if (keepNone || value != null) {
                    values.put(alias, value);
                }
            }
            records.add(new RecordResult(record.get(keyColumn), values));
            if (records.size() == limit) {
                break;
            }
        }
        if (records.isEmpty()) {
            return Collections.emptyList();
        } else {
            return records;
        }
    }

    private boolean match(TableQueryFilter filter, Map<String, Object> record) {
        switch (filter.getOperator()) {
            case NOT:
                return !this.match((TableQueryFilter) filter.getOperands().get(0), record);
            case AND:
                return this.match((TableQueryFilter) filter.getOperands().get(0), record)
                        && this.match((TableQueryFilter) filter.getOperands().get(1), record);
            case OR:
                return this.match((TableQueryFilter) filter.getOperands().get(0), record)
                        || this.match((TableQueryFilter) filter.getOperands().get(1), record);
            default:
                var result = ColumnType.compare(
                        this.getType(filter.getOperands().get(0)),
                        this.getValue(filter.getOperands().get(0), record),
                        this.getType(filter.getOperands().get(1)),
                        this.getValue(filter.getOperands().get(1), record));
                switch (filter.getOperator()) {
                    case EQUAL:
                        return result == 0;
                    case LESS:
                        return result < 0;
                    case LESS_EQUAL:
                        return result <= 0;
                    case GREATER:
                        return result > 0;
                    case GREATER_EQUAL:
                        return result >= 0;
                    default:
                        throw new IllegalArgumentException("Unexpected value: " + filter.getOperator());
                }
        }
    }

    private ColumnType getType(Object operand) {
        if (operand instanceof TableQueryFilter.Column) {
            var colName = ((TableQueryFilter.Column) operand).getName();
            var colSchema = this.schema.getColumnSchemaByName(colName);
            if (colSchema == null) {
                throw new SwValidationException(SwValidationException.ValidSubject.DATASTORE,
                        "invalid filter, unknown column " + colName);
            }
            return colSchema.getType();
        } else if (operand instanceof TableQueryFilter.Constant) {
            var type = ((TableQueryFilter.Constant) operand).getType();
            if (type == null) {
                throw new SwProcessException(ErrorType.DATASTORE, "invalid filter, constant type is null");
            }
            return type;
        } else if (operand instanceof TableQueryFilter) {
            return ColumnTypeScalar.BOOL;
        } else {
            throw new IllegalArgumentException("invalid operand type " + operand.getClass());
        }
    }

    private Object getValue(Object operand, Map<String, Object> record) {
        if (operand instanceof TableQueryFilter.Column) {
            return record.get(((TableQueryFilter.Column) operand).getName());
        } else if (operand instanceof TableQueryFilter.Constant) {
            return ((TableQueryFilter.Constant) operand).getValue();
        } else {
            throw new IllegalArgumentException("invalid operand type " + operand.getClass());
        }
    }

    private void checkFilter(TableQueryFilter filter) {
        switch (filter.getOperator()) {
            case NOT:
            case AND:
            case OR:
                for (var op : filter.getOperands()) {
                    this.checkFilter((TableQueryFilter) op);
                }
                break;
            case EQUAL:
            case LESS:
            case LESS_EQUAL:
            case GREATER:
            case GREATER_EQUAL:
                this.checkComparability(filter.getOperands().stream()
                        .map(this::getType)
                        .collect(Collectors.toList()));
                break;
            default:
                throw new IllegalArgumentException("Unexpected operator: " + filter.getOperator());
        }
    }

    private void checkComparability(List<ColumnType> types) {
        types = types.stream().filter(t -> t != ColumnTypeScalar.UNKNOWN).collect(Collectors.toList());
        if (types.isEmpty()) {
            return;
        }
        ColumnType type1 = types.get(0);
        for (var type : types) {
            if (!type.isComparableWith(type1)) {
                throw new SwValidationException(SwValidationException.ValidSubject.DATASTORE,
                        MessageFormat.format("invalid filter, can not compare {0} with {1}", type));
            }
        }
    }

    private static Map<String, Object> decodeRecord(TableSchema schema, Map<String, Object> record) {
        if (record == null) {
            return null;
        }
        var ret = new HashMap<String, Object>();
        for (var entry : record.entrySet()) {
            var name = entry.getKey();
            var value = entry.getValue();
            var columnSchema = schema.getColumnSchemaByName(name);
            if (columnSchema == null) {
                throw new SwValidationException(SwValidationException.ValidSubject.DATASTORE,
                        "no schema found for column " + name);
            }
            var columnType = columnSchema.getType();
            try {
                ret.put(name, columnType.decode(value));
            } catch (Exception e) {
                throw new SwValidationException(SwValidationException.ValidSubject.DATASTORE,
                        MessageFormat.format("fail to decode value {0} for column {1}: {2}",
                                value,
                                name,
                                e.getMessage()));
            }
        }
        return ret;
    }

}
