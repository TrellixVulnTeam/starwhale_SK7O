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

package ai.starwhale.mlops.schedule.k8s;

import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1EmptyDirVolumeSource;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobSpec;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.util.Yaml;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
public class K8sJobTemplate {

    public static final Map<String, String> starwhaleJobLabel = Map.of("owner", "starwhale");

    public static final String JOB_IDENTITY_LABEL = "job-name";

    public static final String PIP_CACHE_VOLUME_NAME = "pip-cache";

    private final String pipCacheHostPath;

    public static final String DEVICE_LABEL_NAME_PREFIX = "device.starwhale.ai-";

    final String template;
    final V1Job v1Job;

    public K8sJobTemplate(
            @Value("${sw.infra.k8s.job-template-path}") String templatePath,
            @Value("${sw.infra.k8s.host-path-for-cache}") String pipCacheHostPath
    )
            throws IOException {
        if (!StringUtils.hasText(templatePath)) {
            this.template = getJobDefaultTemplate();
        } else {
            this.template = Files.readString(Paths.get(templatePath));
        }
        v1Job = Yaml.loadAs(template, V1Job.class);
        this.pipCacheHostPath = pipCacheHostPath;
    }

    public List<V1Container> getInitContainerTemplates() {
        return v1Job.getSpec().getTemplate().getSpec().getInitContainers();
    }

    public List<V1Container> getContainersTemplates() {
        return v1Job.getSpec().getTemplate().getSpec().getContainers();
    }

    public V1Job renderJob(String jobName,
            Map<String, ContainerOverwriteSpec> containerSpecMap, Map<String, String> nodeSelectors) {
        V1Job job = Yaml.loadAs(template, V1Job.class);
        job.getMetadata().name(jobName);
        HashMap<String, String> labels = new HashMap<>();
        labels.putAll(starwhaleJobLabel);
        labels.put(JOB_IDENTITY_LABEL, jobName);
        job.getMetadata().labels(labels);
        V1JobSpec jobSpec = job.getSpec();
        Objects.requireNonNull(jobSpec, "can not get job spec");
        V1PodSpec podSpec = jobSpec.getTemplate().getSpec();
        Objects.requireNonNull(podSpec, "can not get pod spec");
        if (null != nodeSelectors) {
            Map<String, String> templateSelector = podSpec.getNodeSelector();
            if (null != templateSelector) {
                nodeSelectors.putAll(templateSelector);
            }
            podSpec.nodeSelector(nodeSelectors);
        }
        Stream.concat(podSpec.getContainers().stream(), podSpec.getInitContainers().stream()).forEach(c -> {
            ContainerOverwriteSpec containerOverwriteSpec = containerSpecMap.get(c.getName());
            if (null == containerOverwriteSpec) {
                return;
            }
            if (null != containerOverwriteSpec.resourceOverwriteSpec
                    && null != containerOverwriteSpec.resourceOverwriteSpec.getResourceSelector()) {
                c.resources(containerOverwriteSpec.resourceOverwriteSpec.getResourceSelector());
            }
            if (!CollectionUtils.isEmpty(containerOverwriteSpec.cmds)) {
                c.args(containerOverwriteSpec.cmds);
            }
            if (StringUtils.hasText(containerOverwriteSpec.image)) {
                c.image(containerOverwriteSpec.image);
            }
            if (!CollectionUtils.isEmpty(containerOverwriteSpec.envs)) {
                c.env(containerOverwriteSpec.envs);
            }

        });

        // patch pip cache volume
        List<V1Volume> volumes = job.getSpec().getTemplate().getSpec().getVolumes();
        var volume = volumes.stream().filter(v -> v.getName().equals(PIP_CACHE_VOLUME_NAME))
                .findFirst().orElse(null);
        if (volume != null) {
            if (pipCacheHostPath.isEmpty()) {
                // make volume emptyDir
                volume.setHostPath(null);
                volume.emptyDir(new V1EmptyDirVolumeSource());
            } else {
                volume.getHostPath().path(pipCacheHostPath);
            }
        }

        if (jobSpec.getTemplate().getMetadata() == null) {
            jobSpec.getTemplate().metadata(new V1ObjectMeta());
        }
        var meta = jobSpec.getTemplate().getMetadata();
        addDeviceInfoLabel(meta, containerSpecMap);

        return job;
    }

    private String getJobDefaultTemplate() throws IOException {
        String file = "template/job.yaml";
        InputStream is = this.getClass().getClassLoader()
                .getResourceAsStream(file);
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private void addDeviceInfoLabel(V1ObjectMeta meta, Map<String, ContainerOverwriteSpec> specs) {
        if (meta == null) {
            return;
        }
        if (meta.getLabels() == null) {
            meta.labels(new HashMap<>());
        }
        specs.values().forEach(spec -> {
            if (spec.resourceOverwriteSpec == null) {
                return;
            }
            var request = spec.resourceOverwriteSpec.getResourceSelector().getRequests();
            if (request == null) {
                return;
            }
            request.keySet().forEach(rc -> meta.getLabels().put(DEVICE_LABEL_NAME_PREFIX + rc, "true"));
        });
    }
}
