FROM node:14 as builder

# Copy source files
WORKDIR /usr/src/app
COPY . /usr/src/app 

# Install dependencies
RUN npm install

# Build prod - output path should be specified orelse directory not found error will thrown during build.
RUN npm run build
FROM nginx:alpine

# Expose port
EXPOSE 7000 443

# Clean nginx
RUN rm -rf /usr/share/nginx/html/*

# Copy dist
COPY --from=builder /usr/src/app/dist/qs-ui /usr/share/nginx/html
COPY /nginx-config/nginx.conf /etc/nginx/nginx.conf

WORKDIR /usr/share/nginx/html

# Permission - orelse we cannot access application.
RUN chown root /usr/share/nginx/html/*
RUN chmod 755 /usr/share/nginx/html/*
RUN chmod +x /usr/share/nginx/html/config.sh

ARG API_URL
ARG DASHBOARD_URL

RUN ./config.sh $API_URL $DASHBOARD_URL

# Start
CMD ["nginx", "-g", "daemon off;"]

