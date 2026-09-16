# --- Étape 1 : Build ---
FROM node:18-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# URL de l'API utilisée par le navigateur (externalisée, surchargeable au build)
ARG NG_APP_API_URL=http://localhost:8089/gestiondestock
ENV NG_APP_API_URL=$NG_APP_API_URL
RUN sed -i "s|apiUrl: '[^']*'|apiUrl: '$NG_APP_API_URL'|" src/environments/environment.prod.ts \
 && npm run build -- --configuration production

# --- Étape 2 : Nginx ---
FROM nginx:alpine
COPY --from=build /app/dist/my-first-project /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
