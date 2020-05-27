#!/usr/bin/env bash

rm -rf cache
mkdir cache
cp proxy_list_output.json cache/proxy_list.json

cd cache
git init

git remote add github git@github.com:NekoX-Dev/ProxyList.git
git remote add gitlab git@gitlab.com:NekohaSekai/nekox-proxy-list.git
git remote add gitee git@gitee.com:nekoshizuku/AwesomeRepo.git

git add . --all
git commit -m "喵"

git push github master -f
git push gitlab master -f
git push gitee master -f

cd ..
rm -rf cache