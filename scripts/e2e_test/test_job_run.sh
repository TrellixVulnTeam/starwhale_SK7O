#!/usr/bin/env bash

set -e

if [[ ! -z ${DEBUG} ]]; then
    set -x
fi

curl -D - --location --request POST "http://$1/api/v1/login" \
--header 'Accept: application/json' \
--form 'userName="starwhale"' \
--form 'userPwd="abcd1234"' -o /dev/null | while read line
do
  if [[ "${line}" =~ ^Authorization.* ]] ; then
    echo "${line}" > auth_header.h
  fi
done

auth_header=`cat auth_header.h`
sudo apt-get install jq
job_id=`curl -X 'POST' \
  "http://$1/api/v1/project/1/job" \
  -H 'accept: */*' \
  -H "$auth_header" \
  -H 'Content-Type: application/json' \
  -d '{
  "modelVersionUrl": "1",
  "datasetVersionUrls": "1",
  "runtimeVersionUrl": "1",
  "device": "1",
  "deviceAmount": 0.4,
  "comment": "string"
}' | jq -r '.data'`

if [ "$job_id" == "null" ] ; then
  echo "Error! job id is null"  1>&2
  exit 1
fi

while true
do
  if curl -X 'GET' \
    "http://$1/api/v1/project/1/job/$job_id" \
    -H 'accept: application/json' \
    -H "$auth_header" | jq -r '.data.jobStatus' > jobStatus ; then echo "8082 well"; else  kubectl logs --tail=10 -l starwhale.ai/role=controller -n $SWNS; continue; fi
  job_status=`cat jobStatus`
  if [ "$job_status" == "null" ] ; then
    echo "Error! job_status id is null"  1>&2
    exit 1
  fi
  if [[ "$job_status" = "SUCCESS" ]] ; then
      echo "job success done"
      break
  elif [[ "$job_status" = "FAIL" ]] ; then
        echo "job FAIL"
        break
  elif [[ -z "$job_status" ]] ; then
    if kill -9 `ps -ef|grep port-forward | grep -v grep | awk '{print $2}'` ; then echo "kill success"; fi
    nohup kubectl port-forward --namespace $SWNS svc/$SWNAME-controller 8082:8082 &
    sleep 20
  else
    echo "job status for " "$job_id" "is" "$job_status"
#    kubectl logs --tail=10 -l job-name=1 -n starwhale -c data-provider
#    kubectl logs --tail=10 -l job-name=1 -n starwhale -c untar
#    kubectl logs --tail=10 -l job-name=1 -n starwhale -c worker
#    kubectl logs --tail=10 -l job-name=1 -n starwhale -c result-uploader

#    kubectl logs -f -l starwhale.ai/role=controller -n starwhale
#    kubectl describe pod -l "job-name in (1,2,3,4,5,6,7,8,9,10)" -n starwhale
#    kubectl describe node
    sleep 10
  fi
done

task_id=`curl -X 'GET' \
  "http://$1/api/v1/project/1/job/$job_id/task?pageNum=1&pageSize=10" \
  -H 'accept: application/json' \
  -H "$auth_header" | jq -r '.data.list[1].id'`

echo $task_id
curl -X 'GET'  "http://$1/api/v1/log/offline/$task_id"   -H 'accept: application/json'   -H "$auth_header" | jq -r '.data[0]'
log_file=`curl -X 'GET'  "http://$1/api/v1/log/offline/$task_id"   -H 'accept: application/json'   -H "$auth_header" | jq -r '.data[0]'`
echo $log_file

echo "task log is:"

curl -X 'GET' \
  "http://$1/api/v1/log/offline/$task_id/$log_file" \
  -H 'accept: plain/text' \
  -H "$auth_header"

#echo "agent log is:"
#docker logs compose_agent_1

job_status=`cat jobStatus`
if [[ "$job_status" == "FAIL" ]] ; then
  exit 1
fi

