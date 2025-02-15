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

package ai.starwhale.mlops.domain.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import ai.starwhale.mlops.api.protocol.runtime.RuntimeVersionVo;
import ai.starwhale.mlops.api.protocol.user.UserVo;
import ai.starwhale.mlops.common.IdConvertor;
import ai.starwhale.mlops.common.VersionAliasConvertor;
import ai.starwhale.mlops.domain.runtime.po.RuntimeVersionEntity;
import ai.starwhale.mlops.domain.user.UserConvertor;
import ai.starwhale.mlops.domain.user.po.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RuntimeVersionConvertorTest {

    private RuntimeVersionConvertor runtimeVersionConvertor;

    @BeforeEach
    public void setUp() {
        UserConvertor userConvertor = mock(UserConvertor.class);
        given(userConvertor.convert(any(UserEntity.class))).willReturn(UserVo.empty());
        given(userConvertor.revert(any(UserVo.class))).willReturn(UserEntity.builder().build());
        runtimeVersionConvertor = new RuntimeVersionConvertor(
                new IdConvertor(),
                userConvertor,
                new VersionAliasConvertor()
        );
    }

    @Test
    public void testConvert() {
        var res = runtimeVersionConvertor.convert(RuntimeVersionEntity.builder()
                .id(1L)
                .versionName("name1")
                .versionOrder(2L)
                .owner(UserEntity.builder().build())
                .versionTag("tag1")
                .versionMeta("meta1")
                .image("image1")
                .build());
        assertThat(res, allOf(
                notNullValue(),
                hasProperty("name", is("name1")),
                hasProperty("alias", is("v2")),
                hasProperty("owner", isA(UserVo.class)),
                hasProperty("tag", is("tag1")),
                hasProperty("meta", is("meta1")),
                hasProperty("image", is("image1"))
        ));
    }

    @Test
    public void testRevert() {
        assertThrows(UnsupportedOperationException.class,
                () -> runtimeVersionConvertor.revert(RuntimeVersionVo.builder().build()));
    }
}
