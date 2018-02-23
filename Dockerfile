FROM node:alpine

COPY . .
ENV ACTIVE_ENV="test"
RUN npm install
CMD [ "npm", "run", "serve" ]