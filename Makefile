.PHONY: dev dev-local deploy-k8s down port-forward

ifneq (,$(wildcard .env))
  include .env
  export
endif

SPRING_DATASOURCE_USERNAME ?= $(POSTGRES_USER)
SPRING_DATASOURCE_PASSWORD ?= $(POSTGRES_PASSWORD)

dev:
	docker compose up --build

dev-local:
	docker compose up -d --force-recreate db
	@echo "Waiting for db..." && \
	  until bash -c '</dev/tcp/localhost/5432' 2>/dev/null; do sleep 1; done
	@trap 'kill 0' INT TERM; \
	  (cd backend && mvn spring-boot:run) & \
	  (cd frontend && npm run dev) & \
	  wait

down:
	docker compose down -v

deploy-k8s:
	kubectl apply -f k8s/namespace.yaml
	kubectl create secret generic postgres-secret \
		--from-env-file=.env \
		--namespace=avivly \
		--dry-run=client -o yaml | kubectl apply -f -
	kubectl apply -f k8s/

port-forward:
	kubectl -n avivly port-forward svc/nginx 8080:80 --address 0.0.0.0
