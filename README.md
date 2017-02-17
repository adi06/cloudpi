# cloudpi

## Description
This application is CloudPi, which uses cloud resources to compute the digits of Pi. It follows the typical three-tier architecture
* The web tier serves the requests for computing Pi.
* The application tier performs the computation.
* The data tier stores the computation result.

The application tier needs to automatically scales up and down based on demand of the requests.

## Application Stack
* Spring boot
* Amazon Java SDK
* Amazon S3
* Amazon Simple Queue Service
