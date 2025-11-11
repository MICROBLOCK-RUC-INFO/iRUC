#!/bin/bash

# 获取运行中的 Node.js 进程ID
PIDS=$(ps aux | grep node | grep -v grep | awk '{print $2}')

if [ -z "$PIDS" ]; then
  echo "No Node.js processes found to stop."
else
  echo "Stopping Node.js services..."
  kill $PIDS
  echo "All Node.js services have been stopped."
fi
