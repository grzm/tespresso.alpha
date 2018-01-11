try {
    require("source-map-support").install();
} catch(err) {
}
require("../../target/cljs/node-dev/out/goog/bootstrap/nodejs.js");
require("../../target/cljs/node-dev/tests.js");
goog.require("com.grzm.tespresso.node_test_runner");
goog.require("cljs.nodejscli");
