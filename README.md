# Teahouse

![build badge](https://github.com/jonatan-ivanov/teahouse/actions/workflows/gradle.yml/badge.svg)

Demo setup for Spring Boot apps with Prometheus, Grafana, Loki, Tempo, and Spring Boot Admin to demonstrate Observability use-cases.

## Start Kubernetes Tunnel

If you want to test the services locally you can use Telepresence (or similar tools) to expose the services to your local machine. You need to have Telepresence installed and running on your machine. You can start the tunnel with this command:

```shell
telepresence connect
```

URLs in the rest of this guide assume that you are deploying into a Kubernetes namespace called `apps`.

## Start dependencies

```shell
for f in config/core/*; do kubectl apply -f $f; done
```

## Stop dependencies

```shell
for f in config/core/*; do kubectl delete -f $f; done
```

## Start the apps (using in-memory H2 DB)

```shell
kubectl apply -f water-service/config
kubectl apply -f tealeaf-service/config
kubectl apply -f tea-service/config
```

## Start load tests

See `SteepTeaSimulation.java` for duration, request rate, and traffic patterns.

```shell
./gradlew :load-gen:gatlingRun
```

## Useful URLs

- Tea UI: http://tea-service.apps:8080/steep
- Tea Service: http://tea-service.apps:8080
- Tealeaf Service: http://tealeaf-service.apps:8080
- Water Service: http://water-service.apps:8080
- Prometheus: http://prometheus.apps:9090
- Loki, Grafana, Tempo: http://grafana.apps:3200

## Errors simulation

When start the apps for the first time, `english breakfast` is missing from the DB but you can make requests through the UI using `english breakfast` and the load generator also sends requests containing it. Those calls will end-up with HTTP 500; approximately 10% of the requests should fail: ~0.5 rq/sec error- and ~4.5 rq/sec success rate (~5 rq/sec total throughput, see `SteepTeaSimulation.java`).

You should see these errors on the throughput panel of the Tea API dashboard and Grafana also alerts on them (see the emails in [MailDev](#useful-urls)).

If you want to fix these errors, you need to create a record in the DB for `english breakfast`. The easiest way is sending an HTTP POST request to `/tealeaves` to create the resource (you can also log into the DB and insert the record for example using [Adminer](#useful-urls)). The `Makefile` contains a goal for this to make it simple for you, you can run this to fix errors (httpie and jq needed):

```shell
make errors-fixed
```

If you want the errors back again, you need to remove the record from the DB, the `Makefile` contains a goal for this too, so you can run this to inject errors:

```shell
make errors
```

## Latency simulation

If you [start the apps with the `mysql` profile](#start-the-apps-using-mysql), the apps are not connected to the DB directly but through [ToxiProxy](#useful-urls) so that you can inject failures (i.e.: latency) on the network. You can do this in multiple ways (e.g.: using the [ToxiProxy UI](#useful-urls) or the ToxiProxy CLI). The `Makefile` contains a goal for this to make it simple for you, you can run this to inject latency:

```shell
make chaos
```

And this to eliminate the extra latency:

```shell
make order
```
