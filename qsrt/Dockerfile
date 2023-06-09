#FROM node:14.17 as frontend-builder
FROM node:12 as frontend-builder

WORKDIR /frontend
COPY package.json package-lock.json /frontend/
RUN npm ci

COPY client /frontend/client
COPY webpack.config.js /frontend/
RUN npm run build

#FROM python:3.7-slim-buster
FROM python:3.7-slim

EXPOSE 5000

# Controls whether to install extra dependencies needed for all data sources.
ARG skip_ds_deps

RUN useradd --create-home redash

# Ubuntu packages
RUN apt-get update && \
  apt-get install -y \
    curl \
    gnupg \
    build-essential \
    pwgen \
    libffi-dev \
    sudo \
    git-core \
    wget \
    # Postgres client
    libpq-dev \
    # ODBC support:
    g++ unixodbc-dev \
    # for SAML
    xmlsec1 \
    # Additional packages required for data sources:
    libssl-dev \
    default-libmysqlclient-dev \
    freetds-dev \
    libsasl2-dev && \
  # MSSQL ODBC Driver:
  #curl https://packages.microsoft.com/keys/microsoft.asc | apt-key add - && \
  #curl https://packages.microsoft.com/config/debian/10/prod.list > /etc/apt/sources.list.d/mssql-release.list && \
  apt-get update && \
  #ACCEPT_EULA=Y apt-get install -y msodbcsql17 && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Disalbe PIP Cache and Version Check
ENV PIP_DISABLE_PIP_VERSION_CHECK=1
ENV PIP_NO_CACHE_DIR=1
# ENV PIP_USE_DEPRECATED=legacy-resolver

# We first copy only the requirements file, to avoid rebuilding on every file
# change.
COPY requirements.txt requirements_bundles.txt requirements_dev.txt requirements_all_ds.txt ./
RUN pip install -r requirements.txt -r requirements_dev.txt
RUN if [ "x$skip_ds_deps" = "x" ] ; then pip install -r requirements_all_ds.txt ; else echo "Skipping pip install -r requirements_all_ds.txt" ; fi

COPY . /app
COPY --from=frontend-builder /frontend/client/dist /app/client/dist
RUN chown redash:redash -R /app
RUN chown redash:redash -R /app/manage.py
RUN chown redash:redash -R /app/bin/docker-entrypoint
USER redash

RUN chmod +x /app/bin/docker-entrypoint
RUN chmod +x /app/manage.py

#ENTRYPOINT ["bash", "/app/bin/docker-entrypoint"]
ENTRYPOINT ["sh", "/app/bin/docker-entrypoint"]
CMD ["server"]
