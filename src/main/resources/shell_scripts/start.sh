#!/bin/bash

elastic_ip=52.41.222.166
image_id=ami-c527a9a5
security_groups=sg_cloudpi
instance_type=t2.micro
key_name=kp_cloudpi

instanceId=`aws ec2 describe-addresses --query 'Addresses[*].InstanceId' --output text`
if [ -z ${instanceId} ];
then
#create web tier instance
echo "creating web tier instance.."
instanceId=`aws ec2 run-instances --image-id ${image_id} \
                                  --security-groups ${security_groups} \
                                  --count 1 \
                                  --instance-type ${instance_type} \
                                  --key-name ${key_name} \
                                  --query 'Instances[0].InstanceId' \
                                  --output text`
echo "waiting for instance state to be running.."
while true
do
        instance_state=`aws ec2 describe-instance-status --instance-id ${instanceId} \
                                                        --query 'InstanceStatuses[*].InstanceState.Name' \
                                                        --output text`
        if [ "${instance_state}" = "running" ];
        then
                break
        fi
done
#associate elastic ip
aws ec2 associate-address --instance-id ${instanceId}  --public-ip ${elastic_ip} --allow-reassociation > /dev/null
else
echo "starting instance ${instanceId} "
aws ec2 start-instances --instance-ids ${instanceId} > /dev/null
fi
echo "waiting for the service in server to be up.."
sleep 60 
#while true
#do
#	instance_status=`aws ec2 describe-instance-status --instance-id ${instanceId} --query 'InstanceStatuses[*].InstanceStatus.Status' --output text`
#	if [ "${instance_status}" = "ok" ];
#	then
#		break
#	fi
#done
echo "Service url : "
echo "http://${elastic_ip}/cloudpi?input=<value>"
echo "Note: please try after 30 seconds if the service call is not reachable"
