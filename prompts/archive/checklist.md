# Strategy Param Schema — Task Checklist

## Phase 1 — Foundation (sequential)
- [x] **Task 1** — `ParamType` + `StrategyParamDefinition` → `mvn compile`
- [x] **Task 2** — `StrategyParamValidator` → `mvn compile`
- [x] **Task 3** — Interface + 3 strategy impls (atomic) → `mvn compile`
- [x] **Task 4** — `StrategyRegistry` → `mvn compile`

## Phase 2 — Service layer (parallel after Task 4)
- [x] **Task 5a** — `CreateLinkRequest` + fix test call sites → `mvn compile`
- [x] **Task 5b** — `StrategyController` → `mvn compile`

## Phase 3 — Wire-up (sequential)
- [x] **Task 6** — `LinkService` + remove shim → `mvn compile` + `mvn test`

## Phase 4 — Frontend
- [x] **Task 7** — `api.js` + `LinkForm.jsx` → `npm run build`

## Phase 5 — Tests (parallel)
- [x] **Task 8a** — `StrategyParamValidatorTest` (12 cases) → `mvn test -Dtest=StrategyParamValidatorTest`
- [x] **Task 8b** — Update 3 strategy unit tests → `mvn test -Dtest=RandomBase62StrategyTest+HashTruncateStrategyTest+SequentialStrategyTest`
- [x] **Task 8c** — `StrategyControllerTest` + 6 integration tests → `mvn test -Dtest=StrategyControllerTest+LinkControllerIntegrationTest`

## Final gate
- [x] `mvn test -pl backend` — full suite green
- [x] `npm run build` — frontend builds clean
