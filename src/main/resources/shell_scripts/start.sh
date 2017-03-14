#!/bin/bash

elastic_ip=52.41.222.166
image_id=ami-55ef6335
security_groups=sg_cloudpi
instance_type=t2.micro
key_name=kp_cloudpi


#create web tier instance
instanceId=`aws ec2 run-instances --image-id ${image_id} \
                                  --security-groups ${security_groups} \
                                  --count 1 \
                                  --instance-type ${instance_type} \
                                  --key-name ${key_name} \
                                  --query 'Instances[0].InstanceId' \
                                  --output text`
#instance state
while true
do
        instance_state=`aws ec2 describe-instance-status --instance-id ${instanceId} \
                                                        --query 'InstanceStatuses[*].InstanceState.Name' \
                                                        --output text`
        echo "instance state: $instance_state"
        if [ "${instance_state}" = "running" ];
        then
                break
        fi
echo "getting instance state"
done
#associate elastic ip
aws ec2 associate-address --instance-id ${instanceId}  --public-ip ${elastic_ip} --allow-reassociation
echo "http://${elastic_ip}/cloudpi?input=<value>"