#! /bin/bash

function get_json_value()
{
  local json=$1
  local key=$2

  if [[ -z "$3" ]]; then
    local num=1
  else
    local num=$3
  fi

  local value=$(echo "${json}" | awk -F"[,:}]" '{for(i=1;i<=NF;i++){if($i~/'${key}'\042/){print $(i+1)}}}' | tr -d '"' | sed -n ${num}p)

   echo ${value}
}

echo '请输入您要监控的网址'
json=''
read json

data=$(get_json_value ${json} "url")

echo "json:$json, data:$data"
