FROM ubuntu:18.04

RUN apt-get update

RUN apt-get install -y wget

#Java Setup
RUN apt-get update --fix-missing
RUN apt-get install -y default-jdk

#Scala setup
RUN apt-get remove scala-library scala
RUN wget http://scala-lang.org/files/archive/scala-2.12.6.deb
RUN dpkg -i scala-2.12.6.deb
RUN apt-get update
RUN apt-get install -y scala

RUN apt-get install -y gnupg2
RUN echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
RUN apt-get update
RUN apt-get install -y sbt

#Maven Setup
RUN apt-get install -y maven


# Set the home directory to /root and cd into that directory
ENV HOME /root
WORKDIR /root


# Copy all app files into the image
COPY . .

# Download dependancies and build the app
RUN mvn package


# Allow port 8080 to be accessed from outside the container
EXPOSE 8080

ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.2.1/wait /wait
RUN chmod +x /wait

# Run the app
CMD /wait && java -jar target/office-hours-0.0.1-jar-with-dependencies.jar
