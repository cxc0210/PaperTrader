# Paper Trader 项目规范

本文件记录项目约定，后续所有代码改动都遵守。

## 1. DTO 命名

- 请求 DTO：以 `Req` 结尾。例：`CreateUserReq`、`ChangePasswordReq`
- 响应 DTO：以 `Res` 结尾。例：`UserRes`
- 接口不直接返回 Entity，统一返回对应的 `Res` DTO，按需控制暴露哪些字段。

## 2. Controller 请求方法

- 除 `HelloController` 外，所有 Controller **只接受 POST 请求**。
- 原来用 GET 的接口（列表、单个查询）改为 POST，body 传空，参数放在路径里（`@PathVariable`）。
- 增、删、改也统一用 POST，用路径区分动作。

示例（`@RequestMapping("/user")`）：

| 动作 | 路径 | body |
|---|---|---|
| 列表 | `POST /user/list` | 空 |
| 单个 | `POST /user/{id}` | 空 |
| 创建 | `POST /user/create` | `CreateUserReq` |
| 删除 | `POST /user/{id}/delete` | 空 |
| 改密码 | `POST /user/{id}/password` | `ChangePasswordReq` |

## 3. 统一返回格式

所有接口返回统一结构 `Result<T>`（`common` 包）：

```json
{ "code": 0, "message": "", "data": null }
```

- `code = 0` 表示成功；非 0 为错误码，错误信息放 `message`。
- 成功时 `message` 为空字符串 `""`。
- 有数据放 `data`，无数据 `data` 为 `null`。
- HTTP 状态码统一 200，业务状态只看 `code`。
- 成功用 `Result.ok(data)` / `Result.ok()`；失败由 `GlobalExceptionHandler` 统一转成 `Result.error(code, message)`。
