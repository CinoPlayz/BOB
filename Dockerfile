# NodeJS base image
FROM node:20

# Declaring env
ENV NODE_ENV development

# Working directory in the container
WORKDIR /BOB-ZPBackend

# Prepare for npm install
COPY package*.json ./

# Installing dependencies
RUN npm install

# Copy the rest
COPY . .

# Command to run the application
CMD ["sh", "-c", "node ./bin/www $DBURL"]

# Exposing server port
EXPOSE 3001

# NodeJS Run Syntax: node ./bin/www [MongoDBURL]
# Example: node ./bin/www mongodb://127.0.0.1/ZP

# Build
# docker build -f Dockerfile -t bob_zpbackend:0.1 .

# Run
# docker run -d -p 3001:3001 -e DBURL="MongoDBAtlasURL" --name BOB_ZPBackend bob_zpbackend:0.1
