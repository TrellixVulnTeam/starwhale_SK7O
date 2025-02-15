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

package ai.starwhale.mlops.api;

import ai.starwhale.mlops.api.protocol.ResponseMessage;
import ai.starwhale.mlops.api.protocol.system.ResourcePoolVo;
import ai.starwhale.mlops.api.protocol.system.SystemVersionVo;
import ai.starwhale.mlops.api.protocol.system.UpgradeProgressVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "System")
@Validated
public interface SystemApi {

    @Operation(summary = "Get the list of resource pool")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "ok",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = List.class)))
            })
    @GetMapping(value = "/system/resourcePool")
    ResponseEntity<ResponseMessage<List<ResourcePoolVo>>> listResourcePools();

    @Operation(summary = "Upgrade system version or cancel upgrade")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "ok")})
    @PostMapping(value = "/system/version/{action}")
    ResponseEntity<ResponseMessage<String>> systemVersionAction(
            @Parameter(
                    in = ParameterIn.PATH,
                    description = "Action: upgrade or cancel",
                    required = true,
                    schema = @Schema(allowableValues = {"upgrade", "cancel"}))
            @PathVariable("action")
                    String action);

    @Operation(summary = "Get current version of the system")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "ok",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SystemVersionVo.class)))
            })
    @GetMapping(value = "/system/version")
    ResponseEntity<ResponseMessage<SystemVersionVo>> getCurrentVersion();

    @Operation(summary = "Get latest version of the system")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "ok",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SystemVersionVo.class)))
            })
    @GetMapping(value = "/system/version/latest")
    ResponseEntity<ResponseMessage<SystemVersionVo>> getLatestVersion();

    @Operation(
            summary = "Get the current upgrade progress",
            description =
                    "Get the current server upgrade process. If downloading, return the download progress")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "ok",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UpgradeProgressVo.class)))
            })
    @GetMapping(value = "/system/version/progress")
    ResponseEntity<ResponseMessage<UpgradeProgressVo>> getUpgradeProgress();

    @Operation(
            summary = "Update system settings",
            description =
                    "Update system settings")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "ok",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = String.class)))
            })
    @PreAuthorize("hasAnyRole('OWNER')")
    @PostMapping(value = "/system/setting")
    ResponseEntity<ResponseMessage<String>> updateSetting(@RequestBody String setting);

    @Operation(
            summary = "Get system settings",
            description =
                    "Get system settings in yaml string")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "ok",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = String.class)))
            })
    @PreAuthorize("hasAnyRole('OWNER')")
    @GetMapping(value = "/system/setting")
    ResponseEntity<ResponseMessage<String>> querySetting();
}
