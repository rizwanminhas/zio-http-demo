# A demo app for the ZIO Http

### How to "hot reload" for development?
1. I am using the `sbt-revolver` dependency in `project/plugins.sbt` for hot reloading.
2. On terminal first type `sbt`
3. While in the sbt prompt, type `~reStart` to run the in the hot reload mode.
4. Or from intellij double click the `reStart` task from the `sbt tasks` in the sbt panel.

### Sample Http calls via curl
1. GET: `curl localhost:9000/greet`
2. POST: `curl -X POST localhost:9000/greet`
3. POST with headers: `curl -X POST localhost:9000/greet -H "Origin: google.com" -v`
4. or `curl -X POST localhost:9000/greet -H "Origin: localhost" -v`