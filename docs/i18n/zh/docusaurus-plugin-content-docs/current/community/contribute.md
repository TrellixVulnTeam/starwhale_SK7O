---
title: Starwhale开源贡献指南
---

## 1. 参与贡献

Starwhale 非常欢迎来自开源社区的贡献，包括但不限于以下方式：

- 描述使用过程中的遇到的问题
- 提交Feature Request
- 参与Slack和Github Issues讨论
- 参与Code Review
- 改进文档和示例程序
- 修复程序Bug
- 增加Test Case
- 改进代码的可读性
- 开发新的Features
- 编写Enhancement Proposal

可以通过以下方式参与开发者社区，获取最新信息和联系Starwhale开发者：

- [Slack](https://starwhale.slack.com/)
- [Github Issues](https://github.com/star-whale/starwhale/issues)
- [Twitter](https://twitter.com/starwhaleai)
- Email: *developer@starwhale.ai*

Starwhale社区使用[Github Issues](https://github.com/star-whale/starwhale/issues)来跟踪问题和管理新特性的开发。可以选择"good first issue"或"help wanted"标签的issue，作为参与开发Starwhale的起点。

## 2. Starwhale资源列表

- [主页](http://starwhale.ai)
- [Starwhale Cloud](https://cloud.starwhale.cn)
- [官方文档](https://doc.starwhale.ai)
- [Github Repo](https://github.com/star-whale/starwhale)
- [Python Package](https://pypi.org/project/starwhale/)
- Docker镜像：[Docker Hub](https://hub.docker.com/u/starwhaleai)，[ghcr.io](https://github.com/orgs/star-whale/packages)
- [Helm Charts](https://artifacthub.io/packages/helm/starwhale/starwhale)

## 3. 代码基本结构

核心目录组织及功能说明如下：

- [client](https://github.com/star-whale/starwhale/tree/main/client)：swcli和Python SDK的实现，使用Python3编写，对应Starwhale Standalone Instance的所有功能。
  - [api](https://github.com/star-whale/starwhale/tree/main/client/starwhale/api)：Python SDK的接口定义和实现。
  - [cli](https://github.com/star-whale/starwhale/tree/main/client/starwhale/cli)：Command Line Interface的入口点。
  - [base](https://github.com/star-whale/starwhale/tree/main/client/starwhale/base)：Python 端的一些基础抽象。
  - [core](https://github.com/star-whale/starwhale/tree/main/client/starwhale/core)：Starwhale 核心概念的实现，包括Dataset、Model、Runtime、Project、Job、Evaluation等。
  - [utils](https://github.com/star-whale/starwhale/tree/main/client/starwhale/utils)：Python 端的一些工具函数。
- [console](https://github.com/star-whale/starwhale/tree/main/console)：前端的实现，使用React + TypeScript编写，对应Starwhale Cloud Instance的Web UI。
- [server](https://github.com/star-whale/starwhale/tree/main/server)：Starwhale Controller的实现，使用Java编写，对应Starwhale Cloud Instance的后端API。
- [docker](https://github.com/star-whale/starwhale/tree/main/docker)：Helm Charts，绝大多数Docker Image的Dockerfile等。
- [docs](https://github.com/star-whale/starwhale/tree/main/docs)：Starwhale官方文档。
- [example](https://github.com/star-whale/starwhale/tree/main/example)：示例程序，包含MNIST等例子。
- [scripts](https://github.com/star-whale/starwhale/tree/main/scripts)：一些Bash和Python脚本，用来进行E2E测试和软件发布等。

## 4. 搭建针对Standalone Instance的本地开发环境

Standalone Instance采用Python编写，当要修改Python SDK和swcli时，需要进行相应的环境搭建。

### 4.1 前置条件

- OS：Linux或macOS
- Python：3.7~3.10
- Docker：>=19.03 (非必须，当调试dockerize、生成docker image或采用docker为载体运行模型任务时需要)
- Python隔离环境：Python venv 或 virtualenv 或 conda等都可以，用来构建一个隔离的Python环境
- [Fork Starwhale Github Repo](https://github.com/star-whale/starwhale/fork)

### 4.2 从源码进行安装

clone 代码到本地：

```bash
git clone https://github.com/${your username}/starwhale.git
cd starwhale/client
```

使用Conda创建一个Starwhale开发环境，或者使用venv/virtualenv等创建：

```bash
conda create -n starwhale-dev python=3.8 -y
conda activate starwhale-dev
```

安装Client包及依赖到starwhale-dev环境中：

```bash
make install-sw
make install-dev-req
```

输入`swcli --version`命令，观察是否安装成功，开发环境的swcli版本是 `0.0.0.dev0` ：

```bash
❯ swcli --version
swcli, version 0.0.0.dev0

❯ swcli --version
/home/username/anaconda3/envs/starwhale-dev/bin/swcli
```

### 4.3 本地修改代码

现在可以对Starwhale代码进行修改，**不需要重复安装(`make install-sw`命令)就能在当前starwhale-dev环境是测试cli或sdk**。Starwhale Repo中设置了 [.editorconfig](https://github.com/star-whale/starwhale/blob/main/.editorconfig) 文件，大部分IDE或代码编辑器会自动支持该文件的导入，采用统一的缩进设置。

### 4.4 执行代码检查和测试

在 `starwhale` 目录中操作，会执行单元测试、client的e2e测试、mypy检查、flake8检查和isort检查等。

```console
make client-all-check
```

## 5. 搭建针对Cloud Instance的本地开发环境

Cloud Instance的后端采用Java编写，前端采用React+TypeScript编写，可以按需搭建相应的开发环境。
