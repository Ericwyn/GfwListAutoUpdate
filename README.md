# GfwListAutoUpdate
基于[gfwlist](https://github.com/gfwlist/gfwlist)自动构建的，适用于Shadowsocks桌面版和极路由版本的加速列表，可以自己配置特定的加速网站

# 生成
## 设置自定义列表加速列表
在运行jar同目录下增加private.list文件，每一行代表一个加速网址，例如

    bitbucket.org
    speedtest.cn
    
## 生成
在GfwLists文件夹下其中
 - `gw-shadowsocks.dnslist` 是给极路由版本的Shadowsocks使用的
 - `proxy.pac` 是给桌面版Shadowsocks使用的