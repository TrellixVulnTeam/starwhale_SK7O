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

package ai.starwhale.mlops.domain.swds;


import static ai.starwhale.mlops.domain.swds.po.SwDatasetVersionEntity.STATUS_AVAILABLE;
import static ai.starwhale.mlops.domain.swds.po.SwDatasetVersionEntity.STATUS_UN_AVAILABLE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.starwhale.mlops.JobMockHolder;
import ai.starwhale.mlops.api.protocol.swds.upload.UploadRequest;
import ai.starwhale.mlops.configuration.json.ObjectMapperConfig;
import ai.starwhale.mlops.domain.job.bo.Job;
import ai.starwhale.mlops.domain.job.cache.HotJobHolder;
import ai.starwhale.mlops.domain.job.cache.HotJobHolderImpl;
import ai.starwhale.mlops.domain.project.ProjectManager;
import ai.starwhale.mlops.domain.project.mapper.ProjectMapper;
import ai.starwhale.mlops.domain.project.po.ProjectEntity;
import ai.starwhale.mlops.domain.storage.StoragePathCoordinator;
import ai.starwhale.mlops.domain.swds.index.datastore.DataStoreTableNameHelper;
import ai.starwhale.mlops.domain.swds.index.datastore.IndexWriter;
import ai.starwhale.mlops.domain.swds.mapper.SwDatasetMapper;
import ai.starwhale.mlops.domain.swds.mapper.SwDatasetVersionMapper;
import ai.starwhale.mlops.domain.swds.po.SwDatasetEntity;
import ai.starwhale.mlops.domain.swds.po.SwDatasetVersionEntity;
import ai.starwhale.mlops.domain.swds.upload.HotSwdsHolder;
import ai.starwhale.mlops.domain.swds.upload.SwdsUploader;
import ai.starwhale.mlops.domain.swds.upload.SwdsVersionWithMetaConverter;
import ai.starwhale.mlops.domain.user.UserService;
import ai.starwhale.mlops.domain.user.bo.User;
import ai.starwhale.mlops.exception.SwValidationException;
import ai.starwhale.mlops.storage.LengthAbleInputStream;
import ai.starwhale.mlops.storage.StorageAccessService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

/**
 * test for {@link SwdsUploader}
 */
public class SwdsUploaderTest {

    final DataStoreTableNameHelper dataStoreTableNameHelper = new DataStoreTableNameHelper();

    final IndexWriter indexWriter = mock(IndexWriter.class);

    @Test
    public void testSwdsUploader() throws IOException {
        YAMLMapper yamlMapper = new ObjectMapperConfig().yamlMapper();
        SwdsVersionWithMetaConverter swdsVersionWithMetaConverter = new SwdsVersionWithMetaConverter(
                yamlMapper);
        HotSwdsHolder hotSwdsHolder = new HotSwdsHolder(swdsVersionWithMetaConverter);
        SwDatasetMapper swdsMapper = mock(SwDatasetMapper.class);
        SwDatasetVersionMapper swdsVersionMapper = mock(SwDatasetVersionMapper.class);
        StoragePathCoordinator storagePathCoordinator = new StoragePathCoordinator("/test");
        StorageAccessService storageAccessService = mock(StorageAccessService.class);
        UserService userService = mock(UserService.class);
        when(userService.currentUserDetail()).thenReturn(User.builder().idTableKey(1L).build());
        ProjectMapper projectMapper = mock(ProjectMapper.class);
        ProjectManager projectManager = mock(ProjectManager.class);
        when(projectManager.findByNameOrDefault(anyString(), anyLong())).thenReturn(
                ProjectEntity.builder().id(1L).build());
        when(projectManager.getProject(anyString())).thenReturn(ProjectEntity.builder().id(1L).build());

        HotJobHolder hotJobHolder = new HotJobHolderImpl();

        SwdsUploader swdsUploader = new SwdsUploader(hotSwdsHolder, swdsMapper, swdsVersionMapper,
                storagePathCoordinator, storageAccessService, userService, yamlMapper,
                hotJobHolder, projectManager, dataStoreTableNameHelper, indexWriter);

        swdsUploader.create(HotSwdsHolderTest.MANIFEST, "_manifest.yaml", new UploadRequest());
        String dsVersionId = "mizwkzrqgqzdemjwmrtdmmjummzxczi3";
        swdsUploader.uploadBody(
                dsVersionId,
                new MockMultipartFile("index.jsonl", "index.jsonl", "plain/text", index_file_content.getBytes()),
                "abc/index.jsonl");

        swdsUploader.end(dsVersionId);

        verify(storageAccessService).put(anyString(), any(byte[].class));
        verify(storageAccessService).put(anyString(), any(InputStream.class), anyLong());
        verify(swdsVersionMapper).updateStatus(null, STATUS_AVAILABLE);
        verify(swdsVersionMapper).addNewVersion(any(SwDatasetVersionEntity.class));
        String dsName = "testds3";
        verify(swdsMapper).findByName(eq(dsName), anyLong());
        verify(swdsMapper).addDataset(any(SwDatasetEntity.class));

        when(storageAccessService.list(anyString())).thenReturn(Stream.of("a", "b"));
        swdsUploader.create(HotSwdsHolderTest.MANIFEST, "_manifest.yaml", new UploadRequest());
        swdsUploader.cancel(dsVersionId);
        verify(swdsVersionMapper).deleteById(null);
        verify(storageAccessService).list(anyString());
        verify(storageAccessService).delete("a");
        verify(storageAccessService).delete("b");
        verify(storageAccessService, times(0)).delete("c");

        when(swdsMapper.findByName(eq(dsName), anyLong())).thenReturn(
                SwDatasetEntity.builder().id(1L).projectId(1L).build());
        SwDatasetVersionEntity mockedEntity = SwDatasetVersionEntity.builder()
                .id(1L)
                .versionName("testversion")
                .status(STATUS_AVAILABLE)
                .build();
        when(swdsVersionMapper.findByDsIdAndVersionNameForUpdate(1L, dsVersionId)).thenReturn(mockedEntity);
        when(swdsVersionMapper.findByDsIdAndVersionName(1L, dsVersionId)).thenReturn(mockedEntity);
        when(storageAccessService.get(anyString())).thenReturn(
                new LengthAbleInputStream(
                        new ByteArrayInputStream(index_file_content.getBytes()),
                        index_file_content.getBytes().length
                )
        );
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);
        ServletOutputStream mockOutPutStream = new ServletOutputStream() {

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {

            }

        };
        when(httpResponse.getOutputStream()).thenReturn(mockOutPutStream);
        swdsUploader.pull("project", dsName, dsVersionId, "index.jsonl", httpResponse);

        Assertions.assertThrowsExactly(SwValidationException.class,
                () -> swdsUploader.create(HotSwdsHolderTest.MANIFEST, "_manifest.yaml", new UploadRequest()));

        JobMockHolder jobMockHolder = new JobMockHolder();
        Job mockJob = jobMockHolder.mockJob();
        hotJobHolder.adopt(mockJob);
        UploadRequest uploadRequest = new UploadRequest();
        uploadRequest.setForce("1");
        Assertions.assertThrowsExactly(SwValidationException.class,
                () -> swdsUploader.create(HotSwdsHolderTest.MANIFEST, "_manifest.yaml", uploadRequest));
        hotJobHolder.remove(mockJob.getId());
        swdsUploader.create(HotSwdsHolderTest.MANIFEST, "_manifest.yaml", uploadRequest);
        verify(swdsVersionMapper, times(1)).updateStatus(1L, STATUS_UN_AVAILABLE);


    }

    static final String index_file_content = "/*\n"
            + " * Copyright 2022 Starwhale, Inc. All Rights Reserved.\n"
            + " *\n"
            + " * Licensed under the Apache License, Version 2.0 (the \"License\");\n"
            + " * you may not use this file except in compliance with the License.\n"
            + " * You may obtain a copy of the License at\n"
            + " *\n"
            + " * http://www.apache.org/licenses/LICENSE-2.0\n"
            + " *\n"
            + " * Unless required by applicable law or agreed to in writing, software\n"
            + " * distributed under the License is distributed on an \"AS IS\" BASIS,\n"
            + " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
            + " * See the License for the specific language governing permissions and\n"
            + " * limitations under the License.\n"
            + " */";
}

