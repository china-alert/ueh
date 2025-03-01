#!/usr/bin/env bash
  
# 配置文件名称
# (该配置文件放置在jar包同级目录下并且必须存在已经配置文件名称具备统一性！！！请根据实际的配置文件名称进行修改)

# 启动一个目录下的所有jar包
function read_dir(){
for file in `ls $1`
do
  #如果当前文件是文件夹则递归处理
  if [ -d $1"/"$file ];
  then
    read_dir $1"/"$file
  else
    # 当前文件不是一个文件夹
    if [[ -f $1"/"$file && `echo "$1/$file"|grep '/lib/'|wc -l` -eq 0 && `echo "$1/$file"|grep '/bak/'|wc -l` -eq 0 ]];
    then
        # 如果当前文件是一个.jar结尾的文件则启动它
        if [[ ${file:0-4} == '.jar' ]];
        then
          echo $1/$file 开始启动...
          cd $1
           nohup java -jar $file > /dev/null 2>&1 &
            echo $1"/"$file  启动成功!
            echo ""
            cd - > /dev/null
        fi
    fi
  fi
done
}
#读取第一个参数
read_dir $1

