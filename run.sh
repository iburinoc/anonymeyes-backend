#!/bin/bash

java -cp /home/ubuntu/anonymeyes-backend/bin/:/home/ubuntu/anonymeyes-backend/lib/xuggle-xuggler-5.4.jar:/home/ubuntu/anonymeyes-backend/lib/slf4j-api-1.7.7.jar:/home/ubuntu/anonymeyes-backend/lib/slf4j-simple-1.7.7.jar com.topsradiance.anonymeyes.backend.Server ~/anonymeyes-rails/public/recorded-videos/
