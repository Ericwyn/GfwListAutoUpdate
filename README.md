# GfwListAutoUpdate
基于[gfwlist](https://github.com/gfwlist/gfwlist)自动构建的，适用于Shadowsocks桌面版和极路由版本的加速列表，可以自己配置特定的加速网站

# 使用
## 前提条件之一
 - 能够Shadowsocks桌面版的系统
 - 安装了Shadowsocks的极路由，安装教程见[Hiwifi-ss](https://github.com/qiwihui/hiwifi-ss)，**注意，用此方法外安装极路由上的Shadowsocks可能并不适用于本加速列表**

## 自定义加速列表
在运行jar同目录下增加private.list文件，每一行代表一个加速网址，例如

    bitbucket.org
    speedtest.cn

## 生成
在GfwLists文件夹下其中

 - `gw-shadowsocks.dnslist` 是给极路由版本的Shadowsocks使用的
 - `proxy.pac` 是给桌面版Shadowsocks使用的

## 直接下载自动构建版本
可直接通过一下网址下载每天自动构建的列表，文件的开头会显示自动构建的时间
注意：自动构建将在gfwlist外添加以下的网址作为附加的加速列表
    
    bitbucket.org
    speedtest.cn
    
 - 供极路由使用的加速列表——gw-shadowsocks.dnslist
    - [http://www.burano.tk/GfwLists/gw-shadowsocks.dnslist](http://www.burano.tk/GfwLists/gw-shadowsocks.dnslist) 
    - 可自己ssh进入极路由后台，而后使用以下命令进行更新
    
            cd /etc/gw-redsocks/gw-shadowsocks && mv gw-shadowsocks.dnslist gw-shadowsocks.dnslist.bac && wget http://www.burano.tk/GfwLists/gw-shadowsocks.dnslist && /etc/init.d/dnsmasq restart
 
 - 供桌面版Shadowsocks使用的加速列表——proxy.pac
    - [http://www.burano.tk/GfwLists/proxy.pac](http://www.burano.tk/GfwLists/proxy.pac)
 
