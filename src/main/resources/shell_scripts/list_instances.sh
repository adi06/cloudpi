#!/bin/bash

# Reference http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/US_SingleMetricPerInstance.html
# TODO error handling

start=`date --date 'now - 5 minutes' +%Y-%m-%dT%T`
end=`date --date 'now' +%Y-%m-%dT%T`

# describe running instances
out=`aws ec2 describe-instances --filters "Name=instance-type,Values=t2.micro,Name=instance-state-name,Values=running,pending" \
     							--query 'Reservations[*].Instances[*].InstanceId' \
     							--output text`
status=`echo $?`
if [ $status != 0 ];
then
        exit $status
fi

instanceIds=($out)
# get maximum cpu utilization
for instanceId in "${instanceIds[@]}"
do
        res=`aws cloudwatch get-metric-statistics --metric-name CPUUtilization \
                                     --start-time ${start} \
                                     --end-time ${end}\
                                     --period 60 \
                                     --namespace AWS/EC2 \
                                     --statistics Maximum \
                                     --dimensions Name=InstanceId,Value=${instanceId} \
                                     --query 'Datapoints[*].Maximum' \
                                     --output text`
        echo $instanceId $res
done