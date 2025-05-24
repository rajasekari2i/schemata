package base

import (
	"context"
	"os"

	"github.com/jackc/pgx/v4/pgxpool"
	"github.com/sirupsen/logrus"
)

type ObieContext struct {
	db  *pgxpool.Pool
	log *logrus.Logger
}

func NewObieContext() *ObieContext {
	log := logrus.New()
	if os.Getenv("CONNECT_ACCESS_TOKEN") == "" {
		log.Error("Unable to access CONNECT_ACCESS_TOKEN")
	}
	dbConnURL := "postgresql://postgres:OpsBe@ch1@3$@104.197.194.76:5432/opsbeach"

	// creating a pool to get connection from, useful for concurrent upserts in routines
	db, err := pgxpool.Connect(context.Background(), dbConnURL)
	// connConfig, err := pgx.Connect(context.Background(), dbConnURL)
	if err != nil {
		log.Error(os.Stderr, "Unable to connect to database: %v\n", err)
	}

	return &ObieContext{db: db, log: log}
}

func (context ObieContext) GetDB() *pgxpool.Pool {
	return context.db
}

func (context ObieContext) GetLog() *logrus.Logger {
	return context.log
}
