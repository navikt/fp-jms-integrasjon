# fp-jms-integrasjon

Shared JMS/MQ transport library for Foreldrepenger applications.

## Shared context

- Source of truth for shared domain, architecture, and conventions: `navikt/fp-context`
- Copilot Space: `navikt/TeamForeldrepenger`

## Repo-specific context

## Repo-specific context

| Topic             | Details                                                                                                |
|-------------------|--------------------------------------------------------------------------------------------------------|
| Role              | Shared JMS/MQ transport layer. Keep payload contracts and business semantics in consuming applications |
| Consumers         | `fp-sak` for oppdrag and kvittering queues, `fptilbake` for kravgrunnlag queues                        |
| Tech stack        | Java, JMS 2.0 with `com.ibm.mq.jakarta.client`                                                         |
| Release model | SemVer parallel to IBM release level. Notify team on new major version                                 |

Contains producer/consumer base classes, configuration, session setup, MQ exception mapping, retry/pause mechanics.

## Verification

- Build and test the library locally, then verify via consumer builds and tests
- No support in `navikt/fp-autotest` - do not attempt verification using test suites
