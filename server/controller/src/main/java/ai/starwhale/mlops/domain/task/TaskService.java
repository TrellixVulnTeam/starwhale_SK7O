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

package ai.starwhale.mlops.domain.task;

import ai.starwhale.mlops.api.protocol.report.resp.ResultPath;
import ai.starwhale.mlops.api.protocol.task.TaskVo;
import ai.starwhale.mlops.common.PageParams;
import ai.starwhale.mlops.common.util.PageUtil;
import ai.starwhale.mlops.domain.job.JobManager;
import ai.starwhale.mlops.domain.job.bo.Job;
import ai.starwhale.mlops.domain.job.po.JobEntity;
import ai.starwhale.mlops.domain.system.mapper.ResourcePoolMapper;
import ai.starwhale.mlops.domain.system.po.ResourcePoolEntity;
import ai.starwhale.mlops.domain.system.resourcepool.bo.ResourcePool;
import ai.starwhale.mlops.domain.task.converter.TaskConvertor;
import ai.starwhale.mlops.domain.task.mapper.TaskMapper;
import ai.starwhale.mlops.domain.task.po.TaskEntity;
import ai.starwhale.mlops.exception.SwProcessException;
import ai.starwhale.mlops.exception.SwProcessException.ErrorType;
import ai.starwhale.mlops.storage.StorageAccessService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TaskService {

    private final TaskConvertor taskConvertor;

    private final TaskMapper taskMapper;

    private final StorageAccessService storageAccessService;

    private final JobManager jobManager;

    private final ResourcePoolMapper resourcePoolMapper;

    public TaskService(TaskConvertor taskConvertor, TaskMapper taskMapper,
            StorageAccessService storageAccessService, JobManager jobManager,
            ResourcePoolMapper resourcePoolMapper) {
        this.taskConvertor = taskConvertor;
        this.taskMapper = taskMapper;
        this.storageAccessService = storageAccessService;
        this.jobManager = jobManager;
        this.resourcePoolMapper = resourcePoolMapper;
    }

    public PageInfo<TaskVo> listTasks(String jobUrl, PageParams pageParams) {
        PageHelper.startPage(pageParams.getPageNum(), pageParams.getPageSize());
        Long jobId = jobManager.getJobId(jobUrl);
        JobEntity job = jobManager.findJob(Job.builder().id(jobId).build());
        Long resourcePoolId = job.getResourcePoolId();
        String label = ResourcePool.DEFAULT;
        if (null != resourcePoolId) {
            ResourcePoolEntity resourcePool = resourcePoolMapper.findById(resourcePoolId);
            label = resourcePool.getLabel();
        }
        final String resourcePool = label;
        List<TaskVo> tasks = taskMapper.listTasks(jobId).stream().map(taskConvertor::convert)
                .peek(taskVo -> taskVo.setResourcePool(resourcePool)).collect(
                        Collectors.toList());
        return PageInfo.of(tasks);

    }

    public List<String> offLineLogFiles(Long taskId) {
        ResultPath resultPath = resultPathOfTask(taskId);
        try {
            String logDir = resultPath.logDir();
            return storageAccessService.list(logDir)
                    .map(path -> trimPath(path, logDir))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("read logs path from storage failed {}", taskId, e);
            throw new SwProcessException(ErrorType.DB).tip("read log path from db failed");
        }

    }

    public String logContent(Long taskId, String logFileName) {
        ResultPath resultPath = resultPathOfTask(taskId);
        String logDir = resultPath.logDir();
        try (InputStream inputStream = storageAccessService.get(
                logDir + PATH_SPLITERATOR + logFileName)) {
            return new String(inputStream.readAllBytes());
        } catch (IOException e) {
            log.error("read logs path from storage failed {}", taskId, e);
            throw new SwProcessException(ErrorType.DB).tip("read log path from db failed");
        }

    }

    private ResultPath resultPathOfTask(Long taskId) {
        TaskEntity taskById = taskMapper.findTaskById(taskId);
        return new ResultPath(taskById.getOutputPath());
    }

    static final String PATH_SPLITERATOR = "/";

    String trimPath(String fullPath, String dir) {
        return fullPath.replace(dir, "").replace(PATH_SPLITERATOR, "");
    }

    public TaskVo findTask(Long taskId) {
        TaskEntity entity = taskMapper.findTaskById(taskId);

        return taskConvertor.convert(entity);
    }

}
