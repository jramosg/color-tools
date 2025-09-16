# Use the official Clojure image based on OpenJDK
FROM clojure:tools-deps

# Set working directory
WORKDIR /app

# Copy dependency files first for better Docker layer caching
COPY deps.edn build.clj pom.xml ./

# Download dependencies (this will be cached if deps don't change)
RUN clojure -P -X:build

# Copy the rest of the source code
COPY . .

# Set environment variables for Clojars deployment
# Note: These will be overridden by CI/CD environment variables
ENV CLOJARS_USERNAME=""
ENV CLOJARS_PASSWORD=""

# Build the jar
RUN clojure -T:build ci

# Default command to deploy (can be overridden)
CMD ["clojure", "-T:build", "deploy"]