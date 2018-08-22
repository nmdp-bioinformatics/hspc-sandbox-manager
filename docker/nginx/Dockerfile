# Set nginx base image
FROM nginx

# File Author / Maintainer
MAINTAINER Travis Cummings

# Copy custom configuration file from the current directory
COPY nginx.conf /etc/nginx
RUN mkdir /etc/nginx/sites-available
RUN chmod 777 /etc/nginx/sites-available
RUN mkdir /etc/nginx/sites-enabled

# sandman
COPY sandman.conf /etc/nginx/sites-available
RUN ln -sf /etc/nginx/sites-available/sandman.conf /etc/nginx/sites-enabled/sandman.conf
EXPOSE 8080
