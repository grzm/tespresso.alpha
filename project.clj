(defproject com.grzm/tespresso.alpha "0.0.1-NOT-IMPORTANT"
  :plugins [[lein-cljsbuild "1.1.7"]]
  :profiles {:dev
             {:dependencies [[byte-streams "0.2.3"]
                             [com.stuartsierra/component "0.3.2"]
                             [org.clojure/clojure "1.9.0"]
                             [org.clojure/clojurescript "1.9.946"]
                             [org.clojure/spec.alpha "0.1.143"]
                             [org.clojure/test.check "0.10.0-alpha2"]
                             [org.clojure/tools.logging "0.4.0"]]
              :plugins      [[com.jakemccrary/lein-test-refresh "0.22.0"]]}

             :self-host
             {:dependencies [[byte-streams "0.2.3"]
                             [com.stuartsierra/component "0.3.2"]
                             [org.clojure/clojure "1.9.0"]
                             [org.clojure/clojurescript "1.9.946"]
                             [org.clojure/spec.alpha "0.1.123"]
                             [org.clojure/test.check "0.10.0-alpha2"]
                             [org.clojure/tools.logging "0.4.0"]]
              :main         clojure.main}}
  :source-paths ^:replace ["src"]
  :test-paths ^:replace ["test/src"]
  :cljsbuild
  {:builds
   [{:id             "node-dev"
     :source-paths   ["src" "test/src" "test/node/src"]
     :notify-command ["node" "test/node/dev.js"]
     :compiler       {:optimizations :none
                      :main          com.grzm.tespresso.node-test-runner
                      :static-fns    true
                      :target        :nodejs
                      :output-to     "target/cljs/node-dev/tests.js"
                      :output-dir    "target/cljs/node-dev/out"
                      :source-map    true}}]})
