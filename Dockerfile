# Using OpenJDK11
FROM adoptopenjdk/openjdk11:alpine
RUN adduser -h /app -D exec

ADD ./build/install/CandyBot /app/binaries
VOLUME /app/data

# Permission Management
RUN chown -R exec:exec /app/*
RUN chmod -R 777 /app/*
USER exec
WORKDIR /app

# GO
ENTRYPOINT /app/binaries/bin/CandyBot

RUN ls -alhXR . && echo \## END
