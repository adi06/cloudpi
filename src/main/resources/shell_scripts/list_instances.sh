#!/bin/bash

# Reference http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/US_SingleMetricPerInstance.html

start=`date -u --date='-30minutes' +%Y-%m-%dT%T`
end=`date -u +%Y-%m-%dT%T`
# describe running instances
out=`aws ec2 describe-instances --filters "Name=instance-type,Values=t2.micro,Name=instance-state-name,Values=running,pending" \
     							--query 'Reservations[*].Instances[*].InstanceId' \
     							--output text`

instanceIds=($out)
# get maximum cpu utilization
	for instanceId in "${instanceIds[@]}"
	do
		res=`aws cloudwatch get-metric-statistics --metric-name CPUUtilization \
                                     --start-time ${start} \
                                     --end-time ${end}\
                                     --period 300 \
                                     --namespace AWS/EC2 \
                                     --statistics Maximum \
                                     --dimensions Name=InstanceId,Value=${instanceId} \
				     --query 'Datapoints[0].Maximum' \
                                     --output text` 
		echo $instanceId $res
	done
