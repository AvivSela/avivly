# Fix 9 — Deduplicate `proxy_set_header` directives in nginx

## Context

**Prerequisites:** Fixes #10, #8, #7, #6, #2, #5, #4, #3 applied (backend changes complete).

`nginx/nginx.conf:12` and `:26`

The same four `proxy_set_header` directives are copy-pasted in both the `/api/` location block and the short-code regex block. Any future header addition (e.g., `X-Request-ID` for distributed tracing) must be applied in both places; missing one silently breaks one traffic class.

nginx applies `server`-level `proxy_set_header` directives to all descendant `location` blocks that do not define their own. Moving the headers to `server` level removes the duplication without changing behaviour.

## Objective

Move the four `proxy_set_header` directives to the `server` block. Remove them from both `location` blocks.

## Implementation

Replace the entire contents of `nginx/nginx.conf` with:

```nginx
events {}

http {
    server_tokens off;

    limit_req_zone $binary_remote_addr zone=redirects:10m rate=30r/m;

    server {
        listen 80;

        proxy_set_header Host              $host;
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        location /api/ {
            proxy_pass http://backend:8080;
        }

        location ^~ /assets/ {
            proxy_pass http://frontend:80;
        }

        location ~ ^/[A-Za-z0-9_-]+ {
            limit_req zone=redirects burst=5 nodelay;
            proxy_pass http://backend:8080;
        }

        location / {
            proxy_pass http://frontend:80;
        }
    }
}
```

## Verify

```bash
docker run --rm -v "$(pwd)/nginx/nginx.conf:/etc/nginx/nginx.conf:ro" nginx:alpine nginx -t
```

Output must include `syntax is ok` and `test is successful`.

## Commit

`fix: move proxy_set_header to server block to eliminate nginx duplication (#9)`
