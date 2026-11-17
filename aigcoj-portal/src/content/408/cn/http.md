# HTTP、DNS 与 Web 请求全流程

## 一、从输入 URL 到页面展示

1. 浏览器解析 URL，检查缓存。
2. DNS 解析域名 → IP。
3. 建立 TCP 连接（三次握手），HTTPS 还要 TLS 握手。
4. 发送 HTTP 请求。
5. 服务器处理并返回响应。
6. 浏览器解析 HTML，加载 CSS/JS/图片。
7. 渲染页面，关闭或复用连接。

## 二、DNS

- 递归查询：本机 DNS 服务器替你把活干完。
- 迭代查询：本地 DNS 依次询问根、顶级、权威服务器。
- 记录类型：A、AAAA、CNAME、MX、NS、TXT。
- 缓存：浏览器、系统、本地 DNS、权威服务器，按 TTL 过期。

## 三、HTTP 版本演进

| 版本 | 特点 |
| --- | --- |
| HTTP/1.0 | 短连接，每个请求一个 TCP |
| HTTP/1.1 | 持久连接、管线化、Host 头、分块传输 |
| HTTP/2 | 二进制分帧、多路复用、头部压缩、服务器推送 |
| HTTP/3 | 基于 QUIC/UDP，0-RTT，抗队头阻塞 |

## 四、常见状态码

- 1xx：信息性，100 Continue。
- 2xx：成功，200 OK、201 Created、204 No Content。
- 3xx：重定向，301 永久、302 临时、304 未修改。
- 4xx：客户端错误，400、401、403、404、429。
- 5xx：服务端错误，500、502、503、504。

## 五、HTTP 与 HTTPS

- HTTPS = HTTP + TLS，默认端口 443。
- TLS 握手：协商版本与套件、证书验证、密钥交换。
- 对称加密传数据，非对称加密传密钥，证书防中间人。
- HTTP/2 仍需 TCP，HTTP/3 改用 QUIC，减少握手延迟。

## 六、Cookie、Session、Token

- Cookie 存在客户端，自动随请求发送。
- Session 存在服务端，靠 SessionID 关联。
- Token（JWT）自包含，适合分布式与跨域。
- 安全属性：HttpOnly、Secure、SameSite。

## 七、常考计算

- 请求时间 = DNS + TCP + TLS + 请求/响应 + 渲染。
- 持久连接下，多个资源可复用 TCP，减少握手开销。
- 管道化仍有队头阻塞，HTTP/2 多路复用解决应用层队头阻塞。

> 计网大题常把 DNS、TCP、HTTP 串起来问「访问网页的完整过程」，按分层顺序作答最稳。
