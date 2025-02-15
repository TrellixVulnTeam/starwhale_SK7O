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

package ai.starwhale.mlops.domain.task.status.watchers;

import ai.starwhale.mlops.domain.job.bo.Job;
import ai.starwhale.mlops.domain.job.status.JobStatus;
import ai.starwhale.mlops.domain.job.status.JobUpdateHelper;
import ai.starwhale.mlops.domain.job.step.StepHelper;
import ai.starwhale.mlops.domain.job.step.bo.Step;
import ai.starwhale.mlops.domain.job.step.mapper.StepMapper;
import ai.starwhale.mlops.domain.job.step.status.StepStatus;
import ai.starwhale.mlops.domain.job.step.status.StepStatusMachine;
import ai.starwhale.mlops.domain.job.step.trigger.StepTrigger;
import ai.starwhale.mlops.domain.task.bo.Task;
import ai.starwhale.mlops.domain.task.status.TaskStatus;
import ai.starwhale.mlops.domain.task.status.TaskStatusChangeWatcher;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * task status change for job status change
 */
@Slf4j
@Component
@Order(2)
public class TaskWatcherForJobStatus implements TaskStatusChangeWatcher {

    final StepHelper stepHelper;

    final StepStatusMachine stepStatusMachine;

    final StepMapper stepMapper;

    final StepTrigger stepTrigger;

    final JobUpdateHelper jobUpdateHelper;

    public TaskWatcherForJobStatus(
            StepHelper stepHelper,
            StepStatusMachine stepStatusMachine,
            StepMapper stepMapper,
            StepTrigger stepTrigger,
            JobUpdateHelper jobUpdateHelper) {
        this.stepHelper = stepHelper;
        this.stepStatusMachine = stepStatusMachine;
        this.stepMapper = stepMapper;
        this.stepTrigger = stepTrigger;
        this.jobUpdateHelper = jobUpdateHelper;
    }

    @Override
    public void onTaskStatusChange(Task task, TaskStatus oldStatus) {
        Step step = task.getStep();
        Job job = step.getJob();
        log.debug("updating job {} status for task {}", job.getId(), task.getId());
        synchronized (job) {
            log.debug("lock got for job {} and task {}", job.getId(), task.getId());
            Collection<TaskStatus> taskStatuses = step.getTasks().parallelStream().map(Task::getStatus).collect(
                    Collectors.toSet());
            StepStatus stepNewStatus = stepHelper.desiredStepStatus(taskStatuses);
            if (step.getStatus() == stepNewStatus) {
                log.debug("step status not changed {} id {}", stepNewStatus, step.getId());
                return;
            }
            if (!stepStatusMachine.couldTransfer(step.getStatus(), stepNewStatus)) {
                log.warn("step status change unexpectedly from {} to {} of id {} forbidden", step.getStatus(),
                        stepNewStatus, step.getId());
                return;
            }
            updateStepStatus(step, stepNewStatus);
            jobUpdateHelper.updateJob(job);
            if (step.getStatus() == StepStatus.SUCCESS && job.getStatus() != JobStatus.SUCCESS) {
                stepTrigger.triggerNextStep(step);
            }

        }
    }

    private void updateStepStatus(Step step, StepStatus stepNewStatus) {
        log.info("step status change from {} to {} with id {}", step.getStatus(), stepNewStatus,
                step.getId());
        step.setStatus(stepNewStatus);
        stepMapper.updateStatus(List.of(step.getId()), stepNewStatus);
        var now = new Date();
        if (stepStatusMachine.isFinal(stepNewStatus)) {
            step.setFinishTime(now.getTime());
            stepMapper.updateFinishedTime(step.getId(), now);
        }
        if (StepStatus.RUNNING == stepNewStatus) {
            step.setStartTime(now.getTime());
            stepMapper.updateStartedTime(step.getId(), now);
        }
    }

}
