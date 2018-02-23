FROM node:alpine

COPY . .
ENV ACTIVE_ENV="test"
RUN sed -i -e "s/replacethiswithcurrentenvironment/$ACTIVE_ENV/g" src/static/js/services.js
RUN npm install
CMD [ "npm", "run", "serve" ]