# Alerts Demo

1. Start the services in cluster and apps per the README. (Or docker-compose might work for the services YMMV.)
2. Open up Grafana to localhost, e.g. with a port forward `kubectl port-forward svc/grafana 3000:3000`
3. Look at Grafana alerts UI and see that there are 2 alerts configured in the "Teahouse": http://localhost:3000/alerting/list.
4. Run the `tea-service` locally either on command line or in IDE. (This is just a mini-hack to make the alert actuator visible to the scraper - it would work in cluster as well.)
5. Check the alert actuator is available: http://localhost:8080/actuator/alert
6. Run the `alert-service`. On start up it scrapes the `tea-service` actuator and converts the alert meta-data into Grafana alerts.
7. Look at Grafana again and notice there is a new alert in the "Teahouse" folder. It also shows up as a green heart icon in the top-right panel of the "Tea API" dashboard.
8. Trigger the alert by pinging the `tea-service` in cluster, e.g. `kubectl port-forward svc/tea-service 8080:8080` and then `curl localhost:8080/tea/earlgrey?size=small` a few times ("earlygrey" doesn't exist so that is an error).