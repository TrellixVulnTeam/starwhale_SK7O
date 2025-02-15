---
title: 模型评测
---

## 1. starwhale.PipelineHandler

提供默认的模型评测过程定义，需要用户实现 `ppl` 和 `cmp` 函数。Github上的[代码链接](https://github.com/star-whale/starwhale/blob/dc6e6fdeae2f7c5bd0e72ccd8fb50768b1ce0826/client/starwhale/api/_impl/model.py)。

```python
from abc import ABCMeta, abstractmethod

class PipelineHandler(metaclass=ABCMeta):
    def __init__(self,
        ignore_annotations: bool = False,
        ignore_error: bool = False,
    ) -> None:
        ...

    @abstractmethod
    def ppl(self, data: Any, **kw: Any) -> Any:
        raise NotImplementedError

    @abstractmethod
    def cmp(self, ppl_result: PPLResultIterator) -> Any
        raise NotImplementedError
```

`PipelineHandler` 类实例化时可以定义两个参数：当`ignore_annotations`为False时，PPLResultIterator中会携带数据集所对应的 annotations信息，保证index上与推理结果是一一对应的；当 `ignore_error`为True是，会忽略ppl过程中的错误，可以解决比较大的数据集样本中，有个别数据错误导致ppl失败，进而导致无法完成评测的问题。

`ppl` 函数用来进行推理，输入参数为 data和kw。data表示数据集中某个样本，kw为一个字典，目前包含 `annotations` 和 `index`。每条数据集样本都会调用`ppl`函数，输出为模型推理值，会自动被记录和存储，可以在cmp函数中通过 `ppl_result` 参数获取。

`cmp` 函数一般用来进行推理结果的汇总，并产生最终的评测报告数据，只会调用一次。`cmp` 函数的参数为 `ppl_result` ，该值是 `PPLResultIterator` 类型，可以被迭代。迭代出来的对象为一个字典，包含 `result`, `annotations` 和 `data_id` 三个元素。`result` 为 `ppl` 返回的元素，由于使用了 pickle做序列化-反序列化，data["result"] 变量直接能获取ppl函数return的值；`annotations` 为构建数据集时写入的，此阶段的result["annotations"]为一个dict类型。`data_id` 表示数据集对应的index。

另外，在PipelineHandler及其子类中可以访问 `self.context` 获取 `starwhale.Context` 类型的上下文信息。

常见的使用方法示例如下：

```python

class Example(PipelineHandler):
    def __init__(self) -> None:
        super().__init__()
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.model = self._load_model(self.device)

    def ppl(self, img: Image, **kw):
        data_tensor = self._pre(img)
        output = self.model(data_tensor)
        return self._post(output)

    def cmp(self, ppl_result):
        result, label, pr = [], [], []
        for _data in ppl_result:
            label.append(_data["annotations"]["label"])
            result.extend(_data["result"][0])
            pr.extend(_data["result"][1])
        return label, result, pr

    def _pre(self, input: Image) -> torch.Tensor:
        ...

    def _post(self, input):
        ...

    def _load_model(self, device):
        ...
```

## 2. starwhale.Context

执行模型评测过程中传入的上下文信息，包括Project、Task ID等。Github上的[代码链接](https://github.com/star-whale/starwhale/blob/dc6e6fdeae2f7c5bd0e72ccd8fb50768b1ce0826/client/starwhale/api/_impl/job.py)。Context的内容是自动注入的，用户通过 `@pass_context` 使用context，或在 继承 `PipelineHandler` 类内使用，目前Context可以获得如下值：

```python

@pass_context
def func(context: Context):
    ...
    print(context.project)
    print(context.version)
    print(context.step)
    ...

Context(
    workdir: Path,
    step: str = "",
    total: int = 1,
    index: int = 0,
    dataset_uris: t.List[str] = [],
    version: str = "",
    project: str = "",
)
```

|参数|说明|
|---|----|
|project|project名字|
|version|Evaluation 版本号|
|step|step名字|
|total|step下所有的task数量|
|index|当前task的索引编号，从零开始|
|dataset_uris|dataset uri字符串的列表|
|workdir|model.yaml所在目录|

## 3. starwhale.PPLResultStorage

`ppl`函数中使用，能够保存 `ppl` 结果、数据集index和对应的数据集annotations。Github上的[代码链接](https://github.com/star-whale/starwhale/blob/dc6e6fdeae2f7c5bd0e72ccd8fb50768b1ce0826/client/starwhale/api/_impl/model.py)。

| 函数   | 说明                                                                                                              |
|------|-----------------------------------------------------------------------------------------------------------------|
| save | data_id: t.Union[int, str] 数据唯一索引值<br/> result: t.Any 专指ppl过程的评测结果值<br/> **kwargs: t.Any 其他待存储的信息，如annotations等 |

使用例子如下：

```python
from starwhale import pass_context, Context, PPLResultStorage

@pass_context
def func(context: Context) -> None:
    ppl_result_storage = PPLResultStorage(context)
    for _idx, _data, _annotations in _dataloader:
        pred_value = eval_process(_data, ...)
        ppl_result_storage.save(
            data_id=_idx,
            result=pred_value,
            ...
            annotations=_annotations,
        )
```

## 4. starwhale.PPLResultIterator

`cmp`函数中使用，是一个可迭代的对象，能够输出 `ppl` 结果、数据集index和对应的数据集annotations。Github上的[代码链接](https://github.com/star-whale/starwhale/blob/dc6e6fdeae2f7c5bd0e72ccd8fb50768b1ce0826/client/starwhale/api/_impl/model.py)。

```python
from starwhale import PipelineHandler, PPLResultIterator

class Example(PipelineHandler):
    def cmp(
        self, ppl_result: PPLResultIterator
    ) -> t.Tuple[t.List[int], t.List[int], t.List[t.List[float]]]:
        result, label, pr = [], [], []
        for _data in ppl_result:
            label.append(_data["annotations"]["label"])
            result.extend(_data["result"][0])
            pr.extend(_data["result"][1])
            print(_data["data_id"])
        return label, result, pr

```

## 5. starwhale.multi_classification

修饰器，适用于多分类问题，用来简化cmp结果的进一步计算和结果存储，能更好的呈现评测结果。Github上的[代码链接](https://github.com/star-whale/starwhale/blob/dc6e6fdeae2f7c5bd0e72ccd8fb50768b1ce0826/client/starwhale/api/_impl/metric.py)。

```python

@multi_classification(
    confusion_matrix_normalize="all",
    show_hamming_loss=True,
    show_cohen_kappa_score=True,
    show_roc_auc=True,
    all_labels=[i for i in range(0, 10)],
)
def cmp(ppl_result: PPLResultIterator) -> t.Tuple[t.List[int], t.List[int], t.List[t.List[float]]]:
    label, result, probability_matrix = [], [], []
    return label, result, probability_matrix

@multi_classification(
    confusion_matrix_normalize="all",
    show_hamming_loss=True,
    show_cohen_kappa_score=True,
    show_roc_auc=False,
    all_labels=[i for i in range(0, 10)],
)
def cmp(ppl_result: PPLResultIterator) -> t.Tuple[t.List[int], t.List[int], t.List[t.List[float]]]:
    label, result = [], [], []
    return label, result
```

|参数|说明|
|---|----|
|`confusion_matrix_normalize`| `true`(rows), `pred`(columns) 或 `all`(rows+columns) |
|`show_hamming_loss`|是否计算hamming loss|
|`show_cohen_kappa_score`|是否计算 cohen kappa score|
|`show_roc_auc`|是否计算roc/auc, 计算的时候，需要函数返回(label，result, probability_matrix) 三元组，否则只需返回(label, result) 两元组即可|
|all_labels|所有的labels|

`multi_classification` 修饰器使用sklearn lib对多分类问题进行结果分析，输出confusion matrix, roc, auc等值，并且会写入到 starwhale的 DataStore 中。使用的时候需要对所修饰的函数返回值有一定要求，返回(label, result, probability_matrix) 或 (label, result)。

## 6. starwhale.step

修饰器，可以指定DAG的依赖关系和Task数量、资源等配置，实现用户自定义评测过程。Github上的[代码链接](https://github.com/star-whale/starwhale/blob/dc6e6fdeae2f7c5bd0e72ccd8fb50768b1ce0826/client/starwhale/api/_impl/job.py)。使用 `step` 可以完全不依赖于 `PipelineHandler` 预定义的基本模型评测过程，可以自行定义多阶段和每个阶段的依赖、资源和任务并发数等。

```python
@step(
    resources: Optional[t.Dict[str, Any]] = None,
    concurrency: int = 1,
    task_num: int = 1,
    needs: Optional[List[str]] = None,
)
def func():
    ...

```

|参数|说明|
|---|----|
|`resources`|该step中每个task所依赖的资源情况|
|`concurrency`|task执行的并发度|
|`task_num`|step会被分成task的数量|
|`needs`|依赖的step列表|

`resources` 格式为：

- 简化表达方式：代表request和limit同时设置，且值相同。

  ```python
  {
    {名称}:{数量},
    ...
  }
  ```

- 完全表达方式：代表分别对limit和request进行设置。

  ```python
  {
    {名称}:{"request": {数量},"limit": {数量}},
    ...
  }
  ```

其中，名称为资源的种类，目前支持 `cpu`、`gpu` 和 `memory`。当种类为 `cpu` 时，数量的类型为float, 没有单位，1表示1个cpu core，对应Kubernetes resource的request和limit；当种类为 `gpu` 时，数量的类型为int，没有单位，1表示1个gpu，对应Kubernetes resource的request和limit；当种类为 `memory`时，数量的类型为float，没有单位，1表示1MB内存，对应Kubernetes resource的request和limit。`resources` 使用列表的方式支持指定多个资源，且这些资源都满足时才会进行调度。当不写 `resources` 时，会使用所在Kubernetes的cpu、memory默认值。 `resources` 表示的是一个task执行时所需要的资源情况，并不是step所有task的资源总和限制。**目前 `resources` 只在Cloud Instance中生效**。 `resources` 使用例子如下：

```python
@step()
@step(resources={"cpu":1})
@step(resources={"gpu":1})
@step(resources={"memory":100})
@step(resources={"cpu": 0.1, "gpu": 1, "memory": 100})
@step(resources={"cpu": {"request": 0.1, "limit": 0.2}, "gpu": {"request": 1, "limit": 1}, "memory": {"request": 100, "limit": 200}})
```
