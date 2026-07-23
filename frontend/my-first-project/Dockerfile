# --- Étape 1 : Build ---
FROM node:18-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration production

# --- Étape 2 : Nginx ---
FROM nginx:alpine
COPY --from=build /app/dist/my-first-project /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
