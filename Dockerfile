FROM node:alpine

COPY . .
ENV TARGET_ENV="test"
RUN npm install
CMD [ "npm", "run", "serve" ]