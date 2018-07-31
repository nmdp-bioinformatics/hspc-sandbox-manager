# Set nginx base image
FROM nginx

# File Author / Maintainer
MAINTAINER Travis Cummings

# Copy custom configuration file from the current directory
COPY nginx.conf /etc/nginx
RUN mkdir /etc/nginx/sites-available
RUN chmod 777 /etc/nginx/sites-available
RUN mkdir /etc/nginx/sites-enabled

# auth
COPY auth.conf /etc/nginx/sites-available
RUN ln -sf /etc/nginx/sites-available/auth.conf /etc/nginx/sites-enabled/auth.conf
EXPOSE 8060

# auth-debug
COPY auth-debug.conf /etc/nginx/sites-available
RUN ln -sf /etc/nginx/sites-available/auth-debug.conf /etc/nginx/sites-enabled/auth-debug.conf
EXPOSE 5060

# api-dstu2
COPY api-dstu2.conf /etc/nginx/sites-available
RUN ln -sf /etc/nginx/sites-available/api-dstu2.conf /etc/nginx/sites-enabled/api-dstu2.conf
EXPOSE 8075

# api-dstu2-debug
COPY api-dstu2-debug.conf /etc/nginx/sites-available
RUN ln -sf /etc/nginx/sites-available/api-dstu2-debug.conf /etc/nginx/sites-enabled/api-dstu2-debug.conf
EXPOSE 5075

# api-stu3
COPY api-stu3.conf /etc/nginx/sites-available
RUN ln -sf /etc/nginx/sites-available/api-stu3.conf /etc/nginx/sites-enabled/api-stu3.conf
EXPOSE 8076

# api-stu3-debug
COPY api-stu3-debug.conf /etc/nginx/sites-available
RUN ln -sf /etc/nginx/sites-available/api-stu3-debug.conf /etc/nginx/sites-enabled/api-stu3-debug.conf
EXPOSE 5076

# sandman-api
COPY sandman-api.conf /etc/nginx/sites-available
RUN ln -sf /etc/nginx/sites-available/sandman-api.conf /etc/nginx/sites-enabled/sandman-api.conf
EXPOSE 12000

# sandman-api-debug
COPY sandman-api-debug.conf /etc/nginx/sites-available
RUN ln -sf /etc/nginx/sites-available/sandman-api-debug.conf /etc/nginx/sites-enabled/sandman-api-debug.conf
EXPOSE 12005

# sandman
COPY sandman.conf /etc/nginx/sites-available
RUN ln -sf /etc/nginx/sites-available/sandman.conf /etc/nginx/sites-enabled/sandman.conf
EXPOSE 8080
