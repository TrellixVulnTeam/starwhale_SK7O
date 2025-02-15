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

package ai.starwhale.mlops.domain.project;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;

import ai.starwhale.mlops.api.protocol.user.UserVo;
import ai.starwhale.mlops.common.IdConvertor;
import ai.starwhale.mlops.common.OrderParams;
import ai.starwhale.mlops.common.PageParams;
import ai.starwhale.mlops.domain.project.bo.Project;
import ai.starwhale.mlops.domain.project.bo.Project.Privacy;
import ai.starwhale.mlops.domain.project.mapper.ProjectMapper;
import ai.starwhale.mlops.domain.project.mapper.ProjectRoleMapper;
import ai.starwhale.mlops.domain.project.po.ProjectEntity;
import ai.starwhale.mlops.domain.project.po.ProjectRoleEntity;
import ai.starwhale.mlops.domain.user.RoleConvertor;
import ai.starwhale.mlops.domain.user.UserConvertor;
import ai.starwhale.mlops.domain.user.UserService;
import ai.starwhale.mlops.domain.user.bo.Role;
import ai.starwhale.mlops.domain.user.bo.User;
import ai.starwhale.mlops.domain.user.po.RoleEntity;
import ai.starwhale.mlops.domain.user.po.UserEntity;
import ai.starwhale.mlops.exception.api.StarwhaleApiException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProjectServiceTest {

    private ProjectService service;

    private ProjectMapper projectMapper;

    private ProjectRoleMapper projectRoleMapper;

    @BeforeEach
    public void setUp() {
        ProjectEntity project1 = ProjectEntity.builder()
                .id(1L).projectName("p1").ownerId(1L).isDefault(1).isDeleted(0).privacy(1).description("project1")
                .build();
        ProjectEntity project2 = ProjectEntity.builder()
                .id(2L).projectName("p2").ownerId(2L).isDefault(0).isDeleted(0).privacy(0).description("project2")
                .build();

        ProjectManager projectManager = mock(ProjectManager.class);
        given(projectManager.getProjectId(same("1"))).willReturn(1L);
        given(projectManager.getProjectId(same("2"))).willReturn(2L);
        given(projectManager.getProjectId(same("p1"))).willReturn(1L);
        given(projectManager.getProjectId(same("p2"))).willReturn(2L);
        given(projectManager.listProjects(anyString(), any(), any())).willReturn(List.of(project1, project2));
        given(projectManager.existProject(same("exist_project"))).willReturn(true);

        UserService userService = mock(UserService.class);
        given(userService.currentUserDetail()).willReturn(User.builder()
                .name("starwhale")
                .id(1L)
                .idTableKey(1L)
                .roles(Set.of(Role.builder().roleName("Owner").roleCode("OWNER").build()))
                .build());
        given(userService.getProjectRolesOfUser(any(), any())).willReturn(Collections.emptyList());

        projectRoleMapper = mock(ProjectRoleMapper.class);

        projectMapper = mock(ProjectMapper.class);
        given(projectMapper.findProject(same(1L)))
                .willReturn(project1);
        given(projectMapper.findProject(same(2L)))
                .willReturn(project2);

        IdConvertor idConvertor = new IdConvertor();
        UserConvertor userConvertor = new UserConvertor(idConvertor);
        ProjectConvertor projectConvertor = new ProjectConvertor(idConvertor, userConvertor);
        RoleConvertor roleConvertor = new RoleConvertor(idConvertor);
        ProjectRoleConvertor projectRoleConvertor = new ProjectRoleConvertor(
                idConvertor,
                projectConvertor,
                roleConvertor,
                userConvertor);
        service = new ProjectService(projectMapper,
                projectManager,
                projectConvertor,
                projectRoleMapper,
                projectRoleConvertor,
                idConvertor,
                userService);
    }

    @Test
    public void testFindProject() {
        var p1 = service.findProject("1");
        assertThat(p1, allOf(
                notNullValue(),
                hasProperty("name", is("p1")),
                hasProperty("id", is("1")),
                hasProperty("privacy", is("PUBLIC")),
                hasProperty("description", is("project1")),
                hasProperty("owner", isA(UserVo.class))
        ));
        var p2 = service.findProject("p2");
        assertThat(p2, allOf(
                notNullValue(),
                hasProperty("name", is("p2")),
                hasProperty("id", is("2")),
                hasProperty("privacy", is("PRIVATE")),
                hasProperty("description", is("project2")),
                hasProperty("owner", isA(UserVo.class))
        ));
    }

    @Test
    public void testListProject() {
        var res = service.listProject("",
                PageParams.builder().build(),
                OrderParams.builder().build(),
                User.builder().build());
        assertThat(res, allOf(
                notNullValue(),
                hasProperty("total", is(2L))
        ));
    }

    @Test
    public void testCreateProject() {
        given(projectMapper.createProject(any(ProjectEntity.class)))
                .willAnswer(invocation -> {
                    var entity = (ProjectEntity) invocation.getArgument(0);
                    entity.setId(1L);
                    return null;
                });

        var res = service.createProject(Project.builder()
                .name("test1")
                .owner(User.builder().id(1L).build())
                .privacy(Privacy.PRIVATE)
                .build());
        assertThat(res, is(1L));

        assertThrows(StarwhaleApiException.class,
                () -> service.createProject(Project.builder()
                        .name("exist_project")
                        .owner(User.builder().id(1L).build())
                        .privacy(Privacy.PRIVATE)
                        .build()));
    }

    @Test
    public void testDeleteProject() {
        given(projectMapper.deleteProject(same(1L))).willReturn(1);
        given(projectMapper.deleteProject(same(2L))).willReturn(1);

        var res = service.deleteProject("2");
        assertThat(res, is(true));

        res = service.deleteProject("p2");
        assertThat(res, is(true));

        assertThrows(StarwhaleApiException.class,
                () -> service.deleteProject("1"));

        assertThrows(StarwhaleApiException.class,
                () -> service.deleteProject("not_exist"));
    }

    @Test
    public void testRecoverProject() {
        given(projectMapper.findProject(99L))
                .willReturn(ProjectEntity.builder().id(99L).projectName("del.deleted").build());
        given(projectMapper.listProjects(anyString(), any(), same(1), any()))
                .willReturn(Collections.emptyList());
        given(projectMapper.listProjects(matches("^one.*"), any(), same(1), any()))
                .willReturn(List.of(ProjectEntity.builder().id(1L).build()));
        given(projectMapper.listProjects(matches("^many.*"), any(), same(1), any()))
                .willReturn(List.of(ProjectEntity.builder().id(1L).build(), ProjectEntity.builder().id(2L).build()));
        given(projectMapper.listProjects(matches("^exist_project.*"), any(), same(1), any()))
                .willReturn(List.of(ProjectEntity.builder().id(1L).build()));

        var res = service.recoverProject("99");
        assertThat(res, is(99L));

        res = service.recoverProject("one");
        assertThat(res, is(1L));

        assertThrows(StarwhaleApiException.class,
                () -> service.recoverProject("many"));

        assertThrows(StarwhaleApiException.class,
                () -> service.recoverProject("p1"));

        assertThrows(StarwhaleApiException.class,
                () -> service.recoverProject("exist_project"));
    }

    @Test
    public void testModifyProject() {
        given(projectMapper.modifyProject(argThat(p -> p.getId() == 1L)))
                .willReturn(1);
        given(projectMapper.findProjectByNameForUpdate("p2"))
                .willReturn(ProjectEntity.builder().id(2L).projectName("p2").build());
        var res = service.modifyProject("1", "pro1", null, 1L, "PUBLIC");
        assertThat(res, is(true));

        res = service.modifyProject("p1", "pro1", null, 1L, "PUBLIC");
        assertThat(res, is(true));

        res = service.modifyProject("2", "pro1", null, 1L, "PUBLIC");
        assertThat(res, is(false));

        res = service.modifyProject("1", "pro1", null, 1L, "PUBLIC");
        assertThat(res, is(true));

        res = service.modifyProject("2", "p2", null, 1L, "PUBLIC");
        assertThat(res, is(false));

        assertThrows(StarwhaleApiException.class,
                () -> service.modifyProject("1", "p2", "", 1L, "PUBLIC"));
    }

    @Test
    public void testListProjectRoles() {
        given(projectRoleMapper.listProjectRoles(same(1L)))
                .willReturn(List.of(
                        ProjectRoleEntity.builder()
                                .id(1L)
                                .project(ProjectEntity.builder().id(1L).build())
                                .role(RoleEntity.builder().id(1L).build())
                                .user(UserEntity.builder().id(1L).build())
                                .build(),
                        ProjectRoleEntity.builder()
                                .id(2L)
                                .project(ProjectEntity.builder().id(1L).build())
                                .role(RoleEntity.builder().id(2L).build())
                                .user(UserEntity.builder().id(2L).build())
                                .build()));
        given(projectRoleMapper.listProjectRoles(same(2L)))
                .willReturn(Collections.emptyList());

        var res = service.listProjectRoles("1");
        assertThat(res, allOf(
                notNullValue(),
                hasSize(2)
        ));
        res = service.listProjectRoles("p1");
        assertThat(res, allOf(
                notNullValue(),
                hasSize(2)
        ));
        res = service.listProjectRoles("2");
        assertThat(res, allOf(
                notNullValue(),
                hasSize(0)
        ));
    }

    @Test
    public void testAddProjectRole() {
        given(projectRoleMapper.addProjectRole(any())).willReturn(1);
        var res = service.addProjectRole("1", 1L, 1L);
        assertThat(res, is(true));

        res = service.addProjectRole("p1", 1L, 1L);
        assertThat(res, is(true));

    }

    @Test
    public void testModifyProjectRole() {
        given(projectRoleMapper.updateProjectRole(argThat(r -> r.getId() == 1L)))
                .willReturn(1);
        var res = service.modifyProjectRole("", 1L, 2L);
        assertThat(res, is(true));

        res = service.modifyProjectRole("", 2L, 2L);
        assertThat(res, is(false));
    }

    @Test
    public void testDeleteProjectRole() {
        given(projectRoleMapper.deleteProjectRole(same(1L))).willReturn(1);
        var res = service.deleteProjectRole("", 1L);
        assertThat(res, is(true));

        res = service.deleteProjectRole("", 2L);
        assertThat(res, is(false));
    }

}
