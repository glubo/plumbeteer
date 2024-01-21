FROM busybox:1.36.1

# Create a non-root user to own the files and run our server
RUN adduser -D static
USER static
WORKDIR /home/static

# Copy the static website
# Use the .dockerignore file to control what ends up inside the image!
ADD health /home/static/
ADD plumbeteer.tgz /home/static/

EXPOSE 8080
# Run BusyBox httpd
CMD ["busybox", "httpd", "-f", "-v", "-p", "8080"]
