FROM node:22-alpine

WORKDIR /app

COPY package*.json /app

RUN npm ci

COPY . /app

EXPOSE 4200

CMD ["npm", "start"]
