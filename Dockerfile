FROM node:22-bookworm-slim

RUN apt-get update && apt-get install -y \
    ca-certificates \
    curl \
    git \
    bubblewrap \
 && rm -rf /var/lib/apt/lists/* \
 && update-ca-certificates

RUN npm install -g @openai/codex

ENV CODEX_HOME=/codex-home
WORKDIR /workspace
RUN mkdir -p /codex-home /workspace

ENTRYPOINT ["codex"]