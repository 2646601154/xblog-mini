# Xblog-mini AGENTS

## Project Overview
- Multi-module blog system: blog-api (Java/Spring Boot), blog-web, blog-admin (Vue), blog-uniapp (UniApp)
- PRD: `doc/PRD.md` — source of truth for architecture, API, and database design
- SQL scripts: `sql/init.sql` (schema), `sql/test-data.sql` (test data)

## Development Rules
- **Database**: Logical foreign keys only (no FK constraints). Cascade deletes handled in application layer.
- **Package**: `com.xblog` (not `com.xblog.xblog`)
- **Config**: `blog-api/src/main/resources/application.yml`
- **JWT**: HS256, 7-day expiration. Store in Redis with key `user:token:{userId}`.
- **File uploads**: Max 5MB per image, 20MB per file. Allowed types: jpg, png, gif, webp.

## Key Commands
```bash
# Backend (run from blog-api/)
./mvnw spring-boot:run

# SQL init (MySQL)
mysql -u root -p < sql/init.sql
mysql -u root -p < sql/test-data.sql
```

## Git Conventions
- Branch: `main` (no develop branch for MVP)
- Commits: `<type>(<scope>): <subject>`

## Test Account
- Admin: `admin / 123456`
- User: `testuser / 123456`

## Environment Variables (for blog-api)
```bash
MYSQL_ROOT_PASSWORD, JWT_SECRET, OSS_ENDPOINT, OSS_ACCESS_KEY_ID, OSS_ACCESS_KEY_SECRET, OSS_BUCKET_NAME
```
