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

### To test CORS
1. execute `curl -X POST localhost:9000/greet -H "Origin: google.com" -v` and notice you won't see any access-control* headers i.e. this request will be blocked.
2. execute `curl -X POST localhost:9000/greet -H "Origin: localhost" -v` and you will notice the following extra headers i.e. the request will be allowed:
    ```
    access-control-expose-headers: *
    access-control-allow-origin: localhost
    access-control-allow-methods: "GET,POST"
    access-control-allow-credentials: true
    ```
   
### To test CSRF
1. execute `curl localhost:9000/greet` and you will notice this extra set cookie header `set-cookie x-csrf-token=68aed7e1-a13a-4069-80dd-a75b1a753b0b`
2. execute `curl -X POST localhost:9000/greet -v` and you will see 403 Forbidden response.
3. Now finally send the request with `x-csrf-token` header and cookie like `curl -X POST localhost:9000/greet -H "x-csrf-token: 68aed7e1-a13a-4069-80dd-a75b1a753b0b" --cookie "x-csrf-token=68aed7e1-a13a-4069-80dd-a75b1a753b0b" -v` and you will see the response returns `200 Ok`.