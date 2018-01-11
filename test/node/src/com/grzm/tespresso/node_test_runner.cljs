(ns com.grzm.tespresso.node-test-runner
  (:require
   [cljs.nodejs :as nodejs]
   [cljs.test :as test :refer-macros [run-tests]]
   [com.grzm.tespresso-test]
   [com.grzm.tespresso.component-test]
   [com.grzm.tespresso.spec-test]))

(nodejs/enable-util-print!)

(defn -main []
  (run-tests
    'com.grzm.tespresso-test
    'com.grzm.tespresso.component-test
    'com.grzm.tespresso.spec-test))

(set! *main-cli-fn* -main)
