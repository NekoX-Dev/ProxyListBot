#!/usr/bin/env bash

rm -rf cache
mkdir cache
mv proxy_list_output cache/proxy_list

cd cache
git init

git config user.name "世界"
git config user.email "i@nekox.me"
git config credential.helper store
git config user.signingkey "DA55ACBFDDA40853BFC5496ECD109927C34A63C4"
git config commit.gpgsign true

export GIT_SSH_COMMAND="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no"

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