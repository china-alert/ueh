#!/usr/bin/env bash
# 查看某个目录下所有jar程序的状态
function read_dir(){
for file in `ls $1`
do
  #如果当前文件是文件夹则递归处理
  if [ -d $1"/"$file ]
  then
    read_dir $1"/"$file
  else
    # 当前文件不是一个文件夹
    if [[ -f $1"/"$file && `echo "$1/$file"|grep '/lib/'|wc -l` -eq 0 ]]
    then
        if [[ ${file:0-4} == '.jar' ]];
        then
            # 获取pid
                pid=`ps -ef | grep $file | grep -v grep | awk '{print $2}'`
            # -z 表示如果$pid为空时则输出提示
                if [ -z $pid ];then
                        echo ""
		echo "Service $file is not running! It's not necessary to stop it!"
                        echo ""
                else
                        echo ""
		kill -9 $pid
                echo "Service stop successfully！pid:${pid} which has been killed forcibly!"
                        echo ""
                fi
        fi
    fi
  fi
done
}
#读取第一个参数
read_dir $1



