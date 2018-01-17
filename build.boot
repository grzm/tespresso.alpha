(def project 'com.grzm/tespresso.alpha)
(def version "0.1.3")

(set-env! :resource-paths #{"resources" "src"}
          :dependencies   '[[crisptrutski/boot-cljs-test "0.3.5-SNAPSHOT" :scope "test"]
                            [metosin/boot-alt-test "0.3.2" :scope "test"]
                            [seancorfield/boot-tools-deps "0.1.4" :scope "test"
                             :exclusions [ch.qos.logback/logback-classic
                                          org.clojure/clojure]]])

(task-options!
 pom {:project     project
      :version     version
      :description "clojure.test helpers"
      :url         "http://github.com/grzm/tespresso.alpha"
      :scm         {:url "https://github.com/grzm/tespresso.alpha"}
      :license     {"MIT"
                    "https://opensource.org/licenses/MIT"}})

(require '[boot-tools-deps.core :refer [deps]])

(deftask build
  "Build and install the project locally."
  []
  (comp (deps) (pom) (aot) (jar) (install)))

(require '[metosin.boot-alt-test :as boot-alt-test])

(deftask alt-test
  []
  (comp (deps :aliases [:test])
        (boot-alt-test/alt-test :filter 'com.grzm.tespresso-test/exclude-capture)))

(require '[crisptrutski.boot-cljs-test :as boot-cljs-test])
(task-options!
  boot-cljs-test/test-cljs
  {:ids ["com/grzm/tespresso/tespresso_test_suite"]
   :js-env :node})
(deftask test-cljs
  []
  (comp (deps :aliases [:test])
        (boot-cljs-test/test-cljs)))
