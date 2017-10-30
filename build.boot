(def project 'com.grzm/tespresso.alpha)
(def version "0.1.1-SNAPSHOT")

(set-env! :resource-paths #{"resources" "src"}
          :dependencies   '[[adzerk/boot-test "RELEASE" :scope "test"]
                            [byte-streams "0.2.3"]
                            [com.stuartsierra/component "0.3.2"]
                            [org.clojure/clojure "RELEASE"]
                            [org.clojure/spec.alpha "0.1.123" :scope "test"]
                            [org.clojure/tools.logging "0.4.0" :scope "test"]
])

(task-options!
 pom {:project     project
      :version     version
      :description "clojure.test helpers"
      :url         "http://github.com/grzm/tespresso.alpha"
      :scm         {:url "https://github.com/grzm/tespresso.alpha"}
      :license     {"MIT"
                    "https://opensource.org/licenses/MIT"}})

(deftask build
  "Build and install the project locally."
  []
  (comp (pom) (aot) (jar) (install)))

(require '[adzerk.boot-test :refer [test]])
