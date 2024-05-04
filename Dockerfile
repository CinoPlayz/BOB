####################################################################################################
## Builder
####################################################################################################

# NodeJS builder image
FROM node:22.1 AS builder

# Declaring env
ENV NODE_ENV production
ENV DBURL=$DBURL

# Working directory in the container
WORKDIR /BOB-ZPBackend

# Prepare for npm install
COPY package*.json ./

# Copy the rest
COPY . .

# Installing dependencies
RUN npm install --production

####################################################################################################
## Final image
####################################################################################################

# NodeJS Distroless base image
FROM gcr.io/distroless/nodejs22-debian12

#Copy from builder folders BOB-ZPBackend to BOB-ZPBackend on base image
COPY --from=builder /BOB-ZPBackend /BOB-ZPBackend

#Changes working dir
WORKDIR /BOB-ZPBackend

#Switches users
USER nonroot

# Exposing server port
EXPOSE 3001

# Command to run the application (copied from builder image)
CMD ["./bin/www"]

# NodeJS Run Syntax: node ./bin/www [MongoDBURI]
# Example: node ./bin/www mongodb://127.0.0.1/ZP

# Build
# docker build -f Dockerfile -t bob_zpbackend:0.1 .

# Run
# docker run -d -p 3001:3001 -e DBURI="MongoDBAtlasURL" --name BOB_ZPBackend bob_zpbackend:0.1
