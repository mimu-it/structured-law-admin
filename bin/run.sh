#!/usr/bin/env bash
echo "[信息] 启动法派法库web"

APP_NAME=ruoyi-admin.jar  # 定义JAVA程序名

cd $(pwd)


COMMAND="$1"

if [[ "$COMMAND" != "start" ]] && [[ "$COMMAND" != "stop" ]] && [[ "$COMMAND" != "restart" ]]; then
	echo "Usage: $0 start | stop | restart"
	exit 0
fi


# Java 命令行参数，根据需要开启下面的配置，改成自己需要的，注意等号前后不能有空格
# JAVA_OPTS=-Xms256m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m
JAVA_OPTS="-XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m -Xms1024m -Xmx1024m -Xmn256m -Xss256k -XX:SurvivorRatio=8 -XX:+UseConcMarkSweepGC"

function start()
{
    # 运行为后台进程，并在控制台输出信息， 退出终端不会中断程序，ctrl+c 会中断
    nohup java -jar ${JAVA_OPTS} ${APP_NAME}
}

function stop()
{
	# 查询进程并终止
	PID=`ps -ef | grep $APP_NAME | grep -v grep | awk '{print $2}'`
	echo "$APP_NAME 的进程 $PID 已存在"
	if [[ -n "$PID" ]]; then
	    kill -9 $PID
		echo "$APP_NAME 的进程 $PID 已经终止"
	fi
}

if [[ "$COMMAND" == "start" ]]; then
	start
elif [[ "$COMMAND" == "stop" ]]; then
    stop
else
    stop
    start
fi