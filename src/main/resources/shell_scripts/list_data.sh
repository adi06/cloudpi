#!/bin/bash

bucket_name=s3-cloudpi

list_keys=(`aws s3api list-objects --bucket ${bucket_name} --query 'Contents[].{Key: Key}' --output text`)
    #get objects
if [ ${#list_keys[@]} -ne 0 ]; then

    for key in "${list_keys[@]}"
    do
	if [ "$key" != "None" ]; then
        	aws s3api get-object --bucket ${bucket_name} --key $key /tmp/$key > /dev/null
        	echo $key `cat /tmp/$key`
	else
		echo "bucket is empty"
	fi
    done
else
echo "bucket is empty"
fi
