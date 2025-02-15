---
title: 数据集命令
---

## 1. 基本信息

```bash
swcli [GLOBAL OPTIONS] dataset [OPTIONS] COMMAND [ARGS]...
```

dataset命令提供适用于Standalone Instance和Cloud Instance的Starwhale Dataset全生命周期的管理，包括构建、查看、分发等功能。在Standalone Instance中，dataset命令使用本地磁盘存储数据集文件。dataset命令通过HTTP API对Cloud Instance对象进行操作。

**Dataset URI**格式：`[<Project URI>/dataset/]<dataset name>[/version/<version id>]`。

dataset包含如下子命令：

|Command|Standalone|Cloud|
|-------|----------|-----|
|`build`|✅|❌|
|`copy`|✅|✅|
|`diff`|✅|❌|
|`history`|✅|✅|
|`info`|✅|✅|
|`list`|✅|✅|
|`recover`|✅|✅|
|`remove`|✅|✅|
|`summary`|✅|✅|
|`tag`|✅|❌|

## 2. 构建数据集

```bash
swcli dataset build [OPTIONS] WORKDIR
```

`dataset build` 命令会在 `WORKDIR` 目录中寻找 `dataset.yaml`文件，并以该文件的配置为起点开始构建数据集。`WORKDIR`参数应为一个合法的路径字符串，`dataset.yaml`中handler路径是相对于 `WORKDIR` 的，一般在这个目录中会有 `.swignore`文件、数据集构建代码等。目前构建数据集会自动包括 `WORKDIR` 及子目录中的Python文件，便于更好的版本化最终数据集，若不想携带此类文件，可以在 `WORKDIR` 中创建 `.swignore` 文件并进行相关设置。**`dataset build`命令目前只能在standalone instance下执行**。

`dataset build` 命令会调用用户代码进行数据集构建，如果有第三方库的使用，建议使用Starwhale Runtime对依赖进行管理，便于后续迭代。

`dataset build` 命令参数如下：

|参数|参数别名|必要性|类型|默认值|说明|
|------|--------|-------|-----------|-----|-----------|
|`--project`|`-p`|❌|String|`swcli project select`命令设定的project|Project URI|
|`--dataset-yaml`|`-f`|❌|String|dataset.yaml|建议使用默认的dataset.yaml，无需修改。|
|`--append`||❌|Boolean|False|当指定该参数时，表示此次数据集构建会继承 `--append-from`版本的数据集内容，实现追加数据集的目的。|
|`--append-from`||❌|String|latest|与 `--append` 参数组合使用，指定继承数据集的版本，注意此处并不是Dataset URI，而是同一个数据集下的其他版本号或tag，默认为latest，即最近一次构建的版本。|
|`--runtime`||❌|String||`--runtime`参数为Standalone Instance中的Runtime URI。若设置，则表示数据集构建的时候会使用该Runtime提供的运行时环境；若不设置，则使用当前shell环境作为运行时。设置`--runtime`参数是安全的，只在build运行时才会使用Runtime，不会污染当前shell环境。|

## 3. 分发数据集

```bash
swcli dataset copy [OPTIONS] SRC DEST
```

`dataset copy` 命令能对构建好的数据集实现高效的分发，既可以从Standalone Instance上传数据集到Cloud Instance，又可以从Cloud Instance上下载数据到本地的Standalone Instance，目前不支持Cloud A 到 Cloud B， Standalone A到Standalone B这种层面的数据集分发。`SRC` 参数为 `Dataset URI` 格式，`DEST` 参数为 `Project URI`格式。

Starwhale的数据集分发是高效的，会通过类似chunk机制按需拷贝数据集中的内容，可避免重复拷贝。当发生重复拷贝且需强制覆盖时，可以使用 `--force` 参数。

`dataset copy` 命令参数如下：

|参数|参数别名|必要性|类型|默认值|说明|
|------|--------|-------|-----------|-----|-----------|
|`--force`|`-f`|❌|Boolean|False|`DEST` 存在相同version的dataset，指定该参数后执行copy命令就会强制覆盖。|
|`--with-auth`||❌|Boolean|False|当构建remote-link形态的dataset时，一般会使用 `starwhale.S3LinkAuth` 存储密钥信息，`swcli dataset build` 执行后，会在对应的swds目录中产生一个 `.auth-env` 文件。当指定 `--with-auth` 参数时，若从standalone instance向cloud instance 分发数据集，则会携带有敏感信息的`.auth-env`文件，cloud Instance的controller会自动对该文件进行处理，确保运行评测任务加载数据集时建立安全、合法的连接。目前不支持从cloud instance到standalone instance分发数据集时指定 `--with-auth` 参数，即无法下载cloud instance中的密钥文件。|

## 4. 对比数据集

```bash
swcli dataset diff [OPTIONS] BASE_URI COMPARE_URI
```

`dataset diff` 命令提供同一个数据集中两个不同版本的数据对比。`BASE_URI` 为Base的Dataset URI，`COMPARE_URI` 为与Base对比的Dataset URI。**`dataset diff`命令目前只能在standalone instance下执行**。

`dataset diff` 命令参数如下：

|参数|参数别名|必要性|类型|默认值|说明|
|------|--------|-------|-----------|-----|-----------|
|`--show-details`||❌|Boolean|False|指定该参数后会输出对比的详细信息，包括每行数据的差异，可能会输出较多内容。|

![dataset-diff.png](../../img/dataset-diff.png)

## 5. 查看数据集摘要信息

```bash
swcli dataset summary DATASET
```

`dataset summary` 命令输出数据集具体版本的摘要信息，包括数据集行数、尺寸和数据形态等信息。`DATASET` 参数为Dataset URI，需包含version。

## 6. 查看数据集历史版本

```bash
swcli dataset history [OPTIONS] DATASET
```

`dataset history` 命令输出数据集所有未删除的版本信息。`DATASET` 参数为Dataset URI，其他参数如下：

|参数|参数别名|必要性|类型|默认值|说明|
|------|--------|-------|-----------|-----|-----------|
|`--fullname`||❌|Boolean|False|显示完整的版本信息，默认只显示版本号的前12位。|

## 7. 查看数据集详细信息

```bash
swcli dataset info [OPTIONS] DATASET
```

`dataset info` 命令输出数据集或数据集中某个版本的详细信息。`DATASET` 参数为Dataset URI，其他参数如下：

|参数|参数别名|必要性|类型|默认值|说明|
|------|--------|-------|-----------|-----|-----------|
|`--fullname`||❌|Boolean|False|显示完整的版本信息，默认只显示版本号的前12位。|

![dataset-info.gif](../../img/dataset-info.gif)

## 8. 展示数据集列表

```bash
swcli dataset list [OPTIONS]
```

`dataset list` 命令输出当前选定的instance和project下的所有数据集及相关版本。命令参数如下：

|参数|参数别名|必要性|类型|默认值|说明|
|------|--------|-------|-----------|-----|-----------|
|`--project`|`-p`|❌|String|`swcli project select`命令选定的默认project|Project URI|
|`--fullname`||❌|Boolean|False|显示完整的版本信息，默认只显示版本号的前12位。|
|`--show-removed`||❌|Boolean|False|显示本地已经删除但能恢复的数据集。|
|`--page`||❌|Integer|1|Cloud Instance中分页显示中page序号。|
|`--size`||❌|Integer|20|Cloud Instance中分页显示中每页数量。|

## 9. 删除数据集

```bash
swcli dataset remove [OPTIONS] DATASET
```

`dataset remove` 命令可以删除整个数据集或数据集中的某个版本。删除可以分为硬删除和软删除，默认为软删除，指定 `--force` 参数为硬删除。所有的软删除数据集在没有GC之前，都可以通过 `swcli dataset recover` 命令进行恢复。`DATASET` 参数为Dataset URI，当没有指定版本时，会对整个数据集所有版本进行删除，若URI带有版本信息，则只会对该版本进行删除。软删除的数据集，可以通过 `swcli dataset list --show-removed` 命令查看。

|参数|参数别名|必要性|类型|默认值|说明|
|------|--------|-------|-----------|-----|-----------|
|`--force`|`-f`|❌|Boolean|False|强制删除，不可恢复|

## 10. 恢复软删除的数据集

```bash
swcli dataset recover [OPTIONS] DATASET
```

`dataset recover` 命令可以对软删除的数据集进行恢复。`DATASET` 参数为Dataset URI，若有版本信息则需要是完整的版本号，不支持简写。支持整个dataset的恢复和某个tag dataset的恢复。其他参数如下：

|参数|参数别名|必要性|类型|默认值|说明|
|------|--------|-------|-----------|-----|-----------|
|`--force`|`-f`|❌|Boolean|False|强制恢复，处理类似恢复版本冲突的情况。|

![dataset-recover.png](../../img/dataset-recover.png)

## 11. 标记数据集

```bash
swcli dataset tag [OPTIONS] DATASET [TAGS]...
```

`dataset tag` 命令可以对数据集中的某个版本标记Tag，后续可以通过Tag对数据集进行操作。`DATASET` 参数为Dataset URI，需包含 `/version/{version id or tag}` 部分。 `TAGS` 参数可以指定一个或多个，为字符串格式。默认为追加标签，当指定 `--remove` 参数后，则为删除标签。 **`dataset tag`命令目前只能在standalone instance下执行**。

一个数据集中的某个版本可以指定零个或多个标签，一个标签在该数据集范围内，只能指向一个版本。当本地构建或从cloud instance下载某个版本的数据集时，swcli会提供auto-fast-tag机制，标记类似 `v1`, `v2` 这种自增的、简易使用的标签。当本地构建一个数据集的新版时，会自动标记 `latest` 标签。

|参数|参数别名|必要性|类型|默认值|说明|
|------|--------|-------|-----------|-----|-----------|
|`--remove`|`-r`|❌|Boolean|False|删除标签|
|`--quiet`|`-q`|❌|Boolean|False|忽略标签操作中的错误，例如删除不存在的标签，添加不合法的标签等|

![dataset-tags.png](../../img/dataset-tags.png)
