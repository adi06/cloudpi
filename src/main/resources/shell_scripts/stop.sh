#!/bin/bash

echo "stopping the web tier.."
instanceId=`aws ec2 describe-addresses --query 'Addresses[*].InstanceId' --output text`
echo "instance id: $instanceId"
if [ ! -z ${instanceId} ]; then
aws ec2 stop-instances --instance-ids ${instanceId} > /dev/null
sleep 60 
else
echo "Something went wrong could not find instanceId" 
fi
