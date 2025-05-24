#stage 1
FROM golang:latest AS builder

WORKDIR /app

COPY go.* ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -v -o slack-app

#stage 2
FROM gcr.io/distroless/base-debian11

WORKDIR /cur
COPY --from=builder /app .
CMD ["/cur/slack-app"]