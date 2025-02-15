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

package ai.starwhale.mlops.domain.task.mapper;

import ai.starwhale.mlops.domain.task.po.TaskEntity;
import ai.starwhale.mlops.domain.task.status.TaskStatus;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TaskMapper {

    List<TaskEntity> listTasks(@Param("jobId") Long jobId);

    TaskEntity findTaskById(@Param("taskId") Long taskId);

    int addTask(@Param("task") TaskEntity task);

    int addAll(@Param("taskList") List<TaskEntity> taskList);

    void updateTaskStatus(@Param("ids") List<Long> taskIds, @Param("taskStatus") TaskStatus taskStatus);

    List<TaskEntity> findTaskByStatus(@Param("taskStatus") TaskStatus taskStatus);

    List<TaskEntity> findTaskByStatusIn(@Param("taskStatusList") List<TaskStatus> taskStatusList);

    void updateTaskFinishedTime(@Param("taskId") Long taskId, @Param("finishedTime") Date finishedTime);

    void updateTaskStartedTime(@Param("taskId") Long taskId, @Param("startedTime") Date startedTime);

    List<TaskEntity> findByStepId(@Param("stepId") Long stepId);

    void updateTaskRequest(@Param("taskId") Long taskId, @Param("request") String request);
}

