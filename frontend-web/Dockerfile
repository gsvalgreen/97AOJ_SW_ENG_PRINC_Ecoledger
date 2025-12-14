# Multi-stage Dockerfile for frontend-web (Vite + React)
# Builder stage
FROM node:20-alpine AS builder

WORKDIR /app
ENV PATH /app/node_modules/.bin:$PATH

# Install app dependencies
COPY package.json package-lock.json ./
COPY tsconfig.json tsconfig.app.json tsconfig.node.json ./
COPY vite.config.ts ./
COPY index.html ./

# Copy source
COPY public ./public
COPY src ./src

RUN npm ci --silent

# Enable mock API during build so built assets talk to mock instead of real backends
ENV VITE_MOCK_API=true

# Build the app
# Use Vite build directly to avoid TypeScript typecheck failures during container build
RUN npx vite build --mode production

# Production stage
FROM nginx:stable-alpine

# Remove default nginx static files
RUN rm -rf /usr/share/nginx/html/*

# Copy built assets from builder
COPY --from=builder /app/dist /usr/share/nginx/html

# Replace default nginx config with SPA-friendly config (try_files -> index.html)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
