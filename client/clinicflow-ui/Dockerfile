FROM node:22-alpine AS build
WORKDIR /app

ARG VITE_API_BASE_URL=/api
ARG VITE_CLINIC_SLUG=demo

ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_CLINIC_SLUG=${VITE_CLINIC_SLUG}

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:1.27-alpine
WORKDIR /usr/share/nginx/html

COPY nginx.conf /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist ./

EXPOSE 80
