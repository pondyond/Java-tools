杀掉名字包含nginx的进程
ps aux|grep nginx |grep -v grep|awk '{print $2}'|xargs kill -9
