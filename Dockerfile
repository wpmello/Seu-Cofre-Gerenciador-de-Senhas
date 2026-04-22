FROM node:22-bookworm-slim

ENV DEBIAN_FRONTEND=noninteractive
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH="${JAVA_HOME}/bin:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${PATH}"

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        git \
        bubblewrap \
        openjdk-17-jdk \
        unzip \
    && update-ca-certificates \
    && mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools \
    && curl -fsSL -o /tmp/android-commandlinetools.zip https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip \
    && unzip -q /tmp/android-commandlinetools.zip -d /tmp/android-cmdline-tools \
    && mv /tmp/android-cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest \
    && yes | ${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager --sdk_root=${ANDROID_SDK_ROOT} \
        "platform-tools" \
        "platforms;android-36" \
        "build-tools;36.0.0" \
    && npm install -g @openai/codex \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* /tmp/android-commandlinetools.zip /tmp/android-cmdline-tools

ENV CODEX_HOME=/codex-home
WORKDIR /workspace

RUN mkdir -p /codex-home /workspace

ENTRYPOINT ["codex"]
